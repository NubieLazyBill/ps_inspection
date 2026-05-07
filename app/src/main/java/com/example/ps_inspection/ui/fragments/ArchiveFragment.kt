package com.example.ps_inspection.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
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
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.data.repositories.InspectionArchiveData
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
import java.io.File

class ArchiveFragment : Fragment() {
    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

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
                else -> false
            }
        }

        override fun onPrepareMenu(menu: Menu) {
            menu.findItem(R.id.action_global_photos)?.icon?.setTint(android.graphics.Color.parseColor("#4CAF50"))
            menu.findItem(R.id.action_global_comments)?.icon?.setTint(android.graphics.Color.parseColor("#4CAF50"))
        }
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

        // Загружаем осмотры с сервера
        viewLifecycleOwner.lifecycleScope.launch {
            loadServerArchives()
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
            val serverData = sheetsService.getAllInspections()

            if (serverData != null && serverData.isNotEmpty()) {
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
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                // Объединяем с локальными архивами, убираем дубликаты
                val allDisplayDates = allArchives.map { it.displayDate }.toSet()
                val newServerArchives = serverArchives.filter { it.displayDate !in allDisplayDates }
                allArchives = (allArchives + newServerArchives).sortedByDescending { it.displayDate }
                adapter.updateData(allArchives)

                if (allArchives.isNotEmpty()) {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerViewArchives.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

            InspectionArchiveData(
                timestamp = System.currentTimeMillis(),
                displayDate = "${row["Дата"] ?: ""} ${row["Время"] ?: ""}",
                oru35 = oru35,
                oru220 = oru220,
                atg = InspectionATGData(),
                oru500 = InspectionORU500Data(),
                buildings = InspectionBuildingsData(),
                outdoorTemp = row["t наружного воздуха"] ?: "",
                inspectorName = row["ФИО дежурного"] ?: "",
                inspectorPosition = row["Должность"] ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().removeMenuProvider(menuProvider)
    }
}