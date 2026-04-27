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
    }

    // В конце класса SharedInspectionViewModel

    /* --- Функции для комментариев АТГ ---
    fun updateATGComment(text: String) {
        val newData = _atgData.value.copy(atgComment = text)
        _atgData.value = newData
    }
     */

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

    private val _atgComments = MutableStateFlow<Map<String, String>>(emptyMap())
    val atgComments: StateFlow<Map<String, String>> = _atgComments


    fun updateATGComment(equipmentKey: String, comment: String) {
        val current = _atgData.value ?: return
        _atgData.value = current.copy(
            // 2 АТГ
            commentAtg2C = if (equipmentKey == "2 АТГ ф.С") comment else current.commentAtg2C,
            commentAtg2B = if (equipmentKey == "2 АТГ ф.В") comment else current.commentAtg2B,
            commentAtg2A = if (equipmentKey == "2 АТГ ф.А") comment else current.commentAtg2A,
            // АТГ резервная фаза
            commentAtgReserve = if (equipmentKey == "АТГ резервная") comment else current.commentAtgReserve,
            // 3 АТГ
            commentAtg3C = if (equipmentKey == "3 АТГ ф.С") comment else current.commentAtg3C,
            commentAtg3B = if (equipmentKey == "3 АТГ ф.В") comment else current.commentAtg3B,
            commentAtg3A = if (equipmentKey == "3 АТГ ф.А") comment else current.commentAtg3A,
            // Реакторы
            commentReactorC = if (equipmentKey == "Реактор ф.С") comment else current.commentReactorC,
            commentReactorB = if (equipmentKey == "Реактор ф.В") comment else current.commentReactorB,
            commentReactorA = if (equipmentKey == "Реактор ф.А") comment else current.commentReactorA
        )
    }
}