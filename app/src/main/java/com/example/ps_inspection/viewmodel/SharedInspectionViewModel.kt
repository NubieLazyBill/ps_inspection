package com.example.ps_inspection.viewmodel

import android.content.Context
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

class SharedInspectionViewModel : ViewModel() {

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
                _outdoorTemp.value  // ← Добавить
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Функции обновления данных (теперь с автосохранением)
    fun updateORU35Data(update: InspectionORU35Data.() -> Unit) {
        val newData = _oru35Data.value.copy().apply(update)
        _oru35Data.value = newData
        autoSave()  // 🔒 Автосохранение при изменении
    }

    fun updateORU220Data(update: InspectionORU220Data.() -> Unit) {
        val newData = _oru220Data.value.copy().apply(update)
        _oru220Data.value = newData
        autoSave()  // 🔒 Автосохранение при изменении
    }

    fun updateATGData(update: InspectionATGData.() -> Unit) {
        val newData = _atgData.value.copy().apply(update)
        _atgData.value = newData
        autoSave()  // 🔒 Автосохранение при изменении
    }

    fun updateBuildingsData(update: InspectionBuildingsData.() -> Unit) {
        val newData = _buildingsData.value.copy().apply(update)
        _buildingsData.value = newData
        autoSave()  // 🔒 Автосохранение при изменении
    }

    fun updateORU500Data(update: InspectionORU500Data.() -> Unit) {
        val newData = _oru500Data.value.copy().apply(update)
        _oru500Data.value = newData
        autoSave()  // 🔒 Автосохранение при изменении
    }

