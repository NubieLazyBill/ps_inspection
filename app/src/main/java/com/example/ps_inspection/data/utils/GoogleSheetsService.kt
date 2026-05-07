package com.example.ps_inspection.data.services

import android.content.Context
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import com.google.api.services.sheets.v4.model.*
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.Request

class GoogleSheetsService(private val context: Context) {

    companion object {
        private const val SPREADSHEET_ID = "1nLCzhELUrG2crhIzrBmCitXIGFJGf5Tl0PQuLNSy5cI"
        private const val SHEET_NAME = "Осмотры"
        private const val CREDENTIALS_FILE = "service_account.json" // ← Положить в assets/
    }

    private fun getSheetsService(): Sheets {
        val credentials = GoogleCredentials.fromStream(
            context.assets.open(CREDENTIALS_FILE)
        ).createScoped(listOf("https://www.googleapis.com/auth/spreadsheets"))

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        return Sheets.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(credentials))
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
                    .append(SPREADSHEET_ID, "$SHEET_NAME!A:Z", body)
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
                    .get(SPREADSHEET_ID, "$SHEET_NAME!A:IY")
                    .execute()

                val values = result.getValues() ?: return@withContext null
                if (values.isEmpty()) return@withContext null

                val headers = values[0].map { it.toString() }

                values.drop(1).map { row ->
                    headers.mapIndexed { index, header ->
                        header to (row.getOrNull(index)?.toString() ?: "")
                    }.toMap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun deleteRow(rowIndex: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sheetsService = getSheetsService()

                // Сначала получаем sheetId
                val spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute()
                val sheetId = spreadsheet.sheets.first { it.properties.title == SHEET_NAME }.properties.sheetId

                // Удаляем строку (rowIndex = номер строки, которую нужно удалить)
                val request = DeleteDimensionRequest()
                    .setRange(
                        DimensionRange()
                            .setSheetId(sheetId)
                            .setDimension("ROWS")
                            .setStartIndex(rowIndex)
                            .setEndIndex(rowIndex + 1)
                    )

                val batchRequest = BatchUpdateSpreadsheetRequest()
                    .setRequests(listOf(Request().setDeleteDimension(request)))

                sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}