package com.example.ps_inspection.data.utils

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
import androidx.core.content.FileProvider
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.data.repositories.CommentStorageManager
import com.example.ps_inspection.data.repositories.InspectionArchiveData
import com.example.ps_inspection.data.repositories.InspectionArchiveManager
import com.example.ps_inspection.data.repositories.LastInspectionManager
import com.example.ps_inspection.data.repositories.UserManager
import com.example.ps_inspection.data.services.GoogleSheetsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExcelExportService(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToExcel(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = "",
        progressOru35: Int = 0,
        progressOru220: Int = 0,
        progressOru500: Int = 0,
        progressAtg: Int = 0,
        progressBuildings: Int = 0
    ): Uri? {
        saveAllDataEverywhere(
            oru35Data, oru220Data, atgData, oru500Data, buildingsData,
            outdoorTemp,
            progressOru35, progressOru220, progressOru500, progressAtg, progressBuildings
        )

        return try {
            val inputStream: InputStream = context.assets.open("blanks_template.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            rememberDashCells(sheet)

            // Вызываем ОДИН раз с температурой
            val userManager = UserManager(context)
            val inspector = userManager.getCurrentUser()
            fillDataToTemplate(sheet, oru35Data, oru220Data, atgData, oru500Data, buildingsData,
                outdoorTemp, inspector.name, inspector.position)

            restoreDashCells(sheet)
            addCommentsSheet(workbook, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            val uri = saveWorkbook(workbook)
            inputStream.close()
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Унифицированный метод сохранения
    private fun saveWorkbook(workbook: Workbook): Uri? {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
        val fileName = "Осмотр_${dateFormat.format(Date())}.xlsx"

        return try {
            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        workbook.write(outputStream)
                    }
                    it
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }

                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                workbook.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun exportArchiveToExcel(
        archiveData: InspectionArchiveData
    ): Uri? {
        return try {
            val inputStream: InputStream = context.assets.open("blanks_template.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            rememberDashCells(sheet)

            // Безопасно получаем значения (могут отсутствовать в старых архивах)
            val outdoorTemp = try { archiveData.outdoorTemp } catch (e: Exception) { "" }
            val inspectorName = try { archiveData.inspectorName } catch (e: Exception) { "" }
            val inspectorPosition = try { archiveData.inspectorPosition } catch (e: Exception) { "" }

            fillDataToTemplate(sheet,
                archiveData.oru35,
                archiveData.oru220,
                archiveData.atg,
                archiveData.oru500,
                archiveData.buildings,
                outdoorTemp ?: "",
                inspectorName ?: "",
                inspectorPosition ?: ""
            )

            restoreDashCells(sheet)

            addCommentsSheet(workbook,
                archiveData.oru35,
                archiveData.oru220,
                archiveData.atg,
                archiveData.oru500,
                archiveData.buildings
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = saveWorkbook(workbook)
                inputStream.close()
                uri
            } else {
                saveWorkbookLegacy(workbook, inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addCommentsSheet(
        workbook: Workbook,
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ) {
        val commentsSheet = workbook.createSheet("Комментарии")

        val commentStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        val headerRow = commentsSheet.createRow(0)
        headerRow.createCell(0).apply {
            setCellValue("Оборудование")
            cellStyle = commentStyle
        }
        headerRow.createCell(1).apply {
            setCellValue("Комментарий")
            cellStyle = commentStyle
        }
        headerRow.createCell(2).apply {
            setCellValue("Дата")
            cellStyle = commentStyle
        }
        headerRow.createCell(3).apply {
            setCellValue("Осмотр выполнил")
            cellStyle = commentStyle
        }

        val commentStorage = CommentStorageManager(context)
        val allComments = commentStorage.loadAllComments()

        val userManager = UserManager(context)
        val currentUser = userManager.getCurrentUser()
        val inspectorName = "${currentUser.position} ${currentUser.name}"

        var rowNum = 1

        val equipmentOrder = listOf(
            "ORU35_ТСН", "ORU35_ТТ-35 2ТСН", "ORU35_ТТ-35 3ТСН", "ORU35_В-35 2ТСН", "ORU35_В-35 3ТСН",
            "ORU220_Мирная", "ORU220_Мирная ТТ", "ORU220_Топаз", "ORU220_Топаз ТТ",
            "ORU220_ОВ", "ORU220_ОВ ТТ", "ORU220_ТН-220 ОСШ",
            "ORU220_2АТГ", "ORU220_2АТГ ТТ", "ORU220_ШСВ", "ORU220_ШСВ ТТ",
            "ORU220_3АТГ", "ORU220_3АТГ ТТ", "ORU220_Орбита", "ORU220_Орбита ТТ",
            "ORU220_Факел", "ORU220_Факел ТТ", "ORU220_Комета-1", "ORU220_Комета-1 ТТ",
            "ORU220_Комета-2", "ORU220_Комета-2 ТТ", "ORU220_1ТН-220", "ORU220_2ТН-220",
            "ORU500_В-500 Р-500 2С", "ORU500_В-500 ВШТ-31", "ORU500_В-500 ВЛТ-30",
            "ORU500_В-500 ВШЛ-32", "ORU500_В-500 ВШЛ-21", "ORU500_В-500 ВШТ-22",
            "ORU500_В-500 ВЛТ-20", "ORU500_В-500 ВШТ-11", "ORU500_В-500 ВШЛ-12",
            "ORU500_ТТ-500 ВШТ-31", "ORU500_ТТ-500 ВЛТ-30", "ORU500_ТТ-500 ВШЛ-32",
            "ORU500_ТТ-500 ВШЛ-21", "ORU500_ТТ-500 ВШТ-22", "ORU500_ТТ-500 ВЛТ-20",
            "ORU500_ТТ-500 ВШТ-11", "ORU500_ТТ-500 ВШЛ-12",
            "ORU500_1ТН-500", "ORU500_2ТН-500", "ORU500_ТН-500 СГРЭС-1",
            "ORU500_Трачуковская ТТ", "ORU500_Трачуковская 2ТН", "ORU500_Трачуковская 1ТН", "ORU500_Белозёрная 2ТН",
            "ATG_2 АТГ ф.С", "ATG_2 АТГ ф.В", "ATG_2 АТГ ф.А", "ATG_АТГ резервная",
            "ATG_3 АТГ ф.С", "ATG_3 АТГ ф.В", "ATG_3 АТГ ф.А",
            "ATG_Реактор ф.С", "ATG_Реактор ф.В", "ATG_Реактор ф.А", "ATG_ТН-35",
            "BUILDINGS_Компрессорная №1", "BUILDINGS_Баллонная №1",
            "BUILDINGS_Компрессорная №2", "BUILDINGS_Баллонная №2",
            "BUILDINGS_КПЗ ОПУ", "BUILDINGS_КПЗ-2", "BUILDINGS_Насосная пожаротушения",
            "BUILDINGS_Мастерская по ремонту ВВ", "BUILDINGS_Артскважина",
            "BUILDINGS_Здание артезианской скважины", "BUILDINGS_Помещение 1 (2) АБ",
            "BUILDINGS_Помещение п/этажа №1,2,3"
        )

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        for (key in equipmentOrder) {
            val comments = allComments[key] ?: continue
            val displayName = key.substringAfter("_")

            for (comment in comments) {
                if (!comment.text.isNullOrBlank()) {
                    val row = commentsSheet.createRow(rowNum++)
                    row.createCell(0).apply {
                        setCellValue(displayName)
                        cellStyle = commentStyle
                    }
                    row.createCell(1).apply {
                        setCellValue(comment.text)
                        cellStyle = commentStyle
                    }
                    row.createCell(2).apply {
                        setCellValue(dateFormat.format(Date(comment.timestamp)))
                        cellStyle = commentStyle
                    }
                    row.createCell(3).apply {
                        // 🔧 Безопасная проверка author
                        setCellValue(comment.author ?: inspectorName)
                        cellStyle = commentStyle
                    }
                }
            }
        }

        if (rowNum == 1) {
            val row = commentsSheet.createRow(1)
            row.createCell(0).apply {
                setCellValue("📝 Нет комментариев")
                cellStyle = commentStyle
            }
        }

        commentsSheet.setColumnWidth(0, 35 * 256)
        commentsSheet.setColumnWidth(1, 50 * 256)
        commentsSheet.setColumnWidth(2, 18 * 256)
        commentsSheet.setColumnWidth(3, 25 * 256)
    }

    private fun fillDataToTemplate(
        sheet: Sheet,
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = "",
        inspectorName: String = "",
        inspectorPosition: String = ""
    ) {

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        Log.d("ExcelExport", "=== fillDataToTemplate START ===")

        setCellValue(sheet, 0, 1, currentDate)
        setCellValue(sheet, 0, 14, currentDate)

        // t нв
        Log.d("ExcelExport", "outdoorTemp='$outdoorTemp', isNotBlank=${outdoorTemp.isNotBlank()}")
        if (outdoorTemp.isNotBlank()) {
            Log.d("ExcelExport", "Пишем в R0(17) и F54(5)")
            setCellValue(sheet, 0, 19, outdoorTemp)
            setCellValue(sheet, 54, 7, outdoorTemp)
        } else {
            Log.d("ExcelExport", "outdoorTemp ПУСТОЙ - не пишем")
        }

        // ОРУ-35 кВ данные
        setCellValue(sheet, 5, 2, oru35Data.v352tsnA)
        setCellValue(sheet, 5, 3, oru35Data.v352tsnB)
        setCellValue(sheet, 5, 4, oru35Data.v352tsnC)

        setCellValue(sheet, 6, 2, oru35Data.v353tsnA)
        setCellValue(sheet, 6, 3, oru35Data.v353tsnB)
        setCellValue(sheet, 6, 4, oru35Data.v353tsnC)

        setCellValue(sheet, 4, 6, oru35Data.tsn2)
        setCellValue(sheet, 5, 6, oru35Data.tsn3)
        setCellValue(sheet, 6, 6, oru35Data.tsn4)

        setCellValue(sheet, 4, 8, oru35Data.tt352tsnA)
        setCellValue(sheet, 4, 9, oru35Data.tt352tsnB)
        setCellValue(sheet, 4, 10, oru35Data.tt352tsnC)

        setCellValue(sheet, 5, 8, oru35Data.tt353tsnA)
        setCellValue(sheet, 5, 9, oru35Data.tt353tsnB)
        setCellValue(sheet, 5, 10, oru35Data.tt353tsnC)

        setCellValue(sheet, 6, 8, atgData.tn352atg)
        setCellValue(sheet, 6, 10, atgData.tn353atg)

        // АТГ, Реактор
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

        setCellValue(sheet, 18, 1, atgData.reactor_c_oil_tank)
        setCellValue(sheet, 18, 3, atgData.reactor_c_pressure_500)
        setCellValue(sheet, 18, 5, atgData.reactor_c_temp_ts)
        setCellValue(sheet, 18, 7, atgData.reactor_c_pump_group1)
        setCellValue(sheet, 18, 8, atgData.reactor_c_pump_group2)
        setCellValue(sheet, 18, 9, atgData.reactor_c_pump_group3)
        setCellValue(sheet, 18, 10, atgData.reactor_c_tt_neutral)

        setCellValue(sheet, 19, 1, atgData.reactor_b_oil_tank)
        setCellValue(sheet, 19, 3, atgData.reactor_b_pressure_500)
        setCellValue(sheet, 19, 5, atgData.reactor_b_temp_ts)
        setCellValue(sheet, 19, 7, atgData.reactor_b_pump_group1)
        setCellValue(sheet, 19, 8, atgData.reactor_b_pump_group2)
        setCellValue(sheet, 19, 9, atgData.reactor_b_pump_group3)
        setCellValue(sheet, 19, 10, atgData.reactor_b_tt_neutral)

        setCellValue(sheet, 20, 1, atgData.reactor_a_oil_tank)
        setCellValue(sheet, 20, 3, atgData.reactor_a_pressure_500)
        setCellValue(sheet, 20, 5, atgData.reactor_a_temp_ts)
        setCellValue(sheet, 20, 7, atgData.reactor_a_pump_group1)
        setCellValue(sheet, 20, 8, atgData.reactor_a_pump_group2)
        setCellValue(sheet, 20, 9, atgData.reactor_a_pump_group3)
        setCellValue(sheet, 20, 10, atgData.reactor_a_tt_neutral)

        // ОРУ-500
        setCellValue(sheet, 24, 2, oru500Data.purgingR5002sA1)
        setCellValue(sheet, 24, 3, oru500Data.purgingR5002sB1)
        setCellValue(sheet, 24, 4, oru500Data.purgingR5002sC1)
        setCellValue(sheet, 25, 2, oru500Data.purgingR5002sA2)
        setCellValue(sheet, 25, 3, oru500Data.purgingR5002sB2)
        setCellValue(sheet, 25, 4, oru500Data.purgingR5002sC2)

        setCellValue(sheet, 27, 2, oru500Data.gasPressureVsht31A)
        setCellValue(sheet, 27, 3, oru500Data.gasPressureVsht31B)
        setCellValue(sheet, 27, 4, oru500Data.gasPressureVsht31C)

        setCellValue(sheet, 27, 6, oru500Data.oilTtVsht31A)
        setCellValue(sheet, 27, 7, oru500Data.oilTtVsht31B)
        setCellValue(sheet, 27, 8, oru500Data.oilTtVsht31C)

        setCellValue(sheet, 28, 2, oru500Data.gasPressureVlt30A)
        setCellValue(sheet, 28, 3, oru500Data.gasPressureVlt30B)
        setCellValue(sheet, 28, 4, oru500Data.gasPressureVlt30C)

        setCellValue(sheet, 28, 6, oru500Data.oilTtVlt30A)
        setCellValue(sheet, 28, 7, oru500Data.oilTtVlt30B)
        setCellValue(sheet, 28, 8, oru500Data.oilTtVlt30C)

        setCellValue(sheet, 42, 6, oru500Data.oilTtTrachukovskayaA)
        setCellValue(sheet, 42, 7, oru500Data.oilTtTrachukovskayaB)
        setCellValue(sheet, 42, 8, oru500Data.oilTtTrachukovskayaC)

        setCellValue(sheet, 50, 8, oru500Data.oil2tnTrachukovskayaA)
        setCellValue(sheet, 50, 9, oru500Data.oil2tnTrachukovskayaB)
        setCellValue(sheet, 50, 10, oru500Data.oil2tnTrachukovskayaC)

        setCellValue(sheet, 50, 2, oru500Data.oil1tnTrachukovskayaA)
        setCellValue(sheet, 50, 3, oru500Data.oil1tnTrachukovskayaB)
        setCellValue(sheet, 50, 4, oru500Data.oil1tnTrachukovskayaC)

        setCellValue(sheet, 30, 2, oru500Data.purgingVshl32A1)
        setCellValue(sheet, 30, 3, oru500Data.purgingVshl32B1)
        setCellValue(sheet, 30, 4, oru500Data.purgingVshl32C1)
        setCellValue(sheet, 31, 2, oru500Data.purgingVshl32A2)
        setCellValue(sheet, 31, 3, oru500Data.purgingVshl32B2)
        setCellValue(sheet, 31, 4, oru500Data.purgingVshl32C2)

        setCellValue(sheet, 30, 6, oru500Data.oilTtVshl32A)
        setCellValue(sheet, 30, 7, oru500Data.oilTtVshl32B)
        setCellValue(sheet, 30, 8, oru500Data.oilTtVshl32C)

        setCellValue(sheet, 32, 2, oru500Data.purgingVshl21A1)
        setCellValue(sheet, 32, 3, oru500Data.purgingVshl21B1)
        setCellValue(sheet, 32, 4, oru500Data.purgingVshl21C1)
        setCellValue(sheet, 33, 2, oru500Data.purgingVshl21A2)
        setCellValue(sheet, 33, 3, oru500Data.purgingVshl21B2)
        setCellValue(sheet, 33, 4, oru500Data.purgingVshl21C2)

        setCellValue(sheet, 32, 6, oru500Data.oilTtVshl21A)
        setCellValue(sheet, 32, 7, oru500Data.oilTtVshl21B)
        setCellValue(sheet, 32, 8, oru500Data.oilTtVshl21C)

        setCellValue(sheet, 34, 2, oru500Data.purgingVsht22A1)
        setCellValue(sheet, 34, 3, oru500Data.purgingVsht22B1)
        setCellValue(sheet, 34, 4, oru500Data.purgingVsht22C1)
        setCellValue(sheet, 35, 2, oru500Data.purgingVsht22A2)
        setCellValue(sheet, 35, 3, oru500Data.purgingVsht22B2)
        setCellValue(sheet, 35, 4, oru500Data.purgingVsht22C2)

        setCellValue(sheet, 34, 6, oru500Data.oilTtVsht22A)
        setCellValue(sheet, 34, 7, oru500Data.oilTtVsht22B)
        setCellValue(sheet, 34, 8, oru500Data.oilTtVsht22C)

        setCellValue(sheet, 36, 2, oru500Data.purgingVlt20A1)
        setCellValue(sheet, 36, 3, oru500Data.purgingVlt20B1)
        setCellValue(sheet, 36, 4, oru500Data.purgingVlt20C1)
        setCellValue(sheet, 37, 2, oru500Data.purgingVlt20A2)
        setCellValue(sheet, 37, 3, oru500Data.purgingVlt20B2)
        setCellValue(sheet, 37, 4, oru500Data.purgingVlt20C2)

        setCellValue(sheet, 36, 6, oru500Data.oilTtVlt20A)
        setCellValue(sheet, 36, 7, oru500Data.oilTtVlt20B)
        setCellValue(sheet, 36, 8, oru500Data.oilTtVlt20C)

        setCellValue(sheet, 38, 2, oru500Data.purgingVsht11A1)
        setCellValue(sheet, 38, 3, oru500Data.purgingVsht11B1)
        setCellValue(sheet, 38, 4, oru500Data.purgingVsht11C1)
        setCellValue(sheet, 39, 2, oru500Data.purgingVsht11A2)
        setCellValue(sheet, 39, 3, oru500Data.purgingVsht11B2)
        setCellValue(sheet, 39, 4, oru500Data.purgingVsht11C2)

        setCellValue(sheet, 38, 6, oru500Data.oilTtVsht11A)
        setCellValue(sheet, 38, 7, oru500Data.oilTtVsht11B)
        setCellValue(sheet, 38, 8, oru500Data.oilTtVsht11C)

        setCellValue(sheet, 40, 2, oru500Data.purgingVshl12A1)
        setCellValue(sheet, 40, 3, oru500Data.purgingVshl12B1)
        setCellValue(sheet, 40, 4, oru500Data.purgingVshl12C1)
        setCellValue(sheet, 41, 2, oru500Data.purgingVshl12A2)
        setCellValue(sheet, 41, 3, oru500Data.purgingVshl12B2)
        setCellValue(sheet, 41, 4, oru500Data.purgingVshl12C2)

        setCellValue(sheet, 40, 6, oru500Data.oilTtVshl12A)
        setCellValue(sheet, 40, 7, oru500Data.oilTtVshl12B)
        setCellValue(sheet, 40, 8, oru500Data.oilTtVshl12C)

        setCellValue(sheet, 56, 2, oru500Data.oil2tnBelozernayaA)
        setCellValue(sheet, 56, 3, oru500Data.oil2tnBelozernayaB)
        setCellValue(sheet, 56, 4, oru500Data.oil2tnBelozernayaC)

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

        // ОРУ-220
        setCellValue(sheet, 6, 15, oru220Data.purgingMirnayaA)
        setCellValue(sheet, 6, 16, oru220Data.purgingMirnayaB)
        setCellValue(sheet, 6, 17, oru220Data.purgingMirnayaC)

        setCellValue(sheet, 6, 19, oru220Data.oilMirnayaA)
        setCellValue(sheet, 6, 20, oru220Data.oilMirnayaB)
        setCellValue(sheet, 6, 21, oru220Data.oilMirnayaC)

        setCellValue(sheet, 7, 15, oru220Data.purgingTopazA)
        setCellValue(sheet, 7, 16, oru220Data.purgingTopazB)
        setCellValue(sheet, 7, 17, oru220Data.purgingTopazC)

        setCellValue(sheet, 7, 19, oru220Data.oilTopazA)
        setCellValue(sheet, 7, 20, oru220Data.oilTopazB)
        setCellValue(sheet, 7, 21, oru220Data.oilTopazC)

        setCellValue(sheet, 8, 15, oru220Data.purgingOvA)
        setCellValue(sheet, 8, 16, oru220Data.purgingOvB)
        setCellValue(sheet, 8, 17, oru220Data.purgingOvC)

        setCellValue(sheet, 8, 19, oru220Data.oilOvA)
        setCellValue(sheet, 8, 20, oru220Data.oilOvB)
        setCellValue(sheet, 8, 21, oru220Data.oilOvC)

        setCellValue(sheet, 19, 23, oru220Data.tnOsshFvUpper)
        setCellValue(sheet, 20, 23, oru220Data.tnOsshFvLower)

        setCellValue(sheet, 9, 15, oru220Data.purgingV2atgA)
        setCellValue(sheet, 9, 16, oru220Data.purgingV2atgB)
        setCellValue(sheet, 9, 17, oru220Data.purgingV2atgC)

        setCellValue(sheet, 9, 19, oru220Data.oilTt2atgA)
        setCellValue(sheet, 9, 20, oru220Data.oilTt2atgB)
        setCellValue(sheet, 9, 21, oru220Data.oilTt2atgC)

        setCellValue(sheet, 10, 15, oru220Data.purgingShSV220A)
        setCellValue(sheet, 10, 16, oru220Data.purgingShSV220B)
        setCellValue(sheet, 10, 17, oru220Data.purgingShSV220C)

        setCellValue(sheet, 10, 19, oru220Data.oilTtShSV220A)
        setCellValue(sheet, 10, 20, oru220Data.oilTtShSV220B)
        setCellValue(sheet, 10, 21, oru220Data.oilTtShSV220C)

        setCellValue(sheet, 11, 15, oru220Data.purgingV3atgA)
        setCellValue(sheet, 11, 16, oru220Data.purgingV3atgB)
        setCellValue(sheet, 11, 17, oru220Data.purgingV3atgC)

        setCellValue(sheet, 11, 19, oru220Data.oilTt3atgA)
        setCellValue(sheet, 11, 20, oru220Data.oilTt3atgB)
        setCellValue(sheet, 11, 21, oru220Data.oilTt3atgC)

        setCellValue(sheet, 12, 15, oru220Data.purgingOrbitaA)
        setCellValue(sheet, 12, 16, oru220Data.purgingOrbitaB)
        setCellValue(sheet, 12, 17, oru220Data.purgingOrbitaC)

        setCellValue(sheet, 12, 19, oru220Data.oilOrbitaA)
        setCellValue(sheet, 12, 20, oru220Data.oilOrbitaB)
        setCellValue(sheet, 12, 21, oru220Data.oilOrbitaC)

        setCellValue(sheet, 13, 15, oru220Data.purgingFakelA)
        setCellValue(sheet, 13, 16, oru220Data.purgingFakelB)
        setCellValue(sheet, 13, 17, oru220Data.purgingFakelC)

        setCellValue(sheet, 13, 19, oru220Data.oilFakelA)
        setCellValue(sheet, 13, 20, oru220Data.oilFakelB)
        setCellValue(sheet, 13, 21, oru220Data.oilFakelC)

        setCellValue(sheet, 14, 15, oru220Data.purgingCometa2A)
        setCellValue(sheet, 14, 16, oru220Data.purgingCometa2B)
        setCellValue(sheet, 14, 17, oru220Data.purgingCometa2C)

        setCellValue(sheet, 14, 19, oru220Data.oilCometa2A)
        setCellValue(sheet, 14, 20, oru220Data.oilCometa2B)
        setCellValue(sheet, 14, 21, oru220Data.oilCometa2C)

        setCellValue(sheet, 15, 15, oru220Data.purgingCometa1A)
        setCellValue(sheet, 15, 16, oru220Data.purgingCometa1B)
        setCellValue(sheet, 15, 17, oru220Data.purgingCometa1C)

        setCellValue(sheet, 15, 19, oru220Data.oilCometa1A)
        setCellValue(sheet, 15, 20, oru220Data.oilCometa1B)
        setCellValue(sheet, 15, 21, oru220Data.oilCometa1C)

        setCellValue(sheet, 19, 15, oru220Data.tn1UpperA)
        setCellValue(sheet, 19, 16, oru220Data.tn1UpperB)
        setCellValue(sheet, 19, 17, oru220Data.tn1UpperC)
        setCellValue(sheet, 20, 15, oru220Data.tn1LowerA)
        setCellValue(sheet, 20, 16, oru220Data.tn1LowerB)
        setCellValue(sheet, 20, 17, oru220Data.tn1LowerC)

        setCellValue(sheet, 19, 20, oru220Data.tn2UpperA)
        setCellValue(sheet, 19, 21, oru220Data.tn2UpperB)
        setCellValue(sheet, 19, 22, oru220Data.tn2UpperC)
        setCellValue(sheet, 20, 20, oru220Data.tn2LowerA)
        setCellValue(sheet, 20, 21, oru220Data.tn2LowerB)
        setCellValue(sheet, 20, 22, oru220Data.tn2LowerC)

        // Здания
        setCellValue(sheet, 23, 15, buildingsData.compressor1Valve)
        setCellValue(sheet, 23, 18, buildingsData.compressor1Heating)
        setCellValue(sheet, 23, 21, buildingsData.compressor1Temp)

        setCellValue(sheet, 24, 15, buildingsData.ballroom1Valve)
        setCellValue(sheet, 24, 18, buildingsData.ballroom1Heating)
        setCellValue(sheet, 24, 21, buildingsData.ballroom1Temp)

        setCellValue(sheet, 25, 15, buildingsData.compressor2Valve)
        setCellValue(sheet, 25, 18, buildingsData.compressor2Heating)
        setCellValue(sheet, 25, 21, buildingsData.compressor2Temp)

        setCellValue(sheet, 26, 15, buildingsData.ballroom2Valve)
        setCellValue(sheet, 26, 18, buildingsData.ballroom2Heating)
        setCellValue(sheet, 26, 21, buildingsData.ballroom2Temp)

        setCellValue(sheet, 27, 15, buildingsData.kpzOpuValve)
        setCellValue(sheet, 27, 18, buildingsData.kpzOpuHeating)
        setCellValue(sheet, 27, 21, buildingsData.kpzOpuTemp)

        setCellValue(sheet, 28, 15, buildingsData.kpz2Valve)
        setCellValue(sheet, 28, 18, buildingsData.kpz2Heating)
        setCellValue(sheet, 28, 21, buildingsData.kpz2Temp)

        setCellValue(sheet, 29, 15, buildingsData.firePumpValve)
        setCellValue(sheet, 29, 18, buildingsData.firePumpHeating)
        setCellValue(sheet, 29, 21, buildingsData.firePumpTemp)
        setCellValue(sheet, 29, 23, buildingsData.firePumpWaterLevel)

        setCellValue(sheet, 30, 18, buildingsData.workshopHeating)
        setCellValue(sheet, 30, 21, buildingsData.workshopTemp)

        setCellValue(sheet, 31, 18, buildingsData.artWellHeating)

        setCellValue(sheet, 32, 18, buildingsData.artesianWellHeating)

        setCellValue(sheet, 33, 18, buildingsData.roomAbHeating)
        setCellValue(sheet, 33, 21, buildingsData.roomAbTemp)

        setCellValue(sheet, 34, 18, buildingsData.basementHeating)
        setCellValue(sheet, 34, 21, buildingsData.basementTemp)

        // Осмотр выполнил (сначала должность, потом ФИО — как в шаблоне)
        if (inspectorName.isNotBlank()) {
            // Левая часть — "Осмотр выполнил:"
            setCellValue(sheet, 58, 8, "$inspectorPosition $inspectorName")

            // Правая часть — "Осмотр выполнил:"
            setCellValue(sheet, 59, 19, "$inspectorPosition $inspectorName")
        }
    }

    private fun setCellValue(sheet: Sheet, rowNum: Int, colNum: Int, value: String) {
        try {
            val row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
            val cell = row.getCell(colNum) ?: row.createCell(colNum)
            cell.setCellValue(value)

            // 🔧 Копируем существующий стиль ячейки и меняем только выравнивание
            val originalStyle = cell.cellStyle
            val newStyle = sheet.workbook.createCellStyle()
            newStyle.cloneStyleFrom(originalStyle)  // копируем всё из шаблона
            newStyle.alignment = HorizontalAlignment.CENTER
            newStyle.verticalAlignment = VerticalAlignment.CENTER
            cell.cellStyle = newStyle
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveWorkbookFromTemplate(workbook: Workbook, inputStream: InputStream): Uri? {
        inputStream.close()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
        val fileName = "Осмотр_${dateFormat.format(Date())}.xlsx"

        return try {
            val resolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        workbook.write(outputStream)
                    }
                    it
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)

                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }

                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            workbook.close()
        }
    }


    private fun saveWorkbookLegacy(workbook: Workbook, inputStream: InputStream): Uri? {
        inputStream.close()

        val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
        val fileName = "Осмотр_${dateFormat.format(Date())}.xlsx"

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            workbook.close()
        }
    }

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy_HH-mm-ss", Locale.getDefault())

    fun exportCommentsToExcel(
        comments: List<Pair<String, Comment>>,
        filterSection: String = "Все"
    ): Uri? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Комментарии")

            // Заголовки
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("№", "Секция", "Оборудование", "Дата и время", "Текст комментария")
            headers.forEachIndexed { index, title ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
            }

            // Данные
            comments.forEachIndexed { index, (sectionEquipment, comment) ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue((index + 1).toDouble())

                val parts = sectionEquipment.split(": ")
                val section = parts.getOrNull(0) ?: ""
                val equipment = parts.getOrNull(1) ?: sectionEquipment

                row.createCell(1).setCellValue(section)
                row.createCell(2).setCellValue(equipment)
                row.createCell(3).setCellValue(comment.getFormattedTime())
                row.createCell(4).setCellValue(comment.text)
            }

            // Вручную устанавливаем ширину колонок (вместо autoSizeColumn)
            sheet.setColumnWidth(0, 8 * 256)    // №
            sheet.setColumnWidth(1, 20 * 256)   // Секция
            sheet.setColumnWidth(2, 30 * 256)   // Оборудование
            sheet.setColumnWidth(3, 20 * 256)   // Дата и время
            sheet.setColumnWidth(4, 50 * 256)   // Текст комментария

            // Сохраняем в кэш
            val fileName = "comments_${System.currentTimeMillis()}.xlsx"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            // Получаем URI для sharing
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🔧 Список для хранения ячеек с прочерками из шаблона
    private val dashCells = mutableSetOf<Pair<Int, Int>>()

    /**
     * Сохраняет данные во всех хранилищах ДО экспорта
     */
    private fun saveAllDataEverywhere(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = "",
        progressOru35: Int = 0,
        progressOru220: Int = 0,
        progressOru500: Int = 0,
        progressAtg: Int = 0,
        progressBuildings: Int = 0
    ) {
        try {
            val lastInspectionManager = LastInspectionManager(context)
            lastInspectionManager.saveLastInspection(oru35Data, oru220Data, atgData, oru500Data, buildingsData)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val userManager = UserManager(context)
            val currentUser = userManager.getCurrentUser()

            val archiveManager = InspectionArchiveManager(context)
            archiveManager.saveToArchive(
                oru35Data, oru220Data, atgData, oru500Data, buildingsData,
                outdoorTemp,
                currentUser.name,
                currentUser.position
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 🔄 Отправка в Google Sheets (в фоне)
        try {
            val sheetsService = GoogleSheetsService(context)
            val userManager = UserManager(context)
            val user = userManager.getCurrentUser()

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = Date()
            val currentDate = dateFormat.format(now)
            val currentTime = timeFormat.format(now)

            // ==================== ОСНОВНЫЕ ДАННЫЕ ====================
            val data = mapOf(
                "Дата" to currentDate,
                "Время" to currentTime,
                "ФИО дежурного" to user.name,
                "Должность" to user.position,
                "t наружного воздуха" to outdoorTemp.ifBlank { "-" },

                // ОРУ-35: ТСН
                "ОРУ-35 2ТСН" to oru35Data.tsn2,
                "ОРУ-35 3ТСН" to oru35Data.tsn3,
                "ОРУ-35 4ТСН" to oru35Data.tsn4,
                "ОРУ-35 ТТ-35 2ТСН А" to oru35Data.tt352tsnA,
                "ОРУ-35 ТТ-35 2ТСН В" to oru35Data.tt352tsnB,
                "ОРУ-35 ТТ-35 2ТСН С" to oru35Data.tt352tsnC,
                "ОРУ-35 ТТ-35 3ТСН А" to oru35Data.tt353tsnA,
                "ОРУ-35 ТТ-35 3ТСН В" to oru35Data.tt353tsnB,
                "ОРУ-35 ТТ-35 3ТСН С" to oru35Data.tt353tsnC,
                "ОРУ-35 В-35 2ТСН А" to oru35Data.v352tsnA,
                "ОРУ-35 В-35 2ТСН В" to oru35Data.v352tsnB,
                "ОРУ-35 В-35 2ТСН С" to oru35Data.v352tsnC,
                "ОРУ-35 В-35 3ТСН А" to oru35Data.v353tsnA,
                "ОРУ-35 В-35 3ТСН В" to oru35Data.v353tsnB,
                "ОРУ-35 В-35 3ТСН С" to oru35Data.v353tsnC,

                // ОРУ-220: Мирная
                "Мирная прод А" to oru220Data.purgingMirnayaA,
                "Мирная прод В" to oru220Data.purgingMirnayaB,
                "Мирная прод С" to oru220Data.purgingMirnayaC,
                "Мирная ТТ масло А" to oru220Data.oilMirnayaA,
                "Мирная ТТ масло В" to oru220Data.oilMirnayaB,
                "Мирная ТТ масло С" to oru220Data.oilMirnayaC,

                // ОРУ-220: Топаз
                "Топаз прод А" to oru220Data.purgingTopazA,
                "Топаз прод В" to oru220Data.purgingTopazB,
                "Топаз прод С" to oru220Data.purgingTopazC,
                "Топаз ТТ масло А" to oru220Data.oilTopazA,
                "Топаз ТТ масло В" to oru220Data.oilTopazB,
                "Топаз ТТ масло С" to oru220Data.oilTopazC,

                // ОРУ-220: ОВ
                "ОВ прод А" to oru220Data.purgingOvA,
                "ОВ прод В" to oru220Data.purgingOvB,
                "ОВ прод С" to oru220Data.purgingOvC,
                "ОВ ТТ масло А" to oru220Data.oilOvA,
                "ОВ ТТ масло В" to oru220Data.oilOvB,
                "ОВ ТТ масло С" to oru220Data.oilOvC,

                // ОРУ-220: ОСШ
                "ТН-220 ОСШ верх" to oru220Data.tnOsshFvUpper,
                "ТН-220 ОСШ низ" to oru220Data.tnOsshFvLower,

                // ОРУ-220: 2АТГ
                "В-220 2АТГ прод А" to oru220Data.purgingV2atgA,
                "В-220 2АТГ прод В" to oru220Data.purgingV2atgB,
                "В-220 2АТГ прод С" to oru220Data.purgingV2atgC,
                "2АТГ ТТ масло А" to oru220Data.oilTt2atgA,
                "2АТГ ТТ масло В" to oru220Data.oilTt2atgB,
                "2АТГ ТТ масло С" to oru220Data.oilTt2atgC,

                // ОРУ-220: ШСВ
                "ШСВ-220 прод А" to oru220Data.purgingShSV220A,
                "ШСВ-220 прод В" to oru220Data.purgingShSV220B,
                "ШСВ-220 прод С" to oru220Data.purgingShSV220C,
                "ШСВ ТТ масло А" to oru220Data.oilTtShSV220A,
                "ШСВ ТТ масло В" to oru220Data.oilTtShSV220B,
                "ШСВ ТТ масло С" to oru220Data.oilTtShSV220C,

                // ОРУ-220: 3АТГ
                "В-220 3АТГ прод А" to oru220Data.purgingV3atgA,
                "В-220 3АТГ прод В" to oru220Data.purgingV3atgB,
                "В-220 3АТГ прод С" to oru220Data.purgingV3atgC,
                "3АТГ ТТ масло А" to oru220Data.oilTt3atgA,
                "3АТГ ТТ масло В" to oru220Data.oilTt3atgB,
                "3АТГ ТТ масло С" to oru220Data.oilTt3atgC,

                // ОРУ-220: Орбита
                "Орбита прод А" to oru220Data.purgingOrbitaA,
                "Орбита прод В" to oru220Data.purgingOrbitaB,
                "Орбита прод С" to oru220Data.purgingOrbitaC,
                "Орбита ТТ масло А" to oru220Data.oilOrbitaA,
                "Орбита ТТ масло В" to oru220Data.oilOrbitaB,
                "Орбита ТТ масло С" to oru220Data.oilOrbitaC,

                // ОРУ-220: Факел
                "Факел прод А" to oru220Data.purgingFakelA,
                "Факел прод В" to oru220Data.purgingFakelB,
                "Факел прод С" to oru220Data.purgingFakelC,
                "Факел ТТ масло А" to oru220Data.oilFakelA,
                "Факел ТТ масло В" to oru220Data.oilFakelB,
                "Факел ТТ масло С" to oru220Data.oilFakelC,

                // ОРУ-220: Комета-1
                "Комета-1 прод А" to oru220Data.purgingCometa1A,
                "Комета-1 прод В" to oru220Data.purgingCometa1B,
                "Комета-1 прод С" to oru220Data.purgingCometa1C,
                "Комета-1 ТТ масло А" to oru220Data.oilCometa1A,
                "Комета-1 ТТ масло В" to oru220Data.oilCometa1B,
                "Комета-1 ТТ масло С" to oru220Data.oilCometa1C,

                // ОРУ-220: Комета-2
                "Комета-2 прод А" to oru220Data.purgingCometa2A,
                "Комета-2 прод В" to oru220Data.purgingCometa2B,
                "Комета-2 прод С" to oru220Data.purgingCometa2C,
                "Комета-2 ТТ масло А" to oru220Data.oilCometa2A,
                "Комета-2 ТТ масло В" to oru220Data.oilCometa2B,
                "Комета-2 ТТ масло С" to oru220Data.oilCometa2C,

                // ОРУ-220: 1ТН-220
                "1ТН-220 верх А" to oru220Data.tn1UpperA,
                "1ТН-220 верх В" to oru220Data.tn1UpperB,
                "1ТН-220 верх С" to oru220Data.tn1UpperC,
                "1ТН-220 низ А" to oru220Data.tn1LowerA,
                "1ТН-220 низ В" to oru220Data.tn1LowerB,
                "1ТН-220 низ С" to oru220Data.tn1LowerC,

                // ОРУ-220: 2ТН-220
                "2ТН-220 верх А" to oru220Data.tn2UpperA,
                "2ТН-220 верх В" to oru220Data.tn2UpperB,
                "2ТН-220 верх С" to oru220Data.tn2UpperC,
                "2ТН-220 низ А" to oru220Data.tn2LowerA,
                "2ТН-220 низ В" to oru220Data.tn2LowerB,
                "2ТН-220 низ С" to oru220Data.tn2LowerC,

                // ОРУ-500: Р-500 2С
                "Р-500 2С I А1" to oru500Data.purgingR5002sA1,
                "Р-500 2С I В1" to oru500Data.purgingR5002sB1,
                "Р-500 2С I С1" to oru500Data.purgingR5002sC1,
                "Р-500 2С II А2" to oru500Data.purgingR5002sA2,
                "Р-500 2С II В2" to oru500Data.purgingR5002sB2,
                "Р-500 2С II С2" to oru500Data.purgingR5002sC2,

                // ОРУ-500: ВШТ-31
                "ВШТ-31 газ А" to oru500Data.gasPressureVsht31A,
                "ВШТ-31 газ В" to oru500Data.gasPressureVsht31B,
                "ВШТ-31 газ С" to oru500Data.gasPressureVsht31C,
                "ВШТ-31 ТТ масло А" to oru500Data.oilTtVsht31A,
                "ВШТ-31 ТТ масло В" to oru500Data.oilTtVsht31B,
                "ВШТ-31 ТТ масло С" to oru500Data.oilTtVsht31C,

                // ОРУ-500: ВЛТ-30
                "ВЛТ-30 газ А" to oru500Data.gasPressureVlt30A,
                "ВЛТ-30 газ В" to oru500Data.gasPressureVlt30B,
                "ВЛТ-30 газ С" to oru500Data.gasPressureVlt30C,
                "ВЛТ-30 ТТ масло А" to oru500Data.oilTtVlt30A,
                "ВЛТ-30 ТТ масло В" to oru500Data.oilTtVlt30B,
                "ВЛТ-30 ТТ масло С" to oru500Data.oilTtVlt30C,

                // ОРУ-500: ВШЛ-32
                "ВШЛ-32 I А1" to oru500Data.purgingVshl32A1,
                "ВШЛ-32 I В1" to oru500Data.purgingVshl32B1,
                "ВШЛ-32 I С1" to oru500Data.purgingVshl32C1,
                "ВШЛ-32 II А2" to oru500Data.purgingVshl32A2,
                "ВШЛ-32 II В2" to oru500Data.purgingVshl32B2,
                "ВШЛ-32 II С2" to oru500Data.purgingVshl32C2,
                "ВШЛ-32 ТТ масло А" to oru500Data.oilTtVshl32A,
                "ВШЛ-32 ТТ масло В" to oru500Data.oilTtVshl32B,
                "ВШЛ-32 ТТ масло С" to oru500Data.oilTtVshl32C,

                // ОРУ-500: ВШЛ-21
                "ВШЛ-21 I А1" to oru500Data.purgingVshl21A1,
                "ВШЛ-21 I В1" to oru500Data.purgingVshl21B1,
                "ВШЛ-21 I С1" to oru500Data.purgingVshl21C1,
                "ВШЛ-21 II А2" to oru500Data.purgingVshl21A2,
                "ВШЛ-21 II В2" to oru500Data.purgingVshl21B2,
                "ВШЛ-21 II С2" to oru500Data.purgingVshl21C2,
                "ВШЛ-21 ТТ масло А" to oru500Data.oilTtVshl21A,
                "ВШЛ-21 ТТ масло В" to oru500Data.oilTtVshl21B,
                "ВШЛ-21 ТТ масло С" to oru500Data.oilTtVshl21C,

                // ОРУ-500: ВШТ-22
                "ВШТ-22 I А1" to oru500Data.purgingVsht22A1,
                "ВШТ-22 I В1" to oru500Data.purgingVsht22B1,
                "ВШТ-22 I С1" to oru500Data.purgingVsht22C1,
                "ВШТ-22 II А2" to oru500Data.purgingVsht22A2,
                "ВШТ-22 II В2" to oru500Data.purgingVsht22B2,
                "ВШТ-22 II С2" to oru500Data.purgingVsht22C2,
                "ВШТ-22 ТТ масло А" to oru500Data.oilTtVsht22A,
                "ВШТ-22 ТТ масло В" to oru500Data.oilTtVsht22B,
                "ВШТ-22 ТТ масло С" to oru500Data.oilTtVsht22C,

                // ОРУ-500: ВЛТ-20
                "ВЛТ-20 I А1" to oru500Data.purgingVlt20A1,
                "ВЛТ-20 I В1" to oru500Data.purgingVlt20B1,
                "ВЛТ-20 I С1" to oru500Data.purgingVlt20C1,
                "ВЛТ-20 II А2" to oru500Data.purgingVlt20A2,
                "ВЛТ-20 II В2" to oru500Data.purgingVlt20B2,
                "ВЛТ-20 II С2" to oru500Data.purgingVlt20C2,
                "ВЛТ-20 ТТ масло А" to oru500Data.oilTtVlt20A,
                "ВЛТ-20 ТТ масло В" to oru500Data.oilTtVlt20B,
                "ВЛТ-20 ТТ масло С" to oru500Data.oilTtVlt20C,

                // ОРУ-500: ВШТ-11
                "ВШТ-11 I А1" to oru500Data.purgingVsht11A1,
                "ВШТ-11 I В1" to oru500Data.purgingVsht11B1,
                "ВШТ-11 I С1" to oru500Data.purgingVsht11C1,
                "ВШТ-11 II А2" to oru500Data.purgingVsht11A2,
                "ВШТ-11 II В2" to oru500Data.purgingVsht11B2,
                "ВШТ-11 II С2" to oru500Data.purgingVsht11C2,
                "ВШТ-11 ТТ масло А" to oru500Data.oilTtVsht11A,
                "ВШТ-11 ТТ масло В" to oru500Data.oilTtVsht11B,
                "ВШТ-11 ТТ масло С" to oru500Data.oilTtVsht11C,

                // ОРУ-500: ВШЛ-12
                "ВШЛ-12 I А1" to oru500Data.purgingVshl12A1,
                "ВШЛ-12 I В1" to oru500Data.purgingVshl12B1,
                "ВШЛ-12 I С1" to oru500Data.purgingVshl12C1,
                "ВШЛ-12 II А2" to oru500Data.purgingVshl12A2,
                "ВШЛ-12 II В2" to oru500Data.purgingVshl12B2,
                "ВШЛ-12 II С2" to oru500Data.purgingVshl12C2,
                "ВШЛ-12 ТТ масло А" to oru500Data.oilTtVshl12A,
                "ВШЛ-12 ТТ масло В" to oru500Data.oilTtVshl12B,
                "ВШЛ-12 ТТ масло С" to oru500Data.oilTtVshl12C,

                // ОРУ-500: ТТ-500 Трачуковская
                "Трачуковская ТТ масло А" to oru500Data.oilTtTrachukovskayaA,
                "Трачуковская ТТ масло В" to oru500Data.oilTtTrachukovskayaB,
                "Трачуковская ТТ масло С" to oru500Data.oilTtTrachukovskayaC,

                // ОРУ-500: 1ТН-500
                "1ТН-500 каск1 А" to oru500Data.tn1500Cascade1A,
                "1ТН-500 каск1 В" to oru500Data.tn1500Cascade1B,
                "1ТН-500 каск1 С" to oru500Data.tn1500Cascade1C,
                "1ТН-500 каск2 А" to oru500Data.tn1500Cascade2A,
                "1ТН-500 каск2 В" to oru500Data.tn1500Cascade2B,
                "1ТН-500 каск2 С" to oru500Data.tn1500Cascade2C,
                "1ТН-500 каск3 А" to oru500Data.tn1500Cascade3A,
                "1ТН-500 каск3 В" to oru500Data.tn1500Cascade3B,
                "1ТН-500 каск3 С" to oru500Data.tn1500Cascade3C,
                "1ТН-500 каск4 А" to oru500Data.tn1500Cascade4A,
                "1ТН-500 каск4 В" to oru500Data.tn1500Cascade4B,
                "1ТН-500 каск4 С" to oru500Data.tn1500Cascade4C,

                // ОРУ-500: 2ТН-500
                "2ТН-500 каск1 А" to oru500Data.tn2500Cascade1A,
                "2ТН-500 каск1 В" to oru500Data.tn2500Cascade1B,
                "2ТН-500 каск1 С" to oru500Data.tn2500Cascade1C,
                "2ТН-500 каск2 А" to oru500Data.tn2500Cascade2A,
                "2ТН-500 каск2 В" to oru500Data.tn2500Cascade2B,
                "2ТН-500 каск2 С" to oru500Data.tn2500Cascade2C,
                "2ТН-500 каск3 А" to oru500Data.tn2500Cascade3A,
                "2ТН-500 каск3 В" to oru500Data.tn2500Cascade3B,
                "2ТН-500 каск3 С" to oru500Data.tn2500Cascade3C,
                "2ТН-500 каск4 А" to oru500Data.tn2500Cascade4A,
                "2ТН-500 каск4 В" to oru500Data.tn2500Cascade4B,
                "2ТН-500 каск4 С" to oru500Data.tn2500Cascade4C,

                // ОРУ-500: ТН-500 СГРЭС-1
                "СГРЭС-1 каск1 А" to oru500Data.tn500Sgres1Cascade1A,
                "СГРЭС-1 каск1 В" to oru500Data.tn500Sgres1Cascade1B,
                "СГРЭС-1 каск1 С" to oru500Data.tn500Sgres1Cascade1C,
                "СГРЭС-1 каск2 А" to oru500Data.tn500Sgres1Cascade2A,
                "СГРЭС-1 каск2 В" to oru500Data.tn500Sgres1Cascade2B,
                "СГРЭС-1 каск2 С" to oru500Data.tn500Sgres1Cascade2C,
                "СГРЭС-1 каск3 А" to oru500Data.tn500Sgres1Cascade3A,
                "СГРЭС-1 каск3 В" to oru500Data.tn500Sgres1Cascade3B,
                "СГРЭС-1 каск3 С" to oru500Data.tn500Sgres1Cascade3C,
                "СГРЭС-1 каск4 А" to oru500Data.tn500Sgres1Cascade4A,
                "СГРЭС-1 каск4 В" to oru500Data.tn500Sgres1Cascade4B,
                "СГРЭС-1 каск4 С" to oru500Data.tn500Sgres1Cascade4C,

                // ОРУ-500: Трачуковская 1ТН
                "Трачук 1ТН масло А" to oru500Data.oil1tnTrachukovskayaA,
                "Трачук 1ТН масло В" to oru500Data.oil1tnTrachukovskayaB,
                "Трачук 1ТН масло С" to oru500Data.oil1tnTrachukovskayaC,

                // ОРУ-500: Трачуковская 2ТН
                "Трачук 2ТН масло А" to oru500Data.oil2tnTrachukovskayaA,
                "Трачук 2ТН масло В" to oru500Data.oil2tnTrachukovskayaB,
                "Трачук 2ТН масло С" to oru500Data.oil2tnTrachukovskayaC,

                // ОРУ-500: Белозёрная 2ТН
                "Белозёрная 2ТН масло А" to oru500Data.oil2tnBelozernayaA,
                "Белозёрная 2ТН масло В" to oru500Data.oil2tnBelozernayaB,
                "Белозёрная 2ТН масло С" to oru500Data.oil2tnBelozernayaC,

                // 2 АТГ ф.С
                "2 АТГ С бак" to atgData.atg2_c_oil_tank,
                "2 АТГ С РПН" to atgData.atg2_c_oil_rpn,
                "2 АТГ С давл500" to atgData.atg2_c_pressure_500,
                "2 АТГ С давл220" to atgData.atg2_c_pressure_220,
                "2 АТГ С ТС1" to atgData.atg2_c_temp_ts1,
                "2 АТГ С ТС2" to atgData.atg2_c_temp_ts2,
                "2 АТГ С насос1" to atgData.atg2_c_pump_group1,
                "2 АТГ С насос2" to atgData.atg2_c_pump_group2,
                "2 АТГ С насос3" to atgData.atg2_c_pump_group3,
                "2 АТГ С насос4" to atgData.atg2_c_pump_group4,

                // 2 АТГ ф.В
                "2 АТГ В бак" to atgData.atg2_b_oil_tank,
                "2 АТГ В РПН" to atgData.atg2_b_oil_rpn,
                "2 АТГ В давл500" to atgData.atg2_b_pressure_500,
                "2 АТГ В давл220" to atgData.atg2_b_pressure_220,
                "2 АТГ В ТС1" to atgData.atg2_b_temp_ts1,
                "2 АТГ В ТС2" to atgData.atg2_b_temp_ts2,
                "2 АТГ В насос1" to atgData.atg2_b_pump_group1,
                "2 АТГ В насос2" to atgData.atg2_b_pump_group2,
                "2 АТГ В насос3" to atgData.atg2_b_pump_group3,
                "2 АТГ В насос4" to atgData.atg2_b_pump_group4,

                // 2 АТГ ф.А
                "2 АТГ А бак" to atgData.atg2_a_oil_tank,
                "2 АТГ А РПН" to atgData.atg2_a_oil_rpn,
                "2 АТГ А давл500" to atgData.atg2_a_pressure_500,
                "2 АТГ А давл220" to atgData.atg2_a_pressure_220,
                "2 АТГ А ТС1" to atgData.atg2_a_temp_ts1,
                "2 АТГ А ТС2" to atgData.atg2_a_temp_ts2,
                "2 АТГ А насос1" to atgData.atg2_a_pump_group1,
                "2 АТГ А насос2" to atgData.atg2_a_pump_group2,
                "2 АТГ А насос3" to atgData.atg2_a_pump_group3,
                "2 АТГ А насос4" to atgData.atg2_a_pump_group4,

                // АТГ резервная
                "АТГ рез бак" to atgData.atg_reserve_oil_tank,
                "АТГ рез РПН" to atgData.atg_reserve_oil_rpn,
                "АТГ рез давл500" to atgData.atg_reserve_pressure_500,
                "АТГ рез давл220" to atgData.atg_reserve_pressure_220,
                "АТГ рез ТС1" to atgData.atg_reserve_temp_ts1,
                "АТГ рез ТС2" to atgData.atg_reserve_temp_ts2,
                "АТГ рез насос1" to atgData.atg_reserve_pump_group1,
                "АТГ рез насос2" to atgData.atg_reserve_pump_group2,
                "АТГ рез насос3" to atgData.atg_reserve_pump_group3,
                "АТГ рез насос4" to atgData.atg_reserve_pump_group4,

                // ТН-35
                "ТН-35 2АТГ" to atgData.tn352atg,
                "ТН-35 3АТГ" to atgData.tn353atg,

                // 3 АТГ ф.С
                "3 АТГ С бак" to atgData.atg3_c_oil_tank,
                "3 АТГ С РПН" to atgData.atg3_c_oil_rpn,
                "3 АТГ С давл500" to atgData.atg3_c_pressure_500,
                "3 АТГ С давл220" to atgData.atg3_c_pressure_220,
                "3 АТГ С ТС1" to atgData.atg3_c_temp_ts1,
                "3 АТГ С ТС2" to atgData.atg3_c_temp_ts2,
                "3 АТГ С насос1" to atgData.atg3_c_pump_group1,
                "3 АТГ С насос2" to atgData.atg3_c_pump_group2,
                "3 АТГ С насос3" to atgData.atg3_c_pump_group3,
                "3 АТГ С насос4" to atgData.atg3_c_pump_group4,

                // 3 АТГ ф.В
                "3 АТГ В бак" to atgData.atg3_b_oil_tank,
                "3 АТГ В РПН" to atgData.atg3_b_oil_rpn,
                "3 АТГ В давл500" to atgData.atg3_b_pressure_500,
                "3 АТГ В давл220" to atgData.atg3_b_pressure_220,
                "3 АТГ В ТС1" to atgData.atg3_b_temp_ts1,
                "3 АТГ В ТС2" to atgData.atg3_b_temp_ts2,
                "3 АТГ В насос1" to atgData.atg3_b_pump_group1,
                "3 АТГ В насос2" to atgData.atg3_b_pump_group2,
                "3 АТГ В насос3" to atgData.atg3_b_pump_group3,
                "3 АТГ В насос4" to atgData.atg3_b_pump_group4,

                // 3 АТГ ф.А
                "3 АТГ А бак" to atgData.atg3_a_oil_tank,
                "3 АТГ А РПН" to atgData.atg3_a_oil_rpn,
                "3 АТГ А давл500" to atgData.atg3_a_pressure_500,
                "3 АТГ А давл220" to atgData.atg3_a_pressure_220,
                "3 АТГ А ТС1" to atgData.atg3_a_temp_ts1,
                "3 АТГ А ТС2" to atgData.atg3_a_temp_ts2,
                "3 АТГ А насос1" to atgData.atg3_a_pump_group1,
                "3 АТГ А насос2" to atgData.atg3_a_pump_group2,
                "3 АТГ А насос3" to atgData.atg3_a_pump_group3,
                "3 АТГ А насос4" to atgData.atg3_a_pump_group4,

                // Реактор ф.С
                "Реактор С бак" to atgData.reactor_c_oil_tank,
                "Реактор С давл500" to atgData.reactor_c_pressure_500,
                "Реактор С ТС" to atgData.reactor_c_temp_ts,
                "Реактор С насос1" to atgData.reactor_c_pump_group1,
                "Реактор С насос2" to atgData.reactor_c_pump_group2,
                "Реактор С насос3" to atgData.reactor_c_pump_group3,
                "Реактор С ТТ нейтр" to atgData.reactor_c_tt_neutral,

                // Реактор ф.В
                "Реактор В бак" to atgData.reactor_b_oil_tank,
                "Реактор В давл500" to atgData.reactor_b_pressure_500,
                "Реактор В ТС" to atgData.reactor_b_temp_ts,
                "Реактор В насос1" to atgData.reactor_b_pump_group1,
                "Реактор В насос2" to atgData.reactor_b_pump_group2,
                "Реактор В насос3" to atgData.reactor_b_pump_group3,
                "Реактор В ТТ нейтр" to atgData.reactor_b_tt_neutral,

                // Реактор ф.А
                "Реактор А бак" to atgData.reactor_a_oil_tank,
                "Реактор А давл500" to atgData.reactor_a_pressure_500,
                "Реактор А ТС" to atgData.reactor_a_temp_ts,
                "Реактор А насос1" to atgData.reactor_a_pump_group1,
                "Реактор А насос2" to atgData.reactor_a_pump_group2,
                "Реактор А насос3" to atgData.reactor_a_pump_group3,
                "Реактор А ТТ нейтр" to atgData.reactor_a_tt_neutral,

                // Здания: Компрессорная №1
                "Компр1 арматура" to buildingsData.compressor1Valve,
                "Компр1 обогрев" to buildingsData.compressor1Heating,
                "Компр1 темп" to buildingsData.compressor1Temp,

                // Здания: Баллонная №1
                "Баллон1 арматура" to buildingsData.ballroom1Valve,
                "Баллон1 обогрев" to buildingsData.ballroom1Heating,
                "Баллон1 темп" to buildingsData.ballroom1Temp,

                // Здания: Компрессорная №2
                "Компр2 арматура" to buildingsData.compressor2Valve,
                "Компр2 обогрев" to buildingsData.compressor2Heating,
                "Компр2 темп" to buildingsData.compressor2Temp,

                // Здания: Баллонная №2
                "Баллон2 арматура" to buildingsData.ballroom2Valve,
                "Баллон2 обогрев" to buildingsData.ballroom2Heating,
                "Баллон2 темп" to buildingsData.ballroom2Temp,

                // Здания: КПЗ ОПУ
                "КПЗ ОПУ арматура" to buildingsData.kpzOpuValve,
                "КПЗ ОПУ обогрев" to buildingsData.kpzOpuHeating,
                "КПЗ ОПУ темп" to buildingsData.kpzOpuTemp,

                // Здания: КПЗ-2
                "КПЗ-2 арматура" to buildingsData.kpz2Valve,
                "КПЗ-2 обогрев" to buildingsData.kpz2Heating,
                "КПЗ-2 темп" to buildingsData.kpz2Temp,

                // Здания: Насосная пожаротушения
                "НПТ арматура" to buildingsData.firePumpValve,
                "НПТ обогрев" to buildingsData.firePumpHeating,
                "НПТ темп" to buildingsData.firePumpTemp,
                "НПТ уровень воды" to buildingsData.firePumpWaterLevel,

                // Здания: Мастерская
                "Мастерская обогрев" to buildingsData.workshopHeating,
                "Мастерская темп" to buildingsData.workshopTemp,

                // Здания: Артскважина
                "Артскважина обогрев" to buildingsData.artWellHeating,

                // Здания: Артезианская скважина
                "Артез скважина обогрев" to buildingsData.artesianWellHeating,

                // Здания: Помещение АБ
                "Помещ АБ обогрев" to buildingsData.roomAbHeating,
                "Помещ АБ темп" to buildingsData.roomAbTemp,

                // Здания: Подвал
                "Подвал обогрев" to buildingsData.basementHeating,
                "Подвал темп" to buildingsData.basementTemp
            )

            CoroutineScope(Dispatchers.IO).launch {
                val success = sheetsService.uploadInspection(data)
                if (success) {
                    Log.d("GoogleSheets", "✅ Данные отправлены в Google Sheets")
                } else {
                    Log.d("GoogleSheets", "❌ Ошибка отправки в Google Sheets")
                }
            }

            // 🔧 ОДНА СТРОКА вместо 150 строк!
            uploadCommentsToServer(sheetsService, currentDate, currentTime, user.name)

        } catch (e: Exception) {
            Log.e("GoogleSheets", "Ошибка отправки в Google Sheets", e)
            e.printStackTrace()
        }
    }


    /**
     * Отправляет ВСЕ комментарии из локального хранилища на сервер
     * Проверяет дубликаты по timestamp перед отправкой
     */
    private fun uploadCommentsToServer(
        sheetsService: GoogleSheetsService,
        currentDate: String,
        currentTime: String,
        inspectorName: String
    ) {
        Log.d("COMMENTS_DEBUG", "=== СБОР КОММЕНТАРИЕВ ИЗ ХРАНИЛИЩА ===")

        val commentStorage = CommentStorageManager(context)
        val allComments = commentStorage.loadAllComments()

        if (allComments.isEmpty()) {
            Log.d("COMMENTS_DEBUG", "📝 Нет комментариев в хранилище")
            return
        }

        val commentsData = mutableListOf<Map<String, String>>()

        for ((key, comments) in allComments) {
            val section = when {
                key.startsWith("ORU35_") -> "ОРУ-35"
                key.startsWith("ORU220_") -> "ОРУ-220"
                key.startsWith("ORU500_") -> "ОРУ-500"
                key.startsWith("ATG_") -> "АТГ"
                key.startsWith("BUILDINGS_") -> "Здания"
                else -> continue
            }
            val equipment = key.substringAfter("_")

            for (comment in comments) {
                if (!comment.text.isNullOrBlank()) {
                    commentsData.add(mapOf(
                        "Секция" to section,
                        "Оборудование" to equipment,
                        "Комментарий" to comment.text,
                        "Дата создания" to comment.getFormattedTime(),
                        "Автор" to (comment.author ?: inspectorName),
                        "Timestamp" to comment.timestamp.toString()
                    ))
                }
            }
        }

        Log.d("COMMENTS_DEBUG", "Всего собрано: ${commentsData.size} комментариев")

        if (commentsData.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                // 🔧 Загружаем существующие timestamp с сервера
                val existingTimestamps = try {
                    sheetsService.getAllComments()?.mapNotNull {
                        it["Timestamp"]?.toLongOrNull()
                    }?.toSet() ?: emptySet()
                } catch (e: Exception) {
                    Log.e("COMMENTS_DEBUG", "Ошибка загрузки существующих timestamp", e)
                    emptySet()
                }

                Log.d("COMMENTS_DEBUG", "Существующих timestamp на сервере: ${existingTimestamps.size}")

                // 🔧 Отправляем только новые (по Timestamp)
                val newComments = commentsData.filter {
                    val ts = it["Timestamp"]?.toLongOrNull()
                    val isNew = ts != null && ts !in existingTimestamps
                    if (!isNew) {
                        Log.d("COMMENTS_DEBUG", "⏭️ Дубликат: [${it["Секция"]}] ${it["Оборудование"]} (ts=$ts)")
                    }
                    isNew
                }

                Log.d("COMMENTS_DEBUG", "Новых для отправки: ${newComments.size} (из ${commentsData.size})")

                if (newComments.isNotEmpty()) {
                    val success = sheetsService.uploadComments(newComments)
                    Log.d("COMMENTS_DEBUG", if (success) "✅ Отправлены на сервер" else "❌ Ошибка отправки")
                } else {
                    Log.d("COMMENTS_DEBUG", "📝 Все комментарии уже есть на сервере")
                }
            }
        }
    }

    /**
     * Запоминает все ячейки с "---" из шаблона ДО заполнения данными
     */
    private fun rememberDashCells(sheet: Sheet) {
        dashCells.clear()
        for (rowNum in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue
            for (colNum in 0 until row.lastCellNum.toInt()) {
                val cell = row.getCell(colNum)
                if (cell != null && cell.cellType == CellType.STRING && cell.stringCellValue.trim() == "---") {
                    dashCells.add(Pair(rowNum, colNum))
                }
            }
        }
    }

    /**
     * Восстанавливает "---" только в тех ячейках, где они были в шаблоне,
     * но сейчас пустые (не были заполнены данными)
     */
    private fun restoreDashCells(sheet: Sheet) {
        for ((rowNum, colNum) in dashCells) {
            val row = sheet.getRow(rowNum) ?: continue
            val cell = row.getCell(colNum)

            // Восстанавливаем "---" только если ячейка пустая
            if (cell == null || cell.cellType == CellType.BLANK ||
                (cell.cellType == CellType.STRING && cell.stringCellValue.isBlank())) {

                val targetCell = cell ?: row.createCell(colNum)
                targetCell.setCellValue("---")
            }
            // Если ячейка уже заполнена данными — НЕ трогаем
        }
    }
}