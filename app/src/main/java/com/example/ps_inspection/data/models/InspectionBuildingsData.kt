package com.example.ps_inspection.data.models

data class InspectionBuildingsData(
    var compressor1Valve: String = "○",
    var compressor1Heating: String = "○",
    var compressor1Temp: String = "",

    var ballroom1Valve: String = "○",
    var ballroom1Heating: String = "○",
    var ballroom1Temp: String = "",

    var compressor2Valve: String = "○",
    var compressor2Heating: String = "○",
    var compressor2Temp: String = "",

    var ballroom2Valve: String = "○",
    var ballroom2Heating: String = "○",
    var ballroom2Temp: String = "",

    var kpzOpuValve: String = "○",
    var kpzOpuHeating: String = "○",
    var kpzOpuTemp: String = "",

    var kpz2Valve: String = "○",
    var kpz2Heating: String = "○",
    var kpz2Temp: String = "",

    var firePumpValve: String = "○",
    var firePumpHeating: String = "○",
    var firePumpTemp: String = "",
    var firePumpWaterLevel: String = "",

    var workshopHeating: String = "○",
    var workshopTemp: String = "",

    var artWellHeating: String = "○",
    var artesianWellHeating: String = "○",

    var roomAbHeating: String = "○",
    var roomAbTemp: String = "",

    var basementHeating: String = "○",
    var basementTemp: String = "",

    var buildingsPhotoFiles: List<String> = emptyList()
)