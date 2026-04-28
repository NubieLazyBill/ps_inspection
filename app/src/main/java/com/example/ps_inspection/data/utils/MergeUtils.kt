package com.example.ps_inspection.data.utils

import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel

// Расширение для SharedInspectionViewModel - методы слияния данных из архива
fun SharedInspectionViewModel.mergeORU35(src: InspectionORU35Data) {
    updateORU35Data {
        if (src.tsn2.isNotBlank()) tsn2 = src.tsn2
        if (src.tsn3.isNotBlank()) tsn3 = src.tsn3
        if (src.tsn4.isNotBlank()) tsn4 = src.tsn4
        if (src.tt352tsnA.isNotBlank()) tt352tsnA = src.tt352tsnA
        if (src.tt352tsnB.isNotBlank()) tt352tsnB = src.tt352tsnB
        if (src.tt352tsnC.isNotBlank()) tt352tsnC = src.tt352tsnC
        if (src.tt353tsnA.isNotBlank()) tt353tsnA = src.tt353tsnA
        if (src.tt353tsnB.isNotBlank()) tt353tsnB = src.tt353tsnB
        if (src.tt353tsnC.isNotBlank()) tt353tsnC = src.tt353tsnC
        if (src.v352tsnA.isNotBlank()) v352tsnA = src.v352tsnA
        if (src.v352tsnB.isNotBlank()) v352tsnB = src.v352tsnB
        if (src.v352tsnC.isNotBlank()) v352tsnC = src.v352tsnC
        if (src.v353tsnA.isNotBlank()) v353tsnA = src.v353tsnA
        if (src.v353tsnB.isNotBlank()) v353tsnB = src.v353tsnB
        if (src.v353tsnC.isNotBlank()) v353tsnC = src.v353tsnC

        // Комментарии
        commentTsn = src.commentTsn
        commentTt352 = src.commentTt352
        commentTt353 = src.commentTt353
        commentV352 = src.commentV352
        commentV353 = src.commentV353

        // Фото
        oru35PhotoFiles = src.oru35PhotoFiles
    }
}

fun SharedInspectionViewModel.mergeORU220(src: InspectionORU220Data) {
    updateORU220Data {
        // Мирная
        purgingMirnayaA = src.purgingMirnayaA
        purgingMirnayaB = src.purgingMirnayaB
        purgingMirnayaC = src.purgingMirnayaC
        oilMirnayaA = src.oilMirnayaA
        oilMirnayaB = src.oilMirnayaB
        oilMirnayaC = src.oilMirnayaC

        // Топаз
        purgingTopazA = src.purgingTopazA
        purgingTopazB = src.purgingTopazB
        purgingTopazC = src.purgingTopazC
        oilTopazA = src.oilTopazA
        oilTopazB = src.oilTopazB
        oilTopazC = src.oilTopazC

        // ОВ
        purgingOvA = src.purgingOvA
        purgingOvB = src.purgingOvB
        purgingOvC = src.purgingOvC
        oilOvA = src.oilOvA
        oilOvB = src.oilOvB
        oilOvC = src.oilOvC

        // ТН-220 ОСШ
        tnOsshFvUpper = src.tnOsshFvUpper
        tnOsshFvLower = src.tnOsshFvLower

        // 2АТГ
        purgingV2atgA = src.purgingV2atgA
        purgingV2atgB = src.purgingV2atgB
        purgingV2atgC = src.purgingV2atgC
        oilTt2atgA = src.oilTt2atgA
        oilTt2atgB = src.oilTt2atgB
        oilTt2atgC = src.oilTt2atgC

        // ШСВ-220
        purgingShSV220A = src.purgingShSV220A
        purgingShSV220B = src.purgingShSV220B
        purgingShSV220C = src.purgingShSV220C
        oilTtShSV220A = src.oilTtShSV220A
        oilTtShSV220B = src.oilTtShSV220B
        oilTtShSV220C = src.oilTtShSV220C

        // 3АТГ
        purgingV3atgA = src.purgingV3atgA
        purgingV3atgB = src.purgingV3atgB
        purgingV3atgC = src.purgingV3atgC
        oilTt3atgA = src.oilTt3atgA
        oilTt3atgB = src.oilTt3atgB
        oilTt3atgC = src.oilTt3atgC

        // Орбита
        purgingOrbitaA = src.purgingOrbitaA
        purgingOrbitaB = src.purgingOrbitaB
        purgingOrbitaC = src.purgingOrbitaC
        oilOrbitaA = src.oilOrbitaA
        oilOrbitaB = src.oilOrbitaB
        oilOrbitaC = src.oilOrbitaC

        // Факел
        purgingFakelA = src.purgingFakelA
        purgingFakelB = src.purgingFakelB
        purgingFakelC = src.purgingFakelC
        oilFakelA = src.oilFakelA
        oilFakelB = src.oilFakelB
        oilFakelC = src.oilFakelC

        // Комета-1
        purgingCometa1A = src.purgingCometa1A
        purgingCometa1B = src.purgingCometa1B
        purgingCometa1C = src.purgingCometa1C
        oilCometa1A = src.oilCometa1A
        oilCometa1B = src.oilCometa1B
        oilCometa1C = src.oilCometa1C

        // Комета-2
        purgingCometa2A = src.purgingCometa2A
        purgingCometa2B = src.purgingCometa2B
        purgingCometa2C = src.purgingCometa2C
        oilCometa2A = src.oilCometa2A
        oilCometa2B = src.oilCometa2B
        oilCometa2C = src.oilCometa2C

        // 1ТН-220
        tn1UpperA = src.tn1UpperA
        tn1UpperB = src.tn1UpperB
        tn1UpperC = src.tn1UpperC
        tn1LowerA = src.tn1LowerA
        tn1LowerB = src.tn1LowerB
        tn1LowerC = src.tn1LowerC

        // 2ТН-220
        tn2UpperA = src.tn2UpperA
        tn2UpperB = src.tn2UpperB
        tn2UpperC = src.tn2UpperC
        tn2LowerA = src.tn2LowerA
        tn2LowerB = src.tn2LowerB
        tn2LowerC = src.tn2LowerC
    }
}

