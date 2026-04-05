package com.example.ps_inspection

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.databinding.FragmentHomeScreenBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnArchive.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_archiveFragment)
        }

        binding.inspectOru35.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU35)
        }

        binding.inspectOru220.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU2202)
        }

        binding.inspectOru500.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU500)
        }

        binding.inspectBuildings.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionBuildings)
        }

        binding.inspectATG.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionATG)
        }

        binding.btnExportExcel.setOnClickListener {
            exportToExcel()
        }

        binding.btnClearData.setOnClickListener {
            clearAllData()
        }

        binding.btnFillFromLast.setOnClickListener {
            fillFromLastInspection()
        }
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
                Toast.makeText(requireContext(), "Осмотр сохранён в Excel", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
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

    // ==================== ЗАПОЛНЕНИЕ ИЗ ПРОШЛОГО ОСМОТРА ====================

    private fun fillFromLastInspection() {
        val manager = LastInspectionManager(requireContext())

        val lastOru35 = manager.getLastOru35Data()
        val lastOru220 = manager.getLastOru220Data()
        val lastAtg = manager.getLastAtgData()
        val lastOru500 = manager.getLastOru500Data()
        val lastBuildings = manager.getLastBuildingsData()

        if (lastOru35 == null || lastOru220 == null || lastAtg == null ||
            lastOru500 == null || lastBuildings == null) {
            Toast.makeText(requireContext(), "Нет сохранённого осмотра. Сначала сохраните Excel-файл.", Toast.LENGTH_LONG).show()
            return
        }

        // Получаем дату последнего осмотра из SharedPreferences
        val prefs = requireContext().getSharedPreferences("inspection_prefs", Context.MODE_PRIVATE)
        val lastDate = prefs.getString("last_inspection_date", "предыдущего")

        // Показываем диалог подтверждения
        AlertDialog.Builder(requireContext())
            .setTitle("Загрузка из прошлого осмотра")
            .setMessage("Загрузить недостающие значения из осмотра от $lastDate?")
            .setPositiveButton("Загрузить") { _, _ ->
                performFillFromLastInspection(lastOru35, lastOru220, lastAtg, lastOru500, lastBuildings)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performFillFromLastInspection(
        lastOru35: InspectionORU35Data,
        lastOru220: InspectionORU220Data,
        lastAtg: InspectionATGData,
        lastOru500: InspectionORU500Data,
        lastBuildings: InspectionBuildingsData
    ) {
        var filledCount = 0

        filledCount += fillEmptyFieldsOru35(lastOru35, sharedViewModel.oru35Data.value)
        filledCount += fillEmptyFieldsOru220(lastOru220, sharedViewModel.oru220Data.value)
        filledCount += fillEmptyFieldsAtg(lastAtg, sharedViewModel.atgData.value)
        filledCount += fillEmptyFieldsOru500(lastOru500, sharedViewModel.oru500Data.value)
        filledCount += fillEmptyFieldsBuildings(lastBuildings, sharedViewModel.buildingsData.value)

        if (filledCount > 0) {
            Toast.makeText(requireContext(), "Заполнено $filledCount пропущенных полей", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Нет пустых полей для заполнения", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fillEmptyFieldsOru35(last: InspectionORU35Data, current: InspectionORU35Data): Int {
        var count = 0

        if (current.tsn2.isEmpty() && last.tsn2.isNotEmpty()) { sharedViewModel.updateORU35Data { tsn2 = last.tsn2 }; count++ }
        if (current.tsn3.isEmpty() && last.tsn3.isNotEmpty()) { sharedViewModel.updateORU35Data { tsn3 = last.tsn3 }; count++ }
        if (current.tsn4.isEmpty() && last.tsn4.isNotEmpty()) { sharedViewModel.updateORU35Data { tsn4 = last.tsn4 }; count++ }

        if (current.tt352tsnA.isEmpty() && last.tt352tsnA.isNotEmpty()) { sharedViewModel.updateORU35Data { tt352tsnA = last.tt352tsnA }; count++ }
        if (current.tt352tsnB.isEmpty() && last.tt352tsnB.isNotEmpty()) { sharedViewModel.updateORU35Data { tt352tsnB = last.tt352tsnB }; count++ }
        if (current.tt352tsnC.isEmpty() && last.tt352tsnC.isNotEmpty()) { sharedViewModel.updateORU35Data { tt352tsnC = last.tt352tsnC }; count++ }

        if (current.tt353tsnA.isEmpty() && last.tt353tsnA.isNotEmpty()) { sharedViewModel.updateORU35Data { tt353tsnA = last.tt353tsnA }; count++ }
        if (current.tt353tsnB.isEmpty() && last.tt353tsnB.isNotEmpty()) { sharedViewModel.updateORU35Data { tt353tsnB = last.tt353tsnB }; count++ }
        if (current.tt353tsnC.isEmpty() && last.tt353tsnC.isNotEmpty()) { sharedViewModel.updateORU35Data { tt353tsnC = last.tt353tsnC }; count++ }

        if (current.v352tsnA.isEmpty() && last.v352tsnA.isNotEmpty()) { sharedViewModel.updateORU35Data { v352tsnA = last.v352tsnA }; count++ }
        if (current.v352tsnB.isEmpty() && last.v352tsnB.isNotEmpty()) { sharedViewModel.updateORU35Data { v352tsnB = last.v352tsnB }; count++ }
        if (current.v352tsnC.isEmpty() && last.v352tsnC.isNotEmpty()) { sharedViewModel.updateORU35Data { v352tsnC = last.v352tsnC }; count++ }

        if (current.v353tsnA.isEmpty() && last.v353tsnA.isNotEmpty()) { sharedViewModel.updateORU35Data { v353tsnA = last.v353tsnA }; count++ }
        if (current.v353tsnB.isEmpty() && last.v353tsnB.isNotEmpty()) { sharedViewModel.updateORU35Data { v353tsnB = last.v353tsnB }; count++ }
        if (current.v353tsnC.isEmpty() && last.v353tsnC.isNotEmpty()) { sharedViewModel.updateORU35Data { v353tsnC = last.v353tsnC }; count++ }

        if (current.tn352atg.isEmpty() && last.tn352atg.isNotEmpty()) { sharedViewModel.updateORU35Data { tn352atg = last.tn352atg }; count++ }
        if (current.tn353atg.isEmpty() && last.tn353atg.isNotEmpty()) { sharedViewModel.updateORU35Data { tn353atg = last.tn353atg }; count++ }

        return count
    }

    private fun fillEmptyFieldsOru220(last: InspectionORU220Data, current: InspectionORU220Data): Int {
        var count = 0

        // Мирная
        if (current.purgingMirnayaA.isEmpty() && last.purgingMirnayaA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingMirnayaA = last.purgingMirnayaA }; count++ }
        if (current.purgingMirnayaB.isEmpty() && last.purgingMirnayaB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingMirnayaB = last.purgingMirnayaB }; count++ }
        if (current.purgingMirnayaC.isEmpty() && last.purgingMirnayaC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingMirnayaC = last.purgingMirnayaC }; count++ }
        if (current.oilMirnayaA.isEmpty() && last.oilMirnayaA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilMirnayaA = last.oilMirnayaA }; count++ }
        if (current.oilMirnayaB.isEmpty() && last.oilMirnayaB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilMirnayaB = last.oilMirnayaB }; count++ }
        if (current.oilMirnayaC.isEmpty() && last.oilMirnayaC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilMirnayaC = last.oilMirnayaC }; count++ }

        // Топаз
        if (current.purgingTopazA.isEmpty() && last.purgingTopazA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingTopazA = last.purgingTopazA }; count++ }
        if (current.purgingTopazB.isEmpty() && last.purgingTopazB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingTopazB = last.purgingTopazB }; count++ }
        if (current.purgingTopazC.isEmpty() && last.purgingTopazC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingTopazC = last.purgingTopazC }; count++ }
        if (current.oilTopazA.isEmpty() && last.oilTopazA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTopazA = last.oilTopazA }; count++ }
        if (current.oilTopazB.isEmpty() && last.oilTopazB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTopazB = last.oilTopazB }; count++ }
        if (current.oilTopazC.isEmpty() && last.oilTopazC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTopazC = last.oilTopazC }; count++ }

        // ОВ
        if (current.purgingOvA.isEmpty() && last.purgingOvA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingOvA = last.purgingOvA }; count++ }
        if (current.purgingOvB.isEmpty() && last.purgingOvB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingOvB = last.purgingOvB }; count++ }
        if (current.purgingOvC.isEmpty() && last.purgingOvC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingOvC = last.purgingOvC }; count++ }
        if (current.oilOvA.isEmpty() && last.oilOvA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilOvA = last.oilOvA }; count++ }
        if (current.oilOvB.isEmpty() && last.oilOvB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilOvB = last.oilOvB }; count++ }
        if (current.oilOvC.isEmpty() && last.oilOvC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilOvC = last.oilOvC }; count++ }

        // ТН-220 ОСШ ф.В
        if (current.tnOsshFvUpper.isEmpty() && last.tnOsshFvUpper.isNotEmpty()) { sharedViewModel.updateORU220Data { tnOsshFvUpper = last.tnOsshFvUpper }; count++ }
        if (current.tnOsshFvLower.isEmpty() && last.tnOsshFvLower.isNotEmpty()) { sharedViewModel.updateORU220Data { tnOsshFvLower = last.tnOsshFvLower }; count++ }

        // В-220 2АТГ
        if (current.purgingV2atgA.isEmpty() && last.purgingV2atgA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingV2atgA = last.purgingV2atgA }; count++ }
        if (current.purgingV2atgB.isEmpty() && last.purgingV2atgB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingV2atgB = last.purgingV2atgB }; count++ }
        if (current.purgingV2atgC.isEmpty() && last.purgingV2atgC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingV2atgC = last.purgingV2atgC }; count++ }
        if (current.oilTt2atgA.isEmpty() && last.oilTt2atgA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTt2atgA = last.oilTt2atgA }; count++ }
        if (current.oilTt2atgB.isEmpty() && last.oilTt2atgB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTt2atgB = last.oilTt2atgB }; count++ }
        if (current.oilTt2atgC.isEmpty() && last.oilTt2atgC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTt2atgC = last.oilTt2atgC }; count++ }

        // ШСВ-220
        if (current.purgingShSV220A.isEmpty() && last.purgingShSV220A.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingShSV220A = last.purgingShSV220A }; count++ }
        if (current.purgingShSV220B.isEmpty() && last.purgingShSV220B.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingShSV220B = last.purgingShSV220B }; count++ }
        if (current.purgingShSV220C.isEmpty() && last.purgingShSV220C.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingShSV220C = last.purgingShSV220C }; count++ }
        if (current.oilTtShSV220A.isEmpty() && last.oilTtShSV220A.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTtShSV220A = last.oilTtShSV220A }; count++ }
        if (current.oilTtShSV220B.isEmpty() && last.oilTtShSV220B.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTtShSV220B = last.oilTtShSV220B }; count++ }
        if (current.oilTtShSV220C.isEmpty() && last.oilTtShSV220C.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTtShSV220C = last.oilTtShSV220C }; count++ }

        // В-220 3АТГ
        if (current.purgingV3atgA.isEmpty() && last.purgingV3atgA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingV3atgA = last.purgingV3atgA }; count++ }
        if (current.purgingV3atgB.isEmpty() && last.purgingV3atgB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingV3atgB = last.purgingV3atgB }; count++ }
        if (current.purgingV3atgC.isEmpty() && last.purgingV3atgC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingV3atgC = last.purgingV3atgC }; count++ }
        if (current.oilTt3atgA.isEmpty() && last.oilTt3atgA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTt3atgA = last.oilTt3atgA }; count++ }
        if (current.oilTt3atgB.isEmpty() && last.oilTt3atgB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTt3atgB = last.oilTt3atgB }; count++ }
        if (current.oilTt3atgC.isEmpty() && last.oilTt3atgC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilTt3atgC = last.oilTt3atgC }; count++ }

        // Орбита
        if (current.purgingOrbitaA.isEmpty() && last.purgingOrbitaA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingOrbitaA = last.purgingOrbitaA }; count++ }
        if (current.purgingOrbitaB.isEmpty() && last.purgingOrbitaB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingOrbitaB = last.purgingOrbitaB }; count++ }
        if (current.purgingOrbitaC.isEmpty() && last.purgingOrbitaC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingOrbitaC = last.purgingOrbitaC }; count++ }
        if (current.oilOrbitaA.isEmpty() && last.oilOrbitaA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilOrbitaA = last.oilOrbitaA }; count++ }
        if (current.oilOrbitaB.isEmpty() && last.oilOrbitaB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilOrbitaB = last.oilOrbitaB }; count++ }
        if (current.oilOrbitaC.isEmpty() && last.oilOrbitaC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilOrbitaC = last.oilOrbitaC }; count++ }

        // Факел
        if (current.purgingFakelA.isEmpty() && last.purgingFakelA.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingFakelA = last.purgingFakelA }; count++ }
        if (current.purgingFakelB.isEmpty() && last.purgingFakelB.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingFakelB = last.purgingFakelB }; count++ }
        if (current.purgingFakelC.isEmpty() && last.purgingFakelC.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingFakelC = last.purgingFakelC }; count++ }
        if (current.oilFakelA.isEmpty() && last.oilFakelA.isNotEmpty()) { sharedViewModel.updateORU220Data { oilFakelA = last.oilFakelA }; count++ }
        if (current.oilFakelB.isEmpty() && last.oilFakelB.isNotEmpty()) { sharedViewModel.updateORU220Data { oilFakelB = last.oilFakelB }; count++ }
        if (current.oilFakelC.isEmpty() && last.oilFakelC.isNotEmpty()) { sharedViewModel.updateORU220Data { oilFakelC = last.oilFakelC }; count++ }

        // Комета-2
        if (current.purgingCometa2A.isEmpty() && last.purgingCometa2A.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingCometa2A = last.purgingCometa2A }; count++ }
        if (current.purgingCometa2B.isEmpty() && last.purgingCometa2B.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingCometa2B = last.purgingCometa2B }; count++ }
        if (current.purgingCometa2C.isEmpty() && last.purgingCometa2C.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingCometa2C = last.purgingCometa2C }; count++ }
        if (current.oilCometa2A.isEmpty() && last.oilCometa2A.isNotEmpty()) { sharedViewModel.updateORU220Data { oilCometa2A = last.oilCometa2A }; count++ }
        if (current.oilCometa2B.isEmpty() && last.oilCometa2B.isNotEmpty()) { sharedViewModel.updateORU220Data { oilCometa2B = last.oilCometa2B }; count++ }
        if (current.oilCometa2C.isEmpty() && last.oilCometa2C.isNotEmpty()) { sharedViewModel.updateORU220Data { oilCometa2C = last.oilCometa2C }; count++ }

        // Комета-1
        if (current.purgingCometa1A.isEmpty() && last.purgingCometa1A.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingCometa1A = last.purgingCometa1A }; count++ }
        if (current.purgingCometa1B.isEmpty() && last.purgingCometa1B.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingCometa1B = last.purgingCometa1B }; count++ }
        if (current.purgingCometa1C.isEmpty() && last.purgingCometa1C.isNotEmpty()) { sharedViewModel.updateORU220Data { purgingCometa1C = last.purgingCometa1C }; count++ }
        if (current.oilCometa1A.isEmpty() && last.oilCometa1A.isNotEmpty()) { sharedViewModel.updateORU220Data { oilCometa1A = last.oilCometa1A }; count++ }
        if (current.oilCometa1B.isEmpty() && last.oilCometa1B.isNotEmpty()) { sharedViewModel.updateORU220Data { oilCometa1B = last.oilCometa1B }; count++ }
        if (current.oilCometa1C.isEmpty() && last.oilCometa1C.isNotEmpty()) { sharedViewModel.updateORU220Data { oilCometa1C = last.oilCometa1C }; count++ }

        // 1ТН-220
        if (current.tn1UpperA.isEmpty() && last.tn1UpperA.isNotEmpty()) { sharedViewModel.updateORU220Data { tn1UpperA = last.tn1UpperA }; count++ }
        if (current.tn1UpperB.isEmpty() && last.tn1UpperB.isNotEmpty()) { sharedViewModel.updateORU220Data { tn1UpperB = last.tn1UpperB }; count++ }
        if (current.tn1UpperC.isEmpty() && last.tn1UpperC.isNotEmpty()) { sharedViewModel.updateORU220Data { tn1UpperC = last.tn1UpperC }; count++ }
        if (current.tn1LowerA.isEmpty() && last.tn1LowerA.isNotEmpty()) { sharedViewModel.updateORU220Data { tn1LowerA = last.tn1LowerA }; count++ }
        if (current.tn1LowerB.isEmpty() && last.tn1LowerB.isNotEmpty()) { sharedViewModel.updateORU220Data { tn1LowerB = last.tn1LowerB }; count++ }
        if (current.tn1LowerC.isEmpty() && last.tn1LowerC.isNotEmpty()) { sharedViewModel.updateORU220Data { tn1LowerC = last.tn1LowerC }; count++ }

        // 2ТН-220
        if (current.tn2UpperA.isEmpty() && last.tn2UpperA.isNotEmpty()) { sharedViewModel.updateORU220Data { tn2UpperA = last.tn2UpperA }; count++ }
        if (current.tn2UpperB.isEmpty() && last.tn2UpperB.isNotEmpty()) { sharedViewModel.updateORU220Data { tn2UpperB = last.tn2UpperB }; count++ }
        if (current.tn2UpperC.isEmpty() && last.tn2UpperC.isNotEmpty()) { sharedViewModel.updateORU220Data { tn2UpperC = last.tn2UpperC }; count++ }
        if (current.tn2LowerA.isEmpty() && last.tn2LowerA.isNotEmpty()) { sharedViewModel.updateORU220Data { tn2LowerA = last.tn2LowerA }; count++ }
        if (current.tn2LowerB.isEmpty() && last.tn2LowerB.isNotEmpty()) { sharedViewModel.updateORU220Data { tn2LowerB = last.tn2LowerB }; count++ }
        if (current.tn2LowerC.isEmpty() && last.tn2LowerC.isNotEmpty()) { sharedViewModel.updateORU220Data { tn2LowerC = last.tn2LowerC }; count++ }

        return count
    }

    private fun fillEmptyFieldsAtg(last: InspectionATGData, current: InspectionATGData): Int {
        var count = 0

        // Функция-помощник для проверки и обновления
        fun checkAndUpdate(currentValue: String, lastValue: String, update: () -> Unit) {
            if (currentValue.isEmpty() && lastValue.isNotEmpty()) {
                update()
                count++
            }
        }

        // 2 АТГ ф.С
        checkAndUpdate(current.atg2_c_oil_tank, last.atg2_c_oil_tank) { sharedViewModel.updateATGData { atg2_c_oil_tank = last.atg2_c_oil_tank } }
        checkAndUpdate(current.atg2_c_oil_rpn, last.atg2_c_oil_rpn) { sharedViewModel.updateATGData { atg2_c_oil_rpn = last.atg2_c_oil_rpn } }
        checkAndUpdate(current.atg2_c_pressure_500, last.atg2_c_pressure_500) { sharedViewModel.updateATGData { atg2_c_pressure_500 = last.atg2_c_pressure_500 } }
        checkAndUpdate(current.atg2_c_pressure_220, last.atg2_c_pressure_220) { sharedViewModel.updateATGData { atg2_c_pressure_220 = last.atg2_c_pressure_220 } }
        checkAndUpdate(current.atg2_c_temp_ts1, last.atg2_c_temp_ts1) { sharedViewModel.updateATGData { atg2_c_temp_ts1 = last.atg2_c_temp_ts1 } }
        checkAndUpdate(current.atg2_c_temp_ts2, last.atg2_c_temp_ts2) { sharedViewModel.updateATGData { atg2_c_temp_ts2 = last.atg2_c_temp_ts2 } }
        checkAndUpdate(current.atg2_c_pump_group1, last.atg2_c_pump_group1) { sharedViewModel.updateATGData { atg2_c_pump_group1 = last.atg2_c_pump_group1 } }
        checkAndUpdate(current.atg2_c_pump_group2, last.atg2_c_pump_group2) { sharedViewModel.updateATGData { atg2_c_pump_group2 = last.atg2_c_pump_group2 } }
        checkAndUpdate(current.atg2_c_pump_group3, last.atg2_c_pump_group3) { sharedViewModel.updateATGData { atg2_c_pump_group3 = last.atg2_c_pump_group3 } }
        checkAndUpdate(current.atg2_c_pump_group4, last.atg2_c_pump_group4) { sharedViewModel.updateATGData { atg2_c_pump_group4 = last.atg2_c_pump_group4 } }

        // 2 АТГ ф.В
        checkAndUpdate(current.atg2_b_oil_tank, last.atg2_b_oil_tank) { sharedViewModel.updateATGData { atg2_b_oil_tank = last.atg2_b_oil_tank } }
        checkAndUpdate(current.atg2_b_oil_rpn, last.atg2_b_oil_rpn) { sharedViewModel.updateATGData { atg2_b_oil_rpn = last.atg2_b_oil_rpn } }
        checkAndUpdate(current.atg2_b_pressure_500, last.atg2_b_pressure_500) { sharedViewModel.updateATGData { atg2_b_pressure_500 = last.atg2_b_pressure_500 } }
        checkAndUpdate(current.atg2_b_pressure_220, last.atg2_b_pressure_220) { sharedViewModel.updateATGData { atg2_b_pressure_220 = last.atg2_b_pressure_220 } }
        checkAndUpdate(current.atg2_b_temp_ts1, last.atg2_b_temp_ts1) { sharedViewModel.updateATGData { atg2_b_temp_ts1 = last.atg2_b_temp_ts1 } }
        checkAndUpdate(current.atg2_b_temp_ts2, last.atg2_b_temp_ts2) { sharedViewModel.updateATGData { atg2_b_temp_ts2 = last.atg2_b_temp_ts2 } }
        checkAndUpdate(current.atg2_b_pump_group1, last.atg2_b_pump_group1) { sharedViewModel.updateATGData { atg2_b_pump_group1 = last.atg2_b_pump_group1 } }
        checkAndUpdate(current.atg2_b_pump_group2, last.atg2_b_pump_group2) { sharedViewModel.updateATGData { atg2_b_pump_group2 = last.atg2_b_pump_group2 } }
        checkAndUpdate(current.atg2_b_pump_group3, last.atg2_b_pump_group3) { sharedViewModel.updateATGData { atg2_b_pump_group3 = last.atg2_b_pump_group3 } }
        checkAndUpdate(current.atg2_b_pump_group4, last.atg2_b_pump_group4) { sharedViewModel.updateATGData { atg2_b_pump_group4 = last.atg2_b_pump_group4 } }

        // 2 АТГ ф.А
        checkAndUpdate(current.atg2_a_oil_tank, last.atg2_a_oil_tank) { sharedViewModel.updateATGData { atg2_a_oil_tank = last.atg2_a_oil_tank } }
        checkAndUpdate(current.atg2_a_oil_rpn, last.atg2_a_oil_rpn) { sharedViewModel.updateATGData { atg2_a_oil_rpn = last.atg2_a_oil_rpn } }
        checkAndUpdate(current.atg2_a_pressure_500, last.atg2_a_pressure_500) { sharedViewModel.updateATGData { atg2_a_pressure_500 = last.atg2_a_pressure_500 } }
        checkAndUpdate(current.atg2_a_pressure_220, last.atg2_a_pressure_220) { sharedViewModel.updateATGData { atg2_a_pressure_220 = last.atg2_a_pressure_220 } }
        checkAndUpdate(current.atg2_a_temp_ts1, last.atg2_a_temp_ts1) { sharedViewModel.updateATGData { atg2_a_temp_ts1 = last.atg2_a_temp_ts1 } }
        checkAndUpdate(current.atg2_a_temp_ts2, last.atg2_a_temp_ts2) { sharedViewModel.updateATGData { atg2_a_temp_ts2 = last.atg2_a_temp_ts2 } }
        checkAndUpdate(current.atg2_a_pump_group1, last.atg2_a_pump_group1) { sharedViewModel.updateATGData { atg2_a_pump_group1 = last.atg2_a_pump_group1 } }
        checkAndUpdate(current.atg2_a_pump_group2, last.atg2_a_pump_group2) { sharedViewModel.updateATGData { atg2_a_pump_group2 = last.atg2_a_pump_group2 } }
        checkAndUpdate(current.atg2_a_pump_group3, last.atg2_a_pump_group3) { sharedViewModel.updateATGData { atg2_a_pump_group3 = last.atg2_a_pump_group3 } }
        checkAndUpdate(current.atg2_a_pump_group4, last.atg2_a_pump_group4) { sharedViewModel.updateATGData { atg2_a_pump_group4 = last.atg2_a_pump_group4 } }

        // АТГ резервная фаза
        checkAndUpdate(current.atg_reserve_oil_tank, last.atg_reserve_oil_tank) { sharedViewModel.updateATGData { atg_reserve_oil_tank = last.atg_reserve_oil_tank } }
        checkAndUpdate(current.atg_reserve_oil_rpn, last.atg_reserve_oil_rpn) { sharedViewModel.updateATGData { atg_reserve_oil_rpn = last.atg_reserve_oil_rpn } }
        checkAndUpdate(current.atg_reserve_pressure_500, last.atg_reserve_pressure_500) { sharedViewModel.updateATGData { atg_reserve_pressure_500 = last.atg_reserve_pressure_500 } }
        checkAndUpdate(current.atg_reserve_pressure_220, last.atg_reserve_pressure_220) { sharedViewModel.updateATGData { atg_reserve_pressure_220 = last.atg_reserve_pressure_220 } }
        checkAndUpdate(current.atg_reserve_temp_ts1, last.atg_reserve_temp_ts1) { sharedViewModel.updateATGData { atg_reserve_temp_ts1 = last.atg_reserve_temp_ts1 } }
        checkAndUpdate(current.atg_reserve_temp_ts2, last.atg_reserve_temp_ts2) { sharedViewModel.updateATGData { atg_reserve_temp_ts2 = last.atg_reserve_temp_ts2 } }
        checkAndUpdate(current.atg_reserve_pump_group1, last.atg_reserve_pump_group1) { sharedViewModel.updateATGData { atg_reserve_pump_group1 = last.atg_reserve_pump_group1 } }
        checkAndUpdate(current.atg_reserve_pump_group2, last.atg_reserve_pump_group2) { sharedViewModel.updateATGData { atg_reserve_pump_group2 = last.atg_reserve_pump_group2 } }
        checkAndUpdate(current.atg_reserve_pump_group3, last.atg_reserve_pump_group3) { sharedViewModel.updateATGData { atg_reserve_pump_group3 = last.atg_reserve_pump_group3 } }
        checkAndUpdate(current.atg_reserve_pump_group4, last.atg_reserve_pump_group4) { sharedViewModel.updateATGData { atg_reserve_pump_group4 = last.atg_reserve_pump_group4 } }

        // 3 АТГ ф.С
        checkAndUpdate(current.atg3_c_oil_tank, last.atg3_c_oil_tank) { sharedViewModel.updateATGData { atg3_c_oil_tank = last.atg3_c_oil_tank } }
        checkAndUpdate(current.atg3_c_oil_rpn, last.atg3_c_oil_rpn) { sharedViewModel.updateATGData { atg3_c_oil_rpn = last.atg3_c_oil_rpn } }
        checkAndUpdate(current.atg3_c_pressure_500, last.atg3_c_pressure_500) { sharedViewModel.updateATGData { atg3_c_pressure_500 = last.atg3_c_pressure_500 } }
        checkAndUpdate(current.atg3_c_pressure_220, last.atg3_c_pressure_220) { sharedViewModel.updateATGData { atg3_c_pressure_220 = last.atg3_c_pressure_220 } }
        checkAndUpdate(current.atg3_c_temp_ts1, last.atg3_c_temp_ts1) { sharedViewModel.updateATGData { atg3_c_temp_ts1 = last.atg3_c_temp_ts1 } }
        checkAndUpdate(current.atg3_c_temp_ts2, last.atg3_c_temp_ts2) { sharedViewModel.updateATGData { atg3_c_temp_ts2 = last.atg3_c_temp_ts2 } }
        checkAndUpdate(current.atg3_c_pump_group1, last.atg3_c_pump_group1) { sharedViewModel.updateATGData { atg3_c_pump_group1 = last.atg3_c_pump_group1 } }
        checkAndUpdate(current.atg3_c_pump_group2, last.atg3_c_pump_group2) { sharedViewModel.updateATGData { atg3_c_pump_group2 = last.atg3_c_pump_group2 } }
        checkAndUpdate(current.atg3_c_pump_group3, last.atg3_c_pump_group3) { sharedViewModel.updateATGData { atg3_c_pump_group3 = last.atg3_c_pump_group3 } }
        checkAndUpdate(current.atg3_c_pump_group4, last.atg3_c_pump_group4) { sharedViewModel.updateATGData { atg3_c_pump_group4 = last.atg3_c_pump_group4 } }

        // 3 АТГ ф.В
        checkAndUpdate(current.atg3_b_oil_tank, last.atg3_b_oil_tank) { sharedViewModel.updateATGData { atg3_b_oil_tank = last.atg3_b_oil_tank } }
        checkAndUpdate(current.atg3_b_oil_rpn, last.atg3_b_oil_rpn) { sharedViewModel.updateATGData { atg3_b_oil_rpn = last.atg3_b_oil_rpn } }
        checkAndUpdate(current.atg3_b_pressure_500, last.atg3_b_pressure_500) { sharedViewModel.updateATGData { atg3_b_pressure_500 = last.atg3_b_pressure_500 } }
        checkAndUpdate(current.atg3_b_pressure_220, last.atg3_b_pressure_220) { sharedViewModel.updateATGData { atg3_b_pressure_220 = last.atg3_b_pressure_220 } }
        checkAndUpdate(current.atg3_b_temp_ts1, last.atg3_b_temp_ts1) { sharedViewModel.updateATGData { atg3_b_temp_ts1 = last.atg3_b_temp_ts1 } }
        checkAndUpdate(current.atg3_b_temp_ts2, last.atg3_b_temp_ts2) { sharedViewModel.updateATGData { atg3_b_temp_ts2 = last.atg3_b_temp_ts2 } }
        checkAndUpdate(current.atg3_b_pump_group1, last.atg3_b_pump_group1) { sharedViewModel.updateATGData { atg3_b_pump_group1 = last.atg3_b_pump_group1 } }
        checkAndUpdate(current.atg3_b_pump_group2, last.atg3_b_pump_group2) { sharedViewModel.updateATGData { atg3_b_pump_group2 = last.atg3_b_pump_group2 } }
        checkAndUpdate(current.atg3_b_pump_group3, last.atg3_b_pump_group3) { sharedViewModel.updateATGData { atg3_b_pump_group3 = last.atg3_b_pump_group3 } }
        checkAndUpdate(current.atg3_b_pump_group4, last.atg3_b_pump_group4) { sharedViewModel.updateATGData { atg3_b_pump_group4 = last.atg3_b_pump_group4 } }

        // 3 АТГ ф.А
        checkAndUpdate(current.atg3_a_oil_tank, last.atg3_a_oil_tank) { sharedViewModel.updateATGData { atg3_a_oil_tank = last.atg3_a_oil_tank } }
        checkAndUpdate(current.atg3_a_oil_rpn, last.atg3_a_oil_rpn) { sharedViewModel.updateATGData { atg3_a_oil_rpn = last.atg3_a_oil_rpn } }
        checkAndUpdate(current.atg3_a_pressure_500, last.atg3_a_pressure_500) { sharedViewModel.updateATGData { atg3_a_pressure_500 = last.atg3_a_pressure_500 } }
        checkAndUpdate(current.atg3_a_pressure_220, last.atg3_a_pressure_220) { sharedViewModel.updateATGData { atg3_a_pressure_220 = last.atg3_a_pressure_220 } }
        checkAndUpdate(current.atg3_a_temp_ts1, last.atg3_a_temp_ts1) { sharedViewModel.updateATGData { atg3_a_temp_ts1 = last.atg3_a_temp_ts1 } }
        checkAndUpdate(current.atg3_a_temp_ts2, last.atg3_a_temp_ts2) { sharedViewModel.updateATGData { atg3_a_temp_ts2 = last.atg3_a_temp_ts2 } }
        checkAndUpdate(current.atg3_a_pump_group1, last.atg3_a_pump_group1) { sharedViewModel.updateATGData { atg3_a_pump_group1 = last.atg3_a_pump_group1 } }
        checkAndUpdate(current.atg3_a_pump_group2, last.atg3_a_pump_group2) { sharedViewModel.updateATGData { atg3_a_pump_group2 = last.atg3_a_pump_group2 } }
        checkAndUpdate(current.atg3_a_pump_group3, last.atg3_a_pump_group3) { sharedViewModel.updateATGData { atg3_a_pump_group3 = last.atg3_a_pump_group3 } }
        checkAndUpdate(current.atg3_a_pump_group4, last.atg3_a_pump_group4) { sharedViewModel.updateATGData { atg3_a_pump_group4 = last.atg3_a_pump_group4 } }

        // Реакторы
        checkAndUpdate(current.reactor_c_oil_tank, last.reactor_c_oil_tank) { sharedViewModel.updateATGData { reactor_c_oil_tank = last.reactor_c_oil_tank } }
        checkAndUpdate(current.reactor_c_pressure_500, last.reactor_c_pressure_500) { sharedViewModel.updateATGData { reactor_c_pressure_500 = last.reactor_c_pressure_500 } }
        checkAndUpdate(current.reactor_c_temp_ts, last.reactor_c_temp_ts) { sharedViewModel.updateATGData { reactor_c_temp_ts = last.reactor_c_temp_ts } }
        checkAndUpdate(current.reactor_c_pump_group1, last.reactor_c_pump_group1) { sharedViewModel.updateATGData { reactor_c_pump_group1 = last.reactor_c_pump_group1 } }
        checkAndUpdate(current.reactor_c_pump_group2, last.reactor_c_pump_group2) { sharedViewModel.updateATGData { reactor_c_pump_group2 = last.reactor_c_pump_group2 } }
        checkAndUpdate(current.reactor_c_pump_group3, last.reactor_c_pump_group3) { sharedViewModel.updateATGData { reactor_c_pump_group3 = last.reactor_c_pump_group3 } }
        checkAndUpdate(current.reactor_c_tt_neutral, last.reactor_c_tt_neutral) { sharedViewModel.updateATGData { reactor_c_tt_neutral = last.reactor_c_tt_neutral } }

        checkAndUpdate(current.reactor_b_oil_tank, last.reactor_b_oil_tank) { sharedViewModel.updateATGData { reactor_b_oil_tank = last.reactor_b_oil_tank } }
        checkAndUpdate(current.reactor_b_pressure_500, last.reactor_b_pressure_500) { sharedViewModel.updateATGData { reactor_b_pressure_500 = last.reactor_b_pressure_500 } }
        checkAndUpdate(current.reactor_b_temp_ts, last.reactor_b_temp_ts) { sharedViewModel.updateATGData { reactor_b_temp_ts = last.reactor_b_temp_ts } }
        checkAndUpdate(current.reactor_b_pump_group1, last.reactor_b_pump_group1) { sharedViewModel.updateATGData { reactor_b_pump_group1 = last.reactor_b_pump_group1 } }
        checkAndUpdate(current.reactor_b_pump_group2, last.reactor_b_pump_group2) { sharedViewModel.updateATGData { reactor_b_pump_group2 = last.reactor_b_pump_group2 } }
        checkAndUpdate(current.reactor_b_pump_group3, last.reactor_b_pump_group3) { sharedViewModel.updateATGData { reactor_b_pump_group3 = last.reactor_b_pump_group3 } }
        checkAndUpdate(current.reactor_b_tt_neutral, last.reactor_b_tt_neutral) { sharedViewModel.updateATGData { reactor_b_tt_neutral = last.reactor_b_tt_neutral } }

        checkAndUpdate(current.reactor_a_oil_tank, last.reactor_a_oil_tank) { sharedViewModel.updateATGData { reactor_a_oil_tank = last.reactor_a_oil_tank } }
        checkAndUpdate(current.reactor_a_pressure_500, last.reactor_a_pressure_500) { sharedViewModel.updateATGData { reactor_a_pressure_500 = last.reactor_a_pressure_500 } }
        checkAndUpdate(current.reactor_a_temp_ts, last.reactor_a_temp_ts) { sharedViewModel.updateATGData { reactor_a_temp_ts = last.reactor_a_temp_ts } }
        checkAndUpdate(current.reactor_a_pump_group1, last.reactor_a_pump_group1) { sharedViewModel.updateATGData { reactor_a_pump_group1 = last.reactor_a_pump_group1 } }
        checkAndUpdate(current.reactor_a_pump_group2, last.reactor_a_pump_group2) { sharedViewModel.updateATGData { reactor_a_pump_group2 = last.reactor_a_pump_group2 } }
        checkAndUpdate(current.reactor_a_pump_group3, last.reactor_a_pump_group3) { sharedViewModel.updateATGData { reactor_a_pump_group3 = last.reactor_a_pump_group3 } }
        checkAndUpdate(current.reactor_a_tt_neutral, last.reactor_a_tt_neutral) { sharedViewModel.updateATGData { reactor_a_tt_neutral = last.reactor_a_tt_neutral } }

        return count
    }

    private fun fillEmptyFieldsOru500(last: InspectionORU500Data, current: InspectionORU500Data): Int {
        var count = 0

        fun checkAndUpdate(currentValue: String, lastValue: String, update: () -> Unit) {
            if (currentValue.isEmpty() && lastValue.isNotEmpty()) {
                update()
                count++
            }
        }

        // В-500 Р-500 2С
        checkAndUpdate(current.purgingR5002sA1, last.purgingR5002sA1) { sharedViewModel.updateORU500Data { purgingR5002sA1 = last.purgingR5002sA1 } }
        checkAndUpdate(current.purgingR5002sB1, last.purgingR5002sB1) { sharedViewModel.updateORU500Data { purgingR5002sB1 = last.purgingR5002sB1 } }
        checkAndUpdate(current.purgingR5002sC1, last.purgingR5002sC1) { sharedViewModel.updateORU500Data { purgingR5002sC1 = last.purgingR5002sC1 } }
        checkAndUpdate(current.purgingR5002sA2, last.purgingR5002sA2) { sharedViewModel.updateORU500Data { purgingR5002sA2 = last.purgingR5002sA2 } }
        checkAndUpdate(current.purgingR5002sB2, last.purgingR5002sB2) { sharedViewModel.updateORU500Data { purgingR5002sB2 = last.purgingR5002sB2 } }
        checkAndUpdate(current.purgingR5002sC2, last.purgingR5002sC2) { sharedViewModel.updateORU500Data { purgingR5002sC2 = last.purgingR5002sC2 } }

        // В-500 ВШТ-31
        checkAndUpdate(current.gasPressureVsht31A, last.gasPressureVsht31A) { sharedViewModel.updateORU500Data { gasPressureVsht31A = last.gasPressureVsht31A } }
        checkAndUpdate(current.gasPressureVsht31B, last.gasPressureVsht31B) { sharedViewModel.updateORU500Data { gasPressureVsht31B = last.gasPressureVsht31B } }
        checkAndUpdate(current.gasPressureVsht31C, last.gasPressureVsht31C) { sharedViewModel.updateORU500Data { gasPressureVsht31C = last.gasPressureVsht31C } }
        checkAndUpdate(current.oilTtVsht31A, last.oilTtVsht31A) { sharedViewModel.updateORU500Data { oilTtVsht31A = last.oilTtVsht31A } }
        checkAndUpdate(current.oilTtVsht31B, last.oilTtVsht31B) { sharedViewModel.updateORU500Data { oilTtVsht31B = last.oilTtVsht31B } }
        checkAndUpdate(current.oilTtVsht31C, last.oilTtVsht31C) { sharedViewModel.updateORU500Data { oilTtVsht31C = last.oilTtVsht31C } }

        // В-500 ВЛТ-30
        checkAndUpdate(current.gasPressureVlt30A, last.gasPressureVlt30A) { sharedViewModel.updateORU500Data { gasPressureVlt30A = last.gasPressureVlt30A } }
        checkAndUpdate(current.gasPressureVlt30B, last.gasPressureVlt30B) { sharedViewModel.updateORU500Data { gasPressureVlt30B = last.gasPressureVlt30B } }
        checkAndUpdate(current.gasPressureVlt30C, last.gasPressureVlt30C) { sharedViewModel.updateORU500Data { gasPressureVlt30C = last.gasPressureVlt30C } }
        checkAndUpdate(current.oilTtVlt30A, last.oilTtVlt30A) { sharedViewModel.updateORU500Data { oilTtVlt30A = last.oilTtVlt30A } }
        checkAndUpdate(current.oilTtVlt30B, last.oilTtVlt30B) { sharedViewModel.updateORU500Data { oilTtVlt30B = last.oilTtVlt30B } }
        checkAndUpdate(current.oilTtVlt30C, last.oilTtVlt30C) { sharedViewModel.updateORU500Data { oilTtVlt30C = last.oilTtVlt30C } }

        // В-500 ВШЛ-32
        checkAndUpdate(current.purgingVshl32A1, last.purgingVshl32A1) { sharedViewModel.updateORU500Data { purgingVshl32A1 = last.purgingVshl32A1 } }
        checkAndUpdate(current.purgingVshl32B1, last.purgingVshl32B1) { sharedViewModel.updateORU500Data { purgingVshl32B1 = last.purgingVshl32B1 } }
        checkAndUpdate(current.purgingVshl32C1, last.purgingVshl32C1) { sharedViewModel.updateORU500Data { purgingVshl32C1 = last.purgingVshl32C1 } }
        checkAndUpdate(current.purgingVshl32A2, last.purgingVshl32A2) { sharedViewModel.updateORU500Data { purgingVshl32A2 = last.purgingVshl32A2 } }
        checkAndUpdate(current.purgingVshl32B2, last.purgingVshl32B2) { sharedViewModel.updateORU500Data { purgingVshl32B2 = last.purgingVshl32B2 } }
        checkAndUpdate(current.purgingVshl32C2, last.purgingVshl32C2) { sharedViewModel.updateORU500Data { purgingVshl32C2 = last.purgingVshl32C2 } }
        checkAndUpdate(current.oilTtVshl32A, last.oilTtVshl32A) { sharedViewModel.updateORU500Data { oilTtVshl32A = last.oilTtVshl32A } }
        checkAndUpdate(current.oilTtVshl32B, last.oilTtVshl32B) { sharedViewModel.updateORU500Data { oilTtVshl32B = last.oilTtVshl32B } }
        checkAndUpdate(current.oilTtVshl32C, last.oilTtVshl32C) { sharedViewModel.updateORU500Data { oilTtVshl32C = last.oilTtVshl32C } }

        // В-500 ВШЛ-21
        checkAndUpdate(current.purgingVshl21A1, last.purgingVshl21A1) { sharedViewModel.updateORU500Data { purgingVshl21A1 = last.purgingVshl21A1 } }
        checkAndUpdate(current.purgingVshl21B1, last.purgingVshl21B1) { sharedViewModel.updateORU500Data { purgingVshl21B1 = last.purgingVshl21B1 } }
        checkAndUpdate(current.purgingVshl21C1, last.purgingVshl21C1) { sharedViewModel.updateORU500Data { purgingVshl21C1 = last.purgingVshl21C1 } }
        checkAndUpdate(current.purgingVshl21A2, last.purgingVshl21A2) { sharedViewModel.updateORU500Data { purgingVshl21A2 = last.purgingVshl21A2 } }
        checkAndUpdate(current.purgingVshl21B2, last.purgingVshl21B2) { sharedViewModel.updateORU500Data { purgingVshl21B2 = last.purgingVshl21B2 } }
        checkAndUpdate(current.purgingVshl21C2, last.purgingVshl21C2) { sharedViewModel.updateORU500Data { purgingVshl21C2 = last.purgingVshl21C2 } }
        checkAndUpdate(current.oilTtVshl21A, last.oilTtVshl21A) { sharedViewModel.updateORU500Data { oilTtVshl21A = last.oilTtVshl21A } }
        checkAndUpdate(current.oilTtVshl21B, last.oilTtVshl21B) { sharedViewModel.updateORU500Data { oilTtVshl21B = last.oilTtVshl21B } }
        checkAndUpdate(current.oilTtVshl21C, last.oilTtVshl21C) { sharedViewModel.updateORU500Data { oilTtVshl21C = last.oilTtVshl21C } }

        // В-500 ВШТ-22
        checkAndUpdate(current.purgingVsht22A1, last.purgingVsht22A1) { sharedViewModel.updateORU500Data { purgingVsht22A1 = last.purgingVsht22A1 } }
        checkAndUpdate(current.purgingVsht22B1, last.purgingVsht22B1) { sharedViewModel.updateORU500Data { purgingVsht22B1 = last.purgingVsht22B1 } }
        checkAndUpdate(current.purgingVsht22C1, last.purgingVsht22C1) { sharedViewModel.updateORU500Data { purgingVsht22C1 = last.purgingVsht22C1 } }
        checkAndUpdate(current.purgingVsht22A2, last.purgingVsht22A2) { sharedViewModel.updateORU500Data { purgingVsht22A2 = last.purgingVsht22A2 } }
        checkAndUpdate(current.purgingVsht22B2, last.purgingVsht22B2) { sharedViewModel.updateORU500Data { purgingVsht22B2 = last.purgingVsht22B2 } }
        checkAndUpdate(current.purgingVsht22C2, last.purgingVsht22C2) { sharedViewModel.updateORU500Data { purgingVsht22C2 = last.purgingVsht22C2 } }
        checkAndUpdate(current.oilTtVsht22A, last.oilTtVsht22A) { sharedViewModel.updateORU500Data { oilTtVsht22A = last.oilTtVsht22A } }
        checkAndUpdate(current.oilTtVsht22B, last.oilTtVsht22B) { sharedViewModel.updateORU500Data { oilTtVsht22B = last.oilTtVsht22B } }
        checkAndUpdate(current.oilTtVsht22C, last.oilTtVsht22C) { sharedViewModel.updateORU500Data { oilTtVsht22C = last.oilTtVsht22C } }

        // В-500 ВЛТ-20
        checkAndUpdate(current.purgingVlt20A1, last.purgingVlt20A1) { sharedViewModel.updateORU500Data { purgingVlt20A1 = last.purgingVlt20A1 } }
        checkAndUpdate(current.purgingVlt20B1, last.purgingVlt20B1) { sharedViewModel.updateORU500Data { purgingVlt20B1 = last.purgingVlt20B1 } }
        checkAndUpdate(current.purgingVlt20C1, last.purgingVlt20C1) { sharedViewModel.updateORU500Data { purgingVlt20C1 = last.purgingVlt20C1 } }
        checkAndUpdate(current.purgingVlt20A2, last.purgingVlt20A2) { sharedViewModel.updateORU500Data { purgingVlt20A2 = last.purgingVlt20A2 } }
        checkAndUpdate(current.purgingVlt20B2, last.purgingVlt20B2) { sharedViewModel.updateORU500Data { purgingVlt20B2 = last.purgingVlt20B2 } }
        checkAndUpdate(current.purgingVlt20C2, last.purgingVlt20C2) { sharedViewModel.updateORU500Data { purgingVlt20C2 = last.purgingVlt20C2 } }
        checkAndUpdate(current.oilTtVlt20A, last.oilTtVlt20A) { sharedViewModel.updateORU500Data { oilTtVlt20A = last.oilTtVlt20A } }
        checkAndUpdate(current.oilTtVlt20B, last.oilTtVlt20B) { sharedViewModel.updateORU500Data { oilTtVlt20B = last.oilTtVlt20B } }
        checkAndUpdate(current.oilTtVlt20C, last.oilTtVlt20C) { sharedViewModel.updateORU500Data { oilTtVlt20C = last.oilTtVlt20C } }

        // В-500 ВШТ-11
        checkAndUpdate(current.purgingVsht11A1, last.purgingVsht11A1) { sharedViewModel.updateORU500Data { purgingVsht11A1 = last.purgingVsht11A1 } }
        checkAndUpdate(current.purgingVsht11B1, last.purgingVsht11B1) { sharedViewModel.updateORU500Data { purgingVsht11B1 = last.purgingVsht11B1 } }
        checkAndUpdate(current.purgingVsht11C1, last.purgingVsht11C1) { sharedViewModel.updateORU500Data { purgingVsht11C1 = last.purgingVsht11C1 } }
        checkAndUpdate(current.purgingVsht11A2, last.purgingVsht11A2) { sharedViewModel.updateORU500Data { purgingVsht11A2 = last.purgingVsht11A2 } }
        checkAndUpdate(current.purgingVsht11B2, last.purgingVsht11B2) { sharedViewModel.updateORU500Data { purgingVsht11B2 = last.purgingVsht11B2 } }
        checkAndUpdate(current.purgingVsht11C2, last.purgingVsht11C2) { sharedViewModel.updateORU500Data { purgingVsht11C2 = last.purgingVsht11C2 } }
        checkAndUpdate(current.oilTtVsht11A, last.oilTtVsht11A) { sharedViewModel.updateORU500Data { oilTtVsht11A = last.oilTtVsht11A } }
        checkAndUpdate(current.oilTtVsht11B, last.oilTtVsht11B) { sharedViewModel.updateORU500Data { oilTtVsht11B = last.oilTtVsht11B } }
        checkAndUpdate(current.oilTtVsht11C, last.oilTtVsht11C) { sharedViewModel.updateORU500Data { oilTtVsht11C = last.oilTtVsht11C } }

        // В-500 ВШЛ-12
        checkAndUpdate(current.purgingVshl12A1, last.purgingVshl12A1) { sharedViewModel.updateORU500Data { purgingVshl12A1 = last.purgingVshl12A1 } }
        checkAndUpdate(current.purgingVshl12B1, last.purgingVshl12B1) { sharedViewModel.updateORU500Data { purgingVshl12B1 = last.purgingVshl12B1 } }
        checkAndUpdate(current.purgingVshl12C1, last.purgingVshl12C1) { sharedViewModel.updateORU500Data { purgingVshl12C1 = last.purgingVshl12C1 } }
        checkAndUpdate(current.purgingVshl12A2, last.purgingVshl12A2) { sharedViewModel.updateORU500Data { purgingVshl12A2 = last.purgingVshl12A2 } }
        checkAndUpdate(current.purgingVshl12B2, last.purgingVshl12B2) { sharedViewModel.updateORU500Data { purgingVshl12B2 = last.purgingVshl12B2 } }
        checkAndUpdate(current.purgingVshl12C2, last.purgingVshl12C2) { sharedViewModel.updateORU500Data { purgingVshl12C2 = last.purgingVshl12C2 } }
        checkAndUpdate(current.oilTtVshl12A, last.oilTtVshl12A) { sharedViewModel.updateORU500Data { oilTtVshl12A = last.oilTtVshl12A } }
        checkAndUpdate(current.oilTtVshl12B, last.oilTtVshl12B) { sharedViewModel.updateORU500Data { oilTtVshl12B = last.oilTtVshl12B } }
        checkAndUpdate(current.oilTtVshl12C, last.oilTtVshl12C) { sharedViewModel.updateORU500Data { oilTtVshl12C = last.oilTtVshl12C } }

        // 1ТН-500
        checkAndUpdate(current.tn1500Cascade1A, last.tn1500Cascade1A) { sharedViewModel.updateORU500Data { tn1500Cascade1A = last.tn1500Cascade1A } }
        checkAndUpdate(current.tn1500Cascade1B, last.tn1500Cascade1B) { sharedViewModel.updateORU500Data { tn1500Cascade1B = last.tn1500Cascade1B } }
        checkAndUpdate(current.tn1500Cascade1C, last.tn1500Cascade1C) { sharedViewModel.updateORU500Data { tn1500Cascade1C = last.tn1500Cascade1C } }
        checkAndUpdate(current.tn1500Cascade2A, last.tn1500Cascade2A) { sharedViewModel.updateORU500Data { tn1500Cascade2A = last.tn1500Cascade2A } }
        checkAndUpdate(current.tn1500Cascade2B, last.tn1500Cascade2B) { sharedViewModel.updateORU500Data { tn1500Cascade2B = last.tn1500Cascade2B } }
        checkAndUpdate(current.tn1500Cascade2C, last.tn1500Cascade2C) { sharedViewModel.updateORU500Data { tn1500Cascade2C = last.tn1500Cascade2C } }
        checkAndUpdate(current.tn1500Cascade3A, last.tn1500Cascade3A) { sharedViewModel.updateORU500Data { tn1500Cascade3A = last.tn1500Cascade3A } }
        checkAndUpdate(current.tn1500Cascade3B, last.tn1500Cascade3B) { sharedViewModel.updateORU500Data { tn1500Cascade3B = last.tn1500Cascade3B } }
        checkAndUpdate(current.tn1500Cascade3C, last.tn1500Cascade3C) { sharedViewModel.updateORU500Data { tn1500Cascade3C = last.tn1500Cascade3C } }
        checkAndUpdate(current.tn1500Cascade4A, last.tn1500Cascade4A) { sharedViewModel.updateORU500Data { tn1500Cascade4A = last.tn1500Cascade4A } }
        checkAndUpdate(current.tn1500Cascade4B, last.tn1500Cascade4B) { sharedViewModel.updateORU500Data { tn1500Cascade4B = last.tn1500Cascade4B } }
        checkAndUpdate(current.tn1500Cascade4C, last.tn1500Cascade4C) { sharedViewModel.updateORU500Data { tn1500Cascade4C = last.tn1500Cascade4C } }

        // 2ТН-500
        checkAndUpdate(current.tn2500Cascade1A, last.tn2500Cascade1A) { sharedViewModel.updateORU500Data { tn2500Cascade1A = last.tn2500Cascade1A } }
        checkAndUpdate(current.tn2500Cascade1B, last.tn2500Cascade1B) { sharedViewModel.updateORU500Data { tn2500Cascade1B = last.tn2500Cascade1B } }
        checkAndUpdate(current.tn2500Cascade1C, last.tn2500Cascade1C) { sharedViewModel.updateORU500Data { tn2500Cascade1C = last.tn2500Cascade1C } }
        checkAndUpdate(current.tn2500Cascade2A, last.tn2500Cascade2A) { sharedViewModel.updateORU500Data { tn2500Cascade2A = last.tn2500Cascade2A } }
        checkAndUpdate(current.tn2500Cascade2B, last.tn2500Cascade2B) { sharedViewModel.updateORU500Data { tn2500Cascade2B = last.tn2500Cascade2B } }
        checkAndUpdate(current.tn2500Cascade2C, last.tn2500Cascade2C) { sharedViewModel.updateORU500Data { tn2500Cascade2C = last.tn2500Cascade2C } }
        checkAndUpdate(current.tn2500Cascade3A, last.tn2500Cascade3A) { sharedViewModel.updateORU500Data { tn2500Cascade3A = last.tn2500Cascade3A } }
        checkAndUpdate(current.tn2500Cascade3B, last.tn2500Cascade3B) { sharedViewModel.updateORU500Data { tn2500Cascade3B = last.tn2500Cascade3B } }
        checkAndUpdate(current.tn2500Cascade3C, last.tn2500Cascade3C) { sharedViewModel.updateORU500Data { tn2500Cascade3C = last.tn2500Cascade3C } }
        checkAndUpdate(current.tn2500Cascade4A, last.tn2500Cascade4A) { sharedViewModel.updateORU500Data { tn2500Cascade4A = last.tn2500Cascade4A } }
        checkAndUpdate(current.tn2500Cascade4B, last.tn2500Cascade4B) { sharedViewModel.updateORU500Data { tn2500Cascade4B = last.tn2500Cascade4B } }
        checkAndUpdate(current.tn2500Cascade4C, last.tn2500Cascade4C) { sharedViewModel.updateORU500Data { tn2500Cascade4C = last.tn2500Cascade4C } }

        // ТН-500 СГРЭС-1
        checkAndUpdate(current.tn500Sgres1Cascade1A, last.tn500Sgres1Cascade1A) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade1A = last.tn500Sgres1Cascade1A } }
        checkAndUpdate(current.tn500Sgres1Cascade1B, last.tn500Sgres1Cascade1B) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade1B = last.tn500Sgres1Cascade1B } }
        checkAndUpdate(current.tn500Sgres1Cascade1C, last.tn500Sgres1Cascade1C) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade1C = last.tn500Sgres1Cascade1C } }
        checkAndUpdate(current.tn500Sgres1Cascade2A, last.tn500Sgres1Cascade2A) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade2A = last.tn500Sgres1Cascade2A } }
        checkAndUpdate(current.tn500Sgres1Cascade2B, last.tn500Sgres1Cascade2B) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade2B = last.tn500Sgres1Cascade2B } }
        checkAndUpdate(current.tn500Sgres1Cascade2C, last.tn500Sgres1Cascade2C) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade2C = last.tn500Sgres1Cascade2C } }
        checkAndUpdate(current.tn500Sgres1Cascade3A, last.tn500Sgres1Cascade3A) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade3A = last.tn500Sgres1Cascade3A } }
        checkAndUpdate(current.tn500Sgres1Cascade3B, last.tn500Sgres1Cascade3B) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade3B = last.tn500Sgres1Cascade3B } }
        checkAndUpdate(current.tn500Sgres1Cascade3C, last.tn500Sgres1Cascade3C) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade3C = last.tn500Sgres1Cascade3C } }
        checkAndUpdate(current.tn500Sgres1Cascade4A, last.tn500Sgres1Cascade4A) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade4A = last.tn500Sgres1Cascade4A } }
        checkAndUpdate(current.tn500Sgres1Cascade4B, last.tn500Sgres1Cascade4B) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade4B = last.tn500Sgres1Cascade4B } }
        checkAndUpdate(current.tn500Sgres1Cascade4C, last.tn500Sgres1Cascade4C) { sharedViewModel.updateORU500Data { tn500Sgres1Cascade4C = last.tn500Sgres1Cascade4C } }

        return count
    }

    private fun fillEmptyFieldsBuildings(last: InspectionBuildingsData, current: InspectionBuildingsData): Int {
        var count = 0

        fun checkAndUpdate(currentValue: String, lastValue: String, update: () -> Unit) {
            if (currentValue.isEmpty() && lastValue.isNotEmpty() && lastValue != "○") {
                update()
                count++
            }
        }

        // Компрессорная №1
        if (current.compressor1Valve == "○" && last.compressor1Valve != "○") { sharedViewModel.updateBuildingsData { compressor1Valve = last.compressor1Valve }; count++ }
        if (current.compressor1Heating == "○" && last.compressor1Heating != "○") { sharedViewModel.updateBuildingsData { compressor1Heating = last.compressor1Heating }; count++ }
        checkAndUpdate(current.compressor1Temp, last.compressor1Temp) { sharedViewModel.updateBuildingsData { compressor1Temp = last.compressor1Temp } }

        // Баллонная №1
        if (current.ballroom1Valve == "○" && last.ballroom1Valve != "○") { sharedViewModel.updateBuildingsData { ballroom1Valve = last.ballroom1Valve }; count++ }
        if (current.ballroom1Heating == "○" && last.ballroom1Heating != "○") { sharedViewModel.updateBuildingsData { ballroom1Heating = last.ballroom1Heating }; count++ }
        checkAndUpdate(current.ballroom1Temp, last.ballroom1Temp) { sharedViewModel.updateBuildingsData { ballroom1Temp = last.ballroom1Temp } }

        // Компрессорная №2
        if (current.compressor2Valve == "○" && last.compressor2Valve != "○") { sharedViewModel.updateBuildingsData { compressor2Valve = last.compressor2Valve }; count++ }
        if (current.compressor2Heating == "○" && last.compressor2Heating != "○") { sharedViewModel.updateBuildingsData { compressor2Heating = last.compressor2Heating }; count++ }
        checkAndUpdate(current.compressor2Temp, last.compressor2Temp) { sharedViewModel.updateBuildingsData { compressor2Temp = last.compressor2Temp } }

        // Баллонная №2
        if (current.ballroom2Valve == "○" && last.ballroom2Valve != "○") { sharedViewModel.updateBuildingsData { ballroom2Valve = last.ballroom2Valve }; count++ }
        if (current.ballroom2Heating == "○" && last.ballroom2Heating != "○") { sharedViewModel.updateBuildingsData { ballroom2Heating = last.ballroom2Heating }; count++ }
        checkAndUpdate(current.ballroom2Temp, last.ballroom2Temp) { sharedViewModel.updateBuildingsData { ballroom2Temp = last.ballroom2Temp } }

        // КПЗ ОПУ
        if (current.kpzOpuValve == "○" && last.kpzOpuValve != "○") { sharedViewModel.updateBuildingsData { kpzOpuValve = last.kpzOpuValve }; count++ }
        if (current.kpzOpuHeating == "○" && last.kpzOpuHeating != "○") { sharedViewModel.updateBuildingsData { kpzOpuHeating = last.kpzOpuHeating }; count++ }
        checkAndUpdate(current.kpzOpuTemp, last.kpzOpuTemp) { sharedViewModel.updateBuildingsData { kpzOpuTemp = last.kpzOpuTemp } }

        // КПЗ-2
        if (current.kpz2Valve == "○" && last.kpz2Valve != "○") { sharedViewModel.updateBuildingsData { kpz2Valve = last.kpz2Valve }; count++ }
        if (current.kpz2Heating == "○" && last.kpz2Heating != "○") { sharedViewModel.updateBuildingsData { kpz2Heating = last.kpz2Heating }; count++ }
        checkAndUpdate(current.kpz2Temp, last.kpz2Temp) { sharedViewModel.updateBuildingsData { kpz2Temp = last.kpz2Temp } }

        // Насосная пожаротушения
        if (current.firePumpValve == "○" && last.firePumpValve != "○") { sharedViewModel.updateBuildingsData { firePumpValve = last.firePumpValve }; count++ }
        if (current.firePumpHeating == "○" && last.firePumpHeating != "○") { sharedViewModel.updateBuildingsData { firePumpHeating = last.firePumpHeating }; count++ }
        checkAndUpdate(current.firePumpTemp, last.firePumpTemp) { sharedViewModel.updateBuildingsData { firePumpTemp = last.firePumpTemp } }

        // Мастерская по ремонту ВВ
        if (current.workshopHeating == "○" && last.workshopHeating != "○") { sharedViewModel.updateBuildingsData { workshopHeating = last.workshopHeating }; count++ }
        checkAndUpdate(current.workshopTemp, last.workshopTemp) { sharedViewModel.updateBuildingsData { workshopTemp = last.workshopTemp } }

        // Артскважина
        if (current.artWellHeating == "○" && last.artWellHeating != "○") { sharedViewModel.updateBuildingsData { artWellHeating = last.artWellHeating }; count++ }

        // Здание артезианской скважины
        if (current.artesianWellHeating == "○" && last.artesianWellHeating != "○") { sharedViewModel.updateBuildingsData { artesianWellHeating = last.artesianWellHeating }; count++ }

        // Помещение 1 (2) АБ
        if (current.roomAbHeating == "○" && last.roomAbHeating != "○") { sharedViewModel.updateBuildingsData { roomAbHeating = last.roomAbHeating }; count++ }
        checkAndUpdate(current.roomAbTemp, last.roomAbTemp) { sharedViewModel.updateBuildingsData { roomAbTemp = last.roomAbTemp } }

        // Помещение п/этажа №1,2,3
        if (current.basementHeating == "○" && last.basementHeating != "○") { sharedViewModel.updateBuildingsData { basementHeating = last.basementHeating }; count++ }
        checkAndUpdate(current.basementTemp, last.basementTemp) { sharedViewModel.updateBuildingsData { basementTemp = last.basementTemp } }

        return count
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}