package com.example.ps_inspection
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ArchiveItem(
    val fileName: String,
    val displayDate: String,
    val statusORU35: FillStatus,
    val statusORU220: FillStatus,
    val statusORU500: FillStatus,
    val statusATG: FillStatus,
    val statusBuildings: FillStatus
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
                        fileName = file.name, displayDate = data.displayDate,
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
    val displayDate: String,  // ✅ это поле обязательно!
    val oru35: InspectionORU35Data,
    val oru220: InspectionORU220Data,
    val atg: InspectionATGData,
    val oru500: InspectionORU500Data,
    val buildings: InspectionBuildingsData
)