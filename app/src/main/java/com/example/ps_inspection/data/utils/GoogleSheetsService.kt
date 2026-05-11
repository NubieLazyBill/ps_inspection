package com.example.ps_inspection.data.services

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.api.services.sheets.v4.model.*
import java.io.IOException

class GoogleSheetsService(private val context: Context) {

    companion object {
        private const val SPREADSHEET_ID = "1nLCzhELUrG2crhIzrBmCitXIGFJGf5Tl0PQuLNSy5cI"
        private const val SHEET_NAME = "Осмотры"
        private const val CREDENTIALS_FILE = "service_account.json"

        // 🔧 Таймауты в миллисекундах
        private const val CONNECT_TIMEOUT_MS = 30000  // 30 секунд
        private const val READ_TIMEOUT_MS = 30000     // 30 секунд
    }

    private fun getSheetsService(): Sheets {
        val credentials = GoogleCredentials.fromStream(
            context.assets.open(CREDENTIALS_FILE)
        ).createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        // 🔧 Создаём инициализатор запросов с таймаутами
        val requestInitializer = HttpRequestInitializer { request ->
            val adapter = HttpCredentialsAdapter(credentials)
            adapter.initialize(request)
            request.connectTimeout = CONNECT_TIMEOUT_MS
            request.readTimeout = READ_TIMEOUT_MS
        }

        return Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
            .setApplicationName("PS Inspection")
            .build()
    }

    suspend fun uploadInspection(data: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sheetsService = getSheetsService()
                val values = listOf(data.values.toList())
                val body = ValueRange().setValues(values)

                sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, "$SHEET_NAME!A:MP", body)
                    .setValueInputOption("RAW")
                    .execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun getAllInspections(): List<Map<String, String>>? {
        return withContext(Dispatchers.IO) {
            try {
                val sheetsService = getSheetsService()
                val result = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, "$SHEET_NAME!A:MP")
                    .execute()

                val values = result.getValues() ?: run {
                    Log.d("SHEETS_DEBUG", "values == null")
                    return@withContext emptyList()
                }

                if (values.isEmpty()) {
                    Log.d("SHEETS_DEBUG", "values пустой")
                    return@withContext emptyList()
                }

                val headers = values[0].map { it.toString() }
                Log.d("SHEETS_DEBUG", "Заголовков: ${headers.size}")

                // Данные
                val data = values.drop(1).map { row ->
                    headers.mapIndexed { index, header ->
                        header to (row.getOrNull(index)?.toString() ?: "")
                    }.toMap()
                }

                Log.d("SHEETS_DEBUG", "Осмотров загружено: ${data.size}")
                data
            } catch (e: IOException) {
                Log.e("SHEETS_DEBUG", "Ошибка сети: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e("SHEETS_DEBUG", "Ошибка загрузки", e)
                null
            }
        }
    }

    suspend fun deleteRow(rowIndex: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sheetsService = getSheetsService()
                val spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute()
                val sheet = spreadsheet.sheets.firstOrNull { it.properties.title == SHEET_NAME }
                    ?: return@withContext false
                val sheetId = sheet.properties.sheetId

                val request = com.google.api.services.sheets.v4.model.Request()
                    .setDeleteDimension(
                        com.google.api.services.sheets.v4.model.DeleteDimensionRequest()
                            .setRange(
                                com.google.api.services.sheets.v4.model.DimensionRange()
                                    .setSheetId(sheetId)
                                    .setDimension("ROWS")
                                    .setStartIndex(rowIndex)
                                    .setEndIndex(rowIndex + 1)
                            )
                    )

                val batchRequest = com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
                    .setRequests(listOf(request))

                sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}