    // Инициализация
    fun initCommentStorage(context: Context) {
        commentStorage = CommentStorageManager(context)
        autoSaveManager = AutoSaveManager(context)  // 🔒 Инициализируем автосохранение
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
        _outdoorTemp.value = ""  // ← Добавить
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
            autoSave()  // 🔒 Сохраняем после добавления фото
        }
    }

    fun removeATGPhoto(fileName: String) {
        val currentList = _atgData.value.atgPhotoFiles.toMutableList()
        if (currentList.remove(fileName)) {
            _atgData.value = _atgData.value.copy(atgPhotoFiles = currentList)
            autoSave()  // 🔒 Сохраняем после удаления фото
        }
    }

    // --- Фото ОРУ-35 ---
    fun addORU35Photo(fileName: String) {
        val currentList = _oru35Data.value.oru35PhotoFiles.toMutableList()
        if (!currentList.contains(fileName)) {
            currentList.add(fileName)
            _oru35Data.value = _oru35Data.value.copy(oru35PhotoFiles = currentList)
            autoSave()  // 🔒 Сохраняем после добавления фото
        }
    }

    fun removeORU35Photo(fileName: String) {
        val currentList = _oru35Data.value.oru35PhotoFiles.toMutableList()
        if (currentList.remove(fileName)) {
            _oru35Data.value = _oru35Data.value.copy(oru35PhotoFiles = currentList)
            autoSave()  // 🔒 Сохраняем после удаления фото
        }
    }

    // ========== АТГ КОММЕНТАРИИ ==========
    fun addATGComment(equipmentKey: String, commentText: String) {
        if (commentText.isBlank()) return
        val prefixedKey = "ATG_$equipmentKey"

        val currentMap = _atgComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText))
        currentMap[equipmentKey] = currentList
        _atgComments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveATGCommentsToData(equipmentKey, currentList.map { it.text })
        autoSave()  // 🔒 Сохраняем после добавления комментария
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

            saveATGCommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после удаления комментария
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

            saveATGCommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после обновления комментария
        }
    }

    private fun saveATGCommentsToData(equipmentKey: String, comments: List<String>) {
        val commentString = comments.joinToString("|||")
        val currentData = _atgData.value
        _atgData.value = currentData.copy(
            commentAtg2C = if (equipmentKey == "2 АТГ ф.С") commentString else currentData.commentAtg2C,
            commentAtg2B = if (equipmentKey == "2 АТГ ф.В") commentString else currentData.commentAtg2B,
            commentAtg2A = if (equipmentKey == "2 АТГ ф.А") commentString else currentData.commentAtg2A,
            commentAtgReserve = if (equipmentKey == "АТГ резервная") commentString else currentData.commentAtgReserve,
            commentAtg3C = if (equipmentKey == "3 АТГ ф.С") commentString else currentData.commentAtg3C,
            commentAtg3B = if (equipmentKey == "3 АТГ ф.В") commentString else currentData.commentAtg3B,
            commentAtg3A = if (equipmentKey == "3 АТГ ф.А") commentString else currentData.commentAtg3A,
            commentReactorC = if (equipmentKey == "Реактор ф.С") commentString else currentData.commentReactorC,
            commentReactorB = if (equipmentKey == "Реактор ф.В") commentString else currentData.commentReactorB,
            commentReactorA = if (equipmentKey == "Реактор ф.А") commentString else currentData.commentReactorA,
            commentTn35 = if (equipmentKey == "ТН-35") commentString else currentData.commentTn35
        )
    }

    private fun loadATGCommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ATG_") }
            .mapKeys { it.key.removePrefix("ATG_") }
        _atgComments.value = filtered
    }

    // ========== ОРУ-35 КОММЕНТАРИИ ==========
    fun addORU35Comment(equipmentKey: String, commentText: String) {
        if (commentText.isBlank()) return
        val prefixedKey = "ORU35_$equipmentKey"

        val currentMap = _oru35Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText))
        currentMap[equipmentKey] = currentList
        _oru35Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveORU35CommentsToData(equipmentKey, currentList.map { it.text })
        autoSave()  // 🔒 Сохраняем после добавления комментария
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

            saveORU35CommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после удаления комментария
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

            saveORU35CommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после обновления комментария
        }
    }

    private fun saveORU35CommentsToData(equipmentKey: String, comments: List<String>) {
        val commentString = comments.joinToString("|||")
        updateORU35Data {
            when (equipmentKey) {
                "ТСН" -> commentTsn = commentString
                "ТТ-35 2ТСН" -> commentTt352 = commentString
                "ТТ-35 3ТСН" -> commentTt353 = commentString
                "В-35 2ТСН" -> commentV352 = commentString
                "В-35 3ТСН" -> commentV353 = commentString
            }
        }
    }

    private fun loadORU35CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ORU35_") }
            .mapKeys { it.key.removePrefix("ORU35_") }
        _oru35Comments.value = filtered
    }

    // ========== ОРУ-220 КОММЕНТАРИИ ==========
    fun addORU220Comment(equipmentKey: String, commentText: String) {
        if (commentText.isBlank()) return
        val prefixedKey = "ORU220_$equipmentKey"

        val currentMap = _oru220Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText))
        currentMap[equipmentKey] = currentList
        _oru220Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveORU220CommentsToData(equipmentKey, currentList.map { it.text })
        autoSave()  // 🔒 Сохраняем после добавления комментария
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

            saveORU220CommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после удаления комментария
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

            saveORU220CommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после обновления комментария
        }
    }

    private fun saveORU220CommentsToData(equipmentKey: String, comments: List<String>) {
        val commentString = comments.joinToString("|||")
        updateORU220Data {
            when (equipmentKey) {
                "Мирная" -> commentMirnaya = commentString
                "Мирная ТТ" -> commentMirnayaTT = commentString
                "Топаз" -> commentTopaz = commentString
                "Топаз ТТ" -> commentTopazTT = commentString
                "ОВ" -> commentOv = commentString
                "ОВ ТТ" -> commentOvTT = commentString
                "ТН-220 ОСШ" -> commentOssh = commentString
                "2АТГ" -> commentV2atg = commentString
                "2АТГ ТТ" -> commentV2atgTT = commentString
                "ШСВ" -> commentShsv = commentString
                "ШСВ ТТ" -> commentShsvTT = commentString
                "3АТГ" -> commentV3atg = commentString
                "3АТГ ТТ" -> commentV3atgTT = commentString
                "Орбита" -> commentOrbita = commentString
                "Орбита ТТ" -> commentOrbitaTT = commentString
                "Факел" -> commentFakel = commentString
                "Факел ТТ" -> commentFakelTT = commentString
                "Комета-1" -> commentCometa1 = commentString
                "Комета-1 ТТ" -> commentCometa1TT = commentString
                "Комета-2" -> commentCometa2 = commentString
                "Комета-2 ТТ" -> commentCometa2TT = commentString
                "1ТН-220" -> commentTn1 = commentString
                "2ТН-220" -> commentTn2 = commentString
            }
        }
    }

    private fun loadORU220CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ORU220_") }
            .mapKeys { it.key.removePrefix("ORU220_") }
        _oru220Comments.value = filtered
    }

    // ========== ОРУ-500 КОММЕНТАРИИ ==========
    fun addORU500Comment(equipmentKey: String, commentText: String) {
        if (commentText.isBlank()) return
        val prefixedKey = "ORU500_$equipmentKey"

        val currentMap = _oru500Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText))
        currentMap[equipmentKey] = currentList
        _oru500Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveORU500CommentsToData(equipmentKey, currentList.map { it.text })
        autoSave()  // 🔒 Сохраняем после добавления комментария
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

            saveORU500CommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после удаления комментария
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

            saveORU500CommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после обновления комментария
        }
    }

    private fun saveORU500CommentsToData(equipmentKey: String, comments: List<String>) {
        val commentString = comments.joinToString("|||")
        updateORU500Data {
            when (equipmentKey) {
                "В-500 Р-500 2С" -> commentR5002s = commentString
                "В-500 ВШТ-31" -> commentVsht31 = commentString
                "В-500 ВЛТ-30" -> commentVlt30 = commentString
                "В-500 ВШЛ-32" -> commentVshl32 = commentString
                "В-500 ВШЛ-21" -> commentVshl21 = commentString
                "В-500 ВШТ-22" -> commentVsht22 = commentString
                "В-500 ВЛТ-20" -> commentVlt20 = commentString
                "В-500 ВШТ-11" -> commentVsht11 = commentString
                "В-500 ВШЛ-12" -> commentVshl12 = commentString
                "ТТ-500 ВШТ-31" -> commentTtVsht31 = commentString
                "ТТ-500 ВЛТ-30" -> commentTtVlt30 = commentString
                "ТТ-500 ВШЛ-32" -> commentTtVshl32 = commentString
                "ТТ-500 ВШЛ-21" -> commentTtVshl21 = commentString
                "ТТ-500 ВШТ-22" -> commentTtVsht22 = commentString
                "ТТ-500 ВЛТ-20" -> commentTtVlt20 = commentString
                "ТТ-500 ВШТ-11" -> commentTtVsht11 = commentString
                "ТТ-500 ВШЛ-12" -> commentTtVshl12 = commentString
                "1ТН-500" -> commentTn1500 = commentString
                "2ТН-500" -> commentTn2500 = commentString
                "ТН-500 СГРЭС-1" -> commentTn500Sgres1 = commentString
                "Трачуковская ТТ" -> commentTrachukovskayaTt = commentString
                "Трачуковская 2ТН" -> commentTrachukovskaya2tn = commentString
                "Трачуковская 1ТН" -> commentTrachukovskaya1tn = commentString
                "Белозёрная 2ТН" -> commentBelozernaya2tn = commentString
            }
        }
    }

    private fun loadORU500CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("ORU500_") }
            .mapKeys { it.key.removePrefix("ORU500_") }
        _oru500Comments.value = filtered
    }

    // ========== BUILDINGS КОММЕНТАРИИ ==========
    fun addBuildingsComment(equipmentKey: String, commentText: String) {
        if (commentText.isBlank()) return
        val prefixedKey = "BUILDINGS_$equipmentKey"

        val currentMap = _buildingsComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(Comment(text = commentText))
        currentMap[equipmentKey] = currentList
        _buildingsComments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[prefixedKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveBuildingsCommentsToData(equipmentKey, currentList.map { it.text })
        autoSave()  // 🔒 Сохраняем после добавления комментария
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

            saveBuildingsCommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после удаления комментария
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

            saveBuildingsCommentsToData(equipmentKey, currentList.map { it.text })
            autoSave()  // 🔒 Сохраняем после обновления комментария
        }
    }

    private fun saveBuildingsCommentsToData(equipmentKey: String, comments: List<String>) {
        val commentString = comments.joinToString("|||")
        updateBuildingsData {
            when (equipmentKey) {
                "Компрессорная №1" -> commentCompressor1 = commentString
                "Баллоная №1" -> commentBallroom1 = commentString
                "Компрессорная №2" -> commentCompressor2 = commentString
                "Баллоная №2" -> commentBallroom2 = commentString
                "КПЗ ОПУ" -> commentKpzOpu = commentString
                "КПЗ-2" -> commentKpz2 = commentString
                "Насосная пожаротушения" -> commentFirePump = commentString
                "Мастерская по ремонту ВВ" -> commentWorkshop = commentString
                "Артскважина" -> commentArtWell = commentString
                "Здание артезианской скважины" -> commentArtesianWell = commentString
                "Помещение 1 (2) АБ" -> commentRoomAb = commentString
                "Помещение п/этажа №1,2,3" -> commentBasement = commentString
            }
        }
    }

    private fun loadBuildingsCommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        val filtered = saved.filterKeys { it.startsWith("BUILDINGS_") }
            .mapKeys { it.key.removePrefix("BUILDINGS_") }
        _buildingsComments.value = filtered
    }

    // Температура наружного воздуха
    private val _outdoorTemp = MutableStateFlow("")
    val outdoorTemp: StateFlow<String> = _outdoorTemp

    fun updateOutdoorTemp(temp: String) {
        _outdoorTemp.value = temp
        autoSave()
    }
}