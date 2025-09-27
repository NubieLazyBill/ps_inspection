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
        buildingsData: InspectionBuildingsData
    ): Uri? {
        return try {
            // Создаем новую книгу Excel
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Осмотр оборудования")

            // Заполняем данные согласно твоему шаблону
            fillHeaderData(sheet, workbook)
            fillORU35Data(sheet, oru35Data)
            fillATGData(sheet, atgData)
            fillBuildingsData(sheet, buildingsData)

            // Автоподбор ширины колонок
            autoSizeColumns(sheet)

            // Сохраняем файл
            saveWorkbook(workbook)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fillHeaderData(sheet: Sheet, workbook: Workbook) {
        // Заполняем заголовок с датой
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        setCellValue(sheet, 0, 0, "Дата:") // A1
        setCellValue(sheet, 0, 1, currentDate) // B1
    }

    private fun fillORU35Data(sheet: Sheet, data: InspectionORU35Data) {
        // ОРУ-35 данные
        // В-35 2ТСН - уровень масла (примерно строка 6, колонка E)
        setCellValue(sheet, 5, 4, data.tsn2) // E6 - Уровень масла 2ТСН

        // ТТ-35 2ТСН (фазы A, B, C) - примерно строка 6, колонки I,J,K
        setCellValue(sheet, 5, 8, data.tt352tsnA) // I6 - ф.А
        setCellValue(sheet, 5, 9, data.tt352tsnB) // J6 - ф.В
        setCellValue(sheet, 5, 10, data.tt352tsnC) // K6 - ф.С

        // Продолжи для остальных полей ORU-35...
    }

    private fun fillATGData(sheet: Sheet, data: InspectionATGData) {
        // 2 АТГ ф.С (примерно строка 11)
        setCellValue(sheet, 10, 1, data.atg2_c_oil_tank) // B11 - Бак
        setCellValue(sheet, 10, 2, data.atg2_c_oil_rpn) // C11 - РПН
        setCellValue(sheet, 10, 3, data.atg2_c_pressure_500) // D11 - 500 кВ
        setCellValue(sheet, 10, 4, data.atg2_c_pressure_220) // E11 - 220 кВ
        setCellValue(sheet, 10, 5, data.atg2_c_temp_ts1) // F11 - ТС1
        setCellValue(sheet, 10, 6, data.atg2_c_temp_ts2) // G11 - ТС2
        setCellValue(sheet, 10, 7, data.atg2_c_pump_group1) // H11 - 1 группа
        setCellValue(sheet, 10, 8, data.atg2_c_pump_group2) // I11 - 2 группа
        setCellValue(sheet, 10, 9, data.atg2_c_pump_group3) // J11 - 3 группа
        setCellValue(sheet, 10, 10, data.atg2_c_pump_group4) // K11 - 4 группа

        // 2 АТГ ф.В (строка 12)
        setCellValue(sheet, 11, 1, data.atg2_b_oil_tank) // B12
        setCellValue(sheet, 11, 2, data.atg2_b_oil_rpn) // C12
        // ... и так далее
    }

    private fun fillBuildingsData(sheet: Sheet, data: InspectionBuildingsData) {
        // Здания и помещения (примерно с строки 31)
        // Компрессорная №1
        setCellValue(sheet, 31, 7, data.compressor1Valve) // H32 - Запорная арматура
        setCellValue(sheet, 31, 8, data.compressor1Heating) // I32 - Обогрев
        setCellValue(sheet, 31, 9, data.compressor1Temp) // J32 - Температура

        // Баллоная №1
        setCellValue(sheet, 32, 7, data.ballroom1Valve) // H33
        setCellValue(sheet, 32, 8, data.ballroom1Heating) // I33
        setCellValue(sheet, 32, 9, data.ballroom1Temp) // J33
        // ... и так далее
    }

    private fun setCellValue(sheet: Sheet, rowNum: Int, colNum: Int, value: String) {
        val row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
        val cell = row.getCell(colNum) ?: row.createCell(colNum)
        cell.setCellValue(value)
    }

    private fun autoSizeColumns(sheet: Sheet) {
        for (i in 0 until 20) { // Автоподбор для первых 20 колонок
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