package com.example.ps_inspection

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class InspectionArchiveManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val archiveDir: File = File(context.filesDir, "inspection_archive")

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        private val DISPLAY_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    }

    // Сохранить текущий осмотр в архив
    fun saveToArchive(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ): File? {
        return try {
            // Создаём папку если не существует
            if (!archiveDir.exists()) {
                archiveDir.mkdirs()
            }

            // Формируем имя файла с датой
            val now = Date()
            val fileName = "${DATE_FORMAT.format(now)}_Осмотр_ПС.json"
            val file = File(archiveDir, fileName)

            // Создаём объект для сохранения
            val archiveData = InspectionArchiveData(
                timestamp = now.time,
                displayDate = DISPLAY_FORMAT.format(now),
                oru35 = oru35Data,
                oru220 = oru220Data,
                atg = atgData,
                oru500 = oru500Data,
                buildings = buildingsData
            )

            // Сохраняем в JSON
            val json = gson.toJson(archiveData)
            file.writeText(json)

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Получить список всех архивных осмотров
    fun getAllArchives(): List<InspectionArchiveInfo> {
        val archives = mutableListOf<InspectionArchiveInfo>()

        if (!archiveDir.exists()) return archives

        archiveDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "json") {
                try {
                    val json = file.readText()
                    val data = gson.fromJson(json, InspectionArchiveData::class.java)
                    archives.add(
                        InspectionArchiveInfo(
                            fileName = file.name,
                            filePath = file.absolutePath,
                            timestamp = data.timestamp,
                            displayDate = data.displayDate
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Сортируем по дате (новые сверху)
        return archives.sortedByDescending { it.timestamp }
    }

    // Загрузить архивный осмотр по имени файла
    fun loadFromArchive(fileName: String): InspectionArchiveData? {
        return try {
            val file = File(archiveDir, fileName)
            if (!file.exists()) return null

            val json = file.readText()
            gson.fromJson(json, InspectionArchiveData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Удалить архивный файл
    fun deleteArchive(fileName: String): Boolean {
        return try {
            val file = File(archiveDir, fileName)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    // Получить файл архива по имени
    fun getArchiveFile(fileName: String): File? {
        val file = File(archiveDir, fileName)
        return if (file.exists()) file else null
    }

    // Очистить весь архив
    fun clearAllArchives(): Int {
        var count = 0
        archiveDir.listFiles()?.forEach { file ->
            if (file.isFile && file.delete()) {
                count++
            }
        }
        return count
    }
}

// Data class для сохранения в архив
data class InspectionArchiveData(
    val timestamp: Long,
    val displayDate: String,
    val oru35: InspectionORU35Data,
    val oru220: InspectionORU220Data,
    val atg: InspectionATGData,
    val oru500: InspectionORU500Data,
    val buildings: InspectionBuildingsData
)

// Data class для отображения в списке
data class InspectionArchiveInfo(
    val fileName: String,
    val filePath: String,
    val timestamp: Long,
    val displayDate: String
)