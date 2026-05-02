package com.example.ps_inspection.data.models

data class InspectionBuildingsData(
    // Компрессорная №1
    var compressor1Valve: String = "○",
    var compressor1Heating: String = "○",
    var compressor1Temp: String = "",

    // Баллонная №1
    var ballroom1Valve: String = "○",
    var ballroom1Heating: String = "○",
    var ballroom1Temp: String = "",

    // Компрессорная №2
    var compressor2Valve: String = "○",
    var compressor2Heating: String = "○",
    var compressor2Temp: String = "",

    // Баллонная №2
    var ballroom2Valve: String = "○",
    var ballroom2Heating: String = "○",
    var ballroom2Temp: String = "",

    // КПЗ ОПУ
    var kpzOpuValve: String = "○",
    var kpzOpuHeating: String = "○",
    var kpzOpuTemp: String = "",

    // КПЗ-2
    var kpz2Valve: String = "○",
    var kpz2Heating: String = "○",
    var kpz2Temp: String = "",

    // Насосная пожаротушения
    var firePumpValve: String = "○",
    var firePumpHeating: String = "○",
    var firePumpTemp: String = "",
    var firePumpWaterLevel: String = "",

    // Мастерская по ремонту ВВ
    var workshopHeating: String = "○",
    var workshopTemp: String = "",

    // Артскважина
    var artWellHeating: String = "○",

    // Здание артезианской скважины
    var artesianWellHeating: String = "○",

    // Помещение 1 (2) АБ
    var roomAbHeating: String = "○",
    var roomAbTemp: String = "",

    // Помещение п/этажа №1,2,3
    var basementHeating: String = "○",
    var basementTemp: String = "",

    // ========== ФОТО ==========
    var buildingsPhotoFiles: List<String> = emptyList(),  // общий список фото для Buildings

// ========== КОММЕНТАРИИ (с разделителем |||) ==========
    var commentCompressor1: String = "",
    var commentBallroom1: String = "",
    var commentCompressor2: String = "",
    var commentBallroom2: String = "",
    var commentKpzOpu: String = "",
    var commentKpz2: String = "",
    var commentFirePump: String = "",
    var commentWorkshop: String = "",
    var commentArtWell: String = "",
    var commentArtesianWell: String = "",
    var commentRoomAb: String = "",
    var commentBasement: String = "",
)