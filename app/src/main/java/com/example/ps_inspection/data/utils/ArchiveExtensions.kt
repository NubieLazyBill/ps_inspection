package com.example.ps_inspection.data.utils

import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data

enum class FillStatus { EMPTY, PARTIAL, FULL }

fun InspectionORU35Data.getFillStatus(): FillStatus =
    listOf(tsn2, tsn3, tsn4, tt352tsnA, tt352tsnB, tt352tsnC, tt353tsnA, tt353tsnB, tt353tsnC,
        v352tsnA, v352tsnB, v352tsnC, v353tsnA, v353tsnB, v353tsnC, /*tn352atg, tn353atg*/)
        .calculateStatus()

fun InspectionORU220Data.getFillStatus(): FillStatus =
    listOf(purgingMirnayaA, purgingMirnayaB, purgingMirnayaC, oilMirnayaA, oilMirnayaB, oilMirnayaC,
        purgingTopazA, purgingTopazB, purgingTopazC, oilTopazA, oilTopazB, oilTopazC,
        purgingOvA, purgingOvB, purgingOvC, oilOvA, oilOvB, oilOvC, tnOsshFvUpper, tnOsshFvLower,
        purgingV2atgA, purgingV2atgB, purgingV2atgC, oilTt2atgA, oilTt2atgB, oilTt2atgC,
        purgingShSV220A, purgingShSV220B, purgingShSV220C, oilTtShSV220A, oilTtShSV220B, oilTtShSV220C,
        purgingV3atgA, purgingV3atgB, purgingV3atgC, oilTt3atgA, oilTt3atgB, oilTt3atgC,
        purgingOrbitaA, purgingOrbitaB, purgingOrbitaC, oilOrbitaA, oilOrbitaB, oilOrbitaC,
        purgingFakelA, purgingFakelB, purgingFakelC, oilFakelA, oilFakelB, oilFakelC,
        purgingCometa1A, purgingCometa1B, purgingCometa1C, oilCometa1A, oilCometa1B, oilCometa1C,
        purgingCometa2A, purgingCometa2B, purgingCometa2C, oilCometa2A, oilCometa2B, oilCometa2C,
        tn1UpperA, tn1UpperB, tn1UpperC, tn1LowerA, tn1LowerB, tn1LowerC,
        tn2UpperA, tn2UpperB, tn2UpperC, tn2LowerA, tn2LowerB, tn2LowerC)
        .calculateStatus()

fun InspectionORU500Data.getFillStatus(): FillStatus =
    listOf(purgingR5002sA1, purgingR5002sB1, purgingR5002sC1, purgingR5002sA2, purgingR5002sB2, purgingR5002sC2,
        gasPressureVsht31A, gasPressureVsht31B, gasPressureVsht31C, oilTtVsht31A, oilTtVsht31B, oilTtVsht31C,
        gasPressureVlt30A, gasPressureVlt30B, gasPressureVlt30C, oilTtVlt30A, oilTtVlt30B, oilTtVlt30C,
        purgingVshl32A1, purgingVshl32B1, purgingVshl32C1, purgingVshl32A2, purgingVshl32B2, purgingVshl32C2,
        oilTtVshl32A, oilTtVshl32B, oilTtVshl32C, purgingVshl21A1, purgingVshl21B1, purgingVshl21C1,
        purgingVshl21A2, purgingVshl21B2, purgingVshl21C2, oilTtVshl21A, oilTtVshl21B, oilTtVshl21C,
        purgingVsht22A1, purgingVsht22B1, purgingVsht22C1, purgingVsht22A2, purgingVsht22B2, purgingVsht22C2,
        oilTtVsht22A, oilTtVsht22B, oilTtVsht22C, purgingVlt20A1, purgingVlt20B1, purgingVlt20C1,
        purgingVlt20A2, purgingVlt20B2, purgingVlt20C2, oilTtVlt20A, oilTtVlt20B, oilTtVlt20C,
        purgingVsht11A1, purgingVsht11B1, purgingVsht11C1, purgingVsht11A2, purgingVsht11B2, purgingVsht11C2,
        oilTtVsht11A, oilTtVsht11B, oilTtVsht11C, purgingVshl12A1, purgingVshl12B1, purgingVshl12C1,
        purgingVshl12A2, purgingVshl12B2, purgingVshl12C2, oilTtVshl12A, oilTtVshl12B, oilTtVshl12C,
        tn1500Cascade1A, tn1500Cascade1B, tn1500Cascade1C, tn1500Cascade2A, tn1500Cascade2B, tn1500Cascade2C,
        tn1500Cascade3A, tn1500Cascade3B, tn1500Cascade3C, tn1500Cascade4A, tn1500Cascade4B, tn1500Cascade4C,
        tn2500Cascade1A, tn2500Cascade1B, tn2500Cascade1C, tn2500Cascade2A, tn2500Cascade2B, tn2500Cascade2C,
        tn2500Cascade3A, tn2500Cascade3B, tn2500Cascade3C, tn2500Cascade4A, tn2500Cascade4B, tn2500Cascade4C,
        tn500Sgres1Cascade1A, tn500Sgres1Cascade1B, tn500Sgres1Cascade1C, tn500Sgres1Cascade2A, tn500Sgres1Cascade2B,
        tn500Sgres1Cascade2C, tn500Sgres1Cascade3A, tn500Sgres1Cascade3B, tn500Sgres1Cascade3C, tn500Sgres1Cascade4A,
        tn500Sgres1Cascade4B, tn500Sgres1Cascade4C, oilTtTrachukovskayaA, oilTtTrachukovskayaB, oilTtTrachukovskayaC,
        oil2tnTrachukovskayaA, oil2tnTrachukovskayaB, oil2tnTrachukovskayaC, oil1tnTrachukovskayaA, oil1tnTrachukovskayaB,
        oil1tnTrachukovskayaC, oil2tnBelozernayaA, oil2tnBelozernayaB, oil2tnBelozernayaC)
        .calculateStatus()

