package com.example.ps_inspection

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExportService(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToExcel(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ): Uri? {
        return try {
            val inputStream: InputStream = context.assets.open("blanks_template.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            fillDataToTemplate(sheet, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            saveWorkbookFromTemplate(workbook, inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fillDataToTemplate(
        sheet: Sheet,
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Заполняем даты (ячейки B1 и O1)
        setCellValue(sheet, 0, 1, currentDate) // B1
        setCellValue(sheet, 0, 14, currentDate) // O1

        // ОРУ-35 кВ данные - ИСПРАВЛЕННЫЕ ИНДЕКСЫ!

        // В-35 2ТСН (строка 6 в Excel = индекс 5)
        setCellValue(sheet, 5, 2, oru35Data.v352tsnA) // C6 - Уровень продувки ф.А
        setCellValue(sheet, 5, 3, oru35Data.v352tsnB) // D6 - Уровень продувки ф.В
        setCellValue(sheet, 5, 4, oru35Data.v352tsnC) // E6 - Уровень продувки ф.С

        // В-35 3ТСН (строка 7 в Excel = индекс 6)
        setCellValue(sheet, 6, 2, oru35Data.v353tsnA) // C7
        setCellValue(sheet, 6, 3, oru35Data.v353tsnB) // D7
        setCellValue(sheet, 6, 4, oru35Data.v353tsnC) // E7

        // ТСНы - Уровень масла (правильно!)
        setCellValue(sheet, 4, 6, oru35Data.tsn2) // G5 - 2ТСН
        setCellValue(sheet, 5, 6, oru35Data.tsn3) // G6 - 3ТСН
        setCellValue(sheet, 6, 6, oru35Data.tsn4) // G7 - 4ТСН

        // ТТ-35 2ТСН (строка 5 в Excel = индекс 4)
        setCellValue(sheet, 4, 8, oru35Data.tt352tsnA) // I5 - ф.А
        setCellValue(sheet, 4, 9, oru35Data.tt352tsnB) // J5 - ф.В
        setCellValue(sheet, 4, 10, oru35Data.tt352tsnC) // K5 - ф.С

        // ТТ-35 3ТСН (строка 6 в Excel = индекс 5)
        setCellValue(sheet, 5, 8, oru35Data.tt353tsnA) // I6 - ф.А
        setCellValue(sheet, 5, 9, oru35Data.tt353tsnB) // J6 - ф.В
        setCellValue(sheet, 5, 10, oru35Data.tt353tsnC) // K6 - ф.С

        // ТН-35 (строка 7 в Excel = индекс 6)
        setCellValue(sheet, 6, 8, oru35Data.tn352atg) // I7 - ТН-35 2АТГ
        setCellValue(sheet, 6, 10, oru35Data.tn353atg) // J7 - ТН-35 3АТГ

        //АТГ, Реактор
        //2АТГ ф.С
        setCellValue(sheet, 10, 1, atgData.atg2_c_oil_tank)
        setCellValue(sheet, 10, 2, atgData.atg2_c_oil_rpn)
        setCellValue(sheet, 10, 3, atgData.atg2_c_pressure_500)
        setCellValue(sheet, 10, 4, atgData.atg2_c_pressure_220)
        setCellValue(sheet, 10, 5, atgData.atg2_c_temp_ts1)
        setCellValue(sheet, 10, 6, atgData.atg2_c_temp_ts2)
        setCellValue(sheet, 10, 7, atgData.atg2_c_pump_group1)
        setCellValue(sheet, 10, 8, atgData.atg2_c_pump_group2)
        setCellValue(sheet, 10, 9, atgData.atg2_c_pump_group3)
        setCellValue(sheet, 10, 10, atgData.atg2_c_pump_group4)

        //2АТГ ф.В
        setCellValue(sheet, 11, 1, atgData.atg2_b_oil_tank)
        setCellValue(sheet, 11, 2, atgData.atg2_b_oil_rpn)
        setCellValue(sheet, 11, 3, atgData.atg2_b_pressure_500)
        setCellValue(sheet, 11, 4, atgData.atg2_b_pressure_220)
        setCellValue(sheet, 11, 5, atgData.atg2_b_temp_ts1)
        setCellValue(sheet, 11, 6, atgData.atg2_b_temp_ts2)
        setCellValue(sheet, 11, 7, atgData.atg2_b_pump_group1)
        setCellValue(sheet, 11, 8, atgData.atg2_b_pump_group2)
        setCellValue(sheet, 11, 9, atgData.atg2_b_pump_group3)
        setCellValue(sheet, 11, 10, atgData.atg2_b_pump_group4)

        //2АТГ ф.А
        setCellValue(sheet, 12, 1, atgData.atg2_a_oil_tank)
        setCellValue(sheet, 12, 2, atgData.atg2_a_oil_rpn)
        setCellValue(sheet, 12, 3, atgData.atg2_a_pressure_500)
        setCellValue(sheet, 12, 4, atgData.atg2_a_pressure_220)
        setCellValue(sheet, 12, 5, atgData.atg2_a_temp_ts1)
        setCellValue(sheet, 12, 6, atgData.atg2_a_temp_ts2)
        setCellValue(sheet, 12, 7, atgData.atg2_a_pump_group1)
        setCellValue(sheet, 12, 8, atgData.atg2_a_pump_group2)
        setCellValue(sheet, 12, 9, atgData.atg2_a_pump_group3)
        setCellValue(sheet, 12, 10, atgData.atg2_a_pump_group4)

        //АТГ резервная фаза
        setCellValue(sheet, 13, 1, atgData.atg_reserve_oil_tank)
        setCellValue(sheet, 13, 2, atgData.atg_reserve_oil_rpn)
        setCellValue(sheet, 13, 3, atgData.atg_reserve_pressure_500)
        setCellValue(sheet, 13, 4, atgData.atg_reserve_pressure_220)
        setCellValue(sheet, 13, 5, atgData.atg_reserve_temp_ts1)
        setCellValue(sheet, 13, 6, atgData.atg_reserve_temp_ts2)
        setCellValue(sheet, 13, 7, atgData.atg_reserve_pump_group1)
        setCellValue(sheet, 13, 8, atgData.atg_reserve_pump_group2)
        setCellValue(sheet, 13, 9, atgData.atg_reserve_pump_group3)
        setCellValue(sheet, 13, 10, atgData.atg_reserve_pump_group4)

        //3АТГ ф.С
        setCellValue(sheet, 14, 1, atgData.atg3_c_oil_tank)
        setCellValue(sheet, 14, 2, atgData.atg3_c_oil_rpn)
        setCellValue(sheet, 14, 3, atgData.atg3_c_pressure_500)
        setCellValue(sheet, 14, 4, atgData.atg3_c_pressure_220)
        setCellValue(sheet, 14, 5, atgData.atg3_c_temp_ts1)
        setCellValue(sheet, 14, 6, atgData.atg3_c_temp_ts2)
        setCellValue(sheet, 14, 7, atgData.atg3_c_pump_group1)
        setCellValue(sheet, 14, 8, atgData.atg3_c_pump_group2)
        setCellValue(sheet, 14, 9, atgData.atg3_c_pump_group3)
        setCellValue(sheet, 14, 10, atgData.atg3_c_pump_group4)

        //3АТГ ф.В
        setCellValue(sheet, 15, 1, atgData.atg3_b_oil_tank)
        setCellValue(sheet, 15, 2, atgData.atg3_b_oil_rpn)
        setCellValue(sheet, 15, 3, atgData.atg3_b_pressure_500)
        setCellValue(sheet, 15, 4, atgData.atg3_b_pressure_220)
        setCellValue(sheet, 15, 5, atgData.atg3_b_temp_ts1)
        setCellValue(sheet, 15, 6, atgData.atg3_b_temp_ts2)
        setCellValue(sheet, 15, 7, atgData.atg3_b_pump_group1)
        setCellValue(sheet, 15, 8, atgData.atg3_b_pump_group2)
        setCellValue(sheet, 15, 9, atgData.atg3_b_pump_group3)
        setCellValue(sheet, 15, 10, atgData.atg3_b_pump_group4)

        //3АТГ ф.А
        setCellValue(sheet, 16, 1, atgData.atg3_a_oil_tank)
        setCellValue(sheet, 16, 2, atgData.atg3_a_oil_rpn)
        setCellValue(sheet, 16, 3, atgData.atg3_a_pressure_500)
        setCellValue(sheet, 16, 4, atgData.atg3_a_pressure_220)
        setCellValue(sheet, 16, 5, atgData.atg3_a_temp_ts1)
        setCellValue(sheet, 16, 6, atgData.atg3_a_temp_ts2)
        setCellValue(sheet, 16, 7, atgData.atg3_a_pump_group1)
        setCellValue(sheet, 16, 8, atgData.atg3_a_pump_group2)
        setCellValue(sheet, 16, 9, atgData.atg3_a_pump_group3)
        setCellValue(sheet, 16, 10, atgData.atg3_a_pump_group4)

        //Р-500 2С ф.С
        setCellValue(sheet, 18, 1, atgData.reactor_c_oil_tank)
        setCellValue(sheet, 18, 3, atgData.reactor_c_pressure_500)
        setCellValue(sheet, 18, 5, atgData.reactor_c_temp_ts)
        setCellValue(sheet, 18, 7, atgData.reactor_c_pump_group1)
        setCellValue(sheet, 18, 8, atgData.reactor_c_pump_group2)
        setCellValue(sheet, 18, 9, atgData.reactor_c_pump_group3)
        setCellValue(sheet, 18, 10, atgData.reactor_c_tt_neutral)

        //Р-500 2С ф.В
        setCellValue(sheet, 19, 1, atgData.reactor_b_oil_tank)
        setCellValue(sheet, 19, 3, atgData.reactor_b_pressure_500)
        setCellValue(sheet, 19, 5, atgData.reactor_b_temp_ts)
        setCellValue(sheet, 19, 7, atgData.reactor_b_pump_group1)
        setCellValue(sheet, 19, 8, atgData.reactor_b_pump_group2)
        setCellValue(sheet, 19, 9, atgData.reactor_b_pump_group3)
        setCellValue(sheet, 19, 10, atgData.reactor_b_tt_neutral)

        //Р-500 2С ф.А
        setCellValue(sheet, 20, 1, atgData.reactor_a_oil_tank)
        setCellValue(sheet, 20, 3, atgData.reactor_a_pressure_500)
        setCellValue(sheet, 20, 5, atgData.reactor_a_temp_ts)
        setCellValue(sheet, 20, 7, atgData.reactor_a_pump_group1)
        setCellValue(sheet, 20, 8, atgData.reactor_a_pump_group2)
        setCellValue(sheet, 20, 9, atgData.reactor_a_pump_group3)
        setCellValue(sheet, 20, 10, atgData.reactor_a_tt_neutral)

        //ОРУ-500
        //В-500 Р-500 2С
        setCellValue(sheet, 24, 2, oru500Data.purgingR5002sA1)
        setCellValue(sheet, 24, 3, oru500Data.purgingR5002sB1)
        setCellValue(sheet, 24, 4, oru500Data.purgingR5002sC1)
        setCellValue(sheet, 25, 2, oru500Data.purgingR5002sA2)
        setCellValue(sheet, 25, 3, oru500Data.purgingR5002sB2)
        setCellValue(sheet, 25, 4, oru500Data.purgingR5002sC2)

        //В-500 ВШТ-31
        setCellValue(sheet, 27, 2, oru500Data.gasPressureVsht31A)
        setCellValue(sheet, 27, 3, oru500Data.gasPressureVsht31B)
        setCellValue(sheet, 27, 4, oru500Data.gasPressureVsht31C)

        //ТТ-500 ВШТ-31
        setCellValue(sheet, 27, 6, oru500Data.oilTtVsht31A)
        setCellValue(sheet, 27, 7, oru500Data.oilTtVsht31B)
        setCellValue(sheet, 27, 8, oru500Data.oilTtVsht31C)

        //В-500 ВЛТ-30
        setCellValue(sheet, 28, 2, oru500Data.gasPressureVlt30A)
        setCellValue(sheet, 28, 3, oru500Data.gasPressureVlt30B)
        setCellValue(sheet, 28, 4, oru500Data.gasPressureVlt30C)

        //ТТ-500 ВЛТ-30
        setCellValue(sheet, 28, 6, oru500Data.oilTtVlt30A)
        setCellValue(sheet, 28, 7, oru500Data.oilTtVlt30B)
        setCellValue(sheet, 28, 8, oru500Data.oilTtVlt30C)

        //Трачуки
        //ТТ-500 Трачуковская
        setCellValue(sheet, 42, 2, oru500Data.oilTtTrachukovskayaA)
        setCellValue(sheet, 42, 3, oru500Data.oilTtTrachukovskayaB)
        setCellValue(sheet, 42, 4, oru500Data.oilTtTrachukovskayaC)

        //2ТН-500 Трачуковская
        setCellValue(sheet, 50, 8, oru500Data.oil2tnTrachukovskayaA)
        setCellValue(sheet, 50, 9, oru500Data.oil2tnTrachukovskayaB)
        setCellValue(sheet, 50, 10, oru500Data.oil2tnTrachukovskayaC)

        //1ТН-500 Трачуковская
        setCellValue(sheet, 50, 2, oru500Data.oil1tnTrachukovskayaA)
        setCellValue(sheet, 50, 3, oru500Data.oil1tnTrachukovskayaB)
        setCellValue(sheet, 50, 4, oru500Data.oil1tnTrachukovskayaC)

        //В-500 ВШЛ-32
        setCellValue(sheet, 30, 2, oru500Data.purgingVshl32A1)
        setCellValue(sheet, 30, 3, oru500Data.purgingVshl32B1)
        setCellValue(sheet, 30, 4, oru500Data.purgingVshl32C1)
        setCellValue(sheet, 31, 2, oru500Data.purgingVshl32A2)
        setCellValue(sheet, 31, 3, oru500Data.purgingVshl32B2)
        setCellValue(sheet, 31, 4, oru500Data.purgingVshl32C2)

        //ТТ-500 ВШЛ-32
        setCellValue(sheet, 30, 6, oru500Data.oilTtVshl32A)
        setCellValue(sheet, 30, 7, oru500Data.oilTtVshl32B)
        setCellValue(sheet, 30, 8, oru500Data.oilTtVshl32C)

        //В-500 ВШЛ-21
        setCellValue(sheet, 32, 2, oru500Data.purgingVshl21A1)
        setCellValue(sheet, 32, 3, oru500Data.purgingVshl21B1)
        setCellValue(sheet, 32, 4, oru500Data.purgingVshl21C1)
        setCellValue(sheet, 33, 2, oru500Data.purgingVshl21A2)
        setCellValue(sheet, 33, 3, oru500Data.purgingVshl21B2)
        setCellValue(sheet, 33, 4, oru500Data.purgingVshl21C2)

        //ТТ-500 ВШЛ-21
        setCellValue(sheet, 32, 6, oru500Data.oilTtVshl21A)
        setCellValue(sheet, 32, 7, oru500Data.oilTtVshl21B)
        setCellValue(sheet, 32, 8, oru500Data.oilTtVshl21C)

        //В-500 ВШТ-22
        setCellValue(sheet, 34, 2, oru500Data.purgingVsht22A1)
        setCellValue(sheet, 34, 3, oru500Data.purgingVsht22B1)
        setCellValue(sheet, 34, 4, oru500Data.purgingVsht22C1)
        setCellValue(sheet, 35, 2, oru500Data.purgingVsht22A2)
        setCellValue(sheet, 35, 3, oru500Data.purgingVsht22B2)
        setCellValue(sheet, 35, 4, oru500Data.purgingVsht22C2)

        //ТТ-500 ВШТ-22
        setCellValue(sheet, 34, 6, oru500Data.oilTtVsht22A)
        setCellValue(sheet, 34, 7, oru500Data.oilTtVsht22B)
        setCellValue(sheet, 34, 8, oru500Data.oilTtVsht22C)

        //В-500 ВЛТ-20
        setCellValue(sheet, 36, 2, oru500Data.purgingVlt20A1)
        setCellValue(sheet, 36, 3, oru500Data.purgingVlt20B1)
        setCellValue(sheet, 36, 4, oru500Data.purgingVlt20C1)
        setCellValue(sheet, 37, 2, oru500Data.purgingVlt20A2)
        setCellValue(sheet, 37, 3, oru500Data.purgingVlt20B2)
        setCellValue(sheet, 37, 4, oru500Data.purgingVlt20C2)

        //ТТ-500 ВЛТ-20
        setCellValue(sheet, 36, 6, oru500Data.oilTtVlt20A)
        setCellValue(sheet, 36, 7, oru500Data.oilTtVlt20B)
        setCellValue(sheet, 36, 8, oru500Data.oilTtVlt20C)

        //В-500 ВШТ-11
        setCellValue(sheet, 38, 2, oru500Data.purgingVsht11A1)
        setCellValue(sheet, 38, 3, oru500Data.purgingVsht11B1)
        setCellValue(sheet, 38, 4, oru500Data.purgingVsht11C1)
        setCellValue(sheet, 39, 2, oru500Data.purgingVsht11A2)
        setCellValue(sheet, 39, 3, oru500Data.purgingVsht11B2)
        setCellValue(sheet, 39, 4, oru500Data.purgingVsht11C2)

        //ТТ-500 ВШТ-11
        setCellValue(sheet, 38, 6, oru500Data.oilTtVsht11A)
        setCellValue(sheet, 38, 7, oru500Data.oilTtVsht11B)
        setCellValue(sheet, 38, 8, oru500Data.oilTtVsht11C)

        //В-500 ВШЛ-12
        setCellValue(sheet, 40, 2, oru500Data.purgingVshl12A1)
        setCellValue(sheet, 40, 3, oru500Data.purgingVshl12B1)
        setCellValue(sheet, 40, 4, oru500Data.purgingVshl12C1)
        setCellValue(sheet, 41, 2, oru500Data.purgingVshl12A2)
        setCellValue(sheet, 41, 3, oru500Data.purgingVshl12B2)
        setCellValue(sheet, 41, 4, oru500Data.purgingVshl12C2)

        //ТТ-500 ВШЛ-12
        setCellValue(sheet, 40, 6, oru500Data.oilTtVshl12A)
        setCellValue(sheet, 40, 7, oru500Data.oilTtVshl12B)
        setCellValue(sheet, 40, 8, oru500Data.oilTtVshl12C)

        //ТН-500 Белозёрная
        setCellValue(sheet, 56, 2, oru500Data.oil2tnBelozernayaA)
        setCellValue(sheet, 56, 3, oru500Data.oil2tnBelozernayaB)
        setCellValue(sheet, 56, 4, oru500Data.oil2tnBelozernayaC)

        //1ТН-500
        setCellValue(sheet, 46, 2, oru500Data.tn1500Cascade1A)
        setCellValue(sheet, 46, 3, oru500Data.tn1500Cascade1B)
        setCellValue(sheet, 46, 4, oru500Data.tn1500Cascade1C)
        setCellValue(sheet, 47, 2, oru500Data.tn1500Cascade2A)
        setCellValue(sheet, 47, 3, oru500Data.tn1500Cascade2B)
        setCellValue(sheet, 47, 4, oru500Data.tn1500Cascade2C)
        setCellValue(sheet, 48, 2, oru500Data.tn1500Cascade3A)
        setCellValue(sheet, 48, 3, oru500Data.tn1500Cascade3B)
        setCellValue(sheet, 48, 4, oru500Data.tn1500Cascade3C)
        setCellValue(sheet, 49, 2, oru500Data.tn1500Cascade4A)
        setCellValue(sheet, 49, 3, oru500Data.tn1500Cascade4B)
        setCellValue(sheet, 49, 4, oru500Data.tn1500Cascade4C)

        //2ТН-500
        setCellValue(sheet, 46, 8, oru500Data.tn2500Cascade1A)
        setCellValue(sheet, 46, 9, oru500Data.tn2500Cascade1B)
        setCellValue(sheet, 46, 10, oru500Data.tn2500Cascade1C)
        setCellValue(sheet, 47, 8, oru500Data.tn2500Cascade2A)
        setCellValue(sheet, 47, 9, oru500Data.tn2500Cascade2B)
        setCellValue(sheet, 47, 10, oru500Data.tn2500Cascade2C)
        setCellValue(sheet, 48, 8, oru500Data.tn2500Cascade3A)
        setCellValue(sheet, 48, 9, oru500Data.tn2500Cascade3B)
        setCellValue(sheet, 48, 10, oru500Data.tn2500Cascade3C)
        setCellValue(sheet, 49, 8, oru500Data.tn2500Cascade4A)
        setCellValue(sheet, 49, 9, oru500Data.tn2500Cascade4B)
        setCellValue(sheet, 49, 10, oru500Data.tn2500Cascade4C)

        //ТН-500 СГРЭС-1
        setCellValue(sheet, 52, 2, oru500Data.tn500Sgres1Cascade1A)
        setCellValue(sheet, 52, 3, oru500Data.tn500Sgres1Cascade1B)
        setCellValue(sheet, 52, 4, oru500Data.tn500Sgres1Cascade1C)
        setCellValue(sheet, 53, 2, oru500Data.tn500Sgres1Cascade2A)
        setCellValue(sheet, 53, 3, oru500Data.tn500Sgres1Cascade2B)
        setCellValue(sheet, 53, 4, oru500Data.tn500Sgres1Cascade2C)
        setCellValue(sheet, 54, 2, oru500Data.tn500Sgres1Cascade3A)
        setCellValue(sheet, 54, 3, oru500Data.tn500Sgres1Cascade3B)
        setCellValue(sheet, 54, 4, oru500Data.tn500Sgres1Cascade3C)
        setCellValue(sheet, 55, 2, oru500Data.tn500Sgres1Cascade4A)
        setCellValue(sheet, 55, 3, oru500Data.tn500Sgres1Cascade4B)
        setCellValue(sheet, 55, 4, oru500Data.tn500Sgres1Cascade4C)

        //ОРУ-220
        //В-220 Мирная
        setCellValue(sheet, 6, 15, oru220Data.purgingMirnayaA)
        setCellValue(sheet, 6, 16, oru220Data.purgingMirnayaB)
        setCellValue(sheet, 6, 17, oru220Data.purgingMirnayaC)

        //ТТ-220 Мирная
        setCellValue(sheet, 6, 19, oru220Data.oilMirnayaA)
        setCellValue(sheet, 6, 20, oru220Data.oilMirnayaB)
        setCellValue(sheet, 6, 21, oru220Data.oilMirnayaC)

        //В-220 Топаз
        setCellValue(sheet, 7, 15, oru220Data.purgingTopazA)
        setCellValue(sheet, 7, 16, oru220Data.purgingTopazB)
        setCellValue(sheet, 7, 17, oru220Data.purgingTopazC)

        //ТТ-220 Топаз
        setCellValue(sheet, 7, 19, oru220Data.oilTopazA)
        setCellValue(sheet, 7, 20, oru220Data.oilTopazB)
        setCellValue(sheet, 7, 21, oru220Data.oilTopazC)

        //ОВ-220
        setCellValue(sheet, 8, 15, oru220Data.purgingOvA)
        setCellValue(sheet, 8, 16, oru220Data.purgingOvB)
        setCellValue(sheet, 8, 17, oru220Data.purgingOvC)

        //ТТ-220 ОВ
        setCellValue(sheet, 8, 19, oru220Data.oilOvA)
        setCellValue(sheet, 8, 20, oru220Data.oilOvB)
        setCellValue(sheet, 8, 21, oru220Data.oilOvC)

        //ТН-220 ОСШ
        setCellValue(sheet, 19, 23, oru220Data.oilOvA)
        setCellValue(sheet, 20, 23, oru220Data.oilOvA)

        //В-220 2АТГ
        setCellValue(sheet, 9, 15, oru220Data.purgingV2atgA)
        setCellValue(sheet, 9, 16, oru220Data.purgingV2atgB)
        setCellValue(sheet, 9, 17, oru220Data.purgingV2atgC)

        //ТТ-220 2АТГ
        setCellValue(sheet, 9, 19, oru220Data.oilTt2atgA)
        setCellValue(sheet, 9, 20, oru220Data.oilTt2atgB)
        setCellValue(sheet, 9, 21, oru220Data.oilTt2atgC)

        //ШСВ-220
        setCellValue(sheet, 10, 15, oru220Data.purgingShSV220A)
        setCellValue(sheet, 10, 16, oru220Data.purgingShSV220B)
        setCellValue(sheet, 10, 17, oru220Data.purgingShSV220C)

        //ТТ-220 ШСВ
        setCellValue(sheet, 10, 19, oru220Data.oilTtShSV220A)
        setCellValue(sheet, 10, 20, oru220Data.oilTtShSV220B)
        setCellValue(sheet, 10, 21, oru220Data.oilTtShSV220C)

        //В-220 3АТГ
        setCellValue(sheet, 11, 15, oru220Data.purgingV3atgA)
        setCellValue(sheet, 11, 16, oru220Data.purgingV3atgB)
        setCellValue(sheet, 11, 17, oru220Data.purgingV3atgC)

        //ТТ-220 3АТГ
        setCellValue(sheet, 11, 19, oru220Data.oilTt3atgA)
        setCellValue(sheet, 11, 20, oru220Data.oilTt3atgB)
        setCellValue(sheet, 11, 21, oru220Data.oilTt3atgC)

        //В-220 Орбита
        setCellValue(sheet, 12, 15, oru220Data.purgingOrbitaA)
        setCellValue(sheet, 12, 16, oru220Data.purgingOrbitaB)
        setCellValue(sheet, 12, 17, oru220Data.purgingOrbitaC)

        //ТТ-220 Орбита
        setCellValue(sheet, 12, 19, oru220Data.oilOrbitaA)
        setCellValue(sheet, 12, 20, oru220Data.oilOrbitaB)
        setCellValue(sheet, 12, 21, oru220Data.oilOrbitaC)

        //В-220 Факел
        setCellValue(sheet, 13, 15, oru220Data.purgingFakelA)
        setCellValue(sheet, 13, 16, oru220Data.purgingFakelB)
        setCellValue(sheet, 13, 17, oru220Data.purgingFakelC)

        //ТТ-220 Факел
        setCellValue(sheet, 13, 19, oru220Data.oilFakelA)
        setCellValue(sheet, 13, 20, oru220Data.oilFakelB)
        setCellValue(sheet, 13, 21, oru220Data.oilFakelC)

        //В-220 Комета-2
        setCellValue(sheet, 14, 15, oru220Data.purgingCometa2A)
        setCellValue(sheet, 14, 16, oru220Data.purgingCometa2B)
        setCellValue(sheet, 14, 17, oru220Data.purgingCometa2C)

        //ТТ-220 Комета-2
        setCellValue(sheet, 14, 19, oru220Data.oilCometa2A)
        setCellValue(sheet, 14, 20, oru220Data.oilCometa2B)
        setCellValue(sheet, 14, 21, oru220Data.oilCometa2C)

        //В-220 Комета-1
        setCellValue(sheet, 15, 15, oru220Data.purgingCometa1A)
        setCellValue(sheet, 15, 16, oru220Data.purgingCometa1B)
        setCellValue(sheet, 15, 17, oru220Data.purgingCometa1C)

        //ТТ-220 Комета-1
        setCellValue(sheet, 15, 19, oru220Data.oilCometa1A)
        setCellValue(sheet, 15, 20, oru220Data.oilCometa1B)
        setCellValue(sheet, 15, 21, oru220Data.oilCometa1C)

        //1ТН-220
        setCellValue(sheet, 19, 15, oru220Data.tn1UpperA)
        setCellValue(sheet, 19, 16, oru220Data.tn1UpperB)
        setCellValue(sheet, 19, 17, oru220Data.tn1UpperC)
        setCellValue(sheet, 20, 15, oru220Data.tn1LowerA)
        setCellValue(sheet, 20, 16, oru220Data.tn1LowerB)
        setCellValue(sheet, 20, 17, oru220Data.tn1LowerC)

        //2ТН-220
        setCellValue(sheet, 19, 20, oru220Data.tn2UpperA)
        setCellValue(sheet, 19, 21, oru220Data.tn2UpperB)
        setCellValue(sheet, 19, 22, oru220Data.tn2UpperC)
        setCellValue(sheet, 20, 20, oru220Data.tn2LowerA)
        setCellValue(sheet, 20, 21, oru220Data.tn2LowerB)
        setCellValue(sheet, 20, 22, oru220Data.tn2LowerC)

        //Здания/помещения
        //Компрессорная №1
        setCellValue(sheet, 23, 15, buildingsData.compressor1Valve)
        setCellValue(sheet, 23, 18, buildingsData.compressor1Heating)
        setCellValue(sheet, 23, 21, buildingsData.compressor1Temp)

        // Баллонная №1
        setCellValue(sheet, 24, 15, buildingsData.ballroom1Valve)
        setCellValue(sheet, 24, 18, buildingsData.ballroom1Heating)
        setCellValue(sheet, 24, 21, buildingsData.ballroom1Temp)

        //Компрессорная №2
        setCellValue(sheet, 25, 15, buildingsData.compressor2Valve)
        setCellValue(sheet, 25, 18, buildingsData.compressor2Heating)
        setCellValue(sheet, 25, 21, buildingsData.compressor2Temp)

        // Баллонная №2
        setCellValue(sheet, 26, 15, buildingsData.ballroom2Valve)
        setCellValue(sheet, 26, 18, buildingsData.ballroom2Heating)
        setCellValue(sheet, 26, 21, buildingsData.ballroom2Temp)

        //КПЗ ОПУ
        setCellValue(sheet, 27, 15, buildingsData.kpzOpuValve)
        setCellValue(sheet, 27, 18, buildingsData.kpzOpuHeating)
        setCellValue(sheet, 27, 21, buildingsData.kpzOpuTemp)

        //КПЗ-2
        setCellValue(sheet, 28, 15, buildingsData.kpz2Valve)
        setCellValue(sheet, 28, 18, buildingsData.kpz2Heating)
        setCellValue(sheet, 28, 21, buildingsData.kpz2Temp)

        //Насосная
        setCellValue(sheet, 29, 15, buildingsData.firePumpValve)
        setCellValue(sheet, 29, 18, buildingsData.firePumpHeating)
        setCellValue(sheet, 29, 21, buildingsData.firePumpTemp)

        //Мастерская по ремонту ВВ
        setCellValue(sheet, 30, 18, buildingsData.workshopHeating)
        setCellValue(sheet, 30, 21, buildingsData.workshopTemp)

        //Артскважина
        setCellValue(sheet, 31, 18, buildingsData.artWellHeating)

        //Здание артезиантской скважины
        setCellValue(sheet, 32, 18, buildingsData.artesianWellHeating)

        //АБ №1,2
        setCellValue(sheet, 33, 18, buildingsData.roomAbHeating)
        setCellValue(sheet, 33, 21, buildingsData.roomAbTemp)

        //Помещение п/этажа №1,2,3
        setCellValue(sheet, 33, 18, buildingsData.basementHeating)
        setCellValue(sheet, 33, 21, buildingsData.basementTemp)


        // ... остальное заполнение данных как было ранее
        // ВСЁ ФОРМАТИРОВАНИЕ, РАЗМЕРЫ ЯЧЕЕК И НЕИЗМЕНЯЕМЫЕ ДАННЫЕ СОХРАНЯТСЯ!
    }

    private fun setCellValue(sheet: Sheet, rowNum: Int, colNum: Int, value: String) {
        println("🔍 Запись в [Строка:${rowNum+1}, Колонка:${colNum+1}] значение: '$value'")

        try {
            val row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
            val cell = row.getCell(colNum) ?: row.createCell(colNum)

            // Проверяем текущее значение в ячейке
            val currentValue = cell.stringCellValue ?: "пусто"
            println("📝 Текущее значение в ячейке: '$currentValue'")

            cell.setCellValue(value)
            println("✅ Успешно записано!")

            // Проверяем записанное значение
            val writtenValue = cell.stringCellValue ?: "пусто"
            println("📖 Проверка записи: '$writtenValue'")

        } catch (e: Exception) {
            println("❌ ОШИБКА: ${e.message}")
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveWorkbookFromTemplate(workbook: Workbook, inputStream: InputStream): Uri? {
        inputStream.close()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
        val fileName = "Осмотр_${dateFormat.format(Date())}.xlsx"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        return try {
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                it
            }
        } catch (e: Exception) {
            e.printStackTrace()
            workbook.close()
            null
        }
    }
}