fun SharedInspectionViewModel.mergeORU500(src: InspectionORU500Data) {
    updateORU500Data {
        // Р-500 2С (продувка)
        purgingR5002sA1 = src.purgingR5002sA1
        purgingR5002sB1 = src.purgingR5002sB1
        purgingR5002sC1 = src.purgingR5002sC1
        purgingR5002sA2 = src.purgingR5002sA2
        purgingR5002sB2 = src.purgingR5002sB2
        purgingR5002sC2 = src.purgingR5002sC2

        // ВШТ-31, ВЛТ-30 (газ + масло)
        gasPressureVsht31A = src.gasPressureVsht31A
        gasPressureVsht31B = src.gasPressureVsht31B
        gasPressureVsht31C = src.gasPressureVsht31C
        oilTtVsht31A = src.oilTtVsht31A
        oilTtVsht31B = src.oilTtVsht31B
        oilTtVsht31C = src.oilTtVsht31C

        gasPressureVlt30A = src.gasPressureVlt30A
        gasPressureVlt30B = src.gasPressureVlt30B
        gasPressureVlt30C = src.gasPressureVlt30C
        oilTtVlt30A = src.oilTtVlt30A
        oilTtVlt30B = src.oilTtVlt30B
        oilTtVlt30C = src.oilTtVlt30C

        // ВШЛ-32
        purgingVshl32A1 = src.purgingVshl32A1
        purgingVshl32B1 = src.purgingVshl32B1
        purgingVshl32C1 = src.purgingVshl32C1
        purgingVshl32A2 = src.purgingVshl32A2
        purgingVshl32B2 = src.purgingVshl32B2
        purgingVshl32C2 = src.purgingVshl32C2
        oilTtVshl32A = src.oilTtVshl32A
        oilTtVshl32B = src.oilTtVshl32B
        oilTtVshl32C = src.oilTtVshl32C

        // ВШЛ-21
        purgingVshl21A1 = src.purgingVshl21A1
        purgingVshl21B1 = src.purgingVshl21B1
        purgingVshl21C1 = src.purgingVshl21C1
        purgingVshl21A2 = src.purgingVshl21A2
        purgingVshl21B2 = src.purgingVshl21B2
        purgingVshl21C2 = src.purgingVshl21C2
        oilTtVshl21A = src.oilTtVshl21A
        oilTtVshl21B = src.oilTtVshl21B
        oilTtVshl21C = src.oilTtVshl21C

        // ВШТ-22
        purgingVsht22A1 = src.purgingVsht22A1
        purgingVsht22B1 = src.purgingVsht22B1
        purgingVsht22C1 = src.purgingVsht22C1
        purgingVsht22A2 = src.purgingVsht22A2
        purgingVsht22B2 = src.purgingVsht22B2
        purgingVsht22C2 = src.purgingVsht22C2
        oilTtVsht22A = src.oilTtVsht22A
        oilTtVsht22B = src.oilTtVsht22B
        oilTtVsht22C = src.oilTtVsht22C

        // ВЛТ-20
        purgingVlt20A1 = src.purgingVlt20A1
        purgingVlt20B1 = src.purgingVlt20B1
        purgingVlt20C1 = src.purgingVlt20C1
        purgingVlt20A2 = src.purgingVlt20A2
        purgingVlt20B2 = src.purgingVlt20B2
        purgingVlt20C2 = src.purgingVlt20C2
        oilTtVlt20A = src.oilTtVlt20A
        oilTtVlt20B = src.oilTtVlt20B
        oilTtVlt20C = src.oilTtVlt20C

        // ВШТ-11, ВШЛ-12
        purgingVsht11A1 = src.purgingVsht11A1
        purgingVsht11B1 = src.purgingVsht11B1
        purgingVsht11C1 = src.purgingVsht11C1
        purgingVsht11A2 = src.purgingVsht11A2
        purgingVsht11B2 = src.purgingVsht11B2
        purgingVsht11C2 = src.purgingVsht11C2
        oilTtVsht11A = src.oilTtVsht11A
        oilTtVsht11B = src.oilTtVsht11B
        oilTtVsht11C = src.oilTtVsht11C

        purgingVshl12A1 = src.purgingVshl12A1
        purgingVshl12B1 = src.purgingVshl12B1
        purgingVshl12C1 = src.purgingVshl12C1
        purgingVshl12A2 = src.purgingVshl12A2
        purgingVshl12B2 = src.purgingVshl12B2
        purgingVshl12C2 = src.purgingVshl12C2
        oilTtVshl12A = src.oilTtVshl12A
        oilTtVshl12B = src.oilTtVshl12B
        oilTtVshl12C = src.oilTtVshl12C

        // 1ТН-500 (каскады)
        tn1500Cascade1A = src.tn1500Cascade1A
        tn1500Cascade1B = src.tn1500Cascade1B
        tn1500Cascade1C = src.tn1500Cascade1C
        tn1500Cascade2A = src.tn1500Cascade2A
        tn1500Cascade2B = src.tn1500Cascade2B
        tn1500Cascade2C = src.tn1500Cascade2C
        tn1500Cascade3A = src.tn1500Cascade3A
        tn1500Cascade3B = src.tn1500Cascade3B
        tn1500Cascade3C = src.tn1500Cascade3C
        tn1500Cascade4A = src.tn1500Cascade4A
        tn1500Cascade4B = src.tn1500Cascade4B
        tn1500Cascade4C = src.tn1500Cascade4C

        // 2ТН-500 (каскады)
        tn2500Cascade1A = src.tn2500Cascade1A
        tn2500Cascade1B = src.tn2500Cascade1B
        tn2500Cascade1C = src.tn2500Cascade1C
        tn2500Cascade2A = src.tn2500Cascade2A
        tn2500Cascade2B = src.tn2500Cascade2B
        tn2500Cascade2C = src.tn2500Cascade2C
        tn2500Cascade3A = src.tn2500Cascade3A
        tn2500Cascade3B = src.tn2500Cascade3B
        tn2500Cascade3C = src.tn2500Cascade3C
        tn2500Cascade4A = src.tn2500Cascade4A
        tn2500Cascade4B = src.tn2500Cascade4B
        tn2500Cascade4C = src.tn2500Cascade4C

        // ТН-500 СГРЭС-1 (каскады)
        tn500Sgres1Cascade1A = src.tn500Sgres1Cascade1A
        tn500Sgres1Cascade1B = src.tn500Sgres1Cascade1B
        tn500Sgres1Cascade1C = src.tn500Sgres1Cascade1C
        tn500Sgres1Cascade2A = src.tn500Sgres1Cascade2A
        tn500Sgres1Cascade2B = src.tn500Sgres1Cascade2B
        tn500Sgres1Cascade2C = src.tn500Sgres1Cascade2C
        tn500Sgres1Cascade3A = src.tn500Sgres1Cascade3A
        tn500Sgres1Cascade3B = src.tn500Sgres1Cascade3B
        tn500Sgres1Cascade3C = src.tn500Sgres1Cascade3C
        tn500Sgres1Cascade4A = src.tn500Sgres1Cascade4A
        tn500Sgres1Cascade4B = src.tn500Sgres1Cascade4B
        tn500Sgres1Cascade4C = src.tn500Sgres1Cascade4C

        // Трачуковская
        oilTtTrachukovskayaA = src.oilTtTrachukovskayaA
        oilTtTrachukovskayaB = src.oilTtTrachukovskayaB
        oilTtTrachukovskayaC = src.oilTtTrachukovskayaC
        oil2tnTrachukovskayaA = src.oil2tnTrachukovskayaA
        oil2tnTrachukovskayaB = src.oil2tnTrachukovskayaB
        oil2tnTrachukovskayaC = src.oil2tnTrachukovskayaC
        oil1tnTrachukovskayaA = src.oil1tnTrachukovskayaA
        oil1tnTrachukovskayaB = src.oil1tnTrachukovskayaB
        oil1tnTrachukovskayaC = src.oil1tnTrachukovskayaC

        // Белозёрная
        oil2tnBelozernayaA = src.oil2tnBelozernayaA
        oil2tnBelozernayaB = src.oil2tnBelozernayaB
        oil2tnBelozernayaC = src.oil2tnBelozernayaC
    }
}

