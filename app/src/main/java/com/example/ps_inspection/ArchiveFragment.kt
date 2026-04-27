package com.example.ps_inspection

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ps_inspection.databinding.FragmentArchiveBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ArchiveFragment : Fragment() {
    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var archiveManager: InspectionArchiveManager
    private lateinit var adapter: ArchiveAdapter
    private var allArchives = listOf<ArchiveItem>()
    private var currentFilter = "Все"

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_archive, menu)
        }

        override fun onMenuItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_clear_archive -> {
                    showClearAllDialog()
                    true
                }
                else -> false
            }
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
        setupFilters()
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

    private fun setupFilters() {
        binding.filterAll.setOnClickListener {
            currentFilter = "Все"
            updateFilterUI(binding.filterAll)
            filterArchives()
        }
        binding.filterORU35.setOnClickListener {
            currentFilter = "ОРУ-35"
            updateFilterUI(binding.filterORU35)
            filterArchives()
        }
        binding.filterORU220.setOnClickListener {
            currentFilter = "ОРУ-220"
            updateFilterUI(binding.filterORU220)
            filterArchives()
        }
        binding.filterORU500.setOnClickListener {
            currentFilter = "ОРУ-500"
            updateFilterUI(binding.filterORU500)
            filterArchives()
        }

        try {
            binding.filterATG.setOnClickListener {
                currentFilter = "АТГ"
                updateFilterUI(binding.filterATG)
                filterArchives()
            }
        } catch (e: Exception) {}

        try {
            binding.filterBuildings.setOnClickListener {
                currentFilter = "Здания"
                updateFilterUI(binding.filterBuildings)
                filterArchives()
            }
        } catch (e: Exception) {}
    }

    private fun updateFilterUI(selectedView: View) {
        val filters = mutableListOf(
            binding.filterAll,
            binding.filterORU35,
            binding.filterORU220,
            binding.filterORU500
        )

        try { filters.add(binding.filterATG) } catch (e: Exception) {}
        try { filters.add(binding.filterBuildings) } catch (e: Exception) {}

        filters.forEach { filter ->
            if (filter == selectedView) {
                filter.background = requireContext().getDrawable(R.drawable.filter_chip_selected)
                filter.setTextColor(requireContext().getColor(android.R.color.white))
            } else {
                filter.background = requireContext().getDrawable(R.drawable.filter_chip)
                filter.setTextColor(requireContext().getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewArchives.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArchiveAdapter(emptyList()) { archive -> showArchiveOptions(archive) }
        binding.recyclerViewArchives.adapter = adapter
    }

    private fun loadArchives() {
        allArchives = archiveManager.getAllArchives()
        try {
            binding.tvCount.text = allArchives.size.toString()
        } catch (e: Exception) {}
        filterArchives()
    }

    private fun filterArchives() {
        val filtered = if (currentFilter == "Все") {
            allArchives
        } else {
            allArchives.filter { it.equipmentType == currentFilter }
        }

        adapter.updateData(filtered)

        try {
            if (filtered.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerViewArchives.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerViewArchives.visibility = View.VISIBLE
            }
        } catch (e: Exception) {}
    }

    // ⬇️ УПРОЩЁННОЕ МЕНЮ - только "Перенести данные" и "Поделиться", "Удалить"
    private fun showArchiveOptions(archive: ArchiveItem) {
        val options = arrayOf(
            "📤 Поделиться (Excel)",
            "🗑️ Удалить",
            "📥 Перенести данные в текущий осмотр"
        )

        AlertDialog.Builder(requireContext())
            .setTitle(archive.displayDate)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareArchive(archive.fileName)
                    1 -> deleteArchive(archive.fileName)
                    2 -> showMergeDialog(archive.fileName)
                }
            }
            .show()
    }

    // ДИАЛОГ ПЕРЕНОСА ДАННЫХ
    private fun showMergeDialog(fileName: String) {
        val items = arrayOf(
            "ОРУ-35",
            "ОРУ-220",
            "ОРУ-500",
            "АТГ + Реакторы",
            "Здания"
        )
        val checked = BooleanArray(items.size) { false }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📥 Выберите данные для переноса в текущий осмотр")
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

        if (sections[0]) { mergeORU35(archiveData.oru35); count++ }
        if (sections[1]) { mergeORU220(archiveData.oru220); count++ }
        if (sections[2]) { mergeORU500(archiveData.oru500); count++ }
        if (sections[3]) { mergeATG(archiveData.atg); count++ }
        if (sections[4]) { mergeBuildings(archiveData.buildings); count++ }

        if (count > 0) {
            Toast.makeText(requireContext(), "✅ Перенесено разделов: $count", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Ничего не выбрано", Toast.LENGTH_SHORT).show()
        }
    }

    // ... ВАШИ МЕТОДЫ СЛИЯНИЯ (mergeORU35, mergeORU220, mergeORU500, mergeBuildings, mergeATG) ОСТАЮТСЯ ...
    // (они уже есть в вашем файле)

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


    private fun mergeORU35(src: InspectionORU35Data) {
        sharedViewModel.updateORU35Data {
            if (src.tsn2.isNotBlank()) tsn2 = src.tsn2
            if (src.tsn3.isNotBlank()) tsn3 = src.tsn3
            if (src.tsn4.isNotBlank()) tsn4 = src.tsn4
            if (src.tt352tsnA.isNotBlank()) tt352tsnA = src.tt352tsnA
            if (src.tt352tsnB.isNotBlank()) tt352tsnB = src.tt352tsnB
            if (src.tt352tsnC.isNotBlank()) tt352tsnC = src.tt352tsnC
            if (src.tt353tsnA.isNotBlank()) tt353tsnA = src.tt353tsnA
            if (src.tt353tsnB.isNotBlank()) tt353tsnB = src.tt353tsnB
            if (src.tt353tsnC.isNotBlank()) tt353tsnC = src.tt353tsnC
            if (src.v352tsnA.isNotBlank()) v352tsnA = src.v352tsnA
            if (src.v352tsnB.isNotBlank()) v352tsnB = src.v352tsnB
            if (src.v352tsnC.isNotBlank()) v352tsnC = src.v352tsnC
            if (src.v353tsnA.isNotBlank()) v353tsnA = src.v353tsnA
            if (src.v353tsnB.isNotBlank()) v353tsnB = src.v353tsnB
            if (src.v353tsnC.isNotBlank()) v353tsnC = src.v353tsnC
            //if (src.tn352atg.isNotBlank()) tn352atg = src.tn352atg
            //if (src.tn353atg.isNotBlank()) tn353atg = src.tn353atg
        }
    }

    private fun mergeORU220(src: InspectionORU220Data) {
        sharedViewModel.updateORU220Data {
            purgingMirnayaA = src.purgingMirnayaA
            purgingMirnayaB = src.purgingMirnayaB
            purgingMirnayaC = src.purgingMirnayaC
            oilMirnayaA = src.oilMirnayaA
            oilMirnayaB = src.oilMirnayaB
            oilMirnayaC = src.oilMirnayaC
            purgingTopazA = src.purgingTopazA
            purgingTopazB = src.purgingTopazB
            purgingTopazC = src.purgingTopazC
            oilTopazA = src.oilTopazA
            oilTopazB = src.oilTopazB
            oilTopazC = src.oilTopazC
            purgingOvA = src.purgingOvA
            purgingOvB = src.purgingOvB
            purgingOvC = src.purgingOvC
            oilOvA = src.oilOvA
            oilOvB = src.oilOvB
            oilOvC = src.oilOvC
            tnOsshFvUpper = src.tnOsshFvUpper
            tnOsshFvLower = src.tnOsshFvLower
            purgingV2atgA = src.purgingV2atgA
            purgingV2atgB = src.purgingV2atgB
            purgingV2atgC = src.purgingV2atgC
            oilTt2atgA = src.oilTt2atgA
            oilTt2atgB = src.oilTt2atgB
            oilTt2atgC = src.oilTt2atgC
            purgingShSV220A = src.purgingShSV220A
            purgingShSV220B = src.purgingShSV220B
            purgingShSV220C = src.purgingShSV220C
            oilTtShSV220A = src.oilTtShSV220A
            oilTtShSV220B = src.oilTtShSV220B
            oilTtShSV220C = src.oilTtShSV220C
            purgingV3atgA = src.purgingV3atgA
            purgingV3atgB = src.purgingV3atgB
            purgingV3atgC = src.purgingV3atgC
            oilTt3atgA = src.oilTt3atgA
            oilTt3atgB = src.oilTt3atgB
            oilTt3atgC = src.oilTt3atgC
            purgingOrbitaA = src.purgingOrbitaA
            purgingOrbitaB = src.purgingOrbitaB
            purgingOrbitaC = src.purgingOrbitaC
            oilOrbitaA = src.oilOrbitaA
            oilOrbitaB = src.oilOrbitaB
            oilOrbitaC = src.oilOrbitaC
            purgingFakelA = src.purgingFakelA
            purgingFakelB = src.purgingFakelB
            purgingFakelC = src.purgingFakelC
            oilFakelA = src.oilFakelA
            oilFakelB = src.oilFakelB
            oilFakelC = src.oilFakelC
            tn1UpperA = src.tn1UpperA
            tn1UpperB = src.tn1UpperB
            tn1UpperC = src.tn1UpperC
            tn1LowerA = src.tn1LowerA
            tn1LowerB = src.tn1LowerB
            tn1LowerC = src.tn1LowerC
            tn2UpperA = src.tn2UpperA
            tn2UpperB = src.tn2UpperB
            tn2UpperC = src.tn2UpperC
            tn2LowerA = src.tn2LowerA
            tn2LowerB = src.tn2LowerB
            tn2LowerC = src.tn2LowerC
        }
    }

    private fun mergeORU500(src: InspectionORU500Data) {
        sharedViewModel.updateORU500Data {
            purgingR5002sA1 = src.purgingR5002sA1
            purgingR5002sB1 = src.purgingR5002sB1
            purgingR5002sC1 = src.purgingR5002sC1
            purgingR5002sA2 = src.purgingR5002sA2
            purgingR5002sB2 = src.purgingR5002sB2
            purgingR5002sC2 = src.purgingR5002sC2
            gasPressureVsht31A = src.gasPressureVsht31A
            gasPressureVsht31B = src.gasPressureVsht31B
            gasPressureVsht31C = src.gasPressureVsht31C
            oilTtVsht31A = src.oilTtVsht31A
            oilTtVsht31B = src.oilTtVsht31B
            oilTtVsht31C = src.oilTtVsht31C
            gasPressureVlt30A = src.gasPressureVlt30A
            gasPressureVlt30B = src.gasPressureVlt30B
            gasPressureVlt30C = src.gasPressureVlt30C
            oilTtVlt30A = src.oilTtVlt30A
            oilTtVlt30B = src.oilTtVlt30B
            oilTtVlt30C = src.oilTtVlt30C
            purgingVshl32A1 = src.purgingVshl32A1
            purgingVshl32B1 = src.purgingVshl32B1
            purgingVshl32C1 = src.purgingVshl32C1
            purgingVshl32A2 = src.purgingVshl32A2
            purgingVshl32B2 = src.purgingVshl32B2
            purgingVshl32C2 = src.purgingVshl32C2
            oilTtVshl32A = src.oilTtVshl32A
            oilTtVshl32B = src.oilTtVshl32B
            oilTtVshl32C = src.oilTtVshl32C
            purgingVshl21A1 = src.purgingVshl21A1
            purgingVshl21B1 = src.purgingVshl21B1
            purgingVshl21C1 = src.purgingVshl21C1
            purgingVshl21A2 = src.purgingVshl21A2
            purgingVshl21B2 = src.purgingVshl21B2
            purgingVshl21C2 = src.purgingVshl21C2
            oilTtVshl21A = src.oilTtVshl21A
            oilTtVshl21B = src.oilTtVshl21B
            oilTtVshl21C = src.oilTtVshl21C
            purgingVsht22A1 = src.purgingVsht22A1
            purgingVsht22B1 = src.purgingVsht22B1
            purgingVsht22C1 = src.purgingVsht22C1
            purgingVsht22A2 = src.purgingVsht22A2
            purgingVsht22B2 = src.purgingVsht22B2
            purgingVsht22C2 = src.purgingVsht22C2
            oilTtVsht22A = src.oilTtVsht22A
            oilTtVsht22B = src.oilTtVsht22B
            oilTtVsht22C = src.oilTtVsht22C
            purgingVlt20A1 = src.purgingVlt20A1
            purgingVlt20B1 = src.purgingVlt20B1
            purgingVlt20C1 = src.purgingVlt20C1
            purgingVlt20A2 = src.purgingVlt20A2
            purgingVlt20B2 = src.purgingVlt20B2
            purgingVlt20C2 = src.purgingVlt20C2
            oilTtVlt20A = src.oilTtVlt20A
            oilTtVlt20B = src.oilTtVlt20B
            oilTtVlt20C = src.oilTtVlt20C
            tn1500Cascade1A = src.tn1500Cascade1A
            tn1500Cascade1B = src.tn1500Cascade1B
            tn1500Cascade1C = src.tn1500Cascade1C
            tn1500Cascade2A = src.tn1500Cascade2A
            tn1500Cascade2B = src.tn1500Cascade2B
            tn1500Cascade2C = src.tn1500Cascade2C
            tn2500Cascade1A = src.tn2500Cascade1A
            tn2500Cascade1B = src.tn2500Cascade1B
            tn2500Cascade1C = src.tn2500Cascade1C
            tn500Sgres1Cascade1A = src.tn500Sgres1Cascade1A
            tn500Sgres1Cascade1B = src.tn500Sgres1Cascade1B
            tn500Sgres1Cascade1C = src.tn500Sgres1Cascade1C
            oilTtTrachukovskayaA = src.oilTtTrachukovskayaA
            oilTtTrachukovskayaB = src.oilTtTrachukovskayaB
            oilTtTrachukovskayaC = src.oilTtTrachukovskayaC
        }
    }

    private fun mergeBuildings(src: InspectionBuildingsData) {
        sharedViewModel.updateBuildingsData {
            compressor1Valve = src.compressor1Valve
            compressor1Heating = src.compressor1Heating
            compressor1Temp = src.compressor1Temp
            ballroom1Valve = src.ballroom1Valve
            ballroom1Heating = src.ballroom1Heating
            ballroom1Temp = src.ballroom1Temp
            compressor2Valve = src.compressor2Valve
            compressor2Heating = src.compressor2Heating
            compressor2Temp = src.compressor2Temp
            ballroom2Valve = src.ballroom2Valve
            ballroom2Heating = src.ballroom2Heating
            ballroom2Temp = src.ballroom2Temp
            kpzOpuValve = src.kpzOpuValve
            kpzOpuHeating = src.kpzOpuHeating
            kpzOpuTemp = src.kpzOpuTemp
            kpz2Valve = src.kpz2Valve
            kpz2Heating = src.kpz2Heating
            kpz2Temp = src.kpz2Temp
            firePumpValve = src.firePumpValve
            firePumpHeating = src.firePumpHeating
            firePumpTemp = src.firePumpTemp
            workshopHeating = src.workshopHeating
            workshopTemp = src.workshopTemp
            artWellHeating = src.artWellHeating
            artesianWellHeating = src.artesianWellHeating
            roomAbHeating = src.roomAbHeating
            roomAbTemp = src.roomAbTemp
            basementHeating = src.basementHeating
            basementTemp = src.basementTemp
        }
    }

    private fun mergeATG(src: InspectionATGData) {
        sharedViewModel.updateATGData {
            atg2_c_oil_tank = src.atg2_c_oil_tank
            atg2_c_oil_rpn = src.atg2_c_oil_rpn
            atg2_c_pressure_500 = src.atg2_c_pressure_500
            atg2_c_pressure_220 = src.atg2_c_pressure_220
            atg2_c_temp_ts1 = src.atg2_c_temp_ts1
            atg2_c_temp_ts2 = src.atg2_c_temp_ts2
            atg2_c_pump_group1 = src.atg2_c_pump_group1
            atg2_c_pump_group2 = src.atg2_c_pump_group2
            atg2_c_pump_group3 = src.atg2_c_pump_group3
            atg2_c_pump_group4 = src.atg2_c_pump_group4
            atg2_b_oil_tank = src.atg2_b_oil_tank
            atg2_b_oil_rpn = src.atg2_b_oil_rpn
            atg2_b_pressure_500 = src.atg2_b_pressure_500
            atg2_b_pressure_220 = src.atg2_b_pressure_220
            atg2_b_temp_ts1 = src.atg2_b_temp_ts1
            atg2_b_temp_ts2 = src.atg2_b_temp_ts2
            atg2_b_pump_group1 = src.atg2_b_pump_group1
            atg2_b_pump_group2 = src.atg2_b_pump_group2
            atg2_b_pump_group3 = src.atg2_b_pump_group3
            atg2_b_pump_group4 = src.atg2_b_pump_group4
            atg2_a_oil_tank = src.atg2_a_oil_tank
            atg2_a_oil_rpn = src.atg2_a_oil_rpn
            atg2_a_pressure_500 = src.atg2_a_pressure_500
            atg2_a_pressure_220 = src.atg2_a_pressure_220
            atg2_a_temp_ts1 = src.atg2_a_temp_ts1
            atg2_a_temp_ts2 = src.atg2_a_temp_ts2
            atg2_a_pump_group1 = src.atg2_a_pump_group1
            atg2_a_pump_group2 = src.atg2_a_pump_group2
            atg2_a_pump_group3 = src.atg2_a_pump_group3
            atg2_a_pump_group4 = src.atg2_a_pump_group4
            atg_reserve_oil_tank = src.atg_reserve_oil_tank
            atg_reserve_oil_rpn = src.atg_reserve_oil_rpn
            atg_reserve_pressure_500 = src.atg_reserve_pressure_500
            atg_reserve_pressure_220 = src.atg_reserve_pressure_220
            atg_reserve_temp_ts1 = src.atg_reserve_temp_ts1
            atg_reserve_temp_ts2 = src.atg_reserve_temp_ts2
            atg3_c_oil_tank = src.atg3_c_oil_tank
            atg3_c_oil_rpn = src.atg3_c_oil_rpn
            atg3_c_pressure_500 = src.atg3_c_pressure_500
            atg3_c_pressure_220 = src.atg3_c_pressure_220
            atg3_c_temp_ts1 = src.atg3_c_temp_ts1
            atg3_c_temp_ts2 = src.atg3_c_temp_ts2
            atg3_c_pump_group1 = src.atg3_c_pump_group1
            atg3_c_pump_group2 = src.atg3_c_pump_group2
            atg3_c_pump_group3 = src.atg3_c_pump_group3
            atg3_c_pump_group4 = src.atg3_c_pump_group4
            reactor_c_oil_tank = src.reactor_c_oil_tank
            reactor_c_pressure_500 = src.reactor_c_pressure_500
            reactor_c_temp_ts = src.reactor_c_temp_ts
            reactor_c_pump_group1 = src.reactor_c_pump_group1
            reactor_c_pump_group2 = src.reactor_c_pump_group2
            reactor_c_pump_group3 = src.reactor_c_pump_group3
        }
    }

    private fun mergeATGPressuresOnly(src: InspectionATGData) {
        sharedViewModel.updateATGData {
            atg2_c_pump_group1 = src.atg2_c_pump_group1
            atg2_c_pump_group2 = src.atg2_c_pump_group2
            atg2_c_pump_group3 = src.atg2_c_pump_group3
            atg2_c_pump_group4 = src.atg2_c_pump_group4
            atg2_b_pump_group1 = src.atg2_b_pump_group1
            atg2_b_pump_group2 = src.atg2_b_pump_group2
            atg2_b_pump_group3 = src.atg2_b_pump_group3
            atg2_b_pump_group4 = src.atg2_b_pump_group4
            atg2_a_pump_group1 = src.atg2_a_pump_group1
            atg2_a_pump_group2 = src.atg2_a_pump_group2
            atg2_a_pump_group3 = src.atg2_a_pump_group3
            atg2_a_pump_group4 = src.atg2_a_pump_group4
            atg_reserve_pump_group1 = src.atg_reserve_pump_group1
            atg_reserve_pump_group2 = src.atg_reserve_pump_group2
            atg_reserve_pump_group3 = src.atg_reserve_pump_group3
            atg_reserve_pump_group4 = src.atg_reserve_pump_group4
            atg3_c_pump_group1 = src.atg3_c_pump_group1
            atg3_c_pump_group2 = src.atg3_c_pump_group2
            atg3_c_pump_group3 = src.atg3_c_pump_group3
            atg3_c_pump_group4 = src.atg3_c_pump_group4
            reactor_c_pump_group1 = src.reactor_c_pump_group1
            reactor_c_pump_group2 = src.reactor_c_pump_group2
            reactor_c_pump_group3 = src.reactor_c_pump_group3
        }
    }
}