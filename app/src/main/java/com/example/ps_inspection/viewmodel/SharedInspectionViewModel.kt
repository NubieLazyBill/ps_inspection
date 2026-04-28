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
        loadCommentsFromStorage()
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
            commentReactorA = if (equipmentKey == "Реактор ф.А") commentString else currentData.commentReactorA
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

        // Сохраняем в постоянное хранилище
        commentsMap.forEach { (key, value) ->
            if (value.isNotEmpty()) {
                commentStorage.addComment(key, value.last()) // Восстанавливаем последний комментарий
                // Для списка нужно более сложное восстановление, но пока так
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

}