fun SharedInspectionViewModel.mergeBuildings(src: InspectionBuildingsData) {
    updateBuildingsData {
        compressor1Valve = src.compressor1Valve
        compressor1Heating = src.compressor1Heating
        compressor1Temp = src.compressor1Temp
        ballroom1Valve = src.ballroom1Valve
        ballroom1Heating = src.ballroom1Heating
        ballroom1Temp = src.ballroom1Temp
        compressor2Valve = src.compressor2Valve
        compressor2Heating = src.compressor2Heating
        compressor2Temp = src.compressor2Temp
        ballroom2Valve = src.ballroom2Valve
        ballroom2Heating = src.ballroom2Heating
        ballroom2Temp = src.ballroom2Temp
        kpzOpuValve = src.kpzOpuValve
        kpzOpuHeating = src.kpzOpuHeating
        kpzOpuTemp = src.kpzOpuTemp
        kpz2Valve = src.kpz2Valve
        kpz2Heating = src.kpz2Heating
        kpz2Temp = src.kpz2Temp
        firePumpValve = src.firePumpValve
        firePumpHeating = src.firePumpHeating
        firePumpTemp = src.firePumpTemp
        workshopHeating = src.workshopHeating
        workshopTemp = src.workshopTemp
        artWellHeating = src.artWellHeating
        artesianWellHeating = src.artesianWellHeating
        roomAbHeating = src.roomAbHeating
        roomAbTemp = src.roomAbTemp
        basementHeating = src.basementHeating
        basementTemp = src.basementTemp
    }
}

