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
    val timestamp: Long = System.currentTimeMillis(),
    val photoCount: Int = 0,
    val hasComments: Boolean = false,
    val hasPhotos: Boolean = false      // ← добавить это поле
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
                    val photoCount = countPhotosInArchive(data)
                    val hasComments = hasCommentsInArchive(data)
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
                        hasComments = hasComments,
                        hasPhotos = hasPhotos,
                        timestamp = data.timestamp
                    ))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        return archives.sortedByDescending { it.timestamp }
    }

    // В InspectionArchiveManager.kt добавить:

    private fun countPhotosInArchive(data: InspectionArchiveData): Int {
        var count = 0
        count += data.oru35.oru35PhotoFiles?.size ?: 0
        count += data.oru220.oru220PhotoFiles?.size ?: 0
        count += data.oru500.oru500PhotoFiles?.size ?: 0
        count += data.atg.atgPhotoFiles?.size ?: 0
        count += data.buildings.buildingsPhotoFiles?.size ?: 0
        return count
    }

    private fun hasCommentsInArchive(data: InspectionArchiveData): Boolean {
        val comments = listOf(
            data.oru35.commentTsn, data.oru35.commentTt352, data.oru35.commentTt353,
            data.oru35.commentV352, data.oru35.commentV353,
            data.oru220.commentMirnaya, data.oru220.commentMirnayaTT, data.oru220.commentTopaz,
            data.oru220.commentTopazTT, data.oru220.commentOv, data.oru220.commentOvTT,
            data.oru220.commentOssh, data.oru220.commentV2atg, data.oru220.commentV2atgTT,
            data.oru220.commentShsv, data.oru220.commentShsvTT, data.oru220.commentV3atg,
            data.oru220.commentV3atgTT, data.oru220.commentOrbita, data.oru220.commentOrbitaTT,
            data.oru220.commentFakel, data.oru220.commentFakelTT, data.oru220.commentCometa1,
            data.oru220.commentCometa1TT, data.oru220.commentCometa2, data.oru220.commentCometa2TT,
            data.oru220.commentTn1, data.oru220.commentTn2,
            data.oru500.commentR5002s, data.oru500.commentVsht31, data.oru500.commentVlt30,
            data.oru500.commentVshl32, data.oru500.commentVshl21, data.oru500.commentVsht22,
            data.oru500.commentVlt20, data.oru500.commentVsht11, data.oru500.commentVshl12,
            data.oru500.commentTtVsht31, data.oru500.commentTtVlt30, data.oru500.commentTtVshl32,
            data.oru500.commentTtVshl21, data.oru500.commentTtVsht22, data.oru500.commentTtVlt20,
            data.oru500.commentTtVsht11, data.oru500.commentTtVshl12, data.oru500.commentTn1500,
            data.oru500.commentTn2500, data.oru500.commentTn500Sgres1,
            data.oru500.commentTrachukovskayaTt, data.oru500.commentTrachukovskaya2tn,
            data.oru500.commentTrachukovskaya1tn, data.oru500.commentBelozernaya2tn,
            data.atg.commentAtg2C, data.atg.commentAtg2B, data.atg.commentAtg2A,
            data.atg.commentAtgReserve, data.atg.commentAtg3C, data.atg.commentAtg3B,
            data.atg.commentAtg3A, data.atg.commentReactorC, data.atg.commentReactorB,
            data.atg.commentReactorA, data.atg.commentTn35,
            data.buildings.commentCompressor1, data.buildings.commentBallroom1,
            data.buildings.commentCompressor2, data.buildings.commentBallroom2,
            data.buildings.commentKpzOpu, data.buildings.commentKpz2, data.buildings.commentFirePump,
            data.buildings.commentWorkshop, data.buildings.commentArtWell,
            data.buildings.commentArtesianWell, data.buildings.commentRoomAb, data.buildings.commentBasement
        )
        return comments.any { it.isNotBlank() }
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