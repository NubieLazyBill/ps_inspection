package com.example.ps_inspection.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ps_inspection.ui.adapters.ArchiveAdapter
import com.example.ps_inspection.data.repositories.ArchiveItem
import com.example.ps_inspection.data.utils.ExcelExportService
import com.example.ps_inspection.data.repositories.InspectionArchiveManager
import com.example.ps_inspection.data.services.GoogleSheetsService
import com.example.ps_inspection.data.utils.FillStatus
import com.example.ps_inspection.R
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.data.repositories.CommentStorageManager
import com.example.ps_inspection.data.repositories.InspectionArchiveData
import com.example.ps_inspection.data.utils.ProgressCalculator
import com.example.ps_inspection.data.utils.getFillStatus
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentArchiveBinding
import com.example.ps_inspection.data.utils.mergeATG
import com.example.ps_inspection.data.utils.mergeATGPressuresOnly
import com.example.ps_inspection.data.utils.mergeBuildings
import com.example.ps_inspection.data.utils.mergeORU220
import com.example.ps_inspection.data.utils.mergeORU35
import com.example.ps_inspection.data.utils.mergeORU500
import com.example.ps_inspection.ui.MainActivity
import com.example.ps_inspection.ui.fragments.dialogs.GlobalCommentsDialog
import com.example.ps_inspection.ui.fragments.dialogs.GlobalMediaDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import com.google.gson.GsonBuilder
import java.io.File
import java.util.Date
import kotlinx.coroutines.delay

