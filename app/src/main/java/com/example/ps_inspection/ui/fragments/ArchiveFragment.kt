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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ps_inspection.ui.adapters.ArchiveAdapter
import com.example.ps_inspection.data.repositories.ArchiveItem
import com.example.ps_inspection.data.utils.ExcelExportService
import com.example.ps_inspection.data.repositories.InspectionArchiveManager
import com.example.ps_inspection.R
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
            // Устанавливаем цвета иконок
            menu.findItem(R.id.action_global_photos)?.icon?.setTint(android.graphics.Color.parseColor("#4CAF50"))
            menu.findItem(R.id.action_global_comments)?.icon?.setTint(android.graphics.Color.parseColor("#4CAF50"))
            // Корзину не трогаем
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
                    2 -> deleteArchive(archive.fileName)
                    3 -> showMergeDialog(archive.fileName)
                }
            }
            .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().removeMenuProvider(menuProvider)
    }

}