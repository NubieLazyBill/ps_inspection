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
import com.example.ps_inspection.data.repositories.InspectionArchiveData
import com.example.ps_inspection.data.repositories.InspectionArchiveManager
import com.example.ps_inspection.data.repositories.LastInspectionManager


class ExcelExportService(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportToExcel(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = ""  // ← Добавить
    ): Uri? {
        // 🔒 ШАГ 1: СНАЧАЛА сохраняем данные ВЕЗДЕ
        saveAllDataEverywhere(oru35Data, oru220Data, atgData, oru500Data, buildingsData)

        // 🔒 ШАГ 2: ТОЛЬКО ПОТОМ пытаемся создать Excel
        return try {
            val inputStream: InputStream = context.assets.open("blanks_template.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // 🔧 ШАГ 3: Запоминаем где были прочерки в шаблоне
            rememberDashCells(sheet)

            // ШАГ 4: Заполняем данные
            fillDataToTemplate(sheet, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            // 🔧 ШАГ 5: Восстанавливаем прочерки только где не заполнили
            restoreDashCells(sheet)

            // ШАГ 6: Добавляем лист с комментариями
            addCommentsSheet(workbook, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            fillDataToTemplate(sheet, oru35Data, oru220Data, atgData, oru500Data, buildingsData, outdoorTemp)

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

            // 🔧 Запоминаем прочерки из шаблона
            rememberDashCells(sheet)

            // Заполняем данные из архива
            fillDataToTemplate(sheet,
                archiveData.oru35,
                archiveData.oru220,
                archiveData.atg,
                archiveData.oru500,
                archiveData.buildings
                // температура для архива не передаётся
            )

            // 🔧 Восстанавливаем прочерки
            restoreDashCells(sheet)

            // Добавляем комментарии из архива
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

        val headerRow = commentsSheet.createRow(0)
        headerRow.createCell(0).setCellValue("Оборудование")
        headerRow.createCell(1).setCellValue("Комментарий")

        var rowNum = 1

        fun addComment(equipment: String, comment: String) {
            if (comment.isNotBlank()) {
                val row = commentsSheet.createRow(rowNum++)
                row.createCell(0).setCellValue(equipment)
                row.createCell(1).setCellValue(comment)
            }
        }

        // ОРУ-35
        addComment("ТСН", oru35Data.commentTsn)
        addComment("ТТ-35 2ТСН", oru35Data.commentTt352)
        addComment("ТТ-35 3ТСН", oru35Data.commentTt353)
        addComment("В-35 2ТСН", oru35Data.commentV352)
        addComment("В-35 3ТСН", oru35Data.commentV353)

        // ОРУ-220
        addComment("Мирная", oru220Data.commentMirnaya)
        addComment("Мирная ТТ", oru220Data.commentMirnayaTT)
        addComment("Топаз", oru220Data.commentTopaz)
        addComment("Топаз ТТ", oru220Data.commentTopazTT)
        addComment("ОВ", oru220Data.commentOv)
        addComment("ОВ ТТ", oru220Data.commentOvTT)
        addComment("ТН-220 ОСШ", oru220Data.commentOssh)
        addComment("2АТГ", oru220Data.commentV2atg)
        addComment("2АТГ ТТ", oru220Data.commentV2atgTT)
        addComment("ШСВ", oru220Data.commentShsv)
        addComment("ШСВ ТТ", oru220Data.commentShsvTT)
        addComment("3АТГ", oru220Data.commentV3atg)
        addComment("3АТГ ТТ", oru220Data.commentV3atgTT)
        addComment("Орбита", oru220Data.commentOrbita)
        addComment("Орбита ТТ", oru220Data.commentOrbitaTT)
        addComment("Факел", oru220Data.commentFakel)
        addComment("Факел ТТ", oru220Data.commentFakelTT)
        addComment("Комета-1", oru220Data.commentCometa1)
        addComment("Комета-1 ТТ", oru220Data.commentCometa1TT)
        addComment("Комета-2", oru220Data.commentCometa2)
        addComment("Комета-2 ТТ", oru220Data.commentCometa2TT)
        addComment("1ТН-220", oru220Data.commentTn1)
        addComment("2ТН-220", oru220Data.commentTn2)

        // ОРУ-500
        addComment("В-500 Р-500 2С", oru500Data.commentR5002s)
        addComment("В-500 ВШТ-31", oru500Data.commentVsht31)
        addComment("В-500 ВЛТ-30", oru500Data.commentVlt30)
        addComment("В-500 ВШЛ-32", oru500Data.commentVshl32)
        addComment("В-500 ВШЛ-21", oru500Data.commentVshl21)
        addComment("В-500 ВШТ-22", oru500Data.commentVsht22)
        addComment("В-500 ВЛТ-20", oru500Data.commentVlt20)
        addComment("В-500 ВШТ-11", oru500Data.commentVsht11)
        addComment("В-500 ВШЛ-12", oru500Data.commentVshl12)
        addComment("ТТ-500 ВШТ-31", oru500Data.commentTtVsht31)
        addComment("ТТ-500 ВЛТ-30", oru500Data.commentTtVlt30)
        addComment("ТТ-500 ВШЛ-32", oru500Data.commentTtVshl32)
        addComment("ТТ-500 ВШЛ-21", oru500Data.commentTtVshl21)
        addComment("ТТ-500 ВШТ-22", oru500Data.commentTtVsht22)
        addComment("ТТ-500 ВЛТ-20", oru500Data.commentTtVlt20)
        addComment("ТТ-500 ВШТ-11", oru500Data.commentTtVsht11)
        addComment("ТТ-500 ВШЛ-12", oru500Data.commentTtVshl12)
        addComment("1ТН-500", oru500Data.commentTn1500)
        addComment("2ТН-500", oru500Data.commentTn2500)
        addComment("ТН-500 СГРЭС-1", oru500Data.commentTn500Sgres1)
        addComment("Трачуковская ТТ", oru500Data.commentTrachukovskayaTt)
        addComment("Трачуковская 2ТН", oru500Data.commentTrachukovskaya2tn)
        addComment("Трачуковская 1ТН", oru500Data.commentTrachukovskaya1tn)
        addComment("Белозёрная 2ТН", oru500Data.commentBelozernaya2tn)

        // АТГ
        addComment("2 АТГ ф.С", atgData.commentAtg2C)
        addComment("2 АТГ ф.В", atgData.commentAtg2B)
        addComment("2 АТГ ф.А", atgData.commentAtg2A)
        addComment("АТГ резервная", atgData.commentAtgReserve)
        addComment("3 АТГ ф.С", atgData.commentAtg3C)
        addComment("3 АТГ ф.В", atgData.commentAtg3B)
        addComment("3 АТГ ф.А", atgData.commentAtg3A)
        addComment("Реактор ф.С", atgData.commentReactorC)
        addComment("Реактор ф.В", atgData.commentReactorB)
        addComment("Реактор ф.А", atgData.commentReactorA)
        addComment("ТН-35", atgData.commentTn35)

        // Здания
        addComment("Компрессорная №1", buildingsData.commentCompressor1)
        addComment("Баллонная №1", buildingsData.commentBallroom1)
        addComment("Компрессорная №2", buildingsData.commentCompressor2)
        addComment("Баллонная №2", buildingsData.commentBallroom2)
        addComment("КПЗ ОПУ", buildingsData.commentKpzOpu)
        addComment("КПЗ-2", buildingsData.commentKpz2)
        addComment("Насосная пожаротушения", buildingsData.commentFirePump)
        addComment("Мастерская по ремонту ВВ", buildingsData.commentWorkshop)
        addComment("Артскважина", buildingsData.commentArtWell)
        addComment("Здание артезианской скважины", buildingsData.commentArtesianWell)
        addComment("Помещение 1 (2) АБ", buildingsData.commentRoomAb)
        addComment("Помещение п/этажа №1,2,3", buildingsData.commentBasement)

        // 🔧 Вручную устанавливаем ширину колонок (вместо autoSizeColumn, который не работает на Android)
        commentsSheet.setColumnWidth(0, 30 * 256)   // "Оборудование" - 30 символов
        commentsSheet.setColumnWidth(1, 50 * 256)   // "Комментарий" - 50 символов
    }

    private fun fillDataToTemplate(
        sheet: Sheet,
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = ""  // ← Добавить
    ) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        Log.d("ExcelExport", "=== fillDataToTemplate START ===")

        setCellValue(sheet, 0, 1, currentDate)
        setCellValue(sheet, 0, 14, currentDate)

        // t нв
        if (outdoorTemp.isNotBlank()) {
            setCellValue(sheet, 0, 18, outdoorTemp)
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
    }

    private fun setCellValue(sheet: Sheet, rowNum: Int, colNum: Int, value: String) {
        try {
            val row = sheet.getRow(rowNum) ?: sheet.createRow(rowNum)
            val cell = row.getCell(colNum) ?: row.createCell(colNum)
            cell.setCellValue(value)
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

    fun exportToExcelLegacy(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = ""
    ): Uri? {
        // 🔒 СНАЧАЛА сохраняем данные ВЕЗДЕ
        saveAllDataEverywhere(oru35Data, oru220Data, atgData, oru500Data, buildingsData)

        return try {
            val inputStream: InputStream = context.assets.open("blanks_template.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // 🔧 Запоминаем прочерки из шаблона
            rememberDashCells(sheet)

            // Заполняем данные
            fillDataToTemplate(sheet, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            // 🔧 Восстанавливаем прочерки
            restoreDashCells(sheet)

            // Добавляем комментарии
            addCommentsSheet(workbook, oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            val uri = saveWorkbookLegacy(workbook, inputStream)
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        buildingsData: InspectionBuildingsData
    ) {
        try {
            val lastInspectionManager = LastInspectionManager(context)
            lastInspectionManager.saveLastInspection(oru35Data, oru220Data, atgData, oru500Data, buildingsData)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val archiveManager = InspectionArchiveManager(context)
            archiveManager.saveToArchive(oru35Data, oru220Data, atgData, oru500Data, buildingsData)
        } catch (e: Exception) {
            e.printStackTrace()
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