package com.example.ps_inspection.data.repositories
import android.content.Context
import com.example.ps_inspection.data.utils.FillStatus
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.data.utils.getFillStatus
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ✅ Оставляем ТОЛЬКО ОДНО определение ArchiveItem
data class ArchiveItem(
    val fileName: String,
    val displayDate: String,
    val equipmentType: String,
    val statusORU35: FillStatus,
    val statusORU220: FillStatus,
    val statusORU500: FillStatus,
    val statusATG: FillStatus,
    val statusBuildings: FillStatus,
    val timestamp: Long = System.currentTimeMillis()
)

class InspectionArchiveManager(private val context: Context) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val archiveDir: File = File(context.filesDir, "inspection_archive")

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        private val DISPLAY_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    }

    fun saveToArchive(
        oru35Data: InspectionORU35Data, oru220Data: InspectionORU220Data,
        atgData: InspectionATGData, oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ): File? {
        return try {
            if (!archiveDir.exists()) archiveDir.mkdirs()
            val now = Date()
            val file = File(archiveDir, "${DATE_FORMAT.format(now)}_Осмотр_ПС.json")
            val archiveData = InspectionArchiveData(
                timestamp = now.time, displayDate = DISPLAY_FORMAT.format(now),
                oru35 = oru35Data, oru220 = oru220Data, atg = atgData, oru500 = oru500Data, buildings = buildingsData
            )
            file.writeText(gson.toJson(archiveData))
            file
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    fun getAllArchives(): List<ArchiveItem> {
        val archives = mutableListOf<ArchiveItem>()
        if (!archiveDir.exists()) return archives

        archiveDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "json") {
                try {
                    val data = gson.fromJson(file.readText(), InspectionArchiveData::class.java)

                    archives.add(ArchiveItem(
                        fileName = file.name,
                        displayDate = data.displayDate,
                        equipmentType = detectEquipmentType(file.name, data),
                        statusORU35 = data.oru35.getFillStatus(),
                        statusORU220 = data.oru220.getFillStatus(),
                        statusORU500 = data.oru500.getFillStatus(),
                        statusATG = data.atg.getFillStatus(),
                        statusBuildings = data.buildings.getFillStatus()
                    ))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        return archives.sortedByDescending { it.displayDate }
    }

    // Вспомогательный метод для определения типа осмотра
    private fun detectEquipmentType(fileName: String, data: InspectionArchiveData): String {
        // Проверяем, какие данные заполнены
        return when {
            data.oru35.getFillStatus() != FillStatus.EMPTY &&
                    data.oru220.getFillStatus() == FillStatus.EMPTY &&
                    data.oru500.getFillStatus() == FillStatus.EMPTY -> "ОРУ-35"

            data.oru220.getFillStatus() != FillStatus.EMPTY &&
                    data.oru35.getFillStatus() == FillStatus.EMPTY &&
                    data.oru500.getFillStatus() == FillStatus.EMPTY -> "ОРУ-220"

            data.oru500.getFillStatus() != FillStatus.EMPTY &&
                    data.oru35.getFillStatus() == FillStatus.EMPTY &&
                    data.oru220.getFillStatus() == FillStatus.EMPTY -> "ОРУ-500"

            data.atg.getFillStatus() != FillStatus.EMPTY &&
                    data.oru35.getFillStatus() == FillStatus.EMPTY &&
                    data.oru220.getFillStatus() == FillStatus.EMPTY &&
                    data.oru500.getFillStatus() == FillStatus.EMPTY -> "АТГ"

            data.buildings.getFillStatus() != FillStatus.EMPTY &&
                    data.oru35.getFillStatus() == FillStatus.EMPTY &&
                    data.oru220.getFillStatus() == FillStatus.EMPTY &&
                    data.oru500.getFillStatus() == FillStatus.EMPTY &&
                    data.atg.getFillStatus() == FillStatus.EMPTY -> "Здания"

            else -> "Полный осмотр"  // Если заполнено несколько секций
        }
    }

    fun loadFromArchive(fileName: String): InspectionArchiveData? {
        return try {
            val file = File(archiveDir, fileName)
            if (!file.exists()) return null
            gson.fromJson(file.readText(), InspectionArchiveData::class.java)
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    fun deleteArchive(fileName: String): Boolean = try { File(archiveDir, fileName).delete() } catch (e: Exception) { false }
    fun clearAllArchives(): Int { var count = 0; archiveDir.listFiles()?.forEach { if (it.isFile && it.delete()) count++ }; return count }
}

data class InspectionArchiveData(
    val timestamp: Long,
    val displayDate: String,
    val oru35: InspectionORU35Data,
    val oru220: InspectionORU220Data,
    val atg: InspectionATGData,
    val oru500: InspectionORU500Data,
    val buildings: InspectionBuildingsData
)