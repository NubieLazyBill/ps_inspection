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
}