fun SharedInspectionViewModel.mergeATG(src: InspectionATGData) {
    updateATGData {
        // 2 АТГ ф.С
        atg2_c_oil_tank = src.atg2_c_oil_tank
        atg2_c_oil_rpn = src.atg2_c_oil_rpn
        atg2_c_pressure_500 = src.atg2_c_pressure_500
        atg2_c_pressure_220 = src.atg2_c_pressure_220
        atg2_c_temp_ts1 = src.atg2_c_temp_ts1
        atg2_c_temp_ts2 = src.atg2_c_temp_ts2
        atg2_c_pump_group1 = src.atg2_c_pump_group1
        atg2_c_pump_group2 = src.atg2_c_pump_group2
        atg2_c_pump_group3 = src.atg2_c_pump_group3
        atg2_c_pump_group4 = src.atg2_c_pump_group4

        // 2 АТГ ф.В
        atg2_b_oil_tank = src.atg2_b_oil_tank
        atg2_b_oil_rpn = src.atg2_b_oil_rpn
        atg2_b_pressure_500 = src.atg2_b_pressure_500
        atg2_b_pressure_220 = src.atg2_b_pressure_220
        atg2_b_temp_ts1 = src.atg2_b_temp_ts1
        atg2_b_temp_ts2 = src.atg2_b_temp_ts2
        atg2_b_pump_group1 = src.atg2_b_pump_group1
        atg2_b_pump_group2 = src.atg2_b_pump_group2
        atg2_b_pump_group3 = src.atg2_b_pump_group3
        atg2_b_pump_group4 = src.atg2_b_pump_group4

        // 2 АТГ ф.А
        atg2_a_oil_tank = src.atg2_a_oil_tank
        atg2_a_oil_rpn = src.atg2_a_oil_rpn
        atg2_a_pressure_500 = src.atg2_a_pressure_500
        atg2_a_pressure_220 = src.atg2_a_pressure_220
        atg2_a_temp_ts1 = src.atg2_a_temp_ts1
        atg2_a_temp_ts2 = src.atg2_a_temp_ts2
        atg2_a_pump_group1 = src.atg2_a_pump_group1
        atg2_a_pump_group2 = src.atg2_a_pump_group2
        atg2_a_pump_group3 = src.atg2_a_pump_group3
        atg2_a_pump_group4 = src.atg2_a_pump_group4

        // АТГ резервная фаза
        atg_reserve_oil_tank = src.atg_reserve_oil_tank
        atg_reserve_oil_rpn = src.atg_reserve_oil_rpn
        atg_reserve_pressure_500 = src.atg_reserve_pressure_500
        atg_reserve_pressure_220 = src.atg_reserve_pressure_220
        atg_reserve_temp_ts1 = src.atg_reserve_temp_ts1
        atg_reserve_temp_ts2 = src.atg_reserve_temp_ts2
        atg_reserve_pump_group1 = src.atg_reserve_pump_group1
        atg_reserve_pump_group2 = src.atg_reserve_pump_group2
        atg_reserve_pump_group3 = src.atg_reserve_pump_group3
        atg_reserve_pump_group4 = src.atg_reserve_pump_group4

        // 3 АТГ ф.С
        atg3_c_oil_tank = src.atg3_c_oil_tank
        atg3_c_oil_rpn = src.atg3_c_oil_rpn
        atg3_c_pressure_500 = src.atg3_c_pressure_500
        atg3_c_pressure_220 = src.atg3_c_pressure_220
        atg3_c_temp_ts1 = src.atg3_c_temp_ts1
        atg3_c_temp_ts2 = src.atg3_c_temp_ts2
        atg3_c_pump_group1 = src.atg3_c_pump_group1
        atg3_c_pump_group2 = src.atg3_c_pump_group2
        atg3_c_pump_group3 = src.atg3_c_pump_group3
        atg3_c_pump_group4 = src.atg3_c_pump_group4

        // 3 АТГ ф.В
        atg3_b_oil_tank = src.atg3_b_oil_tank
        atg3_b_oil_rpn = src.atg3_b_oil_rpn
        atg3_b_pressure_500 = src.atg3_b_pressure_500
        atg3_b_pressure_220 = src.atg3_b_pressure_220
        atg3_b_temp_ts1 = src.atg3_b_temp_ts1
        atg3_b_temp_ts2 = src.atg3_b_temp_ts2
        atg3_b_pump_group1 = src.atg3_b_pump_group1
        atg3_b_pump_group2 = src.atg3_b_pump_group2
        atg3_b_pump_group3 = src.atg3_b_pump_group3
        atg3_b_pump_group4 = src.atg3_b_pump_group4

        // 3 АТГ ф.А
        atg3_a_oil_tank = src.atg3_a_oil_tank
        atg3_a_oil_rpn = src.atg3_a_oil_rpn
        atg3_a_pressure_500 = src.atg3_a_pressure_500
        atg3_a_pressure_220 = src.atg3_a_pressure_220
        atg3_a_temp_ts1 = src.atg3_a_temp_ts1
        atg3_a_temp_ts2 = src.atg3_a_temp_ts2
        atg3_a_pump_group1 = src.atg3_a_pump_group1
        atg3_a_pump_group2 = src.atg3_a_pump_group2
        atg3_a_pump_group3 = src.atg3_a_pump_group3
        atg3_a_pump_group4 = src.atg3_a_pump_group4

        // Реакторы
        reactor_c_oil_tank = src.reactor_c_oil_tank
        reactor_c_pressure_500 = src.reactor_c_pressure_500
        reactor_c_temp_ts = src.reactor_c_temp_ts
        reactor_c_pump_group1 = src.reactor_c_pump_group1
        reactor_c_pump_group2 = src.reactor_c_pump_group2
        reactor_c_pump_group3 = src.reactor_c_pump_group3
        reactor_c_tt_neutral = src.reactor_c_tt_neutral

        reactor_b_oil_tank = src.reactor_b_oil_tank
        reactor_b_pressure_500 = src.reactor_b_pressure_500
        reactor_b_temp_ts = src.reactor_b_temp_ts
        reactor_b_pump_group1 = src.reactor_b_pump_group1
        reactor_b_pump_group2 = src.reactor_b_pump_group2
        reactor_b_pump_group3 = src.reactor_b_pump_group3
        reactor_b_tt_neutral = src.reactor_b_tt_neutral

        reactor_a_oil_tank = src.reactor_a_oil_tank
        reactor_a_pressure_500 = src.reactor_a_pressure_500
        reactor_a_temp_ts = src.reactor_a_temp_ts
        reactor_a_pump_group1 = src.reactor_a_pump_group1
        reactor_a_pump_group2 = src.reactor_a_pump_group2
        reactor_a_pump_group3 = src.reactor_a_pump_group3
        reactor_a_tt_neutral = src.reactor_a_tt_neutral

        // ТН-35
        tn352atg = src.tn352atg
        tn353atg = src.tn353atg
    }
}

