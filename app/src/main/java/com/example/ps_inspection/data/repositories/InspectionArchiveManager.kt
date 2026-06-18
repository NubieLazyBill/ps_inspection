package com.example.ps_inspection.data.repositories

import android.content.Context
import com.example.ps_inspection.data.utils.FillStatus
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.data.utils.getFillStatus
import com.example.ps_inspection.data.utils.ProgressCalculator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ArchiveItem(
    val fileName: String,
    val displayDate: String,
    val equipmentType: String,
    val statusORU35: FillStatus,
    val statusORU220: FillStatus,
    val statusORU500: FillStatus,
    val statusATG: FillStatus,
    val statusBuildings: FillStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val photoCount: Int = 0,
    val hasPhotos: Boolean = false,
    val inspectorName: String = "",
    val progressOru35: Int = 0,
    val progressOru220: Int = 0,
    val progressOru500: Int = 0,
    val progressAtg: Int = 0,
    val progressBuildings: Int = 0,
    val serverRowId: Int? = null,
    val isLocallyDeleted: Boolean = false
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
        buildingsData: InspectionBuildingsData,
        outdoorTemp: String = "",
        inspectorName: String = "",
        inspectorPosition: String = ""
    ): File? {
        return try {
            if (!archiveDir.exists()) archiveDir.mkdirs()
            val now = Date()
            val file = File(archiveDir, "${DATE_FORMAT.format(now)}_Осмотр_ПС.json")
            val archiveData = InspectionArchiveData(
                timestamp = now.time, displayDate = DISPLAY_FORMAT.format(now),
                oru35 = oru35Data, oru220 = oru220Data, atg = atgData,
                oru500 = oru500Data, buildings = buildingsData,
                outdoorTemp = outdoorTemp,
                inspectorName = inspectorName,
                inspectorPosition = inspectorPosition
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
                    val photoCount = countPhotosInArchive(data)
                    val hasPhotos = photoCount > 0

                    archives.add(ArchiveItem(
                        fileName = file.name,
                        displayDate = data.displayDate,
                        equipmentType = detectEquipmentType(file.name, data),
                        statusORU35 = data.oru35.getFillStatus(),
                        statusORU220 = data.oru220.getFillStatus(),
                        statusORU500 = data.oru500.getFillStatus(),
                        statusATG = data.atg.getFillStatus(),
                        statusBuildings = data.buildings.getFillStatus(),
                        photoCount = photoCount,
                        hasPhotos = hasPhotos,
                        timestamp = data.timestamp,
                        inspectorName = data.inspectorName,
                        progressOru35 = ProgressCalculator.calculateORU35(data.oru35),
                        progressOru220 = ProgressCalculator.calculateORU220(data.oru220),
                        progressOru500 = ProgressCalculator.calculateORU500(data.oru500),
                        progressAtg = ProgressCalculator.calculateATG(data.atg),
                        progressBuildings = ProgressCalculator.calculateBuildings(data.buildings)
                    ))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        return archives.sortedByDescending { it.timestamp }
    }

    private fun countPhotosInArchive(data: InspectionArchiveData): Int {
        var count = 0
        count += data.oru35.oru35PhotoFiles.size
        count += data.oru220.oru220PhotoFiles.size
        count += data.oru500.oru500PhotoFiles.size
        count += data.atg.atgPhotoFiles.size
        count += data.buildings.buildingsPhotoFiles.size
        return count
    }

    private fun detectEquipmentType(fileName: String, data: InspectionArchiveData): String {
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
            else -> "Полный осмотр"
        }
    }

    fun loadFromArchive(fileName: String): InspectionArchiveData? {
        return try {
            val file = File(archiveDir, fileName)
            if (!file.exists()) return null
            gson.fromJson(file.readText(), InspectionArchiveData::class.java)
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    fun getArchiveDir(): File = archiveDir

    fun deleteArchive(fileName: String): Boolean = try {
        File(archiveDir, fileName).delete()
    } catch (e: Exception) { false }

    fun clearAllArchives(): Int {
        var count = 0
        archiveDir.listFiles()?.forEach { if (it.isFile && it.delete()) count++ }
        return count
    }
}

data class InspectionArchiveData(
    val timestamp: Long,
    val displayDate: String,
    val oru35: InspectionORU35Data,
    val oru220: InspectionORU220Data,
    val atg: InspectionATGData,
    val oru500: InspectionORU500Data,
    val buildings: InspectionBuildingsData,
    val outdoorTemp: String = "",
    val inspectorName: String = "",
    val inspectorPosition: String = ""
)