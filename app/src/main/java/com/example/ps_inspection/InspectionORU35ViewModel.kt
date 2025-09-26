// InspectionORU35ViewModel.kt
package com.example.ps_inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InspectionORU35ViewModel : ViewModel() {

    // Наши данные, хранящиеся в StateFlow. Начальное состояние - пустой объект.
    private val _inspectionData = MutableStateFlow(InspectionORU35Data())
    // Публичная неизменяемая версия для UI (Фрагмента)
    val inspectionData: StateFlow<InspectionORU35Data> = _inspectionData

    // Функция для обновления любых данных
    fun updateData(update: InspectionORU35Data.() -> Unit) {
        viewModelScope.launch {
            // Создаем копию текущих данных, применяем к ним функцию `update` и сохраняем новое состояние
            val newData = _inspectionData.value.copy().apply(update)
            _inspectionData.value = newData
        }
    }
}