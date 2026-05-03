package com.example.ps_inspection.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.ps_inspection.data.repositories.CommentStorageManager
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

    // Функция для обновления данных ORU35
    fun updateORU35Data(update: InspectionORU35Data.() -> Unit) {
        val newData = _oru35Data.value.copy().apply(update)
        _oru35Data.value = newData
    }

    // Функция для обновления данных ORU220
    fun updateORU220Data(update: InspectionORU220Data.() -> Unit) {
        val newData = _oru220Data.value.copy().apply(update)
        _oru220Data.value = newData
    }

    // Функция для обновления данных АТГ
    fun updateATGData(update: InspectionATGData.() -> Unit) {
        val newData = _atgData.value.copy().apply(update)
        _atgData.value = newData
    }

    // Функция для обновления данных Buildings
    fun updateBuildingsData(update: InspectionBuildingsData.() -> Unit) {
        val newData = _buildingsData.value.copy().apply(update)
        _buildingsData.value = newData
    }

    // Функция для обновления данных ORU500
    fun updateORU500Data(update: InspectionORU500Data.() -> Unit) {
        val newData = _oru500Data.value.copy().apply(update)
        _oru500Data.value = newData
    }

    // Инициализация хранилища комментариев
    fun initCommentStorage(context: Context) {
        commentStorage = CommentStorageManager(context)

        // Загружаем комментарии для всех фрагментов
        loadCommentsFromStorage()              // АТГ
        loadORU35CommentsFromStorage()         // ОРУ-35
        loadORU220CommentsFromStorage()        // ОРУ-220
        loadORU500CommentsFromStorage()        // ОРУ-500
        loadBuildingsCommentsFromStorage()     // Buildings
    }

    // Очистка всех данных осмотра (комментарии НЕ трогаем!)
    fun clearAllData() {
        _oru35Data.value = InspectionORU35Data()
        _oru220Data.value = InspectionORU220Data()
        _atgData.value = InspectionATGData()
        _oru500Data.value = InspectionORU500Data()
        _buildingsData.value = InspectionBuildingsData()
        // НЕ очищаем комментарии - они остаются!

    }

    // Очистка только комментариев (если нужно)
    fun clearAllComments() {
        commentStorage.clearAllComments()
        loadCommentsFromStorage()
    }

    // --- Функции для фото АТГ ---
    fun addATGPhoto(fileName: String) {
        val currentList = _atgData.value.atgPhotoFiles.toMutableList()
        if (!currentList.contains(fileName)) {
            currentList.add(fileName)
            val newData = _atgData.value.copy(atgPhotoFiles = currentList)
            _atgData.value = newData
        }
    }

    fun removeATGPhoto(fileName: String) {
        val currentList = _atgData.value.atgPhotoFiles.toMutableList()
        if (currentList.remove(fileName)) {
            val newData = _atgData.value.copy(atgPhotoFiles = currentList)
            _atgData.value = newData
        }
    }

    // ========== КОММЕНТАРИИ (СПИСОК) ==========

    private val _atgComments = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val atgComments: StateFlow<Map<String, List<String>>> = _atgComments

    // Загрузить комментарии из хранилища
    private fun loadCommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        _atgComments.value = saved
    }

    // Добавить комментарий
    fun addATGComment(equipmentKey: String, comment: String) {
        if (comment.isBlank()) return
        commentStorage.addComment(equipmentKey, comment)
        loadCommentsFromStorage()
        // Сохраняем также в _atgData для архива
        saveCommentsToAtgData(equipmentKey, commentStorage.getComments(equipmentKey))
    }

    // Удалить комментарий по индексу
    fun removeATGComment(equipmentKey: String, commentIndex: Int) {
        commentStorage.removeComment(equipmentKey, commentIndex)
        loadCommentsFromStorage()
        saveCommentsToAtgData(equipmentKey, commentStorage.getComments(equipmentKey))
    }

    // Редактировать комментарий по индексу
    fun updateATGComment(equipmentKey: String, commentIndex: Int, newComment: String) {
        if (newComment.isBlank()) return
        commentStorage.updateComment(equipmentKey, commentIndex, newComment)
        loadCommentsFromStorage()
        saveCommentsToAtgData(equipmentKey, commentStorage.getComments(equipmentKey))
    }

    // Сохранить комментарии в _atgData (для архива)
    private fun saveCommentsToAtgData(equipmentKey: String, comments: List<String>) {
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
            commentTn35 = if (equipmentKey == "ТН-35") commentString else currentData.commentTn35   // ← добавить
        )
    }

    // Загрузить комментарии из _atgData (при загрузке архива)
    fun loadCommentsFromAtgData() {
        val data = _atgData.value
        val commentsMap = mutableMapOf<String, List<String>>()

        fun parseComments(str: String): List<String> {
            return if (str.isBlank()) emptyList() else str.split("|||")
        }

        commentsMap["2 АТГ ф.С"] = parseComments(data.commentAtg2C)
        commentsMap["2 АТГ ф.В"] = parseComments(data.commentAtg2B)
        commentsMap["2 АТГ ф.А"] = parseComments(data.commentAtg2A)
        commentsMap["АТГ резервная"] = parseComments(data.commentAtgReserve)
        commentsMap["3 АТГ ф.С"] = parseComments(data.commentAtg3C)
        commentsMap["3 АТГ ф.В"] = parseComments(data.commentAtg3B)
        commentsMap["3 АТГ ф.А"] = parseComments(data.commentAtg3A)
        commentsMap["Реактор ф.С"] = parseComments(data.commentReactorC)
        commentsMap["Реактор ф.В"] = parseComments(data.commentReactorB)
        commentsMap["Реактор ф.А"] = parseComments(data.commentReactorA)
        commentsMap["ТН-35"] = parseComments(data.commentTn35)   // ← добавить

        // Сохраняем в постоянное хранилище
        commentsMap.forEach { (key, value) ->
            if (value.isNotEmpty()) {
                commentStorage.addComment(key, value.last())
            }
        }
        loadCommentsFromStorage()
    }

    // --- Функции для фото ОРУ-35 ---
    fun addORU35Photo(fileName: String) {
        val currentList = _oru35Data.value.oru35PhotoFiles.toMutableList()
        if (!currentList.contains(fileName)) {
            currentList.add(fileName)
            val newData = _oru35Data.value.copy(oru35PhotoFiles = currentList)
            _oru35Data.value = newData
        }
    }

    fun removeORU35Photo(fileName: String) {
        val currentList = _oru35Data.value.oru35PhotoFiles.toMutableList()
        if (currentList.remove(fileName)) {
            val newData = _oru35Data.value.copy(oru35PhotoFiles = currentList)
            _oru35Data.value = newData
        }
    }

    // ========== КОММЕНТАРИИ ДЛЯ ОРУ-35 (СПИСОК) ==========

    private val _oru35Comments = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val oru35Comments: StateFlow<Map<String, List<String>>> = _oru35Comments

    // Добавить комментарий
    fun addORU35Comment(equipmentKey: String, comment: String) {
        if (comment.isBlank()) return

        val currentMap = _oru35Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(comment)
        currentMap[equipmentKey] = currentList
        _oru35Comments.value = currentMap

        // Сохраняем в хранилище
        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[equipmentKey] = currentList
        commentStorage.saveAllComments(allComments)

        // Сохраняем в _oru35Data как строку с разделителем
        saveORU35CommentsToData(equipmentKey, currentList)
    }

    // Удалить комментарий по индексу
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

            // Обновляем хранилище
            val allComments = commentStorage.loadAllComments().toMutableMap()
            if (currentList.isEmpty()) {
                allComments.remove(equipmentKey)
            } else {
                allComments[equipmentKey] = currentList
            }
            commentStorage.saveAllComments(allComments)

            // Обновляем _oru35Data
            saveORU35CommentsToData(equipmentKey, currentList)
        }
    }

    // Редактировать комментарий
    fun updateORU35Comment(equipmentKey: String, commentIndex: Int, newComment: String) {
        if (newComment.isBlank()) return

        val currentMap = _oru35Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList[commentIndex] = newComment
            currentMap[equipmentKey] = currentList
            _oru35Comments.value = currentMap

            // Обновляем хранилище
            val allComments = commentStorage.loadAllComments().toMutableMap()
            allComments[equipmentKey] = currentList
            commentStorage.saveAllComments(allComments)

            // Обновляем _oru35Data
            saveORU35CommentsToData(equipmentKey, currentList)
        }
    }

    // Сохранить комментарии в _oru35Data (для архива)
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

    // Загрузить комментарии из _oru35Data
    fun loadORU35CommentsFromData() {
        val data = _oru35Data.value
        val commentsMap = mutableMapOf<String, List<String>>()

        fun parseComments(str: String): List<String> {
            return if (str.isBlank()) emptyList() else str.split("|||")
        }

        commentsMap["ТСН"] = parseComments(data.commentTsn)
        commentsMap["ТТ-35 2ТСН"] = parseComments(data.commentTt352)
        commentsMap["ТТ-35 3ТСН"] = parseComments(data.commentTt353)
        commentsMap["В-35 2ТСН"] = parseComments(data.commentV352)
        commentsMap["В-35 3ТСН"] = parseComments(data.commentV353)

        _oru35Comments.value = commentsMap
    }

    // Загрузить комментарии из хранилища
    fun loadORU35CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        _oru35Comments.value = saved
    }

    // ========== КОММЕНТАРИИ ДЛЯ ОРУ-220 (СПИСОК) ==========

    private val _oru220Comments = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val oru220Comments: StateFlow<Map<String, List<String>>> = _oru220Comments

    fun addORU220Comment(equipmentKey: String, comment: String) {
        if (comment.isBlank()) return

        val currentMap = _oru220Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(comment)
        currentMap[equipmentKey] = currentList
        _oru220Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[equipmentKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveORU220CommentsToData(equipmentKey, currentList)
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
            if (currentList.isEmpty()) {
                allComments.remove(equipmentKey)
            } else {
                allComments[equipmentKey] = currentList
            }
            commentStorage.saveAllComments(allComments)

            saveORU220CommentsToData(equipmentKey, currentList)
        }
    }

    fun updateORU220Comment(equipmentKey: String, commentIndex: Int, newComment: String) {
        if (newComment.isBlank()) return

        val currentMap = _oru220Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList[commentIndex] = newComment
            currentMap[equipmentKey] = currentList
            _oru220Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            allComments[equipmentKey] = currentList
            commentStorage.saveAllComments(allComments)

            saveORU220CommentsToData(equipmentKey, currentList)
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

    fun loadORU220CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        _oru220Comments.value = saved
    }

    fun loadORU220CommentsFromData() {
        val data = _oru220Data.value
        val commentsMap = mutableMapOf<String, List<String>>()

        fun parseComments(str: String): List<String> {
            return if (str.isBlank()) emptyList() else str.split("|||")
        }

        commentsMap["Мирная"] = parseComments(data.commentMirnaya)
        commentsMap["Мирная ТТ"] = parseComments(data.commentMirnayaTT)
        commentsMap["Топаз"] = parseComments(data.commentTopaz)
        commentsMap["Топаз ТТ"] = parseComments(data.commentTopazTT)
        commentsMap["ОВ"] = parseComments(data.commentOv)
        commentsMap["ОВ ТТ"] = parseComments(data.commentOvTT)
        commentsMap["ТН-220 ОСШ"] = parseComments(data.commentOssh)
        commentsMap["2АТГ"] = parseComments(data.commentV2atg)
        commentsMap["2АТГ ТТ"] = parseComments(data.commentV2atgTT)
        commentsMap["ШСВ"] = parseComments(data.commentShsv)
        commentsMap["ШСВ ТТ"] = parseComments(data.commentShsvTT)
        commentsMap["3АТГ"] = parseComments(data.commentV3atg)
        commentsMap["3АТГ ТТ"] = parseComments(data.commentV3atgTT)
        commentsMap["Орбита"] = parseComments(data.commentOrbita)
        commentsMap["Орбита ТТ"] = parseComments(data.commentOrbitaTT)
        commentsMap["Факел"] = parseComments(data.commentFakel)
        commentsMap["Факел ТТ"] = parseComments(data.commentFakelTT)
        commentsMap["Комета-1"] = parseComments(data.commentCometa1)
        commentsMap["Комета-1 ТТ"] = parseComments(data.commentCometa1TT)
        commentsMap["Комета-2"] = parseComments(data.commentCometa2)
        commentsMap["Комета-2 ТТ"] = parseComments(data.commentCometa2TT)
        commentsMap["1ТН-220"] = parseComments(data.commentTn1)
        commentsMap["2ТН-220"] = parseComments(data.commentTn2)

        _oru220Comments.value = commentsMap
    }

    // ========== КОММЕНТАРИИ ДЛЯ ОРУ-500 (СПИСОК) ==========

    private val _oru500Comments = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val oru500Comments: StateFlow<Map<String, List<String>>> = _oru500Comments

    fun addORU500Comment(equipmentKey: String, comment: String) {
        if (comment.isBlank()) return

        val currentMap = _oru500Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(comment)
        currentMap[equipmentKey] = currentList
        _oru500Comments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[equipmentKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveORU500CommentsToData(equipmentKey, currentList)
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
            if (currentList.isEmpty()) {
                allComments.remove(equipmentKey)
            } else {
                allComments[equipmentKey] = currentList
            }
            commentStorage.saveAllComments(allComments)

            saveORU500CommentsToData(equipmentKey, currentList)
        }
    }

    fun updateORU500Comment(equipmentKey: String, commentIndex: Int, newComment: String) {
        if (newComment.isBlank()) return

        val currentMap = _oru500Comments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList[commentIndex] = newComment
            currentMap[equipmentKey] = currentList
            _oru500Comments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            allComments[equipmentKey] = currentList
            commentStorage.saveAllComments(allComments)

            saveORU500CommentsToData(equipmentKey, currentList)
        }
    }

    private fun saveORU500CommentsToData(equipmentKey: String, comments: List<String>) {
        val commentString = comments.joinToString("|||")
        updateORU500Data {
            when (equipmentKey) {
                // В-500
                "В-500 Р-500 2С" -> commentR5002s = commentString
                "В-500 ВШТ-31" -> commentVsht31 = commentString
                "В-500 ВЛТ-30" -> commentVlt30 = commentString
                "В-500 ВШЛ-32" -> commentVshl32 = commentString
                "В-500 ВШЛ-21" -> commentVshl21 = commentString
                "В-500 ВШТ-22" -> commentVsht22 = commentString
                "В-500 ВЛТ-20" -> commentVlt20 = commentString
                "В-500 ВШТ-11" -> commentVsht11 = commentString
                "В-500 ВШЛ-12" -> commentVshl12 = commentString
                // ТТ-500
                "ТТ-500 ВШТ-31" -> commentTtVsht31 = commentString
                "ТТ-500 ВЛТ-30" -> commentTtVlt30 = commentString
                "ТТ-500 ВШЛ-32" -> commentTtVshl32 = commentString
                "ТТ-500 ВШЛ-21" -> commentTtVshl21 = commentString
                "ТТ-500 ВШТ-22" -> commentTtVsht22 = commentString
                "ТТ-500 ВЛТ-20" -> commentTtVlt20 = commentString
                "ТТ-500 ВШТ-11" -> commentTtVsht11 = commentString
                "ТТ-500 ВШЛ-12" -> commentTtVshl12 = commentString
                // ТН-500
                "1ТН-500" -> commentTn1500 = commentString
                "2ТН-500" -> commentTn2500 = commentString
                "ТН-500 СГРЭС-1" -> commentTn500Sgres1 = commentString
                // Трачуковская
                "Трачуковская ТТ" -> commentTrachukovskayaTt = commentString
                "Трачуковская 2ТН" -> commentTrachukovskaya2tn = commentString
                "Трачуковская 1ТН" -> commentTrachukovskaya1tn = commentString
                // Белозёрная
                "Белозёрная 2ТН" -> commentBelozernaya2tn = commentString
            }
        }
    }

    fun loadORU500CommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        // Фильтруем только комментарии для ОРУ-500
        val oru500Keys = setOf(
            "В-500 Р-500 2С", "В-500 ВШТ-31", "В-500 ВЛТ-30", "В-500 ВШЛ-32", "В-500 ВШЛ-21",
            "В-500 ВШТ-22", "В-500 ВЛТ-20", "В-500 ВШТ-11", "В-500 ВШЛ-12",
            "ТТ-500 ВШТ-31", "ТТ-500 ВЛТ-30", "ТТ-500 ВШЛ-32", "ТТ-500 ВШЛ-21",
            "ТТ-500 ВШТ-22", "ТТ-500 ВЛТ-20", "ТТ-500 ВШТ-11", "ТТ-500 ВШЛ-12",
            "1ТН-500", "2ТН-500", "ТН-500 СГРЭС-1",
            "Трачуковская ТТ", "Трачуковская 2ТН", "Трачуковская 1ТН",
            "Белозёрная 2ТН"
        )
        _oru500Comments.value = saved.filterKeys { it in oru500Keys }
    }

    fun loadORU500CommentsFromData() {
        val data = _oru500Data.value
        val commentsMap = mutableMapOf<String, List<String>>()

        fun parseComments(str: String): List<String> {
            return if (str.isBlank()) emptyList() else str.split("|||")
        }

        // В-500
        commentsMap["В-500 Р-500 2С"] = parseComments(data.commentR5002s)
        commentsMap["В-500 ВШТ-31"] = parseComments(data.commentVsht31)
        commentsMap["В-500 ВЛТ-30"] = parseComments(data.commentVlt30)
        commentsMap["В-500 ВШЛ-32"] = parseComments(data.commentVshl32)
        commentsMap["В-500 ВШЛ-21"] = parseComments(data.commentVshl21)
        commentsMap["В-500 ВШТ-22"] = parseComments(data.commentVsht22)
        commentsMap["В-500 ВЛТ-20"] = parseComments(data.commentVlt20)
        commentsMap["В-500 ВШТ-11"] = parseComments(data.commentVsht11)
        commentsMap["В-500 ВШЛ-12"] = parseComments(data.commentVshl12)

        // ТТ-500
        commentsMap["ТТ-500 ВШТ-31"] = parseComments(data.commentTtVsht31)
        commentsMap["ТТ-500 ВЛТ-30"] = parseComments(data.commentTtVlt30)
        commentsMap["ТТ-500 ВШЛ-32"] = parseComments(data.commentTtVshl32)
        commentsMap["ТТ-500 ВШЛ-21"] = parseComments(data.commentTtVshl21)
        commentsMap["ТТ-500 ВШТ-22"] = parseComments(data.commentTtVsht22)
        commentsMap["ТТ-500 ВЛТ-20"] = parseComments(data.commentTtVlt20)
        commentsMap["ТТ-500 ВШТ-11"] = parseComments(data.commentTtVsht11)
        commentsMap["ТТ-500 ВШЛ-12"] = parseComments(data.commentTtVshl12)

        // ТН-500
        commentsMap["1ТН-500"] = parseComments(data.commentTn1500)
        commentsMap["2ТН-500"] = parseComments(data.commentTn2500)
        commentsMap["ТН-500 СГРЭС-1"] = parseComments(data.commentTn500Sgres1)

        // Трачуковская
        commentsMap["Трачуковская ТТ"] = parseComments(data.commentTrachukovskayaTt)
        commentsMap["Трачуковская 2ТН"] = parseComments(data.commentTrachukovskaya2tn)
        commentsMap["Трачуковская 1ТН"] = parseComments(data.commentTrachukovskaya1tn)

        // Белозёрная
        commentsMap["Белозёрная 2ТН"] = parseComments(data.commentBelozernaya2tn)

        _oru500Comments.value = commentsMap
    }

    // ========== КОММЕНТАРИИ ДЛЯ BUILDINGS (СПИСОК) ==========

    private val _buildingsComments = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val buildingsComments: StateFlow<Map<String, List<String>>> = _buildingsComments

    fun addBuildingsComment(equipmentKey: String, comment: String) {
        if (comment.isBlank()) return

        val currentMap = _buildingsComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(comment)
        currentMap[equipmentKey] = currentList
        _buildingsComments.value = currentMap

        val allComments = commentStorage.loadAllComments().toMutableMap()
        allComments[equipmentKey] = currentList
        commentStorage.saveAllComments(allComments)

        saveBuildingsCommentsToData(equipmentKey, currentList)
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
            if (currentList.isEmpty()) {
                allComments.remove(equipmentKey)
            } else {
                allComments[equipmentKey] = currentList
            }
            commentStorage.saveAllComments(allComments)

            saveBuildingsCommentsToData(equipmentKey, currentList)
        }
    }

    fun updateBuildingsComment(equipmentKey: String, commentIndex: Int, newComment: String) {
        if (newComment.isBlank()) return

        val currentMap = _buildingsComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList[commentIndex] = newComment
            currentMap[equipmentKey] = currentList
            _buildingsComments.value = currentMap

            val allComments = commentStorage.loadAllComments().toMutableMap()
            allComments[equipmentKey] = currentList
            commentStorage.saveAllComments(allComments)

            saveBuildingsCommentsToData(equipmentKey, currentList)
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

    fun loadBuildingsCommentsFromStorage() {
        val saved = commentStorage.loadAllComments()
        // Фильтруем только комментарии для Buildings
        val buildingsKeys = setOf(
            "Компрессорная №1", "Баллоная №1", "Компрессорная №2", "Баллоная №2",
            "КПЗ ОПУ", "КПЗ-2", "Насосная пожаротушения", "Мастерская по ремонту ВВ",
            "Артскважина", "Здание артезианской скважины", "Помещение 1 (2) АБ",
            "Помещение п/этажа №1,2,3"
        )
        _buildingsComments.value = saved.filterKeys { it in buildingsKeys }
    }

    fun loadBuildingsCommentsFromData() {
        val data = _buildingsData.value
        val commentsMap = mutableMapOf<String, List<String>>()

        fun parseComments(str: String): List<String> {
            return if (str.isBlank()) emptyList() else str.split("|||")
        }

        commentsMap["Компрессорная №1"] = parseComments(data.commentCompressor1)
        commentsMap["Баллоная №1"] = parseComments(data.commentBallroom1)
        commentsMap["Компрессорная №2"] = parseComments(data.commentCompressor2)
        commentsMap["Баллоная №2"] = parseComments(data.commentBallroom2)
        commentsMap["КПЗ ОПУ"] = parseComments(data.commentKpzOpu)
        commentsMap["КПЗ-2"] = parseComments(data.commentKpz2)
        commentsMap["Насосная пожаротушения"] = parseComments(data.commentFirePump)
        commentsMap["Мастерская по ремонту ВВ"] = parseComments(data.commentWorkshop)
        commentsMap["Артскважина"] = parseComments(data.commentArtWell)
        commentsMap["Здание артезианской скважины"] = parseComments(data.commentArtesianWell)
        commentsMap["Помещение 1 (2) АБ"] = parseComments(data.commentRoomAb)
        commentsMap["Помещение п/этажа №1,2,3"] = parseComments(data.commentBasement)

        _buildingsComments.value = commentsMap
    }

}