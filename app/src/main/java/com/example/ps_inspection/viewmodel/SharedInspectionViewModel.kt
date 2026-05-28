package com.example.ps_inspection.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ps_inspection.data.repositories.AutoSaveManager
import com.example.ps_inspection.data.repositories.CommentStorageManager
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.ps_inspection.data.services.WeatherService
import androidx.lifecycle.viewModelScope
import com.example.ps_inspection.data.services.WeatherData
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import com.example.ps_inspection.data.services.GoogleSheetsService


class SharedInspectionViewModel : ViewModel() {

    private val weatherService = WeatherService()
    private lateinit var sheetsService: GoogleSheetsService
    private lateinit var appContext: Context

    // Инициализация (вызвать из Activity)
    fun init(context: Context) {
        appContext = context.applicationContext
        sheetsService = GoogleSheetsService(appContext)
        Log.d("SharedViewModel", "GoogleSheetsService инициализирован")
    }

    // Переменная для хранения погоды
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData

    // Функция получения погоды
    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val weather = weatherService.getCurrentWeather(latitude, longitude)
            if (weather != null) {
                _weatherData.value = weather
                _outdoorTemp.value = String.format("%.1f", weather.temperature)
            }
        }
    }

    // Данные для экрана ORU35
    private val _oru35Data = MutableStateFlow(InspectionORU35Data())
    val oru35Data: StateFlow<InspectionORU35Data> = _oru35Data

    // Данные для экрана ORU220
    private val _oru220Data = MutableStateFlow(InspectionORU220Data())
    val oru220Data: StateFlow<InspectionORU220Data> = _oru220Data

    // Данные для экрана АТГ
    private val _atgData = MutableStateFlow(InspectionATGData())
    val atgData: StateFlow<InspectionATGData> = _atgData

    // Данные для экрана Buildings
    private val _buildingsData = MutableStateFlow(InspectionBuildingsData())
    val buildingsData: StateFlow<InspectionBuildingsData> = _buildingsData

    // Данные для экрана ORU500
    private val _oru500Data = MutableStateFlow(InspectionORU500Data())
    val oru500Data: StateFlow<InspectionORU500Data> = _oru500Data

    // Хранилище комментариев
    private lateinit var commentStorage: CommentStorageManager

    // 🔒 Автосохранение
    private var autoSaveManager: AutoSaveManager? = null

    // ========== КОММЕНТАРИИ ДЛЯ АТГ ==========
    private val _atgComments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val atgComments: StateFlow<Map<String, List<Comment>>> = _atgComments

    // ========== КОММЕНТАРИИ ДЛЯ ОРУ-35 ==========
    private val _oru35Comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val oru35Comments: StateFlow<Map<String, List<Comment>>> = _oru35Comments

    // ========== КОММЕНТАРИИ ДЛЯ ОРУ-220 ==========
    private val _oru220Comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val oru220Comments: StateFlow<Map<String, List<Comment>>> = _oru220Comments

    // ========== КОММЕНТАРИИ ДЛЯ ОРУ-500 ==========
    private val _oru500Comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val oru500Comments: StateFlow<Map<String, List<Comment>>> = _oru500Comments

    // ========== КОММЕНТАРИИ ДЛЯ BUILDINGS ==========
    private val _buildingsComments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val buildingsComments: StateFlow<Map<String, List<Comment>>> = _buildingsComments

    // 🔒 Приватный метод для автосохранения при КАЖДОМ изменении данных
    private fun autoSave() {
        try {
            autoSaveManager?.saveAllData(
                _oru35Data.value,
                _oru220Data.value,
                _atgData.value,
                _oru500Data.value,
                _buildingsData.value,
                _outdoorTemp.value
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Функции обновления данных (теперь с автосохранением)
    fun updateORU35Data(update: InspectionORU35Data.() -> Unit) {
        val newData = _oru35Data.value.copy().apply(update)
        _oru35Data.value = newData
        autoSave()
    }

    fun updateORU220Data(update: InspectionORU220Data.() -> Unit) {
        val newData = _oru220Data.value.copy().apply(update)
        _oru220Data.value = newData
        autoSave()
    }

    fun updateATGData(update: InspectionATGData.() -> Unit) {
        val newData = _atgData.value.copy().apply(update)
        _atgData.value = newData
        autoSave()
    }

    fun updateBuildingsData(update: InspectionBuildingsData.() -> Unit) {
        val newData = _buildingsData.value.copy().apply(update)
        _buildingsData.value = newData
        autoSave()
    }

    fun updateORU500Data(update: InspectionORU500Data.() -> Unit) {
        val newData = _oru500Data.value.copy().apply(update)
        _oru500Data.value = newData
        autoSave()
    }

    // Инициализация
    fun initCommentStorage(context: Context) {
        commentStorage = CommentStorageManager(context)
        autoSaveManager = AutoSaveManager(context)
        loadAllComments()
    }

    private fun loadAllComments() {
        loadATGCommentsFromStorage()
        loadORU35CommentsFromStorage()
        loadORU220CommentsFromStorage()
        loadORU500CommentsFromStorage()
        loadBuildingsCommentsFromStorage()
    }

    // Очистка (только когда пользователь ЯВНО хочет начать новый осмотр)
    fun clearAllData() {
        _oru35Data.value = InspectionORU35Data()
        _oru220Data.value = InspectionORU220Data()
        _atgData.value = InspectionATGData()
        _oru500Data.value = InspectionORU500Data()
        _buildingsData.value = InspectionBuildingsData()
        _outdoorTemp.value = ""
    }

    fun clearAllComments() {
        commentStorage.clearAllComments()
        loadAllComments()
    }

    // --- Фото АТГ ---
    fun addATGPhoto(fileName: String) {
        val currentList = _atgData.value.atgPhotoFiles.toMutableList()
        if (!currentList.contains(fileName)) {
            currentList.add(fileName)
            _atgData.value = _atgData.value.copy(atgPhotoFiles = currentList)
            autoSave()
        }
    }

    fun removeATGPhoto(fileName: String) {
        val currentList = _atgData.value.atgPhotoFiles.toMutableList()
        if (currentList.remove(fileName)) {
            _atgData.value = _atgData.value.copy(atgPhotoFiles = currentList)
            autoSave()
        }
    }

    // --- Фото ОРУ-35 ---
    fun addORU35Photo(fileName: String) {
        val currentList = _oru35Data.value.oru35PhotoFiles.toMutableList()
        if (!currentList.contains(fileName)) {
            currentList.add(fileName)
            _oru35Data.value = _oru35Data.value.copy(oru35PhotoFiles = currentList)
            autoSave()
        }
    }

    fun removeORU35Photo(fileName: String) {
        val currentList = _oru35Data.value.oru35PhotoFiles.toMutableList()
        if (currentList.remove(fileName)) {
            _oru35Data.value = _oru35Data.value.copy(oru35PhotoFiles = currentList)
            autoSave()
        }
    }

    // ========== АТГ КОММЕНТАРИИ ==========
    fun addATGComment(equipmentKey: String, commentText: String, author: String = "") {
        if (commentText.isBlank()) return
        val prefixedKey = "ATG_$equipmentKey"

        val currentMap = _atgComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText, author = author))
        currentMap[equipmentKey] = currentList
        _atgComments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)

        // ✅ Не сохраняем в Data class — только в CommentStorageManager
        // autoSave() вызывать не нужно, так как автосохранение не касается комментариев
    }

    fun removeATGComment(equipmentKey: String, commentIndex: Int) {
        val currentMap = _atgComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList.removeAt(commentIndex)
            if (currentList.isEmpty()) {
                currentMap.remove(equipmentKey)
            } else {
                currentMap[equipmentKey] = currentList
            }
            _atgComments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ATG_$equipmentKey"
            if (currentList.isEmpty()) {
                allComments.remove(prefixedKey)
            } else {
                allComments[prefixedKey] = currentList
            }
            commentStorage.saveAllComments(allComments)
        }
    }

    fun updateATGComment(equipmentKey: String, commentIndex: Int, newText: String) {
        if (newText.isBlank()) return
        val currentMap = _atgComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            val old = currentList[commentIndex]
            currentList[commentIndex] = old.copy(text = newText)
            currentMap[equipmentKey] = currentList
            _atgComments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ATG_$equipmentKey"
            allComments[prefixedKey] = currentList
            commentStorage.saveAllComments(allComments)
        }
    }

    private fun loadATGCommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ATG_") }
            .mapKeys { it.key.removePrefix("ATG_") }
        _atgComments.value = filtered
    }

    // ========== ОРУ-35 КОММЕНТАРИИ ==========
    fun addORU35Comment(equipmentKey: String, commentText: String, author: String = "") {
        if (commentText.isBlank()) return
        val prefixedKey = "ORU35_$equipmentKey"

        val currentMap = _oru35Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText, author = author))
        currentMap[equipmentKey] = currentList
        _oru35Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)
    }

    fun removeORU35Comment(equipmentKey: String, commentIndex: Int) {
        val currentMap = _oru35Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList.removeAt(commentIndex)
            if (currentList.isEmpty()) {
                currentMap.remove(equipmentKey)
            } else {
                currentMap[equipmentKey] = currentList
            }
            _oru35Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ORU35_$equipmentKey"
            if (currentList.isEmpty()) {
                allComments.remove(prefixedKey)
            } else {
                allComments[prefixedKey] = currentList
            }
            commentStorage.saveAllComments(allComments)
        }
    }

    fun updateORU35Comment(equipmentKey: String, commentIndex: Int, newText: String) {
        if (newText.isBlank()) return
        val currentMap = _oru35Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            val old = currentList[commentIndex]
            currentList[commentIndex] = old.copy(text = newText)
            currentMap[equipmentKey] = currentList
            _oru35Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ORU35_$equipmentKey"
            allComments[prefixedKey] = currentList
            commentStorage.saveAllComments(allComments)
        }
    }

    private fun loadORU35CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ORU35_") }
            .mapKeys { it.key.removePrefix("ORU35_") }
        _oru35Comments.value = filtered
    }

    // ========== ОРУ-220 КОММЕНТАРИИ ==========
    fun addORU220Comment(equipmentKey: String, commentText: String, author: String = "") {
        if (commentText.isBlank()) return
        val prefixedKey = "ORU220_$equipmentKey"

        val currentMap = _oru220Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText, author = author))
        currentMap[equipmentKey] = currentList
        _oru220Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)
    }

    fun removeORU220Comment(equipmentKey: String, commentIndex: Int) {
        val currentMap = _oru220Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList.removeAt(commentIndex)
            if (currentList.isEmpty()) {
                currentMap.remove(equipmentKey)
            } else {
                currentMap[equipmentKey] = currentList
            }
            _oru220Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ORU220_$equipmentKey"
            if (currentList.isEmpty()) {
                allComments.remove(prefixedKey)
            } else {
                allComments[prefixedKey] = currentList
            }
            commentStorage.saveAllComments(allComments)
        }
    }

    fun updateORU220Comment(equipmentKey: String, commentIndex: Int, newText: String) {
        if (newText.isBlank()) return
        val currentMap = _oru220Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            val old = currentList[commentIndex]
            currentList[commentIndex] = old.copy(text = newText)
            currentMap[equipmentKey] = currentList
            _oru220Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ORU220_$equipmentKey"
            allComments[prefixedKey] = currentList
            commentStorage.saveAllComments(allComments)
        }
    }

    private fun loadORU220CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ORU220_") }
            .mapKeys { it.key.removePrefix("ORU220_") }
        _oru220Comments.value = filtered
    }

    // ========== ОРУ-500 КОММЕНТАРИИ ==========
    fun addORU500Comment(equipmentKey: String, commentText: String, author: String = "") {
        if (commentText.isBlank()) return
        val prefixedKey = "ORU500_$equipmentKey"

        val currentMap = _oru500Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText, author = author))
        currentMap[equipmentKey] = currentList
        _oru500Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)
    }

    fun removeORU500Comment(equipmentKey: String, commentIndex: Int) {
        val currentMap = _oru500Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList.removeAt(commentIndex)
            if (currentList.isEmpty()) {
                currentMap.remove(equipmentKey)
            } else {
                currentMap[equipmentKey] = currentList
            }
            _oru500Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ORU500_$equipmentKey"
            if (currentList.isEmpty()) {
                allComments.remove(prefixedKey)
            } else {
                allComments[prefixedKey] = currentList
            }
            commentStorage.saveAllComments(allComments)
        }
    }

    fun updateORU500Comment(equipmentKey: String, commentIndex: Int, newText: String) {
        if (newText.isBlank()) return
        val currentMap = _oru500Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            val old = currentList[commentIndex]
            currentList[commentIndex] = old.copy(text = newText)
            currentMap[equipmentKey] = currentList
            _oru500Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "ORU500_$equipmentKey"
            allComments[prefixedKey] = currentList
            commentStorage.saveAllComments(allComments)
        }
    }

    private fun loadORU500CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ORU500_") }
            .mapKeys { it.key.removePrefix("ORU500_") }
        _oru500Comments.value = filtered
    }

    // ========== BUILDINGS КОММЕНТАРИИ ==========
    fun addBuildingsComment(equipmentKey: String, commentText: String, author: String = "") {
        if (commentText.isBlank()) return
        val prefixedKey = "BUILDINGS_$equipmentKey"

        val currentMap = _buildingsComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText, author = author))
        currentMap[equipmentKey] = currentList
        _buildingsComments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)
    }

    fun removeBuildingsComment(equipmentKey: String, commentIndex: Int) {
        val currentMap = _buildingsComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList.removeAt(commentIndex)
            if (currentList.isEmpty()) {
                currentMap.remove(equipmentKey)
            } else {
                currentMap[equipmentKey] = currentList
            }
            _buildingsComments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "BUILDINGS_$equipmentKey"
            if (currentList.isEmpty()) {
                allComments.remove(prefixedKey)
            } else {
                allComments[prefixedKey] = currentList
            }
            commentStorage.saveAllComments(allComments)
        }
    }

    fun updateBuildingsComment(equipmentKey: String, commentIndex: Int, newText: String) {
        if (newText.isBlank()) return
        val currentMap = _buildingsComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            val old = currentList[commentIndex]
            currentList[commentIndex] = old.copy(text = newText)
            currentMap[equipmentKey] = currentList
            _buildingsComments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            val prefixedKey = "BUILDINGS_$equipmentKey"
            allComments[prefixedKey] = currentList
            commentStorage.saveAllComments(allComments)
        }
    }

    private fun loadBuildingsCommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("BUILDINGS_") }
            .mapKeys { it.key.removePrefix("BUILDINGS_") }
        _buildingsComments.value = filtered
    }

    private val _lastValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastValues: StateFlow<Map<String, String>> = _lastValues.asStateFlow()

    fun loadLastValues(parameterKeys: List<String>) {
        viewModelScope.launch {
            val result = sheetsService.getLastValuesForParameters(parameterKeys)
            _lastValues.value = result
        }
    }

    private val _lastValuesFromSheets = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastValuesFromSheets: StateFlow<Map<String, String>> = _lastValuesFromSheets.asStateFlow()

    fun loadLastValuesFromSheets(parameterKeys: List<String>) {
        viewModelScope.launch {
            val result = sheetsService.getLastValuesForParameters(parameterKeys)
            _lastValuesFromSheets.value = result
            Log.d("LastValues", "Загружено ${result.size} значений из Google Sheets")
        }
    }

    // Температура наружного воздуха
    private val _outdoorTemp = MutableStateFlow("")
    val outdoorTemp: StateFlow<String> = _outdoorTemp

    fun updateOutdoorTemp(temp: String) {
        _outdoorTemp.value = temp
        autoSave()
    }
}