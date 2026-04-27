package com.example.ps_inspection

import java.util.Date

data class InspectionRecord(
    val id: String,
    val timestamp: Date,
    val equipmentType: String, // "ОРУ-35", "ОРУ-220", "ОРУ-500", "АТГ", "Здания"
    val inspectorName: String = "Инспектор",
    val hasDefects: Boolean = false,
    val defectsCount: Int = 0,
    val filePath: String? = null // путь к Excel файлу
)