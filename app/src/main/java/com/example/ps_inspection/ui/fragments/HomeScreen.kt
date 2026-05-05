package com.example.ps_inspection.ui.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.data.utils.ExcelExportService
import com.example.ps_inspection.R
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentHomeScreenBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeScreen : Fragment() {
    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписываемся на обновление данных для прогресса
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru35Data.collectLatest { updateProgressIndicators() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru220Data.collectLatest { updateProgressIndicators() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru500Data.collectLatest { updateProgressIndicators() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.atgData.collectLatest { updateProgressIndicators() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.buildingsData.collectLatest { updateProgressIndicators() }
        }

        // Навигация по карточкам
        binding.cardOru35.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU35)
        }
        binding.cardOru220.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU2202)
        }
        binding.cardOru500.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU500)
        }
        binding.cardAtg.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionATG)
        }
        binding.cardBuildings.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionBuildings)
        }

        // Кнопки нижней панели
        binding.btnBottomArchive.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_archiveFragment)
        }

        binding.btnBottomSave.setOnClickListener {
            exportToExcel()
        }

        binding.btnBottomClear.setOnClickListener {
            clearAllData()
        }

        // Первоначальное обновление
        updateProgressIndicators()
    }

    /**
     * Обновляет индикаторы прогресса для всех карточек
     */
    private fun updateProgressIndicators() {
        updateCardProgress(
            binding.tvProgressOru35,
            calculateORU35Progress()
        )
        updateCardProgress(
            binding.tvProgressOru220,
            calculateORU220Progress()
        )
        updateCardProgress(
            binding.tvProgressOru500,
            calculateORU500Progress()
        )
        updateCardProgress(
            binding.tvProgressAtg,
            calculateATGProgress()
        )
        updateCardProgress(
            binding.tvProgressBuildings,
            calculateBuildingsProgress()
        )
    }

    private fun updateCardProgress(textView: TextView, progress: Int) {
        textView.text = "$progress%"
        when {
            progress == 0 -> {
                textView.setTextColor(0xFF9E9E9E.toInt())
                textView.setBackgroundResource(R.drawable.bg_progress_empty)
            }
            progress < 100 -> {
                textView.setTextColor(0xFFFF9800.toInt())
                textView.setBackgroundResource(R.drawable.bg_progress_partial)
            }
            else -> {
                textView.setTextColor(0xFF4CAF50.toInt())
                textView.setBackgroundResource(R.drawable.bg_progress_complete)
            }
        }
    }

    private fun calculateORU35Progress(): Int {
        val data = sharedViewModel.oru35Data.value
        val fields = listOf(
            data.tsn2, data.tsn3, data.tsn4,
            data.tt352tsnA, data.tt352tsnB, data.tt352tsnC,
            data.tt353tsnA, data.tt353tsnB, data.tt353tsnC,
            data.v352tsnA, data.v352tsnB, data.v352tsnC,
            data.v353tsnA, data.v353tsnB, data.v353tsnC
        )
        return calculatePercent(fields)
    }

    private fun calculateORU220Progress(): Int {
        val data = sharedViewModel.oru220Data.value
        val fields = listOf(
            data.purgingMirnayaA, data.purgingMirnayaB, data.purgingMirnayaC,
            data.oilMirnayaA, data.oilMirnayaB, data.oilMirnayaC,
            data.purgingTopazA, data.purgingTopazB, data.purgingTopazC,
            data.oilTopazA, data.oilTopazB, data.oilTopazC,
            data.purgingOvA, data.purgingOvB, data.purgingOvC,
            data.oilOvA, data.oilOvB, data.oilOvC,
            data.tnOsshFvUpper, data.tnOsshFvLower,
            data.purgingV2atgA, data.purgingV2atgB, data.purgingV2atgC,
            data.oilTt2atgA, data.oilTt2atgB, data.oilTt2atgC,
            data.purgingShSV220A, data.purgingShSV220B, data.purgingShSV220C,
            data.oilTtShSV220A, data.oilTtShSV220B, data.oilTtShSV220C,
            data.purgingV3atgA, data.purgingV3atgB, data.purgingV3atgC,
            data.oilTt3atgA, data.oilTt3atgB, data.oilTt3atgC,
            data.purgingOrbitaA, data.purgingOrbitaB, data.purgingOrbitaC,
            data.oilOrbitaA, data.oilOrbitaB, data.oilOrbitaC,
            data.purgingFakelA, data.purgingFakelB, data.purgingFakelC,
            data.oilFakelA, data.oilFakelB, data.oilFakelC,
            data.purgingCometa1A, data.purgingCometa1B, data.purgingCometa1C,
            data.oilCometa1A, data.oilCometa1B, data.oilCometa1C,
            data.purgingCometa2A, data.purgingCometa2B, data.purgingCometa2C,
            data.oilCometa2A, data.oilCometa2B, data.oilCometa2C,
            data.tn1UpperA, data.tn1UpperB, data.tn1UpperC,
            data.tn1LowerA, data.tn1LowerB, data.tn1LowerC,
            data.tn2UpperA, data.tn2UpperB, data.tn2UpperC,
            data.tn2LowerA, data.tn2LowerB, data.tn2LowerC
        )
        return calculatePercent(fields)
    }

    private fun calculateORU500Progress(): Int {
        val data = sharedViewModel.oru500Data.value
        val fields = listOf(
            data.purgingR5002sA1, data.purgingR5002sB1, data.purgingR5002sC1,
            data.purgingR5002sA2, data.purgingR5002sB2, data.purgingR5002sC2,
            data.gasPressureVsht31A, data.gasPressureVsht31B, data.gasPressureVsht31C,
            data.oilTtVsht31A, data.oilTtVsht31B, data.oilTtVsht31C,
            data.gasPressureVlt30A, data.gasPressureVlt30B, data.gasPressureVlt30C,
            data.oilTtVlt30A, data.oilTtVlt30B, data.oilTtVlt30C,
            data.purgingVshl32A1, data.purgingVshl32B1, data.purgingVshl32C1,
            data.purgingVshl32A2, data.purgingVshl32B2, data.purgingVshl32C2,
            data.oilTtVshl32A, data.oilTtVshl32B, data.oilTtVshl32C,
            data.purgingVshl21A1, data.purgingVshl21B1, data.purgingVshl21C1,
            data.purgingVshl21A2, data.purgingVshl21B2, data.purgingVshl21C2,
            data.oilTtVshl21A, data.oilTtVshl21B, data.oilTtVshl21C,
            data.purgingVsht22A1, data.purgingVsht22B1, data.purgingVsht22C1,
            data.purgingVsht22A2, data.purgingVsht22B2, data.purgingVsht22C2,
            data.oilTtVsht22A, data.oilTtVsht22B, data.oilTtVsht22C,
            data.purgingVlt20A1, data.purgingVlt20B1, data.purgingVlt20C1,
            data.purgingVlt20A2, data.purgingVlt20B2, data.purgingVlt20C2,
            data.oilTtVlt20A, data.oilTtVlt20B, data.oilTtVlt20C,
            data.purgingVsht11A1, data.purgingVsht11B1, data.purgingVsht11C1,
            data.purgingVsht11A2, data.purgingVsht11B2, data.purgingVsht11C2,
            data.oilTtVsht11A, data.oilTtVsht11B, data.oilTtVsht11C,
            data.purgingVshl12A1, data.purgingVshl12B1, data.purgingVshl12C1,
            data.purgingVshl12A2, data.purgingVshl12B2, data.purgingVshl12C2,
            data.oilTtVshl12A, data.oilTtVshl12B, data.oilTtVshl12C,
            data.tn1500Cascade1A, data.tn1500Cascade1B, data.tn1500Cascade1C,
            data.tn1500Cascade2A, data.tn1500Cascade2B, data.tn1500Cascade2C,
            data.tn1500Cascade3A, data.tn1500Cascade3B, data.tn1500Cascade3C,
            data.tn1500Cascade4A, data.tn1500Cascade4B, data.tn1500Cascade4C,
            data.tn2500Cascade1A, data.tn2500Cascade1B, data.tn2500Cascade1C,
            data.tn2500Cascade2A, data.tn2500Cascade2B, data.tn2500Cascade2C,
            data.tn2500Cascade3A, data.tn2500Cascade3B, data.tn2500Cascade3C,
            data.tn2500Cascade4A, data.tn2500Cascade4B, data.tn2500Cascade4C,
            data.tn500Sgres1Cascade1A, data.tn500Sgres1Cascade1B, data.tn500Sgres1Cascade1C,
            data.tn500Sgres1Cascade2A, data.tn500Sgres1Cascade2B, data.tn500Sgres1Cascade2C,
            data.tn500Sgres1Cascade3A, data.tn500Sgres1Cascade3B, data.tn500Sgres1Cascade3C,
            data.tn500Sgres1Cascade4A, data.tn500Sgres1Cascade4B, data.tn500Sgres1Cascade4C,
            data.oilTtTrachukovskayaA, data.oilTtTrachukovskayaB, data.oilTtTrachukovskayaC,
            data.oil2tnTrachukovskayaA, data.oil2tnTrachukovskayaB, data.oil2tnTrachukovskayaC,
            data.oil1tnTrachukovskayaA, data.oil1tnTrachukovskayaB, data.oil1tnTrachukovskayaC,
            data.oil2tnBelozernayaA, data.oil2tnBelozernayaB, data.oil2tnBelozernayaC
        )
        return calculatePercent(fields)
    }

    private fun calculateATGProgress(): Int {
        val data = sharedViewModel.atgData.value
        val fields = listOf(
            // 2 АТГ ф.С
            data.atg2_c_oil_tank, data.atg2_c_oil_rpn, data.atg2_c_pressure_500,
            data.atg2_c_pressure_220, data.atg2_c_temp_ts1, data.atg2_c_temp_ts2,
            data.atg2_c_pump_group1, data.atg2_c_pump_group2, data.atg2_c_pump_group3, data.atg2_c_pump_group4,
            // 2 АТГ ф.В
            data.atg2_b_oil_tank, data.atg2_b_oil_rpn, data.atg2_b_pressure_500,
            data.atg2_b_pressure_220, data.atg2_b_temp_ts1, data.atg2_b_temp_ts2,
            data.atg2_b_pump_group1, data.atg2_b_pump_group2, data.atg2_b_pump_group3, data.atg2_b_pump_group4,
            // 2 АТГ ф.А
            data.atg2_a_oil_tank, data.atg2_a_oil_rpn, data.atg2_a_pressure_500,
            data.atg2_a_pressure_220, data.atg2_a_temp_ts1, data.atg2_a_temp_ts2,
            data.atg2_a_pump_group1, data.atg2_a_pump_group2, data.atg2_a_pump_group3, data.atg2_a_pump_group4,
            // АТГ резерв
            data.atg_reserve_oil_tank, data.atg_reserve_oil_rpn, data.atg_reserve_pressure_500,
            data.atg_reserve_pressure_220, data.atg_reserve_temp_ts1, data.atg_reserve_temp_ts2,
            data.atg_reserve_pump_group1, data.atg_reserve_pump_group2,
            data.atg_reserve_pump_group3, data.atg_reserve_pump_group4,
            // ТН-35
            data.tn352atg, data.tn353atg,
            // 3 АТГ ф.С
            data.atg3_c_oil_tank, data.atg3_c_oil_rpn, data.atg3_c_pressure_500,
            data.atg3_c_pressure_220, data.atg3_c_temp_ts1, data.atg3_c_temp_ts2,
            data.atg3_c_pump_group1, data.atg3_c_pump_group2, data.atg3_c_pump_group3, data.atg3_c_pump_group4,
            // 3 АТГ ф.В
            data.atg3_b_oil_tank, data.atg3_b_oil_rpn, data.atg3_b_pressure_500,
            data.atg3_b_pressure_220, data.atg3_b_temp_ts1, data.atg3_b_temp_ts2,
            data.atg3_b_pump_group1, data.atg3_b_pump_group2, data.atg3_b_pump_group3, data.atg3_b_pump_group4,
            // 3 АТГ ф.А
            data.atg3_a_oil_tank, data.atg3_a_oil_rpn, data.atg3_a_pressure_500,
            data.atg3_a_pressure_220, data.atg3_a_temp_ts1, data.atg3_a_temp_ts2,
            data.atg3_a_pump_group1, data.atg3_a_pump_group2, data.atg3_a_pump_group3, data.atg3_a_pump_group4,
            // Реактор ф.С
            data.reactor_c_oil_tank, data.reactor_c_pressure_500, data.reactor_c_temp_ts,
            data.reactor_c_pump_group1, data.reactor_c_pump_group2, data.reactor_c_pump_group3,
            data.reactor_c_tt_neutral,
            // Реактор ф.В
            data.reactor_b_oil_tank, data.reactor_b_pressure_500, data.reactor_b_temp_ts,
            data.reactor_b_pump_group1, data.reactor_b_pump_group2, data.reactor_b_pump_group3,
            data.reactor_b_tt_neutral,
            // Реактор ф.А
            data.reactor_a_oil_tank, data.reactor_a_pressure_500, data.reactor_a_temp_ts,
            data.reactor_a_pump_group1, data.reactor_a_pump_group2, data.reactor_a_pump_group3,
            data.reactor_a_tt_neutral
        )
        return calculatePercent(fields)
    }

    private fun calculateBuildingsProgress(): Int {
        val data = sharedViewModel.buildingsData.value
        val fields = listOf(
            data.compressor1Valve, data.compressor1Heating, data.compressor1Temp,
            data.ballroom1Valve, data.ballroom1Heating, data.ballroom1Temp,
            data.compressor2Valve, data.compressor2Heating, data.compressor2Temp,
            data.ballroom2Valve, data.ballroom2Heating, data.ballroom2Temp,
            data.kpzOpuValve, data.kpzOpuHeating, data.kpzOpuTemp,
            data.kpz2Valve, data.kpz2Heating, data.kpz2Temp,
            data.firePumpValve, data.firePumpHeating, data.firePumpTemp,
            data.workshopHeating, data.workshopTemp,
            data.artWellHeating,
            data.artesianWellHeating,
            data.roomAbHeating, data.roomAbTemp,
            data.basementHeating, data.basementTemp
        )
        return calculatePercent(fields)
    }

    private fun calculatePercent(fields: List<String>): Int {
        if (fields.isEmpty()) return 0
        // Считаем заполненными только реальные значения (не "○")
        val filled = fields.count { it.isNotBlank() && it != "○" }
        return (filled * 100 / fields.size)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportToExcel() {
        try {
            val oru35Data = sharedViewModel.oru35Data.value
            val oru220Data = sharedViewModel.oru220Data.value
            val atgData = sharedViewModel.atgData.value
            val oru500Data = sharedViewModel.oru500Data.value
            val buildingsData = sharedViewModel.buildingsData.value

            val exportService = ExcelExportService(requireContext())
            val fileUri = exportService.exportToExcel(oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            if (fileUri != null) {
                Toast.makeText(requireContext(), "✅ Осмотр сохранён в Excel", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "❌ Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "❌ Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearAllData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистка данных")
            .setMessage("Вы уверены, что хотите очистить все данные осмотра?")
            .setPositiveButton("Очистить") { _, _ ->
                sharedViewModel.clearAllData()
                Toast.makeText(requireContext(), "Все данные очищены", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}