package com.example.ps_inspection.data.services

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


class GoogleSheetsService(private val context: Context) {

    companion object {
        private const val SPREADSHEET_ID = "1nLCzhELUrG2crhIzrBmCitXIGFJGf5Tl0PQuLNSy5cI"
        private const val SHEET_NAME = "Осмотры"
        private const val COMMENTS_SHEET_NAME = "Комментарии"  // 🔧 Новый лист
        private const val CREDENTIALS_FILE = "service_account.json"
        private const val CONNECT_TIMEOUT_MS = 30000
        private const val READ_TIMEOUT_MS = 30000
    }

    private fun getSheetsService(): Sheets {
        val credentials = GoogleCredentials.fromStream(
            context.assets.open(CREDENTIALS_FILE)
        ).createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

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

    // ========== ОСНОВНЫЕ ОСМОТРЫ ==========

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

                val values = result.getValues() ?: return@withContext emptyList()
                if (values.isEmpty()) return@withContext emptyList()

                val headers = values[0].map { it.toString() }
                val data = values.drop(1).map { row ->
                    headers.mapIndexed { index, header ->
                        header to (row.getOrNull(index)?.toString() ?: "")
                    }.toMap()
                }
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

                val request = Request()
                    .setDeleteDimension(
                        DeleteDimensionRequest()
                            .setRange(
                                DimensionRange()
                                    .setSheetId(sheetId)
                                    .setDimension("ROWS")
                                    .setStartIndex(rowIndex)
                                    .setEndIndex(rowIndex + 1)
                            )
                    )

                val batchRequest = BatchUpdateSpreadsheetRequest()
                    .setRequests(listOf(request))

                sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // ========== КОММЕНТАРИИ (НОВЫЙ ЛИСТ) ==========

    /**
     * Отправляет комментарии на отдельный лист "Комментарии"
     */
    suspend fun uploadComments(commentsData: List<Map<String, String>>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("COMMENTS_DEBUG", "uploadComments: получено ${commentsData.size} записей")
                Log.d("COMMENTS_DEBUG", "Первая запись: ${commentsData.firstOrNull()}")

                val sheetsService = getSheetsService()

                // Убеждаемся, что лист существует и имеет заголовки
                ensureCommentsSheetExists(sheetsService)

                val values = commentsData.map { it.values.toList() }
                Log.d("COMMENTS_DEBUG", "Значения для отправки: ${values.firstOrNull()}")

                val body = ValueRange().setValues(values)

                val response = sheetsService.spreadsheets().values()
                    .append(SPREADSHEET_ID, "$COMMENTS_SHEET_NAME!A:E", body)
                    .setValueInputOption("RAW")
                    .execute()

                Log.d("COMMENTS_DEBUG", "Ответ от сервера: ${response.spreadsheetId}")
                Log.d("COMMENTS_DEBUG", "Отправлено ${commentsData.size} комментариев")
                true
            } catch (e: Exception) {
                Log.e("COMMENTS_DEBUG", "Ошибка отправки комментариев: ${e.message}", e)
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Загружает все комментарии с листа "Комментарии"
     */
    suspend fun getAllComments(): List<Map<String, String>>? {
        return withContext(Dispatchers.IO) {
            try {
                val sheetsService = getSheetsService()

                // Проверяем существует ли лист
                val spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute()
                val hasCommentsSheet = spreadsheet.sheets.any {
                    it.properties.title == COMMENTS_SHEET_NAME
                }

                if (!hasCommentsSheet) {
                    Log.d("COMMENTS_DEBUG", "Лист комментариев не существует, создаю...")
                    ensureCommentsSheetExists(sheetsService)
                    return@withContext emptyList()
                }

                val result = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, "$COMMENTS_SHEET_NAME!A:E")
                    .execute()

                val values = result.getValues() ?: return@withContext emptyList()
                if (values.size < 2) return@withContext emptyList()

                val headers = values[0].map { it.toString() }
                val data = values.drop(1).map { row ->
                    headers.mapIndexed { index, header ->
                        header to (row.getOrNull(index)?.toString() ?: "")
                    }.toMap()
                }
                Log.d("COMMENTS_DEBUG", "Загружено ${data.size} комментариев")
                data
            } catch (e: Exception) {
                Log.e("COMMENTS_DEBUG", "Ошибка загрузки комментариев", e)
                null
            }
        }
    }

    /**
     * Удаляет комментарии для конкретного осмотра (по дате и времени)
     */
    suspend fun deleteCommentsByDateTime(date: String, time: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val allComments = getAllComments() ?: return@withContext false

                // Находим индексы строк для удаления
                val indicesToDelete = mutableListOf<Int>()
                allComments.forEachIndexed { index, comment ->
                    if (comment["Дата"] == date && comment["Время"] == time) {
                        indicesToDelete.add(index + 1) // +1 потому что первая строка - заголовки
                    }
                }

                if (indicesToDelete.isEmpty()) return@withContext false

                // Удаляем с конца, чтобы индексы не сбивались
                indicesToDelete.sortedDescending().forEach { rowIndex ->
                    deleteCommentRow(rowIndex)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Удаляет строку из листа комментариев
     */
    private suspend fun deleteCommentRow(rowIndex: Int) {
        withContext(Dispatchers.IO) {
            try {
                val sheetsService = getSheetsService()
                val spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute()
                val sheet = spreadsheet.sheets.firstOrNull {
                    it.properties.title == COMMENTS_SHEET_NAME
                } ?: return@withContext
                val sheetId = sheet.properties.sheetId

                val request = Request()
                    .setDeleteDimension(
                        DeleteDimensionRequest()
                            .setRange(
                                DimensionRange()
                                    .setSheetId(sheetId)
                                    .setDimension("ROWS")
                                    .setStartIndex(rowIndex)
                                    .setEndIndex(rowIndex + 1)
                            )
                    )

                val batchRequest = BatchUpdateSpreadsheetRequest()
                    .setRequests(listOf(request))

                sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute()
            } catch (e: Exception) {
                Log.e("COMMENTS_DEBUG", "Ошибка удаления строки $rowIndex", e)
            }
        }
    }

    /**
     * Создаёт лист "Комментарии" если его ещё нет
     */
    private suspend fun ensureCommentsSheetExists(sheetsService: Sheets) {
        try {
            val spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute()
            val hasCommentsSheet = spreadsheet.sheets.any {
                it.properties.title == COMMENTS_SHEET_NAME
            }

            if (!hasCommentsSheet) {
                Log.d("COMMENTS_DEBUG", "Создаю лист Комментарии...")

                val addSheetRequest = Request()
                    .setAddSheet(
                        AddSheetRequest()
                            .setProperties(
                                SheetProperties()
                                    .setTitle(COMMENTS_SHEET_NAME)
                            )
                    )

                val batchRequest = BatchUpdateSpreadsheetRequest()
                    .setRequests(listOf(addSheetRequest))

                sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute()

                // 🔧 Небольшая задержка чтобы лист точно создался
                kotlinx.coroutines.delay(500)

                val headers = listOf(listOf("Секция", "Оборудование", "Комментарий", "Дата создания", "Автор", "Timestamp"))
                val body = ValueRange().setValues(headers)

                sheetsService.spreadsheets().values()
                    .update(SPREADSHEET_ID, "$COMMENTS_SHEET_NAME!A1:F1", body)
                    .setValueInputOption("RAW")
                    .execute()

                Log.d("COMMENTS_DEBUG", "Лист Комментарии создан с заголовками")
            }
        } catch (e: Exception) {
            Log.e("COMMENTS_DEBUG", "Ошибка создания листа комментариев", e)
        }
    }

    /**
     * Удаляет все строки комментариев (оставляет только заголовки)
     */
    suspend fun clearAllComments(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val allComments = getAllComments() ?: return@withContext false
                if (allComments.isEmpty()) return@withContext true

                // Удаляем все строки кроме заголовка (индексы 1..n)
                for (i in allComments.size downTo 1) {
                    deleteCommentRow(i)
                    kotlinx.coroutines.delay(100) // Небольшая задержка между запросами
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun getLastValuesForParameters(parameterKeys: List<String>): Map<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                val allData = getAllInspections()
                if (allData.isNullOrEmpty()) return@withContext emptyMap()

                // Сортируем по дате (последний осмотр будет последним)
                val sortedData = allData.sortedBy { it["Дата"] + " " + it["Время"] }
                val lastInspection = sortedData.lastOrNull() ?: return@withContext emptyMap()

                val result = mutableMapOf<String, String>()
                for (key in parameterKeys) {
                    val value = lastInspection[key]?.trim()
                    if (!value.isNullOrBlank() && value != "-" && value != "○") {
                        result[key] = value
                    }
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }
}