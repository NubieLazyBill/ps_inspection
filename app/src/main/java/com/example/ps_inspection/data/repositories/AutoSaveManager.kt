package com.example.ps_inspection.data.repositories

import android.content.Context
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AutoSaveManager(private val context: Context) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val autoSaveFile = File(context.filesDir, "auto_save_inspection.json")

    data class AutoSaveData(
        val timestamp: Long,
        val displayDate: String,
        val oru35: InspectionORU35Data,
        val oru220: InspectionORU220Data,
        val atg: InspectionATGData,
        val oru500: InspectionORU500Data,
        val buildings: InspectionBuildingsData
    )

    fun saveAllData(
        oru35: InspectionORU35Data,
        oru220: InspectionORU220Data,
        atg: InspectionATGData,
        oru500: InspectionORU500Data,
        buildings: InspectionBuildingsData
    ) {
        try {
            val now = Date()
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

            val data = AutoSaveData(
                timestamp = now.time,
                displayDate = dateFormat.format(now),
                oru35 = oru35,
                oru220 = oru220,
                atg = atg,
                oru500 = oru500,
                buildings = buildings
            )
            autoSaveFile.writeText(gson.toJson(data))
            println("✅ AutoSave: данные сохранены в ${dateFormat.format(now)}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ AutoSave: ошибка сохранения - ${e.message}")
        }
    }

    fun loadAllData(): AutoSaveData? {
        return try {
            if (!autoSaveFile.exists()) {
                println("📁 AutoSave: файл не найден")
                return null
            }
            val json = autoSaveFile.readText()
            val data = gson.fromJson(json, AutoSaveData::class.java)
            println("✅ AutoSave: данные загружены от ${data.displayDate}")
            data
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ AutoSave: ошибка загрузки - ${e.message}")
            null
        }
    }

    fun hasAutoSave(): Boolean = autoSaveFile.exists()

    fun clearAutoSave() {
        if (autoSaveFile.exists()) {
            autoSaveFile.delete()
            println("🗑️ AutoSave: файл удалён")
        }
    }

    fun getAutoSaveDate(): String? {
        return if (autoSaveFile.exists()) {
            try {
                val json = autoSaveFile.readText()
                val data = gson.fromJson(json, AutoSaveData::class.java)
                data.displayDate
            } catch (e: Exception) {
                null
            }
        } else null
    }
}