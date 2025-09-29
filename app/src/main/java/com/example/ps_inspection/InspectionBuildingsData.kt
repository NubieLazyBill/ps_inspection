// InspectionBuildingsData.kt
package com.example.ps_inspection

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
    var basementTemp: String = ""
)