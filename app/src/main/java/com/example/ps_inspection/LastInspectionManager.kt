package com.example.ps_inspection

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LastInspectionManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("inspection_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_LAST_INSPECTION = "last_inspection"
    }

    // Сохранить последний осмотр
    fun saveLastInspection(
        oru35Data: InspectionORU35Data,
        oru220Data: InspectionORU220Data,
        atgData: InspectionATGData,
        oru500Data: InspectionORU500Data,
        buildingsData: InspectionBuildingsData
    ) {
        val inspectionData = mapOf(
            "oru35" to oru35Data,
            "oru220" to oru220Data,
            "atg" to atgData,
            "oru500" to oru500Data,
            "buildings" to buildingsData
        )
        val json = gson.toJson(inspectionData)
        prefs.edit().putString(KEY_LAST_INSPECTION, json).apply()

        // Сохраняем текущую дату
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        prefs.edit().putString("last_inspection_date", currentDate).apply()
    }

    // Загрузить последний осмотр
    fun getLastInspection(): Map<String, Any>? {
        val json = prefs.getString(KEY_LAST_INSPECTION, null) ?: return null

        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }

    // Получить данные ORU35 из сохранённого
    fun getLastOru35Data(): InspectionORU35Data? {
        val last = getLastInspection() ?: return null
        return gson.fromJson(gson.toJson(last["oru35"]), InspectionORU35Data::class.java)
    }

    // Получить данные ORU220 из сохранённого
    fun getLastOru220Data(): InspectionORU220Data? {
        val last = getLastInspection() ?: return null
        return gson.fromJson(gson.toJson(last["oru220"]), InspectionORU220Data::class.java)
    }

    // Получить данные АТГ из сохранённого
    fun getLastAtgData(): InspectionATGData? {
        val last = getLastInspection() ?: return null
        return gson.fromJson(gson.toJson(last["atg"]), InspectionATGData::class.java)
    }

    // Получить данные ORU500 из сохранённого
    fun getLastOru500Data(): InspectionORU500Data? {
        val last = getLastInspection() ?: return null
        return gson.fromJson(gson.toJson(last["oru500"]), InspectionORU500Data::class.java)
    }

    // Получить данные Buildings из сохранённого
    fun getLastBuildingsData(): InspectionBuildingsData? {
        val last = getLastInspection() ?: return null
        return gson.fromJson(gson.toJson(last["buildings"]), InspectionBuildingsData::class.java)
    }

    fun getLastInspectionDate(): String? {
        return prefs.getString("last_inspection_date", null)
    }

    fun clearLastInspection() {
        prefs.edit().remove(KEY_LAST_INSPECTION).remove("last_inspection_date").apply()
    }

    fun hasSavedInspection(): Boolean {
        return prefs.getString(KEY_LAST_INSPECTION, null) != null
    }
}