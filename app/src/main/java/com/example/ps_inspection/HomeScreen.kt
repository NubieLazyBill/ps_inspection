package com.example.ps_inspection

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExportService(private val context: Context) {

    fun exportToExcel(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data
    ): Uri? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Бланки по карте №1,2,3,4,5,6")

            fillHeaderData(sheet, workbook)
            fillORU35Data(sheet, oru35Data)
            fillATGData(sheet, atgData)
            fillORU500Data(sheet, oru500Data)
            fillORU220Data(sheet, oru220Data)

            autoSizeColumns(sheet)
            saveWorkbook(workbook)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fillHeaderData(sheet: Sheet, workbook: Workbook) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        setCellValue(sheet, 0, 0, "Дата:") // A1
        setCellValue(sheet, 0, 1, currentDate) // B1
        setCellValue(sheet, 0, 13, "Дата:") // N1
        setCellValue(sheet, 0, 14, currentDate) // O1
    }

    private fun fillORU35Data(sheet: Sheet, data: InspectionORU35Data) {
        // В-35 2ТСН (строка 6)
        setCellValue(sheet, 5, 2, data.v352tsnA) // C6 - продувка ф.А
        setCellValue(sheet, 5, 3, data.v352tsnB) // D6 - продувка ф.В
        setCellValue(sheet, 5, 4, data.v352tsnC) // E6 - продувка ф.С
        setCellValue(sheet, 5, 5, data.tsn2) // F6 - уровень масла 2ТСН

        // В-35 3ТСН (строка 7)
        setCellValue(sheet, 6, 2, data.v353tsnA) // C7
        setCellValue(sheet, 6, 3, data.v353tsnB) // D7
        setCellValue(sheet, 6, 4, data.v353tsnC) // E7
        setCellValue(sheet, 6, 5, data.tsn3) // F7 - уровень масла 3ТСН
        setCellValue(sheet, 6, 6, data.tsn4) // G7 - уровень масла 4ТСН

        // ТТ-35 (строка 6-7)
        setCellValue(sheet, 5, 8, data.tt352tsnA) // I6 - ТТ-35 2ТСН ф.А
        setCellValue(sheet, 5, 9, data.tt352tsnB) // J6 - ТТ-35 2ТСН ф.В
        setCellValue(sheet, 5, 10, data.tt352tsnC) // K6 - ТТ-35 2ТСН ф.С

        setCellValue(sheet, 6, 8, data.tt353tsnA) // I7 - ТТ-35 3ТСН ф.А
        setCellValue(sheet, 6, 9, data.tt353tsnB) // J7 - ТТ-35 3ТСН ф.В
        setCellValue(sheet, 6, 10, data.tt353tsnC) // K7 - ТТ-35 3ТСН ф.С

        // ТН-35 (строка 7)
        setCellValue(sheet, 6, 11, data.tn352atg) // L7 - ТН-35 2АТГ
        setCellValue(sheet, 6, 12, data.tn353atg) // M7 - ТН-35 3АТГ
    }

    private fun fillATGData(sheet: Sheet, data: InspectionATGData) {
        // 2 АТГ ф.С (строка 14)
        setCellValue(sheet, 13, 1, data.atg2_c_oil_tank) // B14 - Бак
        setCellValue(sheet, 13, 2, data.atg2_c_oil_rpn) // C14 - РПН
        setCellValue(sheet, 13, 3, data.atg2_c_pressure_500) // D14 - Давление 500
        setCellValue(sheet, 13, 4, data.atg2_c_pressure_220) // E14 - Давление 220
        setCellValue(sheet, 13, 5, data.atg2_c_temp_ts1) // F14 - ТС1
        setCellValue(sheet, 13, 6, data.atg2_c_temp_ts2) // G14 - ТС2

        // 2 АТГ ф.В (строка 15)
        setCellValue(sheet, 14, 1, data.atg2_b_oil_tank) // B15
        setCellValue(sheet, 14, 2, data.atg2_b_oil_rpn) // C15
        setCellValue(sheet, 14, 3, data.atg2_b_pressure_500) // D15
        setCellValue(sheet, 14, 4, data.atg2_b_pressure_220) // E15
        setCellValue(sheet, 14, 5, data.atg2_b_temp_ts1) // F15
        setCellValue(sheet, 14, 6, data.atg2_b_temp_ts2) // G15

        // 2 АТГ ф.А (строка 16)
        setCellValue(sheet, 15, 1, data.atg2_a_oil_tank) // B16
        setCellValue(sheet, 15, 2, data.atg2_a_oil_rpn) // C16
        setCellValue(sheet, 15, 5, data.atg2_a_temp_ts1) // F16
        setCellValue(sheet, 15, 6, data.atg2_a_temp_ts2) // G16

        // АТГ резервная фаза (строка 17)
        setCellValue(sheet, 16, 1, data.atg_reserve_oil_tank) // B17
        setCellValue(sheet, 16, 2, data.atg_reserve_oil_rpn) // C17
        setCellValue(sheet, 16, 3, data.atg_reserve_pressure_500) // D17
        setCellValue(sheet, 16, 4, data.atg_reserve_pressure_220) // E17
        setCellValue(sheet, 16, 5, data.atg_reserve_temp_ts1) // F17
        setCellValue(sheet, 16, 6, data.atg_reserve_temp_ts2) // G17

        // 3 АТГ ф.С (строка 18)
        setCellValue(sheet, 17, 1, data.atg3_c_oil_tank) // B18
        setCellValue(sheet, 17, 2, data.atg3_c_oil_rpn) // C18
        setCellValue(sheet, 17, 3, data.atg3_c_pressure_500) // D18
        setCellValue(sheet, 17, 4, data.atg3_c_pressure_220) // E18
        setCellValue(sheet, 17, 5, data.atg3_c_temp_ts1) // F18
        setCellValue(sheet, 17, 6, data.atg3_c_temp_ts2) // G18

        // 3 АТГ ф.В (строка 19)
        setCellValue(sheet, 18, 1, data.atg3_b_oil_tank) // B19
        setCellValue(sheet, 18, 2, data.atg3_b_oil_rpn) // C19
        setCellValue(sheet, 18, 3, data.atg3_b_pressure_500) // D19
        setCellValue(sheet, 18, 4, data.atg3_b_pressure_220) // E19
        setCellValue(sheet, 18, 5, data.atg3_b_temp_ts1) // F19
        setCellValue(sheet, 18, 6, data.atg3_b_temp_ts2) // G19

        // 3 АТГ ф.А (строка 20)
        setCellValue(sheet, 19, 1, data.atg3_a_oil_tank) // B20
        setCellValue(sheet, 19, 2, data.atg3_a_oil_rpn) // C20
        setCellValue(sheet, 19, 3, data.atg3_a_pressure_500) // D20
        setCellValue(sheet, 19, 4, data.atg3_a_pressure_220) // E20
        setCellValue(sheet, 19, 5, data.atg3_a_temp_ts1) // F20
        setCellValue(sheet, 19, 6, data.atg3_a_temp_ts2) // G20

        // Реакторы Р-500 2С
        setCellValue(sheet, 24, 1, data.reactor_c_oil_tank) // B25 - ф.С
        setCellValue(sheet, 24, 5, data.reactor_c_temp_ts) // F25

        setCellValue(sheet, 25, 1, data.reactor_b_oil_tank) // B26 - ф.В
        setCellValue(sheet, 25, 5, data.reactor_b_temp_ts) // F26

        setCellValue(sheet, 26, 1, data.reactor_a_oil_tank) // B27 - ф.А
        setCellValue(sheet, 26, 5, data.reactor_a_temp_ts) // F27
    }

    private fun fillORU500Data(sheet: Sheet, data: InspectionORU500Data) {
        // В-500 Р-500 2С I эл. (строка 34)
        setCellValue(sheet, 33, 2, data.purgingR5002sA1) // C34
        setCellValue(sheet, 33, 3, data.purgingR5002sB1) // D34
        setCellValue(sheet, 33, 4, data.purgingR5002sC1) // E34

        // В-500 Р-500 2С II эл. (строка 35)
        setCellValue(sheet, 34, 2, data.purgingR5002sA2) // C35
        setCellValue(sheet, 34, 3, data.purgingR5002sB2) // D35
        setCellValue(sheet, 34, 4, data.purgingR5002sC2) // E35

        // Давление элегаза
        setCellValue(sheet, 37, 3, data.gasPressureVsht31A) // D38 - ВШТ-31
        setCellValue(sheet, 38, 3, data.gasPressureVlt30A) // D39 - ВЛТ-30

        // Остальные выключатели 500 кВ...
        setCellValue(sheet, 40, 2, data.purgingVshl32A1) // C41 - ВШЛ-32 I эл.
        setCellValue(sheet, 41, 2, data.purgingVshl32A2) // C42 - ВШЛ-32 II эл.

        setCellValue(sheet, 43, 2, data.purgingVshl21A1) // C44 - ВШЛ-21 I эл.
        setCellValue(sheet, 44, 2, data.purgingVshl21A2) // C45 - ВШЛ-21 II эл.

        setCellValue(sheet, 46, 2, data.purgingVsht22A1) // C47 - ВШТ-22 I эл.
        setCellValue(sheet, 47, 2, data.purgingVsht22A2) // C48 - ВШТ-22 II эл.

        //setCellValue(sheet, 49, 2, data.purgingVlt20A1) // C50 - ВЛТ-20 I эл.
        //setCellValue(sheet, 50, 2, data.purgingVlt20A2) // C51 - ВЛТ-20 II эл.

        setCellValue(sheet, 52, 2, data.purgingVsht11A1) // C53 - ВШТ-11 I эл.
        setCellValue(sheet, 53, 2, data.purgingVsht11A2) // C54 - ВШТ-11 II эл.

        setCellValue(sheet, 55, 2, data.purgingVshl12A1) // C56 - ВШЛ-12 I эл.
        setCellValue(sheet, 56, 2, data.purgingVshl12A2) // C57 - ВШЛ-12 II эл.

        // ТТ-500
        setCellValue(sheet, 58, 6, data.oilTtTrachukovskayaA) // G59 - Трачуковская ф.А
        setCellValue(sheet, 58, 7, data.oilTtTrachukovskayaB) // H59 - ф.В
        setCellValue(sheet, 58, 8, data.oilTtTrachukovskayaC) // I59 - ф.С

        // ТН-500
        setCellValue(sheet, 63, 2, data.tn1500Cascade1A) // C64 - 1ТН-500 каскад 1 ф.А
        setCellValue(sheet, 63, 3, data.tn1500Cascade1B) // D64 - ф.В
        setCellValue(sheet, 63, 4, data.tn1500Cascade1C) // E64 - ф.С

        setCellValue(sheet, 64, 2, data.tn1500Cascade2A) // C65 - каскад 2 ф.А
        setCellValue(sheet, 64, 3, data.tn1500Cascade2B) // D65 - ф.В
        setCellValue(sheet, 64, 4, data.tn1500Cascade2C) // E65 - ф.С
    }

    private fun fillORU220Data(sheet: Sheet, data: InspectionORU220Data) {
        // В-220 Мирная (строка 9)
        setCellValue(sheet, 8, 14, data.purgingMirnayaA) // O9 - продувка ф.А
        setCellValue(sheet, 8, 15, data.purgingMirnayaB) // P9 - ф.В
        setCellValue(sheet, 8, 16, data.purgingMirnayaC) // Q9 - ф.С

        setCellValue(sheet, 8, 18, data.oilMirnayaA) // S9 - масло ф.А
        setCellValue(sheet, 8, 19, data.oilMirnayaB) // T9 - ф.В
        setCellValue(sheet, 8, 20, data.oilMirnayaC) // U9 - ф.С

        // В-220 Топаз (строка 10)
        setCellValue(sheet, 9, 14, data.purgingTopazA) // O10
        setCellValue(sheet, 9, 15, data.purgingTopazB) // P10
        setCellValue(sheet, 9, 16, data.purgingTopazC) // Q10

        setCellValue(sheet, 9, 18, data.oilTopazA) // S10
        setCellValue(sheet, 9, 19, data.oilTopazB) // T10
        setCellValue(sheet, 9, 20, data.oilTopazC) // U10

        // Продолжи для остальных объектов ОРУ-220...
    }

    private fun setCellValue(sheet: Sheet, rowNum: Int, colNum: Int, value: String) {
        val row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
        val cell = row.getCell(colNum) ?: row.createCell(colNum)
        cell.setCellValue(value)
    }

    private fun autoSizeColumns(sheet: Sheet) {
        for (i in 0 until 25) {
            sheet.autoSizeColumn(i)
        }
    }

    private fun saveWorkbook(workbook: Workbook): Uri {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
        val fileName = "Осмотр_${dateFormat.format(Date())}.xlsx"

        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { fos ->
            workbook.write(fos)
        }
        workbook.close()

        return file.toUri()
    }
}