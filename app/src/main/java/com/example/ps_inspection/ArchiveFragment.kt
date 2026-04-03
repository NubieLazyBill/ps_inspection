package com.example.ps_inspection

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ps_inspection.databinding.FragmentArchiveBinding
import android.widget.TextView

class ArchiveFragment : Fragment() {

    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var archiveManager: InspectionArchiveManager
    private var archives = listOf<InspectionArchiveInfo>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        archiveManager = InspectionArchiveManager(requireContext())

        setupToolbar()
        loadArchives()

        // Кнопка временно закомментирована
        // binding.btnClearArchive.setOnClickListener {
        //     clearAllArchives()
        // }
    }

    private fun setupToolbar() {
        (activity as? MainActivity)?.findViewById<TextView>(R.id.toolbar_title)?.text = "Архив осмотров"
    }

    private fun loadArchives() {
        archives = archiveManager.getAllArchives()

        if (archives.isEmpty()) {
            binding.tvEmptyArchive.visibility = View.VISIBLE
            binding.listArchives.visibility = View.GONE
            //binding.btnClearArchive.visibility = View.GONE
        } else {
            binding.tvEmptyArchive.visibility = View.GONE
            binding.listArchives.visibility = View.VISIBLE
            //binding.btnClearArchive.visibility = View.VISIBLE

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                archives.map { it.displayDate }
            )
            binding.listArchives.adapter = adapter

            binding.listArchives.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                showArchiveOptions(position)
            }
        }
    }

    private fun showArchiveOptions(position: Int) {
        val archive = archives[position]

        val options = arrayOf("Загрузить этот осмотр", "Удалить")

        AlertDialog.Builder(requireContext())
            .setTitle(archive.displayDate)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> loadArchive(archive.fileName)
                    1 -> deleteArchive(position, archive.fileName)
                }
            }
            .show()
    }

    private fun loadArchive(fileName: String) {
        val archiveData = archiveManager.loadFromArchive(fileName)

        if (archiveData == null) {
            Toast.makeText(requireContext(), "Ошибка загрузки архива", Toast.LENGTH_SHORT).show()
            return
        }

        // Загружаем данные в ViewModel
        sharedViewModel.updateORU35Data {
            tsn2 = archiveData.oru35.tsn2
            tsn3 = archiveData.oru35.tsn3
            tsn4 = archiveData.oru35.tsn4
            tt352tsnA = archiveData.oru35.tt352tsnA
            tt352tsnB = archiveData.oru35.tt352tsnB
            tt352tsnC = archiveData.oru35.tt352tsnC
            tt353tsnA = archiveData.oru35.tt353tsnA
            tt353tsnB = archiveData.oru35.tt353tsnB
            tt353tsnC = archiveData.oru35.tt353tsnC
            v352tsnA = archiveData.oru35.v352tsnA
            v352tsnB = archiveData.oru35.v352tsnB
            v352tsnC = archiveData.oru35.v352tsnC
            v353tsnA = archiveData.oru35.v353tsnA
            v353tsnB = archiveData.oru35.v353tsnB
            v353tsnC = archiveData.oru35.v353tsnC
            tn352atg = archiveData.oru35.tn352atg
            tn353atg = archiveData.oru35.tn353atg
        }

        sharedViewModel.updateORU220Data {
            purgingMirnayaA = archiveData.oru220.purgingMirnayaA
            purgingMirnayaB = archiveData.oru220.purgingMirnayaB
            purgingMirnayaC = archiveData.oru220.purgingMirnayaC
            oilMirnayaA = archiveData.oru220.oilMirnayaA
            oilMirnayaB = archiveData.oru220.oilMirnayaB
            oilMirnayaC = archiveData.oru220.oilMirnayaC
            purgingTopazA = archiveData.oru220.purgingTopazA
            purgingTopazB = archiveData.oru220.purgingTopazB
            purgingTopazC = archiveData.oru220.purgingTopazC
            oilTopazA = archiveData.oru220.oilTopazA
            oilTopazB = archiveData.oru220.oilTopazB
            oilTopazC = archiveData.oru220.oilTopazC
            purgingOvA = archiveData.oru220.purgingOvA
            purgingOvB = archiveData.oru220.purgingOvB
            purgingOvC = archiveData.oru220.purgingOvC
            oilOvA = archiveData.oru220.oilOvA
            oilOvB = archiveData.oru220.oilOvB
            oilOvC = archiveData.oru220.oilOvC
            tnOsshFvUpper = archiveData.oru220.tnOsshFvUpper
            tnOsshFvLower = archiveData.oru220.tnOsshFvLower
            purgingV2atgA = archiveData.oru220.purgingV2atgA
            purgingV2atgB = archiveData.oru220.purgingV2atgB
            purgingV2atgC = archiveData.oru220.purgingV2atgC
            oilTt2atgA = archiveData.oru220.oilTt2atgA
            oilTt2atgB = archiveData.oru220.oilTt2atgB
            oilTt2atgC = archiveData.oru220.oilTt2atgC
            purgingShSV220A = archiveData.oru220.purgingShSV220A
            purgingShSV220B = archiveData.oru220.purgingShSV220B
            purgingShSV220C = archiveData.oru220.purgingShSV220C
            oilTtShSV220A = archiveData.oru220.oilTtShSV220A
            oilTtShSV220B = archiveData.oru220.oilTtShSV220B
            oilTtShSV220C = archiveData.oru220.oilTtShSV220C
            purgingV3atgA = archiveData.oru220.purgingV3atgA
            purgingV3atgB = archiveData.oru220.purgingV3atgB
            purgingV3atgC = archiveData.oru220.purgingV3atgC
            oilTt3atgA = archiveData.oru220.oilTt3atgA
            oilTt3atgB = archiveData.oru220.oilTt3atgB
            oilTt3atgC = archiveData.oru220.oilTt3atgC
            purgingOrbitaA = archiveData.oru220.purgingOrbitaA
            purgingOrbitaB = archiveData.oru220.purgingOrbitaB
            purgingOrbitaC = archiveData.oru220.purgingOrbitaC
            oilOrbitaA = archiveData.oru220.oilOrbitaA
            oilOrbitaB = archiveData.oru220.oilOrbitaB
            oilOrbitaC = archiveData.oru220.oilOrbitaC
            purgingFakelA = archiveData.oru220.purgingFakelA
            purgingFakelB = archiveData.oru220.purgingFakelB
            purgingFakelC = archiveData.oru220.purgingFakelC
            oilFakelA = archiveData.oru220.oilFakelA
            oilFakelB = archiveData.oru220.oilFakelB
            oilFakelC = archiveData.oru220.oilFakelC
            purgingCometa1A = archiveData.oru220.purgingCometa1A
            purgingCometa1B = archiveData.oru220.purgingCometa1B
            purgingCometa1C = archiveData.oru220.purgingCometa1C
            oilCometa1A = archiveData.oru220.oilCometa1A
            oilCometa1B = archiveData.oru220.oilCometa1B
            oilCometa1C = archiveData.oru220.oilCometa1C
            purgingCometa2A = archiveData.oru220.purgingCometa2A
            purgingCometa2B = archiveData.oru220.purgingCometa2B
            purgingCometa2C = archiveData.oru220.purgingCometa2C
            oilCometa2A = archiveData.oru220.oilCometa2A
            oilCometa2B = archiveData.oru220.oilCometa2B
            oilCometa2C = archiveData.oru220.oilCometa2C
            tn1UpperA = archiveData.oru220.tn1UpperA
            tn1UpperB = archiveData.oru220.tn1UpperB
            tn1UpperC = archiveData.oru220.tn1UpperC
            tn1LowerA = archiveData.oru220.tn1LowerA
            tn1LowerB = archiveData.oru220.tn1LowerB
            tn1LowerC = archiveData.oru220.tn1LowerC
            tn2UpperA = archiveData.oru220.tn2UpperA
            tn2UpperB = archiveData.oru220.tn2UpperB
            tn2UpperC = archiveData.oru220.tn2UpperC
            tn2LowerA = archiveData.oru220.tn2LowerA
            tn2LowerB = archiveData.oru220.tn2LowerB
            tn2LowerC = archiveData.oru220.tn2LowerC
        }

        sharedViewModel.updateATGData {
            // 2 АТГ ф.С
            atg2_c_oil_tank = archiveData.atg.atg2_c_oil_tank
            atg2_c_oil_rpn = archiveData.atg.atg2_c_oil_rpn
            atg2_c_pressure_500 = archiveData.atg.atg2_c_pressure_500
            atg2_c_pressure_220 = archiveData.atg.atg2_c_pressure_220
            atg2_c_temp_ts1 = archiveData.atg.atg2_c_temp_ts1
            atg2_c_temp_ts2 = archiveData.atg.atg2_c_temp_ts2
            atg2_c_pump_group1 = archiveData.atg.atg2_c_pump_group1
            atg2_c_pump_group2 = archiveData.atg.atg2_c_pump_group2
            atg2_c_pump_group3 = archiveData.atg.atg2_c_pump_group3
            atg2_c_pump_group4 = archiveData.atg.atg2_c_pump_group4

            // 2 АТГ ф.В
            atg2_b_oil_tank = archiveData.atg.atg2_b_oil_tank
            atg2_b_oil_rpn = archiveData.atg.atg2_b_oil_rpn
            atg2_b_pressure_500 = archiveData.atg.atg2_b_pressure_500
            atg2_b_pressure_220 = archiveData.atg.atg2_b_pressure_220
            atg2_b_temp_ts1 = archiveData.atg.atg2_b_temp_ts1
            atg2_b_temp_ts2 = archiveData.atg.atg2_b_temp_ts2
            atg2_b_pump_group1 = archiveData.atg.atg2_b_pump_group1
            atg2_b_pump_group2 = archiveData.atg.atg2_b_pump_group2
            atg2_b_pump_group3 = archiveData.atg.atg2_b_pump_group3
            atg2_b_pump_group4 = archiveData.atg.atg2_b_pump_group4

            // 2 АТГ ф.А
            atg2_a_oil_tank = archiveData.atg.atg2_a_oil_tank
            atg2_a_oil_rpn = archiveData.atg.atg2_a_oil_rpn
            atg2_a_pressure_500 = archiveData.atg.atg2_a_pressure_500
            atg2_a_pressure_220 = archiveData.atg.atg2_a_pressure_220
            atg2_a_temp_ts1 = archiveData.atg.atg2_a_temp_ts1
            atg2_a_temp_ts2 = archiveData.atg.atg2_a_temp_ts2
            atg2_a_pump_group1 = archiveData.atg.atg2_a_pump_group1
            atg2_a_pump_group2 = archiveData.atg.atg2_a_pump_group2
            atg2_a_pump_group3 = archiveData.atg.atg2_a_pump_group3
            atg2_a_pump_group4 = archiveData.atg.atg2_a_pump_group4

            // АТГ резервная фаза
            atg_reserve_oil_tank = archiveData.atg.atg_reserve_oil_tank
            atg_reserve_oil_rpn = archiveData.atg.atg_reserve_oil_rpn
            atg_reserve_pressure_500 = archiveData.atg.atg_reserve_pressure_500
            atg_reserve_pressure_220 = archiveData.atg.atg_reserve_pressure_220
            atg_reserve_temp_ts1 = archiveData.atg.atg_reserve_temp_ts1
            atg_reserve_temp_ts2 = archiveData.atg.atg_reserve_temp_ts2
            atg_reserve_pump_group1 = archiveData.atg.atg_reserve_pump_group1
            atg_reserve_pump_group2 = archiveData.atg.atg_reserve_pump_group2
            atg_reserve_pump_group3 = archiveData.atg.atg_reserve_pump_group3
            atg_reserve_pump_group4 = archiveData.atg.atg_reserve_pump_group4

            // 3 АТГ ф.С
            atg3_c_oil_tank = archiveData.atg.atg3_c_oil_tank
            atg3_c_oil_rpn = archiveData.atg.atg3_c_oil_rpn
            atg3_c_pressure_500 = archiveData.atg.atg3_c_pressure_500
            atg3_c_pressure_220 = archiveData.atg.atg3_c_pressure_220
            atg3_c_temp_ts1 = archiveData.atg.atg3_c_temp_ts1
            atg3_c_temp_ts2 = archiveData.atg.atg3_c_temp_ts2
            atg3_c_pump_group1 = archiveData.atg.atg3_c_pump_group1
            atg3_c_pump_group2 = archiveData.atg.atg3_c_pump_group2
            atg3_c_pump_group3 = archiveData.atg.atg3_c_pump_group3
            atg3_c_pump_group4 = archiveData.atg.atg3_c_pump_group4

            // 3 АТГ ф.В
            atg3_b_oil_tank = archiveData.atg.atg3_b_oil_tank
            atg3_b_oil_rpn = archiveData.atg.atg3_b_oil_rpn
            atg3_b_pressure_500 = archiveData.atg.atg3_b_pressure_500
            atg3_b_pressure_220 = archiveData.atg.atg3_b_pressure_220
            atg3_b_temp_ts1 = archiveData.atg.atg3_b_temp_ts1
            atg3_b_temp_ts2 = archiveData.atg.atg3_b_temp_ts2
            atg3_b_pump_group1 = archiveData.atg.atg3_b_pump_group1
            atg3_b_pump_group2 = archiveData.atg.atg3_b_pump_group2
            atg3_b_pump_group3 = archiveData.atg.atg3_b_pump_group3
            atg3_b_pump_group4 = archiveData.atg.atg3_b_pump_group4

            // 3 АТГ ф.А
            atg3_a_oil_tank = archiveData.atg.atg3_a_oil_tank
            atg3_a_oil_rpn = archiveData.atg.atg3_a_oil_rpn
            atg3_a_pressure_500 = archiveData.atg.atg3_a_pressure_500
            atg3_a_pressure_220 = archiveData.atg.atg3_a_pressure_220
            atg3_a_temp_ts1 = archiveData.atg.atg3_a_temp_ts1
            atg3_a_temp_ts2 = archiveData.atg.atg3_a_temp_ts2
            atg3_a_pump_group1 = archiveData.atg.atg3_a_pump_group1
            atg3_a_pump_group2 = archiveData.atg.atg3_a_pump_group2
            atg3_a_pump_group3 = archiveData.atg.atg3_a_pump_group3
            atg3_a_pump_group4 = archiveData.atg.atg3_a_pump_group4

            // Реакторы
            reactor_c_oil_tank = archiveData.atg.reactor_c_oil_tank
            reactor_c_pressure_500 = archiveData.atg.reactor_c_pressure_500
            reactor_c_temp_ts = archiveData.atg.reactor_c_temp_ts
            reactor_c_pump_group1 = archiveData.atg.reactor_c_pump_group1
            reactor_c_pump_group2 = archiveData.atg.reactor_c_pump_group2
            reactor_c_pump_group3 = archiveData.atg.reactor_c_pump_group3
            reactor_c_tt_neutral = archiveData.atg.reactor_c_tt_neutral

            reactor_b_oil_tank = archiveData.atg.reactor_b_oil_tank
            reactor_b_pressure_500 = archiveData.atg.reactor_b_pressure_500
            reactor_b_temp_ts = archiveData.atg.reactor_b_temp_ts
            reactor_b_pump_group1 = archiveData.atg.reactor_b_pump_group1
            reactor_b_pump_group2 = archiveData.atg.reactor_b_pump_group2
            reactor_b_pump_group3 = archiveData.atg.reactor_b_pump_group3
            reactor_b_tt_neutral = archiveData.atg.reactor_b_tt_neutral

            reactor_a_oil_tank = archiveData.atg.reactor_a_oil_tank
            reactor_a_pressure_500 = archiveData.atg.reactor_a_pressure_500
            reactor_a_temp_ts = archiveData.atg.reactor_a_temp_ts
            reactor_a_pump_group1 = archiveData.atg.reactor_a_pump_group1
            reactor_a_pump_group2 = archiveData.atg.reactor_a_pump_group2
            reactor_a_pump_group3 = archiveData.atg.reactor_a_pump_group3
            reactor_a_tt_neutral = archiveData.atg.reactor_a_tt_neutral
        }

        sharedViewModel.updateORU500Data {
            // В-500 Р-500 2С
            purgingR5002sA1 = archiveData.oru500.purgingR5002sA1
            purgingR5002sB1 = archiveData.oru500.purgingR5002sB1
            purgingR5002sC1 = archiveData.oru500.purgingR5002sC1
            purgingR5002sA2 = archiveData.oru500.purgingR5002sA2
            purgingR5002sB2 = archiveData.oru500.purgingR5002sB2
            purgingR5002sC2 = archiveData.oru500.purgingR5002sC2

            // В-500 ВШТ-31
            gasPressureVsht31A = archiveData.oru500.gasPressureVsht31A
            gasPressureVsht31B = archiveData.oru500.gasPressureVsht31B
            gasPressureVsht31C = archiveData.oru500.gasPressureVsht31C
            oilTtVsht31A = archiveData.oru500.oilTtVsht31A
            oilTtVsht31B = archiveData.oru500.oilTtVsht31B
            oilTtVsht31C = archiveData.oru500.oilTtVsht31C

            // В-500 ВЛТ-30
            gasPressureVlt30A = archiveData.oru500.gasPressureVlt30A
            gasPressureVlt30B = archiveData.oru500.gasPressureVlt30B
            gasPressureVlt30C = archiveData.oru500.gasPressureVlt30C
            oilTtVlt30A = archiveData.oru500.oilTtVlt30A
            oilTtVlt30B = archiveData.oru500.oilTtVlt30B
            oilTtVlt30C = archiveData.oru500.oilTtVlt30C

            // В-500 ВШЛ-32
            purgingVshl32A1 = archiveData.oru500.purgingVshl32A1
            purgingVshl32B1 = archiveData.oru500.purgingVshl32B1
            purgingVshl32C1 = archiveData.oru500.purgingVshl32C1
            purgingVshl32A2 = archiveData.oru500.purgingVshl32A2
            purgingVshl32B2 = archiveData.oru500.purgingVshl32B2
            purgingVshl32C2 = archiveData.oru500.purgingVshl32C2
            oilTtVshl32A = archiveData.oru500.oilTtVshl32A
            oilTtVshl32B = archiveData.oru500.oilTtVshl32B
            oilTtVshl32C = archiveData.oru500.oilTtVshl32C

            // В-500 ВШЛ-21
            purgingVshl21A1 = archiveData.oru500.purgingVshl21A1
            purgingVshl21B1 = archiveData.oru500.purgingVshl21B1
            purgingVshl21C1 = archiveData.oru500.purgingVshl21C1
            purgingVshl21A2 = archiveData.oru500.purgingVshl21A2
            purgingVshl21B2 = archiveData.oru500.purgingVshl21B2
            purgingVshl21C2 = archiveData.oru500.purgingVshl21C2
            oilTtVshl21A = archiveData.oru500.oilTtVshl21A
            oilTtVshl21B = archiveData.oru500.oilTtVshl21B
            oilTtVshl21C = archiveData.oru500.oilTtVshl21C

            // В-500 ВШТ-22
            purgingVsht22A1 = archiveData.oru500.purgingVsht22A1
            purgingVsht22B1 = archiveData.oru500.purgingVsht22B1
            purgingVsht22C1 = archiveData.oru500.purgingVsht22C1
            purgingVsht22A2 = archiveData.oru500.purgingVsht22A2
            purgingVsht22B2 = archiveData.oru500.purgingVsht22B2
            purgingVsht22C2 = archiveData.oru500.purgingVsht22C2
            oilTtVsht22A = archiveData.oru500.oilTtVsht22A
            oilTtVsht22B = archiveData.oru500.oilTtVsht22B
            oilTtVsht22C = archiveData.oru500.oilTtVsht22C

            // В-500 ВЛТ-20
            purgingVlt20A1 = archiveData.oru500.purgingVlt20A1
            purgingVlt20B1 = archiveData.oru500.purgingVlt20B1
            purgingVlt20C1 = archiveData.oru500.purgingVlt20C1
            purgingVlt20A2 = archiveData.oru500.purgingVlt20A2
            purgingVlt20B2 = archiveData.oru500.purgingVlt20B2
            purgingVlt20C2 = archiveData.oru500.purgingVlt20C2
            oilTtVlt20A = archiveData.oru500.oilTtVlt20A
            oilTtVlt20B = archiveData.oru500.oilTtVlt20B
            oilTtVlt20C = archiveData.oru500.oilTtVlt20C

            // В-500 ВШТ-11
            purgingVsht11A1 = archiveData.oru500.purgingVsht11A1
            purgingVsht11B1 = archiveData.oru500.purgingVsht11B1
            purgingVsht11C1 = archiveData.oru500.purgingVsht11C1
            purgingVsht11A2 = archiveData.oru500.purgingVsht11A2
            purgingVsht11B2 = archiveData.oru500.purgingVsht11B2
            purgingVsht11C2 = archiveData.oru500.purgingVsht11C2
            oilTtVsht11A = archiveData.oru500.oilTtVsht11A
            oilTtVsht11B = archiveData.oru500.oilTtVsht11B
            oilTtVsht11C = archiveData.oru500.oilTtVsht11C

            // В-500 ВШЛ-12
            purgingVshl12A1 = archiveData.oru500.purgingVshl12A1
            purgingVshl12B1 = archiveData.oru500.purgingVshl12B1
            purgingVshl12C1 = archiveData.oru500.purgingVshl12C1
            purgingVshl12A2 = archiveData.oru500.purgingVshl12A2
            purgingVshl12B2 = archiveData.oru500.purgingVshl12B2
            purgingVshl12C2 = archiveData.oru500.purgingVshl12C2
            oilTtVshl12A = archiveData.oru500.oilTtVshl12A
            oilTtVshl12B = archiveData.oru500.oilTtVshl12B
            oilTtVshl12C = archiveData.oru500.oilTtVshl12C

            // 1ТН-500
            tn1500Cascade1A = archiveData.oru500.tn1500Cascade1A
            tn1500Cascade1B = archiveData.oru500.tn1500Cascade1B
            tn1500Cascade1C = archiveData.oru500.tn1500Cascade1C
            tn1500Cascade2A = archiveData.oru500.tn1500Cascade2A
            tn1500Cascade2B = archiveData.oru500.tn1500Cascade2B
            tn1500Cascade2C = archiveData.oru500.tn1500Cascade2C
            tn1500Cascade3A = archiveData.oru500.tn1500Cascade3A
            tn1500Cascade3B = archiveData.oru500.tn1500Cascade3B
            tn1500Cascade3C = archiveData.oru500.tn1500Cascade3C
            tn1500Cascade4A = archiveData.oru500.tn1500Cascade4A
            tn1500Cascade4B = archiveData.oru500.tn1500Cascade4B
            tn1500Cascade4C = archiveData.oru500.tn1500Cascade4C

            // 2ТН-500
            tn2500Cascade1A = archiveData.oru500.tn2500Cascade1A
            tn2500Cascade1B = archiveData.oru500.tn2500Cascade1B
            tn2500Cascade1C = archiveData.oru500.tn2500Cascade1C
            tn2500Cascade2A = archiveData.oru500.tn2500Cascade2A
            tn2500Cascade2B = archiveData.oru500.tn2500Cascade2B
            tn2500Cascade2C = archiveData.oru500.tn2500Cascade2C
            tn2500Cascade3A = archiveData.oru500.tn2500Cascade3A
            tn2500Cascade3B = archiveData.oru500.tn2500Cascade3B
            tn2500Cascade3C = archiveData.oru500.tn2500Cascade3C
            tn2500Cascade4A = archiveData.oru500.tn2500Cascade4A
            tn2500Cascade4B = archiveData.oru500.tn2500Cascade4B
            tn2500Cascade4C = archiveData.oru500.tn2500Cascade4C

            // ТН-500 СГРЭС-1
            tn500Sgres1Cascade1A = archiveData.oru500.tn500Sgres1Cascade1A
            tn500Sgres1Cascade1B = archiveData.oru500.tn500Sgres1Cascade1B
            tn500Sgres1Cascade1C = archiveData.oru500.tn500Sgres1Cascade1C
            tn500Sgres1Cascade2A = archiveData.oru500.tn500Sgres1Cascade2A
            tn500Sgres1Cascade2B = archiveData.oru500.tn500Sgres1Cascade2B
            tn500Sgres1Cascade2C = archiveData.oru500.tn500Sgres1Cascade2C
            tn500Sgres1Cascade3A = archiveData.oru500.tn500Sgres1Cascade3A
            tn500Sgres1Cascade3B = archiveData.oru500.tn500Sgres1Cascade3B
            tn500Sgres1Cascade3C = archiveData.oru500.tn500Sgres1Cascade3C
            tn500Sgres1Cascade4A = archiveData.oru500.tn500Sgres1Cascade4A
            tn500Sgres1Cascade4B = archiveData.oru500.tn500Sgres1Cascade4B
            tn500Sgres1Cascade4C = archiveData.oru500.tn500Sgres1Cascade4C
        }

        sharedViewModel.updateBuildingsData {
            compressor1Valve = archiveData.buildings.compressor1Valve
            compressor1Heating = archiveData.buildings.compressor1Heating
            compressor1Temp = archiveData.buildings.compressor1Temp
            ballroom1Valve = archiveData.buildings.ballroom1Valve
            ballroom1Heating = archiveData.buildings.ballroom1Heating
            ballroom1Temp = archiveData.buildings.ballroom1Temp
            compressor2Valve = archiveData.buildings.compressor2Valve
            compressor2Heating = archiveData.buildings.compressor2Heating
            compressor2Temp = archiveData.buildings.compressor2Temp
            ballroom2Valve = archiveData.buildings.ballroom2Valve
            ballroom2Heating = archiveData.buildings.ballroom2Heating
            ballroom2Temp = archiveData.buildings.ballroom2Temp
            kpzOpuValve = archiveData.buildings.kpzOpuValve
            kpzOpuHeating = archiveData.buildings.kpzOpuHeating
            kpzOpuTemp = archiveData.buildings.kpzOpuTemp
            kpz2Valve = archiveData.buildings.kpz2Valve
            kpz2Heating = archiveData.buildings.kpz2Heating
            kpz2Temp = archiveData.buildings.kpz2Temp
            firePumpValve = archiveData.buildings.firePumpValve
            firePumpHeating = archiveData.buildings.firePumpHeating
            firePumpTemp = archiveData.buildings.firePumpTemp
            workshopHeating = archiveData.buildings.workshopHeating
            workshopTemp = archiveData.buildings.workshopTemp
            artWellHeating = archiveData.buildings.artWellHeating
            artesianWellHeating = archiveData.buildings.artesianWellHeating
            roomAbHeating = archiveData.buildings.roomAbHeating
            roomAbTemp = archiveData.buildings.roomAbTemp
            basementHeating = archiveData.buildings.basementHeating
            basementTemp = archiveData.buildings.basementTemp
        }

        Toast.makeText(requireContext(), "Осмотр от ${archiveData.displayDate} загружен", Toast.LENGTH_LONG).show()

        // Возвращаемся на главный экран
        requireActivity().onBackPressed()
    }

    private fun deleteArchive(position: Int, fileName: String) {
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

    private fun clearAllArchives() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистка архива")
            .setMessage("Удалить все сохранённые осмотры из архива?")
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
    }
}