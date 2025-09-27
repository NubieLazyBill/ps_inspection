package com.example.ps_inspection

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Новая функциональность - экспорт в Excel
        binding.btnExportExcel.setOnClickListener {
            exportToExcel()
        }

        // Новая функциональность - очистка данных
        binding.btnClearData.setOnClickListener {
            clearAllData()
        }
    }

    private fun exportToExcel() {
        try {
            // Собираем данные из всех ViewModel
            val oru35Data = sharedViewModel.oru35Data.value
            val oru220Data = sharedViewModel.oru220Data.value
            val atgData = sharedViewModel.atgData.value
            val buildingsData = sharedViewModel.buildingsData.value

            // Создаем сервис экспорта
            val exportService = ExcelExportService(requireContext())

            // Экспортируем в Excel
            val fileUri = exportService.exportToExcel(oru35Data, oru220Data, atgData, buildingsData)

            if (fileUri != null) {
                Toast.makeText(requireContext(), "Файл успешно сохранен!", Toast.LENGTH_LONG).show()

                // Предлагаем поделиться файлом
                shareExcelFile(fileUri)
            } else {
                Toast.makeText(requireContext(), "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun shareExcelFile(fileUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться файлом осмотра"))
    }

    private fun clearAllData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистка данных")
            .setMessage("Вы уверены, что хотите очистить все данные осмотра? Это действие нельзя отменить.")
            .setPositiveButton("Очистить") { _, _ ->
                // Сбрасываем все данные в начальное состояние
                sharedViewModel.updateORU35Data {
                    // Обнуляем все поля ORU35
                    tsn2 = ""; tsn3 = ""; tsn4 = ""; tn352atg = ""; tn353atg = ""
                    tt352tsnA = ""; tt352tsnB = ""; tt352tsnC = ""
                    tt353tsnA = ""; tt353tsnB = ""; tt353tsnC = ""
                    v352tsnA = ""; v352tsnB = ""; v352tsnC = ""
                    v353tsnA = ""; v353tsnB = ""; v353tsnC = ""
                }

                // Аналогично для других экранов - нужно добавить reset методы в ViewModel
                resetATGData()
                resetBuildingsData()
                resetORU220Data()
                resetORU500Data()

                Toast.makeText(requireContext(), "Все данные очищены", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun resetORU500Data() {
        sharedViewModel.updateORU500Data {
            // Ячейка 1: В-500 Р-500 2С
            purgingR5002sA1 = ""
            purgingR5002sB1 = ""
            purgingR5002sC1 = ""
            purgingR5002sA2 = ""
            purgingR5002sB2 = ""
            purgingR5002sC2 = ""

            // Ячейка 2: В-500 ВШТ-31
            gasPressureVsht31A = ""
            gasPressureVsht31B = ""
            gasPressureVsht31C = ""
            oilTtVsht31A = ""
            oilTtVsht31B = ""
            oilTtVsht31C = ""

            // Ячейка 3: В-500 ВЛТ-30
            gasPressureVlt30A = ""
            gasPressureVlt30B = ""
            gasPressureVlt30C = ""
            oilTtVlt30A = ""
            oilTtVlt30B = ""
            oilTtVlt30C = ""
            oilTtTrachukovskayaA = ""
            oilTtTrachukovskayaB = ""
            oilTtTrachukovskayaC = ""
            oil2tnTrachukovskayaA = ""
            oil2tnTrachukovskayaB = ""
            oil2tnTrachukovskayaC = ""
            oil1tnTrachukovskayaA = ""
            oil1tnTrachukovskayaB = ""
            oil1tnTrachukovskayaC = ""

            // Ячейка 4: В-500 ВШЛ-32
            purgingVshl32A1 = ""
            purgingVshl32B1 = ""
            purgingVshl32C1 = ""
            purgingVshl32A2 = ""
            purgingVshl32B2 = ""
            purgingVshl32C2 = ""
            oilTtVshl32A = ""
            oilTtVshl32B = ""
            oilTtVshl32C = ""

            // Ячейка 5: В-500 ВШЛ-21
            purgingVshl21A1 = ""
            purgingVshl21B1 = ""
            purgingVshl21C1 = ""
            purgingVshl21A2 = ""
            purgingVshl21B2 = ""
            purgingVshl21C2 = ""
            oilTtVshl21A = ""
            oilTtVshl21B = ""
            oilTtVshl21C = ""

            // Ячейка 6: В-500 ВШТ-22
            purgingVsht22A1 = ""
            purgingVsht22B1 = ""
            purgingVsht22C1 = ""
            purgingVsht22A2 = ""
            purgingVsht22B2 = ""
            purgingVsht22C2 = ""
            oilTtVsht22A = ""
            oilTtVsht22B = ""
            oilTtVsht22C = ""

            // Ячейка 7: В-500 ВШТ-11
            purgingVsht11A1 = ""
            purgingVsht11B1 = ""
            purgingVsht11C1 = ""
            purgingVsht11A2 = ""
            purgingVsht11B2 = ""
            purgingVsht11C2 = ""
            oilTtVsht11A = ""
            oilTtVsht11B = ""
            oilTtVsht11C = ""

            // Ячейка 8: В-500 ВШЛ-12
            purgingVshl12A1 = ""
            purgingVshl12B1 = ""
            purgingVshl12C1 = ""
            purgingVshl12A2 = ""
            purgingVshl12B2 = ""
            purgingVshl12C2 = ""
            oilTtVshl12A = ""
            oilTtVshl12B = ""
            oilTtVshl12C = ""
            oil2tnBelozernayaA = ""
            oil2tnBelozernayaB = ""
            oil2tnBelozernayaC = ""

            // 1ТН-500 Каскады
            tn1500Cascade1A = ""
            tn1500Cascade1B = ""
            tn1500Cascade1C = ""
            tn1500Cascade2A = ""
            tn1500Cascade2B = ""
            tn1500Cascade2C = ""
            tn1500Cascade3A = ""
            tn1500Cascade3B = ""
            tn1500Cascade3C = ""
            tn1500Cascade4A = ""
            tn1500Cascade4B = ""
            tn1500Cascade4C = ""

            // 2ТН-500 Каскады
            tn2500Cascade1A = ""
            tn2500Cascade1B = ""
            tn2500Cascade1C = ""
            tn2500Cascade2A = ""
            tn2500Cascade2B = ""
            tn2500Cascade2C = ""
            tn2500Cascade3A = ""
            tn2500Cascade3B = ""
            tn2500Cascade3C = ""
            tn2500Cascade4A = ""
            tn2500Cascade4B = ""
            tn2500Cascade4C = ""

            // ТН-500 СГРЭС-1 Каскады
            tn500Sgres1Cascade1A = ""
            tn500Sgres1Cascade1B = ""
            tn500Sgres1Cascade1C = ""
            tn500Sgres1Cascade2A = ""
            tn500Sgres1Cascade2B = ""
            tn500Sgres1Cascade2C = ""
            tn500Sgres1Cascade3A = ""
            tn500Sgres1Cascade3B = ""
            tn500Sgres1Cascade3C = ""
            tn500Sgres1Cascade4A = ""
            tn500Sgres1Cascade4B = ""
            tn500Sgres1Cascade4C = ""
        }
    }

    private fun resetORU220Data() {
        sharedViewModel.updateORU220Data {
            //Сбрасываем все поля ОРУ-220
            // Мирная
            purgingMirnayaA = ""; purgingMirnayaB = ""; purgingMirnayaC = ""
            oilMirnayaA = ""; oilMirnayaB = ""; oilMirnayaC = ""

            // Топаз
            purgingTopazA = ""; purgingTopazB = ""; purgingTopazC = ""
            oilTopazA = ""; oilTopazB = ""; oilTopazC = ""

            // ОВ
            purgingOvA = ""; purgingOvB = ""; purgingOvC = ""
            oilOvA = ""; oilOvB = ""; oilOvC = ""

            // ТН-220 ОСШ ф.В
            tnOsshFvUpper = ""; tnOsshFvLower = ""

            // В-220 2АТГ
            purgingV2atgA = ""; purgingV2atgB = ""; purgingV2atgC = ""
            oilTt2atgA = ""; oilTt2atgB = ""; oilTt2atgC = ""

            // ШСВ-220
            purgingShSV220A = ""; purgingShSV220B = ""; purgingShSV220C = ""
            oilTtShSV220A = ""; oilTtShSV220B = ""; oilTtShSV220C = ""

            // В-220 3АТГ
            purgingV3atgA = ""; purgingV3atgB = ""; purgingV3atgC = ""
            oilTt3atgA = ""; oilTt3atgB = ""; oilTt3atgC = ""

            // Орбита
            purgingOrbitaA = ""; purgingOrbitaB = ""; purgingOrbitaC = ""
            oilOrbitaA = ""; oilOrbitaB = ""; oilOrbitaC = ""

            // Факел
            purgingFakelA = ""; purgingFakelB = ""; purgingFakelC = ""
            oilFakelA = ""; oilFakelB = ""; oilFakelC = ""

            // Комета-1
            purgingCometa1A = ""; purgingCometa1B = ""; purgingCometa1C = ""
            oilCometa1A = ""; oilCometa1B = ""; oilCometa1C = ""

            // Комета-2
            purgingCometa2A = ""; purgingCometa2B = ""; purgingCometa2C = ""
            oilCometa2A = ""; oilCometa2B = ""; oilCometa2C = ""

            // 1ТН-220
            tn1UpperA = ""; tn1UpperB = ""; tn1UpperC = ""
            tn1LowerA = ""; tn1LowerB = ""; tn1LowerC = ""

            // 2ТН-220
            tn2UpperA = ""; tn2UpperB = ""; tn2UpperC = ""
            tn2LowerA = ""; tn2LowerB = ""; tn2LowerC = ""
        }
    }

    private fun resetATGData() {
        sharedViewModel.updateATGData {
            // Сбрасываем все поля АТГ к пустым значениям
            // 2 АТГ ф.С
            atg2_c_oil_tank = ""; atg2_c_oil_rpn = ""; atg2_c_pressure_500 = ""; atg2_c_pressure_220 = ""
            atg2_c_temp_ts1 = ""; atg2_c_temp_ts2 = ""; atg2_c_pump_group1 = ""; atg2_c_pump_group2 = ""
            atg2_c_pump_group3 = ""; atg2_c_pump_group4 = ""

            // 2 АТГ ф.В
            atg2_b_oil_tank = ""; atg2_b_oil_rpn = ""; atg2_b_pressure_500 = ""; atg2_b_pressure_220 = ""
            atg2_b_temp_ts1 = ""; atg2_b_temp_ts2 = ""; atg2_b_pump_group1 = ""; atg2_b_pump_group2 = ""
            atg2_b_pump_group3 = ""; atg2_b_pump_group4 = ""

            // 2 АТГ ф.А
            atg2_a_oil_tank = ""; atg2_a_oil_rpn = ""; atg2_a_pressure_500 = ""; atg2_a_pressure_220 = ""
            atg2_a_temp_ts1 = ""; atg2_a_temp_ts2 = ""; atg2_a_pump_group1 = ""; atg2_a_pump_group2 = ""
            atg2_a_pump_group3 = ""; atg2_a_pump_group4 = ""

            // АТГ резервная фаза
            atg_reserve_oil_tank = ""; atg_reserve_oil_rpn = ""; atg_reserve_pressure_500 = ""; atg_reserve_pressure_220 = ""
            atg_reserve_temp_ts1 = ""; atg_reserve_temp_ts2 = ""; atg_reserve_pump_group1 = ""; atg_reserve_pump_group2 = ""
            atg_reserve_pump_group3 = ""; atg_reserve_pump_group4 = ""

            // 3 АТГ ф.С
            atg3_c_oil_tank = ""; atg3_c_oil_rpn = ""; atg3_c_pressure_500 = ""; atg3_c_pressure_220 = ""
            atg3_c_temp_ts1 = ""; atg3_c_temp_ts2 = ""; atg3_c_pump_group1 = ""; atg3_c_pump_group2 = ""
            atg3_c_pump_group3 = ""; atg3_c_pump_group4 = ""

            // 3 АТГ ф.В
            atg3_b_oil_tank = ""; atg3_b_oil_rpn = ""; atg3_b_pressure_500 = ""; atg3_b_pressure_220 = ""
            atg3_b_temp_ts1 = ""; atg3_b_temp_ts2 = ""; atg3_b_pump_group1 = ""; atg3_b_pump_group2 = ""
            atg3_b_pump_group3 = ""; atg3_b_pump_group4 = ""

            // 3 АТГ ф.А
            atg3_a_oil_tank = ""; atg3_a_oil_rpn = ""; atg3_a_pressure_500 = ""; atg3_a_pressure_220 = ""
            atg3_a_temp_ts1 = ""; atg3_a_temp_ts2 = ""; atg3_a_pump_group1 = ""; atg3_a_pump_group2 = ""
            atg3_a_pump_group3 = ""; atg3_a_pump_group4 = ""

            // Реакторы
            reactor_c_oil_tank = ""; reactor_c_pressure_500 = ""; reactor_c_temp_ts = ""
            reactor_c_pump_group1 = ""; reactor_c_pump_group2 = ""; reactor_c_pump_group3 = ""; reactor_c_tt_neutral = ""

            reactor_b_oil_tank = ""; reactor_b_pressure_500 = ""; reactor_b_temp_ts = ""
            reactor_b_pump_group1 = ""; reactor_b_pump_group2 = ""; reactor_b_pump_group3 = ""; reactor_b_tt_neutral = ""

            reactor_a_oil_tank = ""; reactor_a_pressure_500 = ""; reactor_a_temp_ts = ""
            reactor_a_pump_group1 = ""; reactor_a_pump_group2 = ""; reactor_a_pump_group3 = ""; reactor_a_tt_neutral = ""
        }
    }

    private fun resetBuildingsData() {
        sharedViewModel.updateBuildingsData {
            compressor1Valve = "○"; compressor1Heating = "○"; compressor1Temp = ""

            // Баллоная №1
            ballroom1Valve = "○"; ballroom1Heating = "○"; ballroom1Temp = ""

            // Компрессорная №2
            compressor2Valve = "○"; compressor2Heating = "○"; compressor2Temp = ""

            // Баллоная №2
            ballroom2Valve = "○"; ballroom2Heating = "○"; ballroom2Temp = ""

            // КПЗ ОПУ
            kpzOpuValve = "○"; kpzOpuHeating = "○"; kpzOpuTemp = ""

            // КПЗ-2
            kpz2Valve = "○"; kpz2Heating = "○"; kpz2Temp = ""

            // Насосная пожаротушения
            firePumpValve = "○"; firePumpHeating = "○"; firePumpTemp = ""

            // Мастерская по ремонту ВВ
            workshopHeating = "○"; workshopTemp = ""

            // Артскважина
            artWellHeating = "○"

            // Здание артезианской скважины
            artesianWellHeating = "○"

            // Помещение 1 (2) АБ
            roomAbHeating = "○"; roomAbTemp = ""

            // Помещение п/этажа №1,2,3
            basementHeating = "○"; basementTemp = ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeScreen()
    }
}