fun InspectionATGData.getFillStatus(): FillStatus =
    listOf(atg2_c_oil_tank, atg2_c_oil_rpn, atg2_c_pressure_500, atg2_c_pressure_220, atg2_c_temp_ts1, atg2_c_temp_ts2,
        atg2_c_pump_group1, atg2_c_pump_group2, atg2_c_pump_group3, atg2_c_pump_group4,
        atg2_b_oil_tank, atg2_b_oil_rpn, atg2_b_pressure_500, atg2_b_pressure_220, atg2_b_temp_ts1, atg2_b_temp_ts2,
        atg2_b_pump_group1, atg2_b_pump_group2, atg2_b_pump_group3, atg2_b_pump_group4,
        atg2_a_oil_tank, atg2_a_oil_rpn, atg2_a_pressure_500, atg2_a_pressure_220, atg2_a_temp_ts1, atg2_a_temp_ts2,
        atg2_a_pump_group1, atg2_a_pump_group2, atg2_a_pump_group3, atg2_a_pump_group4,
        atg_reserve_oil_tank, atg_reserve_oil_rpn, atg_reserve_pressure_500, atg_reserve_pressure_220,
        atg_reserve_temp_ts1, atg_reserve_temp_ts2, atg_reserve_pump_group1, atg_reserve_pump_group2,
        atg_reserve_pump_group3, atg_reserve_pump_group4,
        atg3_c_oil_tank, atg3_c_oil_rpn, atg3_c_pressure_500, atg3_c_pressure_220, atg3_c_temp_ts1, atg3_c_temp_ts2,
        atg3_c_pump_group1, atg3_c_pump_group2, atg3_c_pump_group3, atg3_c_pump_group4,
        atg3_b_oil_tank, atg3_b_oil_rpn, atg3_b_pressure_500, atg3_b_pressure_220, atg3_b_temp_ts1, atg3_b_temp_ts2,
        atg3_b_pump_group1, atg3_b_pump_group2, atg3_b_pump_group3, atg3_b_pump_group4,
        atg3_a_oil_tank, atg3_a_oil_rpn, atg3_a_pressure_500, atg3_a_pressure_220, atg3_a_temp_ts1, atg3_a_temp_ts2,
        atg3_a_pump_group1, atg3_a_pump_group2, atg3_a_pump_group3, atg3_a_pump_group4,
        reactor_c_oil_tank, reactor_c_pressure_500, reactor_c_temp_ts, reactor_c_pump_group1, reactor_c_pump_group2,
        reactor_c_pump_group3, reactor_c_tt_neutral,
        reactor_b_oil_tank, reactor_b_pressure_500, reactor_b_temp_ts, reactor_b_pump_group1, reactor_b_pump_group2,
        reactor_b_pump_group3, reactor_b_tt_neutral,
        reactor_a_oil_tank, reactor_a_pressure_500, reactor_a_temp_ts, reactor_a_pump_group1, reactor_a_pump_group2,
        reactor_a_pump_group3, reactor_a_tt_neutral,
        tn352atg, tn353atg)
        .calculateStatus()

fun InspectionBuildingsData.getFillStatus(): FillStatus =
    listOf(compressor1Valve, compressor1Heating, compressor1Temp, ballroom1Valve, ballroom1Heating, ballroom1Temp,
        compressor2Valve, compressor2Heating, compressor2Temp, ballroom2Valve, ballroom2Heating, ballroom2Temp,
        kpzOpuValve, kpzOpuHeating, kpzOpuTemp, kpz2Valve, kpz2Heating, kpz2Temp, firePumpValve, firePumpHeating,
        firePumpTemp, workshopHeating, workshopTemp, artWellHeating, artesianWellHeating, roomAbHeating, roomAbTemp,
        basementHeating, basementTemp)
        .calculateStatus()

// Вспомогательная функция
private fun List<String>.calculateStatus(): FillStatus {
    val filled = count { it.isNotBlank() }
    return when {
        filled == 0 -> FillStatus.EMPTY
        filled == size -> FillStatus.FULL
        else -> FillStatus.PARTIAL
    }
}