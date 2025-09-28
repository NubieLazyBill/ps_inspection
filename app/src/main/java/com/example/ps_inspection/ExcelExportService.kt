package com.example.ps_inspection

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExportService(private val context: Context) {

    fun exportToExcel(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ): Uri? {
        return try {
            // 1. Загружаем шаблон из assets
            val inputStream: InputStream = context.assets.open("blanks_template.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0) // Получаем первый лист

            // 2. Заполняем данные в существующий шаблон
            fillDataToTemplate(sheet, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            // 3. Сохраняем с новым именем (не перезаписываем шаблон)
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

        // ОРУ-35 кВ данные
        setCellValue(sheet, 5, 2, oru35Data.v352tsnA) // C6
        setCellValue(sheet, 5, 3, oru35Data.v352tsnB) // D6
        setCellValue(sheet, 5, 4, oru35Data.v352tsnC) // E6
        setCellValue(sheet, 5, 5, oru35Data.tsn2) // F6

        // ... остальное заполнение данных как было ранее
        // ВСЁ ФОРМАТИРОВАНИЕ, РАЗМЕРЫ ЯЧЕЕК И НЕИЗМЕНЯЕМЫЕ ДАННЫЕ СОХРАНЯТСЯ!
    }

    private fun setCellValue(sheet: Sheet, rowNum: Int, colNum: Int, value: String) {
        val row = sheet.getRow(rowNum) ?: return // Если строки нет - просто выходим, не создаём новую
        val cell = row.getCell(colNum) ?: return // Если ячейки нет - тоже выходим

        // Просто записываем значение в существующую ячейку
        cell.setCellValue(value)
    }

    private fun saveWorkbookFromTemplate(workbook: Workbook, inputStream: InputStream): Uri {
        inputStream.close() // Закрываем поток шаблона

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