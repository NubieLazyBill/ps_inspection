// SharedInspectionViewModel.kt
package com.example.ps_inspection

import androidx.lifecycle.ViewModel
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

    fun clearAllData() {
        _oru35Data.value = InspectionORU35Data()
        _oru220Data.value = InspectionORU220Data()
        _atgData.value = InspectionATGData()
        _oru500Data.value = InspectionORU500Data()
        _buildingsData.value = InspectionBuildingsData()
        _atgComments.value = emptyMap()
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

    // Добавить комментарий
    fun addATGComment(equipmentKey: String, comment: String) {
        if (comment.isBlank()) return

        val currentMap = _atgComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: mutableListOf()
        currentList.add(comment)
        currentMap[equipmentKey] = currentList

        _atgComments.value = currentMap
        saveCommentsToAtgData(equipmentKey, currentList)
    }

    // Удалить комментарий по индексу
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
            saveCommentsToAtgData(equipmentKey, currentList)
        }
    }

    // Редактировать комментарий по индексу
    fun updateATGComment(equipmentKey: String, commentIndex: Int, newComment: String) {
        if (newComment.isBlank()) return

        val currentMap = _atgComments.value.toMutableMap()
        val currentList = currentMap[equipmentKey]?.toMutableList() ?: return
        if (commentIndex in currentList.indices) {
            currentList[commentIndex] = newComment
            currentMap[equipmentKey] = currentList
            _atgComments.value = currentMap
            saveCommentsToAtgData(equipmentKey, currentList)
        }
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

    // Загрузить комментарии из _atgData (вызвать при старте)
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

        _atgComments.value = commentsMap
    }
}