class ArchiveFragment : Fragment() {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    private var cachedServerArchives = listOf<ArchiveItem>()
    private var lastServerLoadTime = 0L

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var archiveManager: InspectionArchiveManager
    private lateinit var adapter: ArchiveAdapter
    private var allArchives = listOf<ArchiveItem>()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_archive, menu)
        }

        override fun onMenuItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_global_photos -> {
                    showGlobalPhotos()
                    true
                }
                R.id.action_global_comments -> {
                    showGlobalComments()
                    true
                }
                R.id.action_clear_archive -> {
                    showClearAllDialog()
                    true
                }
                R.id.action_clear_comments -> {  // 🔧 НОВЫЙ ПУНКТ
                    showClearCommentsDialog()
                    true
                }
                else -> false
            }
        }

        override fun onPrepareMenu(menu: Menu) {
            menu.findItem(R.id.action_global_photos)?.icon?.setTint(android.graphics.Color.parseColor("#4CAF50"))
            menu.findItem(R.id.action_global_comments)?.icon?.setTint(android.graphics.Color.parseColor("#4CAF50"))
        }
    }

    private fun showClearCommentsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Сброс комментариев")
            .setMessage("Удалить ВСЕ локальные комментарии?\n\nЭто не удалит осмотры и фото.\nПри следующей синхронизации комментарии загрузятся с сервера заново.")
            .setPositiveButton("Удалить") { _, _ ->
                val commentStorage = CommentStorageManager(requireContext())
                commentStorage.clearAllComments()
                Toast.makeText(requireContext(), "Комментарии удалены локально", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        archiveManager = InspectionArchiveManager(requireContext())

        setupToolbar()
        setupRecyclerView()
        loadArchives()

        binding.btnRefreshServer.setOnClickListener {
            Log.d("ARCHIVE_DEBUG", "Кнопка нажата!")
            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("ARCHIVE_DEBUG", "Запускаю загрузку с сервера...")
                binding.btnRefreshServer.text = "⏳ Загрузка..."
                binding.btnRefreshServer.isEnabled = false
                try {
                    loadServerArchives()
                    Log.d("ARCHIVE_DEBUG", "Загрузка завершена")
                } catch (e: Exception) {
                    Log.e("ARCHIVE_DEBUG", "Ошибка: ${e.message}", e)
                }
                // Проверяем что фрагмент ещё жив перед обновлением UI
                if (_binding != null) {
                    binding.btnRefreshServer.text = "🔄 Обновить с сервера"
                    binding.btnRefreshServer.isEnabled = true
                }
            }
        }
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupToolbar() {
        (activity as? MainActivity)?.supportActionBar?.apply {
            title = "Архив осмотров"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewArchives.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArchiveAdapter(
            records = emptyList(),
            onMenuClick = { archive, view -> showArchiveOptions(archive, view) }
        )
        binding.recyclerViewArchives.adapter = adapter
    }

    private fun showGlobalPhotos() {
        val dialog = GlobalMediaDialog.newInstance()
        dialog.show(childFragmentManager, "global_media")
    }

    private fun showGlobalComments() {
        val dialog = GlobalCommentsDialog.newInstance()
        dialog.show(childFragmentManager, "global_comments")
    }

    private fun loadArchives() {
        allArchives = archiveManager.getAllArchives()

        if (allArchives.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerViewArchives.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewArchives.visibility = View.VISIBLE
            adapter.updateData(allArchives)
        }
    }

    private suspend fun loadServerArchives() {
        try {
            val sheetsService = GoogleSheetsService(requireContext())
            var serverData = sheetsService.getAllInspections()

            // Если пусто — пробуем ещё раз через 2 секунды
            if (serverData == null || serverData.isEmpty()) {
                Log.d("ARCHIVE_DEBUG", "Первая попытка пустая, пробую ещё раз...")
                delay(2000)
                serverData = sheetsService.getAllInspections()
            }

            if (serverData != null && serverData.isNotEmpty()) {
                val firstRow = serverData.first()
                val keys = firstRow.keys.toList()
                Log.d("SERVER_KEYS", "=== КЛЮЧИ С СЕРВЕРА (первые 5) ===")
                keys.take(5).forEachIndexed { index, key ->
                    Log.d("SERVER_KEYS", "[$index] '$key' -> '${firstRow[key]}'")
                }
                Log.d("SERVER_KEYS", "...")
                Log.d("SERVER_KEYS", "=== КЛЮЧИ С СЕРВЕРА (последние 5) ===")
                keys.takeLast(5).forEachIndexed { index, key ->
                    Log.d("SERVER_KEYS", "[${keys.size - 5 + index}] '$key' -> '${firstRow[key]}'")
                }
                Log.d("SERVER_KEYS", "=== ВСЕГО КЛЮЧЕЙ: ${keys.size} ===")

                val serverArchives = serverData.mapNotNull { row ->
                    try {
                        val archiveData = convertServerRowToArchiveData(row)
                        if (archiveData != null) {
                            ArchiveItem(
                                fileName = "server_${row["Дата"]}_${row["Время"]}".replace(" ", "_"),
                                displayDate = "${row["Дата"] ?: ""} ${row["Время"] ?: ""}",
                                equipmentType = "Полный осмотр",
                                statusORU35 = archiveData.oru35.getFillStatus(),
                                statusORU220 = archiveData.oru220.getFillStatus(),
                                statusORU500 = archiveData.oru500.getFillStatus(),
                                statusATG = archiveData.atg.getFillStatus(),
                                statusBuildings = archiveData.buildings.getFillStatus(),
                                timestamp = System.currentTimeMillis(),
                                photoCount = 0,
                                hasComments = false,
                                hasPhotos = false,
                                inspectorName = row["ФИО дежурного"] ?: "",
                                progressOru35 = ProgressCalculator.calculateORU35(archiveData.oru35),
                                progressOru220 = ProgressCalculator.calculateORU220(archiveData.oru220),
                                progressOru500 = ProgressCalculator.calculateORU500(archiveData.oru500),
                                progressAtg = ProgressCalculator.calculateATG(archiveData.atg),
                                progressBuildings = ProgressCalculator.calculateBuildings(archiveData.buildings)
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                val allDisplayDates = allArchives.map { it.displayDate }.toSet()
                val newServerArchives = serverArchives.filter { it.displayDate !in allDisplayDates }

                Log.d("ARCHIVE_DEBUG", "Серверных осмотров: ${serverArchives.size}")
                Log.d("ARCHIVE_DEBUG", "Новых для добавления: ${newServerArchives.size}")

                // Сохраняем новые серверные осмотры локально
                for (archive in newServerArchives) {
                    try {
                        val row = serverData.find {
                            "${it["Дата"]} ${it["Время"]}" == archive.displayDate
                        }
                        if (row != null) {
                            val archiveData = convertServerRowToArchiveData(row)
                            if (archiveData != null) {
                                val safeDate = archive.displayDate
                                    .replace(" ", "_")
                                    .replace(":", "-")
                                    .replace(".", "-")
                                val fileName = "${safeDate}_Осмотр_ПС.json"
                                val now = Date()

                                val inspectionData = InspectionArchiveData(
                                    timestamp = now.time,
                                    displayDate = archive.displayDate,
                                    oru35 = archiveData.oru35,
                                    oru220 = archiveData.oru220,
                                    atg = archiveData.atg,
                                    oru500 = archiveData.oru500,
                                    buildings = archiveData.buildings,
                                    outdoorTemp = archiveData.outdoorTemp,
                                    inspectorName = archiveData.inspectorName,
                                    inspectorPosition = archiveData.inspectorPosition
                                )

                                val file = File(archiveManager.getArchiveDir(), fileName)
                                if (!file.exists()) {
                                    file.writeText(gson.toJson(inspectionData))
                                    Log.d("ARCHIVE_DEBUG", "Сохранён локально: ${archive.displayDate}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ARCHIVE_DEBUG", "Ошибка сохранения осмотра локально", e)
                    }
                }

                allArchives = (allArchives + newServerArchives).sortedByDescending { it.displayDate }
                Log.d("ARCHIVE_DEBUG", "Всего после объединения: ${allArchives.size}")

                // 💬 Загружаем комментарии с сервера в общее хранилище
                try {
                    val serverComments = sheetsService.getAllComments()
                    if (serverComments != null && serverComments.isNotEmpty()) {
                        Log.d("COMMENTS_DEBUG", "Загружено ${serverComments.size} комментариев с сервера")

                        val commentStorage = CommentStorageManager(requireContext())
                        // 🔧 Вместо загрузки существующих — создаём новую мапу
                        val newComments = mutableMapOf<String, MutableList<Comment>>()
                        var addedCount = 0

                        for (comment in serverComments) {
                            val section = comment["Секция"] ?: ""
                            val equipment = comment["Оборудование"] ?: ""
                            val commentText = comment["Комментарий"] ?: ""
                            val timestamp = comment["Timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                            val author = comment["ФИО дежурного"] ?: ""

                            if (commentText.isBlank()) continue

                            val prefix = when (section) {
                                "ОРУ-35" -> "ORU35"
                                "ОРУ-220" -> "ORU220"
                                "ОРУ-500" -> "ORU500"
                                "АТГ" -> "ATG"
                                "Здания" -> "BUILDINGS"
                                else -> continue
                            }
                            val key = "${prefix}_${equipment}"

                            val list = newComments.getOrPut(key) { mutableListOf() }
                            // 🔧 Проверяем дубликат по тексту И timestamp
                            val isDuplicate = list.any { it.text == commentText && it.timestamp == timestamp }

                            if (!isDuplicate) {
                                list.add(Comment(text = commentText, timestamp = timestamp, author = author))
                                addedCount++
                                Log.d("COMMENTS_DEBUG", "✅ [$key] = '$commentText' (автор: $author)")
                            } else {
                                Log.d("COMMENTS_DEBUG", "⏭️ Дубликат: [$key] = '$commentText'")
                            }
                        }

                        if (addedCount > 0) {
                            commentStorage.saveAllComments(newComments)
                            Log.d("COMMENTS_DEBUG", "Сохранено $addedCount комментариев (полная перезапись)")
                        }
                    } else {
                        Log.d("COMMENTS_DEBUG", "Нет комментариев на сервере")
                    }
                } catch (e: Exception) {
                    Log.e("COMMENTS_DEBUG", "Ошибка загрузки комментариев", e)
                }

                if (allArchives.isNotEmpty() && _binding != null) {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerViewArchives.visibility = View.VISIBLE
                    adapter.updateData(allArchives)
                }
            } else {
                Log.d("ARCHIVE_DEBUG", "serverData пустой или null после двух попыток")
            }
        } catch (e: Exception) {
            Log.e("ARCHIVE_DEBUG", "Ошибка загрузки с сервера", e)
            e.printStackTrace()
        }
    }

    private fun mergeWithLocal(serverArchives: List<ArchiveItem>) {
        val allDisplayDates = allArchives.map { it.displayDate }.toSet()
        val newServerArchives = serverArchives.filter { it.displayDate !in allDisplayDates }

        Log.d("ARCHIVE_DEBUG", "Серверных: ${serverArchives.size}, новых: ${newServerArchives.size}")

        allArchives = (allArchives + newServerArchives).sortedByDescending { it.displayDate }

        if (allArchives.isNotEmpty()) {
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewArchives.visibility = View.VISIBLE
            adapter.updateData(allArchives)
        }
    }

    private fun showArchiveOptions(archive: ArchiveItem, anchorView: View) {
        val options = arrayOf(
            "📤 Отправить (Excel)",
            "📂 Открыть Excel",
            "🗑️ Удалить",
            "📥 Перенести данные из этого осмотра"
        )

        AlertDialog.Builder(requireContext())
            .setTitle(archive.displayDate)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareArchive(archive.fileName)
                    1 -> openArchiveExcel(archive.fileName)
                    2 -> confirmDeleteArchive(archive)
                    3 -> showMergeDialog(archive.fileName)
                }
            }
            .show()
    }

    private fun confirmDeleteArchive(archive: ArchiveItem) {
        // Проверяем, что удаляет тот же дежурный
        val userManager = com.example.ps_inspection.data.repositories.UserManager(requireContext())
        val currentUser = userManager.getCurrentUser()

        if (archive.inspectorName.isNotBlank() && archive.inspectorName != currentUser.name) {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Нет прав")
                .setMessage("Удалить осмотр может только тот, кто его выполнил.\n\nОсмотр выполнил: ${archive.inspectorName}\nТекущий пользователь: ${currentUser.name}")
                .setPositiveButton("Понятно", null)
                .show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Удаление осмотра")
            .setMessage("Удалить осмотр?\n\nЭто действие необратимо.")
            .setPositiveButton("Удалить") { _, _ ->
                // Удаляем и с сервера, и локально
                if (archive.fileName.startsWith("server_")) {
                    deleteServerArchive(archive)
                }
                // Удаляем локально если есть
                try {
                    archiveManager.deleteArchive(archive.fileName)
                } catch (e: Exception) {
                    // Может не быть локальной копии
                }
                loadArchives()
                Toast.makeText(requireContext(), "Осмотр удалён", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteServerArchive(archive: ArchiveItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = sheetsService.getAllInspections()
                val rowIndex = allData?.indexOfFirst {
                    "${it["Дата"]} ${it["Время"]}" == archive.displayDate
                } ?: -1

                if (rowIndex >= 0) {
                    sheetsService.deleteRow(rowIndex + 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openArchiveExcel(fileName: String) {
        // 🔧 Если это серверный осмотр и нет локального файла — загружаем с сервера
        if (fileName.startsWith("server_")) {
            openServerArchiveExcelFromFileName(fileName)
            return
        }

        val archiveData = archiveManager.loadFromArchive(fileName) ?: run {
            Toast.makeText(requireContext(), "Ошибка загрузки архива", Toast.LENGTH_SHORT).show()
            return
        }
        val exportService = ExcelExportService(requireContext())
        val fileUri = exportService.exportArchiveToExcel(archiveData)

        if (fileUri != null) {
            try {
                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(openIntent, "Открыть Excel"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Нет приложения для просмотра Excel", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔧 Новый метод для открытия серверного осмотра
    private fun openServerArchiveExcelFromFileName(fileName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = sheetsService.getAllInspections()

                // Ищем осмотр по displayDate
                val archiveItem = allArchives.find { it.fileName == fileName }
                if (archiveItem == null) {
                    Toast.makeText(requireContext(), "Осмотр не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val rowData = allData?.find {
                    "${it["Дата"]} ${it["Время"]}" == archiveItem.displayDate
                }

                if (rowData != null) {
                    val exportService = ExcelExportService(requireContext())
                    val archiveData = convertServerRowToArchiveData(rowData)
                    archiveData?.let {
                        val fileUri = exportService.exportArchiveToExcel(it)
                        if (fileUri != null) {
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            startActivity(Intent.createChooser(openIntent, "Открыть Excel"))
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Данные не найдены на сервере", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMergeDialog(fileName: String) {
        val items = arrayOf(
            "ОРУ-35",
            "ОРУ-220",
            "ОРУ-500",
            "АТГ + Р-500 2С (Полностью)",
            "АТГ + Р-500 2С (Только давления маслонасосов)",
            "Здания"
        )
        val checked = BooleanArray(items.size) { false }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📥 Выберите данные для переноса")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Перенести") { _, _ ->
                if (checked.any { it }) {
                    mergeDataFromArchive(fileName, checked)
                } else {
                    Toast.makeText(requireContext(), "⚠️ Выберите хотя бы один раздел", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun mergeDataFromArchive(fileName: String, sections: BooleanArray) {
        val archiveData = archiveManager.loadFromArchive(fileName) ?: return
        var count = 0

        if (sections[0]) { sharedViewModel.mergeORU35(archiveData.oru35); count++ }
        if (sections[1]) { sharedViewModel.mergeORU220(archiveData.oru220); count++ }
        if (sections[2]) { sharedViewModel.mergeORU500(archiveData.oru500); count++ }
        if (sections[3]) { sharedViewModel.mergeATG(archiveData.atg); count++ }
        if (sections[4]) { sharedViewModel.mergeATGPressuresOnly(archiveData.atg); count++ }
        if (sections[5]) { sharedViewModel.mergeBuildings(archiveData.buildings); count++ }

        if (count > 0) {
            Toast.makeText(requireContext(), "✅ Перенесено разделов: $count", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareArchive(fileName: String) {
        // 🔧 Если серверный осмотр
        if (fileName.startsWith("server_")) {
            shareServerArchiveFromFileName(fileName)
            return
        }
        val archiveData = archiveManager.loadFromArchive(fileName) ?: run {
            Toast.makeText(requireContext(), "Ошибка загрузки архива", Toast.LENGTH_SHORT).show()
            return
        }
        val exportService = ExcelExportService(requireContext())
        val fileUri = exportService.exportArchiveToExcel(archiveData)

        if (fileUri != null) {
            Toast.makeText(requireContext(), "Файл готов к отправке", Toast.LENGTH_LONG).show()
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Поделиться файлом осмотра"))
        } else {
            Toast.makeText(requireContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareServerArchiveFromFileName(fileName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = sheetsService.getAllInspections()

                val archiveItem = allArchives.find { it.fileName == fileName } ?: return@launch

                val rowData = allData?.find {
                    "${it["Дата"]} ${it["Время"]}" == archiveItem.displayDate
                }

                if (rowData != null) {
                    val exportService = ExcelExportService(requireContext())
                    val archiveData = convertServerRowToArchiveData(rowData)
                    archiveData?.let {
                        val fileUri = exportService.exportArchiveToExcel(it)
                        if (fileUri != null) {
                            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }, "Поделиться файлом осмотра"))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteArchive(fileName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление")
            .setMessage("Удалить этот осмотр из архива?")
            .setPositiveButton("Удалить") { _, _ ->
                if (archiveManager.deleteArchive(fileName)) {
                    Toast.makeText(requireContext(), "Осмотр удалён", Toast.LENGTH_SHORT).show()
                    loadArchives()
                } else {
                    Toast.makeText(requireContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Очистка архива")
            .setMessage("Удалить ВСЕ сохранённые осмотры? Это действие нельзя отменить.")
            .setPositiveButton("Удалить всё") { _, _ ->
                val count = archiveManager.clearAllArchives()
                Toast.makeText(requireContext(), "Удалено $count осмотров", Toast.LENGTH_SHORT).show()
                loadArchives()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun shareServerArchive(archive: ArchiveItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = sheetsService.getAllInspections()
                val rowData = allData?.find {
                    "${it["Дата"]} ${it["Время"]}" == archive.displayDate
                }

                if (rowData != null) {
                    // Создаём временный Excel из данных сервера и шарим
                    val exportService = ExcelExportService(requireContext())
                    // Конвертируем rowData в InspectionArchiveData для экспорта
                    val archiveData = convertServerRowToArchiveData(rowData)
                    archiveData?.let {
                        val fileUri = exportService.exportArchiveToExcel(it)
                        if (fileUri != null) {
                            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }, "Поделиться файлом осмотра"))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openServerArchiveExcel(archive: ArchiveItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = sheetsService.getAllInspections()
                val rowData = allData?.find {
                    "${it["Дата"]} ${it["Время"]}" == archive.displayDate
                }

                if (rowData != null) {
                    val exportService = ExcelExportService(requireContext())
                    val archiveData = convertServerRowToArchiveData(rowData)
                    archiveData?.let {
                        val fileUri = exportService.exportArchiveToExcel(it)
                        if (fileUri != null) {
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            startActivity(Intent.createChooser(openIntent, "Открыть Excel"))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mergeServerArchive(archive: ArchiveItem) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sheetsService = GoogleSheetsService(requireContext())
                val allData = sheetsService.getAllInspections()
                val rowData = allData?.find {
                    "${it["Дата"]} ${it["Время"]}" == archive.displayDate
                }

                if (rowData != null) {
                    val archiveData = convertServerRowToArchiveData(rowData)
                    archiveData?.let {
                        showMergeDialogForServerData(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMergeDialogForServerData(archiveData: InspectionArchiveData) {
        val items = arrayOf(
            "ОРУ-35",
            "ОРУ-220",
            "ОРУ-500",
            "АТГ + Р-500 2С (Полностью)",
            "АТГ + Р-500 2С (Только давления маслонасосов)",
            "Здания"
        )
        val checked = BooleanArray(items.size) { false }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📥 Выберите данные для переноса")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("Перенести") { _, _ ->
                var count = 0
                if (checked[0]) { sharedViewModel.mergeORU35(archiveData.oru35); count++ }
                if (checked[1]) { sharedViewModel.mergeORU220(archiveData.oru220); count++ }
                if (checked[2]) { sharedViewModel.mergeORU500(archiveData.oru500); count++ }
                if (checked[3]) { sharedViewModel.mergeATG(archiveData.atg); count++ }
                if (checked[4]) { sharedViewModel.mergeATGPressuresOnly(archiveData.atg); count++ }
                if (checked[5]) { sharedViewModel.mergeBuildings(archiveData.buildings); count++ }

                if (count > 0) {
                    Toast.makeText(requireContext(), "✅ Перенесено разделов: $count", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Конвертация строки с сервера в InspectionArchiveData
    private fun convertServerRowToArchiveData(row: Map<String, String>): InspectionArchiveData? {
        return try {
            val oru35 = InspectionORU35Data(
                tsn2 = row["ОРУ-35 2ТСН"] ?: "",
                tsn3 = row["ОРУ-35 3ТСН"] ?: "",
                tsn4 = row["ОРУ-35 4ТСН"] ?: "",
                tt352tsnA = row["ОРУ-35 ТТ-35 2ТСН А"] ?: "",
                tt352tsnB = row["ОРУ-35 ТТ-35 2ТСН В"] ?: "",
                tt352tsnC = row["ОРУ-35 ТТ-35 2ТСН С"] ?: "",
                tt353tsnA = row["ОРУ-35 ТТ-35 3ТСН А"] ?: "",
                tt353tsnB = row["ОРУ-35 ТТ-35 3ТСН В"] ?: "",
                tt353tsnC = row["ОРУ-35 ТТ-35 3ТСН С"] ?: "",
                v352tsnA = row["ОРУ-35 В-35 2ТСН А"] ?: "",
                v352tsnB = row["ОРУ-35 В-35 2ТСН В"] ?: "",
                v352tsnC = row["ОРУ-35 В-35 2ТСН С"] ?: "",
                v353tsnA = row["ОРУ-35 В-35 3ТСН А"] ?: "",
                v353tsnB = row["ОРУ-35 В-35 3ТСН В"] ?: "",
                v353tsnC = row["ОРУ-35 В-35 3ТСН С"] ?: ""
            )

            val oru220 = InspectionORU220Data(
                purgingMirnayaA = row["Мирная прод А"] ?: "",
                purgingMirnayaB = row["Мирная прод В"] ?: "",
                purgingMirnayaC = row["Мирная прод С"] ?: "",
                oilMirnayaA = row["Мирная ТТ масло А"] ?: "",
                oilMirnayaB = row["Мирная ТТ масло В"] ?: "",
                oilMirnayaC = row["Мирная ТТ масло С"] ?: "",
                purgingTopazA = row["Топаз прод А"] ?: "",
                purgingTopazB = row["Топаз прод В"] ?: "",
                purgingTopazC = row["Топаз прод С"] ?: "",
                oilTopazA = row["Топаз ТТ масло А"] ?: "",
                oilTopazB = row["Топаз ТТ масло В"] ?: "",
                oilTopazC = row["Топаз ТТ масло С"] ?: "",
                purgingOvA = row["ОВ прод А"] ?: "",
                purgingOvB = row["ОВ прод В"] ?: "",
                purgingOvC = row["ОВ прод С"] ?: "",
                oilOvA = row["ОВ ТТ масло А"] ?: "",
                oilOvB = row["ОВ ТТ масло В"] ?: "",
                oilOvC = row["ОВ ТТ масло С"] ?: "",
                tnOsshFvUpper = row["ТН-220 ОСШ верх"] ?: "",
                tnOsshFvLower = row["ТН-220 ОСШ низ"] ?: "",
                purgingV2atgA = row["В-220 2АТГ прод А"] ?: "",
                purgingV2atgB = row["В-220 2АТГ прод В"] ?: "",
                purgingV2atgC = row["В-220 2АТГ прод С"] ?: "",
                oilTt2atgA = row["2АТГ ТТ масло А"] ?: "",
                oilTt2atgB = row["2АТГ ТТ масло В"] ?: "",
                oilTt2atgC = row["2АТГ ТТ масло С"] ?: "",
                purgingShSV220A = row["ШСВ-220 прод А"] ?: "",
                purgingShSV220B = row["ШСВ-220 прод В"] ?: "",
                purgingShSV220C = row["ШСВ-220 прод С"] ?: "",
                oilTtShSV220A = row["ШСВ ТТ масло А"] ?: "",
                oilTtShSV220B = row["ШСВ ТТ масло В"] ?: "",
                oilTtShSV220C = row["ШСВ ТТ масло С"] ?: "",
                purgingV3atgA = row["В-220 3АТГ прод А"] ?: "",
                purgingV3atgB = row["В-220 3АТГ прод В"] ?: "",
                purgingV3atgC = row["В-220 3АТГ прод С"] ?: "",
                oilTt3atgA = row["3АТГ ТТ масло А"] ?: "",
                oilTt3atgB = row["3АТГ ТТ масло В"] ?: "",
                oilTt3atgC = row["3АТГ ТТ масло С"] ?: "",
                purgingOrbitaA = row["Орбита прод А"] ?: "",
                purgingOrbitaB = row["Орбита прод В"] ?: "",
                purgingOrbitaC = row["Орбита прод С"] ?: "",
                oilOrbitaA = row["Орбита ТТ масло А"] ?: "",
                oilOrbitaB = row["Орбита ТТ масло В"] ?: "",
                oilOrbitaC = row["Орбита ТТ масло С"] ?: "",
                purgingFakelA = row["Факел прод А"] ?: "",
                purgingFakelB = row["Факел прод В"] ?: "",
                purgingFakelC = row["Факел прод С"] ?: "",
                oilFakelA = row["Факел ТТ масло А"] ?: "",
                oilFakelB = row["Факел ТТ масло В"] ?: "",
                oilFakelC = row["Факел ТТ масло С"] ?: "",
                purgingCometa1A = row["Комета-1 прод А"] ?: "",
                purgingCometa1B = row["Комета-1 прод В"] ?: "",
                purgingCometa1C = row["Комета-1 прод С"] ?: "",
                oilCometa1A = row["Комета-1 ТТ масло А"] ?: "",
                oilCometa1B = row["Комета-1 ТТ масло В"] ?: "",
                oilCometa1C = row["Комета-1 ТТ масло С"] ?: "",
                purgingCometa2A = row["Комета-2 прод А"] ?: "",
                purgingCometa2B = row["Комета-2 прод В"] ?: "",
                purgingCometa2C = row["Комета-2 прод С"] ?: "",
                oilCometa2A = row["Комета-2 ТТ масло А"] ?: "",
                oilCometa2B = row["Комета-2 ТТ масло В"] ?: "",
                oilCometa2C = row["Комета-2 ТТ масло С"] ?: "",
                tn1UpperA = row["1ТН-220 верх А"] ?: "",
                tn1UpperB = row["1ТН-220 верх В"] ?: "",
                tn1UpperC = row["1ТН-220 верх С"] ?: "",
                tn1LowerA = row["1ТН-220 низ А"] ?: "",
                tn1LowerB = row["1ТН-220 низ В"] ?: "",
                tn1LowerC = row["1ТН-220 низ С"] ?: "",
                tn2UpperA = row["2ТН-220 верх А"] ?: "",
                tn2UpperB = row["2ТН-220 верх В"] ?: "",
                tn2UpperC = row["2ТН-220 верх С"] ?: "",
                tn2LowerA = row["2ТН-220 низ А"] ?: "",
                tn2LowerB = row["2ТН-220 низ В"] ?: "",
                tn2LowerC = row["2ТН-220 низ С"] ?: ""
            )

            // ⭐ ВОТ ЧЕГО НЕ ХВАТАЛО — заполняем ATG, ORU500, Buildings с сервера
            val atg = InspectionATGData(
                atg2_c_oil_tank = row["2 АТГ С бак"] ?: "",
                atg2_c_oil_rpn = row["2 АТГ С РПН"] ?: "",
                atg2_c_pressure_500 = row["2 АТГ С давл500"] ?: "",
                atg2_c_pressure_220 = row["2 АТГ С давл220"] ?: "",
                atg2_c_temp_ts1 = row["2 АТГ С ТС1"] ?: "",
                atg2_c_temp_ts2 = row["2 АТГ С ТС2"] ?: "",
                atg2_c_pump_group1 = row["2 АТГ С насос1"] ?: "",
                atg2_c_pump_group2 = row["2 АТГ С насос2"] ?: "",
                atg2_c_pump_group3 = row["2 АТГ С насос3"] ?: "",
                atg2_c_pump_group4 = row["2 АТГ С насос4"] ?: "",
                atg2_b_oil_tank = row["2 АТГ В бак"] ?: "",
                atg2_b_oil_rpn = row["2 АТГ В РПН"] ?: "",
                atg2_b_pressure_500 = row["2 АТГ В давл500"] ?: "",
                atg2_b_pressure_220 = row["2 АТГ В давл220"] ?: "",
                atg2_b_temp_ts1 = row["2 АТГ В ТС1"] ?: "",
                atg2_b_temp_ts2 = row["2 АТГ В ТС2"] ?: "",
                atg2_b_pump_group1 = row["2 АТГ В насос1"] ?: "",
                atg2_b_pump_group2 = row["2 АТГ В насос2"] ?: "",
                atg2_b_pump_group3 = row["2 АТГ В насос3"] ?: "",
                atg2_b_pump_group4 = row["2 АТГ В насос4"] ?: "",
                atg2_a_oil_tank = row["2 АТГ А бак"] ?: "",
                atg2_a_oil_rpn = row["2 АТГ А РПН"] ?: "",
                atg2_a_temp_ts1 = row["2 АТГ А ТС1"] ?: "",
                atg2_a_temp_ts2 = row["2 АТГ А ТС2"] ?: "",
                atg2_a_pump_group1 = row["2 АТГ А насос1"] ?: "",
                atg2_a_pump_group2 = row["2 АТГ А насос2"] ?: "",
                atg2_a_pump_group3 = row["2 АТГ А насос3"] ?: "",
                atg2_a_pump_group4 = row["2 АТГ А насос4"] ?: "",
                atg_reserve_oil_tank = row["АТГ рез бак"] ?: "",
                atg_reserve_oil_rpn = row["АТГ рез РПН"] ?: "",
                atg_reserve_pressure_500 = row["АТГ рез давл500"] ?: "",
                atg_reserve_pressure_220 = row["АТГ рез давл220"] ?: "",
                atg_reserve_temp_ts1 = row["АТГ рез ТС1"] ?: "",
                atg_reserve_temp_ts2 = row["АТГ рез ТС2"] ?: "",
                atg_reserve_pump_group1 = row["АТГ рез насос1"] ?: "",
                atg_reserve_pump_group2 = row["АТГ рез насос2"] ?: "",
                atg_reserve_pump_group3 = row["АТГ рез насос3"] ?: "",
                atg_reserve_pump_group4 = row["АТГ рез насос4"] ?: "",
                tn352atg = row["ТН-35 2АТГ"] ?: "",
                tn353atg = row["ТН-35 3АТГ"] ?: "",
                atg3_c_oil_tank = row["3 АТГ С бак"] ?: "",
                atg3_c_oil_rpn = row["3 АТГ С РПН"] ?: "",
                atg3_c_temp_ts1 = row["3 АТГ С ТС1"] ?: "", // нет в данных сервера? проверим ниже
                atg3_c_temp_ts2 = row["3 АТГ С ТС2"] ?: "",
                atg3_c_pump_group1 = row["3 АТГ С насос1"] ?: "",
                atg3_c_pump_group2 = row["3 АТГ С насос2"] ?: "",
                atg3_c_pump_group3 = row["3 АТГ С насос3"] ?: "",
                atg3_c_pump_group4 = row["3 АТГ С насос4"] ?: "",
                atg3_b_oil_tank = row["3 АТГ В бак"] ?: "", // нет в логах
                atg3_b_oil_rpn = row["3 АТГ В РПН"] ?: "",
                atg3_b_pressure_220 = row["3 АТГ В давл220"] ?: "",
                atg3_b_temp_ts1 = row["3 АТГ В ТС1"] ?: "",
                atg3_b_temp_ts2 = row["3 АТГ В ТС2"] ?: "",
                atg3_b_pump_group1 = row["3 АТГ В насос1"] ?: "",
                atg3_b_pump_group2 = row["3 АТГ В насос2"] ?: "",
                atg3_b_pump_group3 = row["3 АТГ В насос3"] ?: "",
                atg3_b_pump_group4 = row["3 АТГ В насос4"] ?: "",
                atg3_a_oil_tank = row["3 АТГ А бак"] ?: "",
                atg3_a_oil_rpn = row["3 АТГ А РПН"] ?: "",
                atg3_a_pressure_220 = row["3 АТГ А давл220"] ?: "",
                atg3_a_temp_ts1 = row["3 АТГ А ТС1"] ?: "",
                atg3_a_temp_ts2 = row["3 АТГ А ТС2"] ?: "",
                atg3_a_pump_group1 = row["3 АТГ А насос1"] ?: "",
                atg3_a_pump_group2 = row["3 АТГ А насос2"] ?: "",
                atg3_a_pump_group3 = row["3 АТГ А насос3"] ?: "",
                atg3_a_pump_group4 = row["3 АТГ А насос4"] ?: "",
                // Реакторы (сейчас их нет):
                reactor_c_oil_tank = row["Реактор С бак"] ?: "",        // ← ДОБАВИТЬ
                reactor_c_pressure_500 = row["Реактор С давл500"] ?: "", // ← ДОБАВИТЬ
                reactor_c_temp_ts = row["Реактор С ТС"] ?: "",
                reactor_c_pump_group1 = row["Реактор С насос1"] ?: "",
                reactor_c_pump_group2 = row["Реактор С насос2"] ?: "",
                reactor_c_pump_group3 = row["Реактор С насос3"] ?: "",
                reactor_c_tt_neutral = row["Реактор С ТТ нейтр"] ?: "",
                reactor_b_oil_tank = row["Реактор В бак"] ?: "",
                reactor_b_pressure_500 = row["Реактор В давл500"] ?: "",
                reactor_b_temp_ts = row["Реактор В ТС"] ?: "",
                reactor_b_pump_group1 = row["Реактор В насос1"] ?: "",
                reactor_b_pump_group2 = row["Реактор В насос2"] ?: "",
                reactor_b_pump_group3 = row["Реактор В насос3"] ?: "",
                reactor_b_tt_neutral = row["Реактор В ТТ нейтр"] ?: "",
                reactor_a_oil_tank = row["Реактор А бак"] ?: "",
                reactor_a_temp_ts = row["Реактор А ТС"] ?: "",
                reactor_a_pump_group1 = row["Реактор А насос1"] ?: "",
                reactor_a_pump_group2 = row["Реактор А насос2"] ?: "",
                reactor_a_pump_group3 = row["Реактор А насос3"] ?: "",
                reactor_a_tt_neutral = row["Реактор А ТТ нейтр"] ?: ""
            )

            val oru500 = InspectionORU500Data(
                purgingR5002sA1 = row["Р-500 2С I А1"] ?: "",
                purgingR5002sB1 = row["Р-500 2С I В1"] ?: "",
                purgingR5002sC1 = row["Р-500 2С I С1"] ?: "",
                purgingR5002sA2 = row["Р-500 2С II А2"] ?: "",
                purgingR5002sB2 = row["Р-500 2С II В2"] ?: "",
                purgingR5002sC2 = row["Р-500 2С II С2"] ?: "",
                gasPressureVsht31A = row["ВШТ-31 газ А"] ?: "",
                gasPressureVsht31B = row["ВШТ-31 газ В"] ?: "",
                gasPressureVsht31C = row["ВШТ-31 газ С"] ?: "",
                oilTtVsht31A = row["ВШТ-31 ТТ масло А"] ?: "",
                oilTtVsht31B = row["ВШТ-31 ТТ масло В"] ?: "",
                oilTtVsht31C = row["ВШТ-31 ТТ масло С"] ?: "",
                gasPressureVlt30A = row["ВЛТ-30 газ А"] ?: "",
                gasPressureVlt30B = row["ВЛТ-30 газ В"] ?: "",
                gasPressureVlt30C = row["ВЛТ-30 газ С"] ?: "",
                oilTtVlt30A = row["ВЛТ-30 ТТ масло А"] ?: "",
                oilTtVlt30B = row["ВЛТ-30 ТТ масло В"] ?: "",
                oilTtVlt30C = row["ВЛТ-30 ТТ масло С"] ?: "",
                purgingVshl32A1 = row["ВШЛ-32 I А1"] ?: "",
                purgingVshl32B1 = row["ВШЛ-32 I В1"] ?: "",
                purgingVshl32C1 = row["ВШЛ-32 I С1"] ?: "",
                purgingVshl32A2 = row["ВШЛ-32 II А2"] ?: "",
                purgingVshl32B2 = row["ВШЛ-32 II В2"] ?: "",
                purgingVshl32C2 = row["ВШЛ-32 II С2"] ?: "",
                oilTtVshl32A = row["ВШЛ-32 ТТ масло А"] ?: "",
                oilTtVshl32B = row["ВШЛ-32 ТТ масло В"] ?: "",
                oilTtVshl32C = row["ВШЛ-32 ТТ масло С"] ?: "",
                purgingVshl21A1 = row["ВШЛ-21 I А1"] ?: "",
                purgingVshl21B1 = row["ВШЛ-21 I В1"] ?: "",
                purgingVshl21C1 = row["ВШЛ-21 I С1"] ?: "",
                purgingVshl21A2 = row["ВШЛ-21 II А2"] ?: "",
                purgingVshl21B2 = row["ВШЛ-21 II В2"] ?: "",
                purgingVshl21C2 = row["ВШЛ-21 II С2"] ?: "",
                oilTtVshl21A = row["ВШЛ-21 ТТ масло А"] ?: "",
                oilTtVshl21B = row["ВШЛ-21 ТТ масло В"] ?: "",
                oilTtVshl21C = row["ВШЛ-21 ТТ масло С"] ?: "",
                purgingVsht22A1 = row["ВШТ-22 I А1"] ?: "",
                purgingVsht22B1 = row["ВШТ-22 I В1"] ?: "",
                purgingVsht22C1 = row["ВШТ-22 I С1"] ?: "",
                purgingVsht22A2 = row["ВШТ-22 II А2"] ?: "",
                purgingVsht22B2 = row["ВШТ-22 II В2"] ?: "",
                purgingVsht22C2 = row["ВШТ-22 II С2"] ?: "",
                oilTtVsht22A = row["ВШТ-22 ТТ масло А"] ?: "",
                oilTtVsht22B = row["ВШТ-22 ТТ масло В"] ?: "",
                oilTtVsht22C = row["ВШТ-22 ТТ масло С"] ?: "",
                purgingVlt20A1 = row["ВЛТ-20 I А1"] ?: "",
                purgingVlt20B1 = row["ВЛТ-20 I В1"] ?: "",
                purgingVlt20C1 = row["ВЛТ-20 I С1"] ?: "",
                purgingVlt20A2 = row["ВЛТ-20 II А2"] ?: "",
                purgingVlt20B2 = row["ВЛТ-20 II В2"] ?: "",
                purgingVlt20C2 = row["ВЛТ-20 II С2"] ?: "",
                oilTtVlt20A = row["ВЛТ-20 ТТ масло А"] ?: "",
                oilTtVlt20B = row["ВЛТ-20 ТТ масло В"] ?: "",
                oilTtVlt20C = row["ВЛТ-20 ТТ масло С"] ?: "",
                purgingVsht11A1 = row["ВШТ-11 I А1"] ?: "",
                purgingVsht11B1 = row["ВШТ-11 I В1"] ?: "",
                purgingVsht11C1 = row["ВШТ-11 I С1"] ?: "",
                purgingVsht11A2 = row["ВШТ-11 II А2"] ?: "",
                purgingVsht11B2 = row["ВШТ-11 II В2"] ?: "",
                purgingVsht11C2 = row["ВШТ-11 II С2"] ?: "",
                oilTtVsht11A = row["ВШТ-11 ТТ масло А"] ?: "",
                oilTtVsht11B = row["ВШТ-11 ТТ масло В"] ?: "",
                oilTtVsht11C = row["ВШТ-11 ТТ масло С"] ?: "",
                purgingVshl12A1 = row["ВШЛ-12 I А1"] ?: "",
                purgingVshl12B1 = row["ВШЛ-12 I В1"] ?: "",
                purgingVshl12C1 = row["ВШЛ-12 I С1"] ?: "",
                purgingVshl12A2 = row["ВШЛ-12 II А2"] ?: "",
                purgingVshl12B2 = row["ВШЛ-12 II В2"] ?: "",
                purgingVshl12C2 = row["ВШЛ-12 II С2"] ?: "",
                oilTtVshl12A = row["ВШЛ-12 ТТ масло А"] ?: "",
                oilTtVshl12B = row["ВШЛ-12 ТТ масло В"] ?: "",
                oilTtVshl12C = row["ВШЛ-12 ТТ масло С"] ?: "",
                oilTtTrachukovskayaA = row["Трачуковская ТТ масло А"] ?: "",
                oilTtTrachukovskayaB = row["Трачуковская ТТ масло В"] ?: "",
                oilTtTrachukovskayaC = row["Трачуковская ТТ масло С"] ?: "",
                tn1500Cascade1A = row["1ТН-500 каск1 А"] ?: "",
                tn1500Cascade1B = row["1ТН-500 каск1 В"] ?: "",
                tn1500Cascade1C = row["1ТН-500 каск1 С"] ?: "",
                tn1500Cascade2A = row["1ТН-500 каск2 А"] ?: "",
                tn1500Cascade2B = row["1ТН-500 каск2 В"] ?: "",
                tn1500Cascade2C = row["1ТН-500 каск2 С"] ?: "",
                tn1500Cascade3A = row["1ТН-500 каск3 А"] ?: "",
                tn1500Cascade3B = row["1ТН-500 каск3 В"] ?: "",
                tn1500Cascade3C = row["1ТН-500 каск3 С"] ?: "",
                tn1500Cascade4A = row["1ТН-500 каск4 А"] ?: "",
                tn1500Cascade4B = row["1ТН-500 каск4 В"] ?: "",
                tn1500Cascade4C = row["1ТН-500 каск4 С"] ?: "",
                tn2500Cascade1A = row["2ТН-500 каск1 А"] ?: "",
                tn2500Cascade1B = row["2ТН-500 каск1 В"] ?: "",
                tn2500Cascade1C = row["2ТН-500 каск1 С"] ?: "",
                tn2500Cascade2A = row["2ТН-500 каск2 А"] ?: "",
                tn2500Cascade2B = row["2ТН-500 каск2 В"] ?: "",
                tn2500Cascade2C = row["2ТН-500 каск2 С"] ?: "",
                tn2500Cascade3A = row["2ТН-500 каск3 А"] ?: "",
                tn2500Cascade3B = row["2ТН-500 каск3 В"] ?: "",
                tn2500Cascade3C = row["2ТН-500 каск3 С"] ?: "",
                tn2500Cascade4A = row["2ТН-500 каск4 А"] ?: "",
                tn2500Cascade4B = row["2ТН-500 каск4 В"] ?: "",
                tn2500Cascade4C = row["2ТН-500 каск4 С"] ?: "",
                tn500Sgres1Cascade1A = row["СГРЭС-1 каск1 А"] ?: "",
                tn500Sgres1Cascade1B = row["СГРЭС-1 каск1 В"] ?: "",
                tn500Sgres1Cascade1C = row["СГРЭС-1 каск1 С"] ?: "",
                tn500Sgres1Cascade2A = row["СГРЭС-1 каск2 А"] ?: "",
                tn500Sgres1Cascade2B = row["СГРЭС-1 каск2 В"] ?: "",
                tn500Sgres1Cascade2C = row["СГРЭС-1 каск2 С"] ?: "",
                tn500Sgres1Cascade3A = row["СГРЭС-1 каск3 А"] ?: "",
                tn500Sgres1Cascade3B = row["СГРЭС-1 каск3 В"] ?: "",
                tn500Sgres1Cascade3C = row["СГРЭС-1 каск3 С"] ?: "",
                tn500Sgres1Cascade4A = row["СГРЭС-1 каск4 А"] ?: "",
                tn500Sgres1Cascade4B = row["СГРЭС-1 каск4 В"] ?: "",
                tn500Sgres1Cascade4C = row["СГРЭС-1 каск4 С"] ?: "",
                oil1tnTrachukovskayaA = row["Трачук 1ТН масло А"] ?: "",
                oil1tnTrachukovskayaB = row["Трачук 1ТН масло В"] ?: "",
                oil1tnTrachukovskayaC = row["Трачук 1ТН масло С"] ?: "",
                oil2tnTrachukovskayaA = row["Трачук 2ТН масло А"] ?: "",
                oil2tnTrachukovskayaB = row["Трачук 2ТН масло В"] ?: "",
                oil2tnTrachukovskayaC = row["Трачук 2ТН масло С"] ?: "",
                oil2tnBelozernayaA = row["Белозёрная 2ТН масло А"] ?: "",
                oil2tnBelozernayaB = row["Белозёрная 2ТН масло В"] ?: "",
                oil2tnBelozernayaC = row["Белозёрная 2ТН масло С"] ?: ""
            )

            val buildings = InspectionBuildingsData(
                compressor1Valve = row["Компр1 арматура"] ?: "",
                compressor1Heating = row["Компр1 обогрев"] ?: "",
                compressor1Temp = row["Компр1 темп"] ?: "",
                ballroom1Valve = row["Баллон1 арматура"] ?: "",
                ballroom1Heating = row["Баллон1 обогрев"] ?: "",
                ballroom1Temp = row["Баллон1 темп"] ?: "",
                compressor2Valve = row["Компр2 арматура"] ?: "",
                compressor2Heating = row["Компр2 обогрев"] ?: "",
                compressor2Temp = row["Компр2 темп"] ?: "",
                ballroom2Valve = row["Баллон2 арматура"] ?: "",
                ballroom2Heating = row["Баллон2 обогрев"] ?: "",
                ballroom2Temp = row["Баллон2 темп"] ?: "",
                kpzOpuValve = row["КПЗ ОПУ арматура"] ?: "",
                kpzOpuHeating = row["КПЗ ОПУ обогрев"] ?: "",
                kpzOpuTemp = row["КПЗ ОПУ темп"] ?: "",
                kpz2Valve = row["КПЗ-2 арматура"] ?: "",
                kpz2Heating = row["КПЗ-2 обогрев"] ?: "",
                kpz2Temp = row["КПЗ-2 темп"] ?: "",
                firePumpValve = row["НПТ арматура"] ?: "",
                firePumpHeating = row["НПТ обогрев"] ?: "",
                firePumpTemp = row["НПТ темп"] ?: "",
                firePumpWaterLevel = row["НПТ уровень воды"] ?: "",
                workshopHeating = row["Мастерская обогрев"] ?: "",
                workshopTemp = row["Мастерская темп"] ?: "",
                artWellHeating = row["Артскважина обогрев"] ?: "",
                artesianWellHeating = row["Артез скважина обогрев"] ?: "",
                roomAbHeating = row["Помещ АБ обогрев"] ?: "",
                roomAbTemp = row["Помещ АБ темп"] ?: "",
                basementHeating = row["Подвал обогрев"] ?: "",
                basementTemp = row["Подвал темп"] ?: ""
            )

            InspectionArchiveData(
                timestamp = System.currentTimeMillis(),
                displayDate = "${row["Дата"] ?: ""} ${row["Время"] ?: ""}",
                oru35 = oru35,
                oru220 = oru220,
                atg = atg,
                oru500 = oru500,
                buildings = buildings,
                outdoorTemp = row["t наружного воздуха"] ?: "",
                inspectorName = row["ФИО дежурного"] ?: "",
                inspectorPosition = row["Должность"] ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().removeMenuProvider(menuProvider)
    }
}