fun SharedInspectionViewModel.mergeATGPressuresOnly(src: InspectionATGData) {
    updateATGData {
        // 2 АТГ ф.С
        atg2_c_pump_group1 = src.atg2_c_pump_group1
        atg2_c_pump_group2 = src.atg2_c_pump_group2
        atg2_c_pump_group3 = src.atg2_c_pump_group3
        atg2_c_pump_group4 = src.atg2_c_pump_group4

        // 2 АТГ ф.В
        atg2_b_pump_group1 = src.atg2_b_pump_group1
        atg2_b_pump_group2 = src.atg2_b_pump_group2
        atg2_b_pump_group3 = src.atg2_b_pump_group3
        atg2_b_pump_group4 = src.atg2_b_pump_group4

        // 2 АТГ ф.А
        atg2_a_pump_group1 = src.atg2_a_pump_group1
        atg2_a_pump_group2 = src.atg2_a_pump_group2
        atg2_a_pump_group3 = src.atg2_a_pump_group3
        atg2_a_pump_group4 = src.atg2_a_pump_group4

        // АТГ резервная фаза
        atg_reserve_pump_group1 = src.atg_reserve_pump_group1
        atg_reserve_pump_group2 = src.atg_reserve_pump_group2
        atg_reserve_pump_group3 = src.atg_reserve_pump_group3
        atg_reserve_pump_group4 = src.atg_reserve_pump_group4

        // 3 АТГ ф.С
        atg3_c_pump_group1 = src.atg3_c_pump_group1
        atg3_c_pump_group2 = src.atg3_c_pump_group2
        atg3_c_pump_group3 = src.atg3_c_pump_group3
        atg3_c_pump_group4 = src.atg3_c_pump_group4

        // 3 АТГ ф.В
        atg3_b_pump_group1 = src.atg3_b_pump_group1
        atg3_b_pump_group2 = src.atg3_b_pump_group2
        atg3_b_pump_group3 = src.atg3_b_pump_group3
        atg3_b_pump_group4 = src.atg3_b_pump_group4

        // 3 АТГ ф.А
        atg3_a_pump_group1 = src.atg3_a_pump_group1
        atg3_a_pump_group2 = src.atg3_a_pump_group2
        atg3_a_pump_group3 = src.atg3_a_pump_group3
        atg3_a_pump_group4 = src.atg3_a_pump_group4

        // Реактор ф.С
        reactor_c_pump_group1 = src.reactor_c_pump_group1
        reactor_c_pump_group2 = src.reactor_c_pump_group2
        reactor_c_pump_group3 = src.reactor_c_pump_group3

        // Реактор ф.В
        reactor_b_pump_group1 = src.reactor_b_pump_group1
        reactor_b_pump_group2 = src.reactor_b_pump_group2
        reactor_b_pump_group3 = src.reactor_b_pump_group3

        // Реактор ф.А
        reactor_a_pump_group1 = src.reactor_a_pump_group1
        reactor_a_pump_group2 = src.reactor_a_pump_group2
        reactor_a_pump_group3 = src.reactor_a_pump_group3
    }
}