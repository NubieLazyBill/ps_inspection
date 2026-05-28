package com.example.ps_inspection.data.utils

object InputValidator {

    // Граничные значения для ОРУ-35
    object ORU35 {
        const val OIL_LEVEL_MIN = -60.0
        const val OIL_LEVEL_MAX = 45.0
    }

    // Граничные значения для ОРУ-500
    object ORU500 {
        const val SF6_PRESSURE_MIN = 0.4
        const val SF6_PRESSURE_MAX = 0.8
    }

    // Граничные значения для АТГ и Реакторов
    object ATG {
        // Уровень масла в баке и РПН (мм)
        const val OIL_LEVEL_MIN = -100.0
        const val OIL_LEVEL_MAX = 100.0

        // Давление на вводах (МПа)
        const val PRESSURE_MIN = 0.0
        const val PRESSURE_MAX = 2.5

        // Температура по ТС-1 и ТС-2 (°C)
        const val TEMP_MIN = 0.0
        const val TEMP_MAX = 60.0

        // Давление маслонасосов (МПа) - для АТГ
        const val PUMP_PRESSURE_MIN = 0.9
        const val PUMP_PRESSURE_MAX = 1.2

        // Давление маслонасосов (МПа) - для Реакторов
        const val REACTOR_PUMP_PRESSURE_MIN = 0.4
        const val REACTOR_PUMP_PRESSURE_MAX = 1.2
    }

    fun isInRange(value: String, min: Double, max: Double): Boolean {
        if (value.isBlank()) return true
        return try {
            val num = value.toDouble()
            num in min..max
        } catch (e: NumberFormatException) {
            true
        }
    }

    fun getRangeMessage(paramName: String, min: Double, max: Double): String {
        return "⚠️ $paramName должен быть в диапазоне от $min до $max"
    }
}