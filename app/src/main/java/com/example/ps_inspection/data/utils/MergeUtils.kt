package com.example.ps_inspection.data.utils

import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.models.InspectionORU220Data
import com.example.ps_inspection.data.models.InspectionORU35Data
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel

// ОРУ-35 — только незаполненные поля
fun SharedInspectionViewModel.mergeORU35(src: InspectionORU35Data) {
    updateORU35Data {
        if (tsn2.isBlank()) tsn2 = src.tsn2
        if (tsn3.isBlank()) tsn3 = src.tsn3
        if (tsn4.isBlank()) tsn4 = src.tsn4
        if (tt352tsnA.isBlank()) tt352tsnA = src.tt352tsnA
        if (tt352tsnB.isBlank()) tt352tsnB = src.tt352tsnB
        if (tt352tsnC.isBlank()) tt352tsnC = src.tt352tsnC
        if (tt353tsnA.isBlank()) tt353tsnA = src.tt353tsnA
        if (tt353tsnB.isBlank()) tt353tsnB = src.tt353tsnB
        if (tt353tsnC.isBlank()) tt353tsnC = src.tt353tsnC
        if (v352tsnA.isBlank()) v352tsnA = src.v352tsnA
        if (v352tsnB.isBlank()) v352tsnB = src.v352tsnB
        if (v352tsnC.isBlank()) v352tsnC = src.v352tsnC
        if (v353tsnA.isBlank()) v353tsnA = src.v353tsnA
        if (v353tsnB.isBlank()) v353tsnB = src.v353tsnB
        if (v353tsnC.isBlank()) v353tsnC = src.v353tsnC
        // комментарии и фото не трогаем
    }
}

// ОРУ-220 — только незаполненные поля
fun SharedInspectionViewModel.mergeORU220(src: InspectionORU220Data) {
    updateORU220Data {
        // Мирная
        if (purgingMirnayaA.isBlank()) purgingMirnayaA = src.purgingMirnayaA
        if (purgingMirnayaB.isBlank()) purgingMirnayaB = src.purgingMirnayaB
        if (purgingMirnayaC.isBlank()) purgingMirnayaC = src.purgingMirnayaC
        if (oilMirnayaA.isBlank()) oilMirnayaA = src.oilMirnayaA
        if (oilMirnayaB.isBlank()) oilMirnayaB = src.oilMirnayaB
        if (oilMirnayaC.isBlank()) oilMirnayaC = src.oilMirnayaC

        // Топаз
        if (purgingTopazA.isBlank()) purgingTopazA = src.purgingTopazA
        if (purgingTopazB.isBlank()) purgingTopazB = src.purgingTopazB
        if (purgingTopazC.isBlank()) purgingTopazC = src.purgingTopazC
        if (oilTopazA.isBlank()) oilTopazA = src.oilTopazA
        if (oilTopazB.isBlank()) oilTopazB = src.oilTopazB
        if (oilTopazC.isBlank()) oilTopazC = src.oilTopazC

        // ОВ
        if (purgingOvA.isBlank()) purgingOvA = src.purgingOvA
        if (purgingOvB.isBlank()) purgingOvB = src.purgingOvB
        if (purgingOvC.isBlank()) purgingOvC = src.purgingOvC
        if (oilOvA.isBlank()) oilOvA = src.oilOvA
        if (oilOvB.isBlank()) oilOvB = src.oilOvB
        if (oilOvC.isBlank()) oilOvC = src.oilOvC

        // ТН-220 ОСШ
        if (tnOsshFvUpper.isBlank()) tnOsshFvUpper = src.tnOsshFvUpper
        if (tnOsshFvLower.isBlank()) tnOsshFvLower = src.tnOsshFvLower

        // 2АТГ
        if (purgingV2atgA.isBlank()) purgingV2atgA = src.purgingV2atgA
        if (purgingV2atgB.isBlank()) purgingV2atgB = src.purgingV2atgB
        if (purgingV2atgC.isBlank()) purgingV2atgC = src.purgingV2atgC
        if (oilTt2atgA.isBlank()) oilTt2atgA = src.oilTt2atgA
        if (oilTt2atgB.isBlank()) oilTt2atgB = src.oilTt2atgB
        if (oilTt2atgC.isBlank()) oilTt2atgC = src.oilTt2atgC

        // ШСВ-220
        if (purgingShSV220A.isBlank()) purgingShSV220A = src.purgingShSV220A
        if (purgingShSV220B.isBlank()) purgingShSV220B = src.purgingShSV220B
        if (purgingShSV220C.isBlank()) purgingShSV220C = src.purgingShSV220C
        if (oilTtShSV220A.isBlank()) oilTtShSV220A = src.oilTtShSV220A
        if (oilTtShSV220B.isBlank()) oilTtShSV220B = src.oilTtShSV220B
        if (oilTtShSV220C.isBlank()) oilTtShSV220C = src.oilTtShSV220C

        // 3АТГ
        if (purgingV3atgA.isBlank()) purgingV3atgA = src.purgingV3atgA
        if (purgingV3atgB.isBlank()) purgingV3atgB = src.purgingV3atgB
        if (purgingV3atgC.isBlank()) purgingV3atgC = src.purgingV3atgC
        if (oilTt3atgA.isBlank()) oilTt3atgA = src.oilTt3atgA
        if (oilTt3atgB.isBlank()) oilTt3atgB = src.oilTt3atgB
        if (oilTt3atgC.isBlank()) oilTt3atgC = src.oilTt3atgC

        // Орбита
        if (purgingOrbitaA.isBlank()) purgingOrbitaA = src.purgingOrbitaA
        if (purgingOrbitaB.isBlank()) purgingOrbitaB = src.purgingOrbitaB
        if (purgingOrbitaC.isBlank()) purgingOrbitaC = src.purgingOrbitaC
        if (oilOrbitaA.isBlank()) oilOrbitaA = src.oilOrbitaA
        if (oilOrbitaB.isBlank()) oilOrbitaB = src.oilOrbitaB
        if (oilOrbitaC.isBlank()) oilOrbitaC = src.oilOrbitaC

        // Факел
        if (purgingFakelA.isBlank()) purgingFakelA = src.purgingFakelA
        if (purgingFakelB.isBlank()) purgingFakelB = src.purgingFakelB
        if (purgingFakelC.isBlank()) purgingFakelC = src.purgingFakelC
        if (oilFakelA.isBlank()) oilFakelA = src.oilFakelA
        if (oilFakelB.isBlank()) oilFakelB = src.oilFakelB
        if (oilFakelC.isBlank()) oilFakelC = src.oilFakelC

        // Комета-1
        if (purgingCometa1A.isBlank()) purgingCometa1A = src.purgingCometa1A
        if (purgingCometa1B.isBlank()) purgingCometa1B = src.purgingCometa1B
        if (purgingCometa1C.isBlank()) purgingCometa1C = src.purgingCometa1C
        if (oilCometa1A.isBlank()) oilCometa1A = src.oilCometa1A
        if (oilCometa1B.isBlank()) oilCometa1B = src.oilCometa1B
        if (oilCometa1C.isBlank()) oilCometa1C = src.oilCometa1C

        // Комета-2
        if (purgingCometa2A.isBlank()) purgingCometa2A = src.purgingCometa2A
        if (purgingCometa2B.isBlank()) purgingCometa2B = src.purgingCometa2B
        if (purgingCometa2C.isBlank()) purgingCometa2C = src.purgingCometa2C
        if (oilCometa2A.isBlank()) oilCometa2A = src.oilCometa2A
        if (oilCometa2B.isBlank()) oilCometa2B = src.oilCometa2B
        if (oilCometa2C.isBlank()) oilCometa2C = src.oilCometa2C

        // 1ТН-220
        if (tn1UpperA.isBlank()) tn1UpperA = src.tn1UpperA
        if (tn1UpperB.isBlank()) tn1UpperB = src.tn1UpperB
        if (tn1UpperC.isBlank()) tn1UpperC = src.tn1UpperC
        if (tn1LowerA.isBlank()) tn1LowerA = src.tn1LowerA
        if (tn1LowerB.isBlank()) tn1LowerB = src.tn1LowerB
        if (tn1LowerC.isBlank()) tn1LowerC = src.tn1LowerC

        // 2ТН-220
        if (tn2UpperA.isBlank()) tn2UpperA = src.tn2UpperA
        if (tn2UpperB.isBlank()) tn2UpperB = src.tn2UpperB
        if (tn2UpperC.isBlank()) tn2UpperC = src.tn2UpperC
        if (tn2LowerA.isBlank()) tn2LowerA = src.tn2LowerA
        if (tn2LowerB.isBlank()) tn2LowerB = src.tn2LowerB
        if (tn2LowerC.isBlank()) tn2LowerC = src.tn2LowerC
    }
}

// ОРУ-500 — только незаполненные поля
fun SharedInspectionViewModel.mergeORU500(src: InspectionORU500Data) {
    updateORU500Data {
        // Р-500 2С I, II (продувка)
        if (purgingR5002sA1.isBlank()) purgingR5002sA1 = src.purgingR5002sA1
        if (purgingR5002sB1.isBlank()) purgingR5002sB1 = src.purgingR5002sB1
        if (purgingR5002sC1.isBlank()) purgingR5002sC1 = src.purgingR5002sC1
        if (purgingR5002sA2.isBlank()) purgingR5002sA2 = src.purgingR5002sA2
        if (purgingR5002sB2.isBlank()) purgingR5002sB2 = src.purgingR5002sB2
        if (purgingR5002sC2.isBlank()) purgingR5002sC2 = src.purgingR5002sC2

        // ВШТ-31
        if (gasPressureVsht31A.isBlank()) gasPressureVsht31A = src.gasPressureVsht31A
        if (gasPressureVsht31B.isBlank()) gasPressureVsht31B = src.gasPressureVsht31B
        if (gasPressureVsht31C.isBlank()) gasPressureVsht31C = src.gasPressureVsht31C
        if (oilTtVsht31A.isBlank()) oilTtVsht31A = src.oilTtVsht31A
        if (oilTtVsht31B.isBlank()) oilTtVsht31B = src.oilTtVsht31B
        if (oilTtVsht31C.isBlank()) oilTtVsht31C = src.oilTtVsht31C

        // ВЛТ-30
        if (gasPressureVlt30A.isBlank()) gasPressureVlt30A = src.gasPressureVlt30A
        if (gasPressureVlt30B.isBlank()) gasPressureVlt30B = src.gasPressureVlt30B
        if (gasPressureVlt30C.isBlank()) gasPressureVlt30C = src.gasPressureVlt30C
        if (oilTtVlt30A.isBlank()) oilTtVlt30A = src.oilTtVlt30A
        if (oilTtVlt30B.isBlank()) oilTtVlt30B = src.oilTtVlt30B
        if (oilTtVlt30C.isBlank()) oilTtVlt30C = src.oilTtVlt30C

        // ВШЛ-32
        if (purgingVshl32A1.isBlank()) purgingVshl32A1 = src.purgingVshl32A1
        if (purgingVshl32B1.isBlank()) purgingVshl32B1 = src.purgingVshl32B1
        if (purgingVshl32C1.isBlank()) purgingVshl32C1 = src.purgingVshl32C1
        if (purgingVshl32A2.isBlank()) purgingVshl32A2 = src.purgingVshl32A2
        if (purgingVshl32B2.isBlank()) purgingVshl32B2 = src.purgingVshl32B2
        if (purgingVshl32C2.isBlank()) purgingVshl32C2 = src.purgingVshl32C2
        if (oilTtVshl32A.isBlank()) oilTtVshl32A = src.oilTtVshl32A
        if (oilTtVshl32B.isBlank()) oilTtVshl32B = src.oilTtVshl32B
        if (oilTtVshl32C.isBlank()) oilTtVshl32C = src.oilTtVshl32C

        // ВШЛ-21
        if (purgingVshl21A1.isBlank()) purgingVshl21A1 = src.purgingVshl21A1
        if (purgingVshl21B1.isBlank()) purgingVshl21B1 = src.purgingVshl21B1
        if (purgingVshl21C1.isBlank()) purgingVshl21C1 = src.purgingVshl21C1
        if (purgingVshl21A2.isBlank()) purgingVshl21A2 = src.purgingVshl21A2
        if (purgingVshl21B2.isBlank()) purgingVshl21B2 = src.purgingVshl21B2
        if (purgingVshl21C2.isBlank()) purgingVshl21C2 = src.purgingVshl21C2
        if (oilTtVshl21A.isBlank()) oilTtVshl21A = src.oilTtVshl21A
        if (oilTtVshl21B.isBlank()) oilTtVshl21B = src.oilTtVshl21B
        if (oilTtVshl21C.isBlank()) oilTtVshl21C = src.oilTtVshl21C

        // ВШТ-22
        if (purgingVsht22A1.isBlank()) purgingVsht22A1 = src.purgingVsht22A1
        if (purgingVsht22B1.isBlank()) purgingVsht22B1 = src.purgingVsht22B1
        if (purgingVsht22C1.isBlank()) purgingVsht22C1 = src.purgingVsht22C1
        if (purgingVsht22A2.isBlank()) purgingVsht22A2 = src.purgingVsht22A2
        if (purgingVsht22B2.isBlank()) purgingVsht22B2 = src.purgingVsht22B2
        if (purgingVsht22C2.isBlank()) purgingVsht22C2 = src.purgingVsht22C2
        if (oilTtVsht22A.isBlank()) oilTtVsht22A = src.oilTtVsht22A
        if (oilTtVsht22B.isBlank()) oilTtVsht22B = src.oilTtVsht22B
        if (oilTtVsht22C.isBlank()) oilTtVsht22C = src.oilTtVsht22C

        // ВЛТ-20
        if (purgingVlt20A1.isBlank()) purgingVlt20A1 = src.purgingVlt20A1
        if (purgingVlt20B1.isBlank()) purgingVlt20B1 = src.purgingVlt20B1
        if (purgingVlt20C1.isBlank()) purgingVlt20C1 = src.purgingVlt20C1
        if (purgingVlt20A2.isBlank()) purgingVlt20A2 = src.purgingVlt20A2
        if (purgingVlt20B2.isBlank()) purgingVlt20B2 = src.purgingVlt20B2
        if (purgingVlt20C2.isBlank()) purgingVlt20C2 = src.purgingVlt20C2
        if (oilTtVlt20A.isBlank()) oilTtVlt20A = src.oilTtVlt20A
        if (oilTtVlt20B.isBlank()) oilTtVlt20B = src.oilTtVlt20B
        if (oilTtVlt20C.isBlank()) oilTtVlt20C = src.oilTtVlt20C

        // ВШТ-11
        if (purgingVsht11A1.isBlank()) purgingVsht11A1 = src.purgingVsht11A1
        if (purgingVsht11B1.isBlank()) purgingVsht11B1 = src.purgingVsht11B1
        if (purgingVsht11C1.isBlank()) purgingVsht11C1 = src.purgingVsht11C1
        if (purgingVsht11A2.isBlank()) purgingVsht11A2 = src.purgingVsht11A2
        if (purgingVsht11B2.isBlank()) purgingVsht11B2 = src.purgingVsht11B2
        if (purgingVsht11C2.isBlank()) purgingVsht11C2 = src.purgingVsht11C2
        if (oilTtVsht11A.isBlank()) oilTtVsht11A = src.oilTtVsht11A
        if (oilTtVsht11B.isBlank()) oilTtVsht11B = src.oilTtVsht11B
        if (oilTtVsht11C.isBlank()) oilTtVsht11C = src.oilTtVsht11C

        // ВШЛ-12
        if (purgingVshl12A1.isBlank()) purgingVshl12A1 = src.purgingVshl12A1
        if (purgingVshl12B1.isBlank()) purgingVshl12B1 = src.purgingVshl12B1
        if (purgingVshl12C1.isBlank()) purgingVshl12C1 = src.purgingVshl12C1
        if (purgingVshl12A2.isBlank()) purgingVshl12A2 = src.purgingVshl12A2
        if (purgingVshl12B2.isBlank()) purgingVshl12B2 = src.purgingVshl12B2
        if (purgingVshl12C2.isBlank()) purgingVshl12C2 = src.purgingVshl12C2
        if (oilTtVshl12A.isBlank()) oilTtVshl12A = src.oilTtVshl12A
        if (oilTtVshl12B.isBlank()) oilTtVshl12B = src.oilTtVshl12B
        if (oilTtVshl12C.isBlank()) oilTtVshl12C = src.oilTtVshl12C

        // Трачуковская
        if (oilTtTrachukovskayaA.isBlank()) oilTtTrachukovskayaA = src.oilTtTrachukovskayaA
        if (oilTtTrachukovskayaB.isBlank()) oilTtTrachukovskayaB = src.oilTtTrachukovskayaB
        if (oilTtTrachukovskayaC.isBlank()) oilTtTrachukovskayaC = src.oilTtTrachukovskayaC
        if (oil2tnTrachukovskayaA.isBlank()) oil2tnTrachukovskayaA = src.oil2tnTrachukovskayaA
        if (oil2tnTrachukovskayaB.isBlank()) oil2tnTrachukovskayaB = src.oil2tnTrachukovskayaB
        if (oil2tnTrachukovskayaC.isBlank()) oil2tnTrachukovskayaC = src.oil2tnTrachukovskayaC
        if (oil1tnTrachukovskayaA.isBlank()) oil1tnTrachukovskayaA = src.oil1tnTrachukovskayaA
        if (oil1tnTrachukovskayaB.isBlank()) oil1tnTrachukovskayaB = src.oil1tnTrachukovskayaB
        if (oil1tnTrachukovskayaC.isBlank()) oil1tnTrachukovskayaC = src.oil1tnTrachukovskayaC

        // Белозёрная
        if (oil2tnBelozernayaA.isBlank()) oil2tnBelozernayaA = src.oil2tnBelozernayaA
        if (oil2tnBelozernayaB.isBlank()) oil2tnBelozernayaB = src.oil2tnBelozernayaB
        if (oil2tnBelozernayaC.isBlank()) oil2tnBelozernayaC = src.oil2tnBelozernayaC

        // 1ТН-500 каскады
        if (tn1500Cascade1A.isBlank()) tn1500Cascade1A = src.tn1500Cascade1A
        if (tn1500Cascade1B.isBlank()) tn1500Cascade1B = src.tn1500Cascade1B
        if (tn1500Cascade1C.isBlank()) tn1500Cascade1C = src.tn1500Cascade1C
        if (tn1500Cascade2A.isBlank()) tn1500Cascade2A = src.tn1500Cascade2A
        if (tn1500Cascade2B.isBlank()) tn1500Cascade2B = src.tn1500Cascade2B
        if (tn1500Cascade2C.isBlank()) tn1500Cascade2C = src.tn1500Cascade2C
        if (tn1500Cascade3A.isBlank()) tn1500Cascade3A = src.tn1500Cascade3A
        if (tn1500Cascade3B.isBlank()) tn1500Cascade3B = src.tn1500Cascade3B
        if (tn1500Cascade3C.isBlank()) tn1500Cascade3C = src.tn1500Cascade3C
        if (tn1500Cascade4A.isBlank()) tn1500Cascade4A = src.tn1500Cascade4A
        if (tn1500Cascade4B.isBlank()) tn1500Cascade4B = src.tn1500Cascade4B
        if (tn1500Cascade4C.isBlank()) tn1500Cascade4C = src.tn1500Cascade4C

        // 2ТН-500 каскады
        if (tn2500Cascade1A.isBlank()) tn2500Cascade1A = src.tn2500Cascade1A
        if (tn2500Cascade1B.isBlank()) tn2500Cascade1B = src.tn2500Cascade1B
        if (tn2500Cascade1C.isBlank()) tn2500Cascade1C = src.tn2500Cascade1C
        if (tn2500Cascade2A.isBlank()) tn2500Cascade2A = src.tn2500Cascade2A
        if (tn2500Cascade2B.isBlank()) tn2500Cascade2B = src.tn2500Cascade2B
        if (tn2500Cascade2C.isBlank()) tn2500Cascade2C = src.tn2500Cascade2C
        if (tn2500Cascade3A.isBlank()) tn2500Cascade3A = src.tn2500Cascade3A
        if (tn2500Cascade3B.isBlank()) tn2500Cascade3B = src.tn2500Cascade3B
        if (tn2500Cascade3C.isBlank()) tn2500Cascade3C = src.tn2500Cascade3C
        if (tn2500Cascade4A.isBlank()) tn2500Cascade4A = src.tn2500Cascade4A
        if (tn2500Cascade4B.isBlank()) tn2500Cascade4B = src.tn2500Cascade4B
        if (tn2500Cascade4C.isBlank()) tn2500Cascade4C = src.tn2500Cascade4C

        // ТН-500 СГРЭС-1 каскады
        if (tn500Sgres1Cascade1A.isBlank()) tn500Sgres1Cascade1A = src.tn500Sgres1Cascade1A
        if (tn500Sgres1Cascade1B.isBlank()) tn500Sgres1Cascade1B = src.tn500Sgres1Cascade1B
        if (tn500Sgres1Cascade1C.isBlank()) tn500Sgres1Cascade1C = src.tn500Sgres1Cascade1C
        if (tn500Sgres1Cascade2A.isBlank()) tn500Sgres1Cascade2A = src.tn500Sgres1Cascade2A
        if (tn500Sgres1Cascade2B.isBlank()) tn500Sgres1Cascade2B = src.tn500Sgres1Cascade2B
        if (tn500Sgres1Cascade2C.isBlank()) tn500Sgres1Cascade2C = src.tn500Sgres1Cascade2C
        if (tn500Sgres1Cascade3A.isBlank()) tn500Sgres1Cascade3A = src.tn500Sgres1Cascade3A
        if (tn500Sgres1Cascade3B.isBlank()) tn500Sgres1Cascade3B = src.tn500Sgres1Cascade3B
        if (tn500Sgres1Cascade3C.isBlank()) tn500Sgres1Cascade3C = src.tn500Sgres1Cascade3C
        if (tn500Sgres1Cascade4A.isBlank()) tn500Sgres1Cascade4A = src.tn500Sgres1Cascade4A
        if (tn500Sgres1Cascade4B.isBlank()) tn500Sgres1Cascade4B = src.tn500Sgres1Cascade4B
        if (tn500Sgres1Cascade4C.isBlank()) tn500Sgres1Cascade4C = src.tn500Sgres1Cascade4C
    }
}

// ЗДАНИЯ — только незаполненные поля
fun SharedInspectionViewModel.mergeBuildings(src: InspectionBuildingsData) {
    updateBuildingsData {
        // Компрессорная №1
        if (compressor1Valve == "○") compressor1Valve = src.compressor1Valve
        if (compressor1Heating == "○") compressor1Heating = src.compressor1Heating
        if (compressor1Temp.isBlank()) compressor1Temp = src.compressor1Temp

        // Баллонная №1
        if (ballroom1Valve == "○") ballroom1Valve = src.ballroom1Valve
        if (ballroom1Heating == "○") ballroom1Heating = src.ballroom1Heating
        if (ballroom1Temp.isBlank()) ballroom1Temp = src.ballroom1Temp

        // Компрессорная №2
        if (compressor2Valve == "○") compressor2Valve = src.compressor2Valve
        if (compressor2Heating == "○") compressor2Heating = src.compressor2Heating
        if (compressor2Temp.isBlank()) compressor2Temp = src.compressor2Temp

        // Баллонная №2
        if (ballroom2Valve == "○") ballroom2Valve = src.ballroom2Valve
        if (ballroom2Heating == "○") ballroom2Heating = src.ballroom2Heating
        if (ballroom2Temp.isBlank()) ballroom2Temp = src.ballroom2Temp

        // КПЗ ОПУ
        if (kpzOpuValve == "○") kpzOpuValve = src.kpzOpuValve
        if (kpzOpuHeating == "○") kpzOpuHeating = src.kpzOpuHeating
        if (kpzOpuTemp.isBlank()) kpzOpuTemp = src.kpzOpuTemp

        // КПЗ-2
        if (kpz2Valve == "○") kpz2Valve = src.kpz2Valve
        if (kpz2Heating == "○") kpz2Heating = src.kpz2Heating
        if (kpz2Temp.isBlank()) kpz2Temp = src.kpz2Temp

        // Насосная пожаротушения
        if (firePumpValve == "○") firePumpValve = src.firePumpValve
        if (firePumpHeating == "○") firePumpHeating = src.firePumpHeating
        if (firePumpTemp.isBlank()) firePumpTemp = src.firePumpTemp
        if (firePumpWaterLevel.isBlank()) firePumpWaterLevel = src.firePumpWaterLevel

        // Мастерская
        if (workshopHeating == "○") workshopHeating = src.workshopHeating
        if (workshopTemp.isBlank()) workshopTemp = src.workshopTemp

        // Артскважина
        if (artWellHeating == "○") artWellHeating = src.artWellHeating
        if (artesianWellHeating == "○") artesianWellHeating = src.artesianWellHeating

        // Помещение АБ
        if (roomAbHeating == "○") roomAbHeating = src.roomAbHeating
        if (roomAbTemp.isBlank()) roomAbTemp = src.roomAbTemp

        // Подвал
        if (basementHeating == "○") basementHeating = src.basementHeating
        if (basementTemp.isBlank()) basementTemp = src.basementTemp

        // ⚠️ Комментарии и фото НЕ копируем — они остаются от текущего осмотра
        // buildingsPhotoFiles не трогаем
        // comment* поля не трогаем
    }
}

// АТГ — полный перенос (только пустые поля)
fun SharedInspectionViewModel.mergeATG(src: InspectionATGData) {
    updateATGData {
        // 2 АТГ ф.С
        if (atg2_c_oil_tank.isBlank()) atg2_c_oil_tank = src.atg2_c_oil_tank
        if (atg2_c_oil_rpn.isBlank()) atg2_c_oil_rpn = src.atg2_c_oil_rpn
        if (atg2_c_pressure_500.isBlank()) atg2_c_pressure_500 = src.atg2_c_pressure_500
        if (atg2_c_pressure_220.isBlank()) atg2_c_pressure_220 = src.atg2_c_pressure_220
        if (atg2_c_temp_ts1.isBlank()) atg2_c_temp_ts1 = src.atg2_c_temp_ts1
        if (atg2_c_temp_ts2.isBlank()) atg2_c_temp_ts2 = src.atg2_c_temp_ts2
        if (atg2_c_pump_group1.isBlank()) atg2_c_pump_group1 = src.atg2_c_pump_group1
        if (atg2_c_pump_group2.isBlank()) atg2_c_pump_group2 = src.atg2_c_pump_group2
        if (atg2_c_pump_group3.isBlank()) atg2_c_pump_group3 = src.atg2_c_pump_group3
        if (atg2_c_pump_group4.isBlank()) atg2_c_pump_group4 = src.atg2_c_pump_group4

        // 2 АТГ ф.В
        if (atg2_b_oil_tank.isBlank()) atg2_b_oil_tank = src.atg2_b_oil_tank
        if (atg2_b_oil_rpn.isBlank()) atg2_b_oil_rpn = src.atg2_b_oil_rpn
        if (atg2_b_pressure_500.isBlank()) atg2_b_pressure_500 = src.atg2_b_pressure_500
        if (atg2_b_pressure_220.isBlank()) atg2_b_pressure_220 = src.atg2_b_pressure_220
        if (atg2_b_temp_ts1.isBlank()) atg2_b_temp_ts1 = src.atg2_b_temp_ts1
        if (atg2_b_temp_ts2.isBlank()) atg2_b_temp_ts2 = src.atg2_b_temp_ts2
        if (atg2_b_pump_group1.isBlank()) atg2_b_pump_group1 = src.atg2_b_pump_group1
        if (atg2_b_pump_group2.isBlank()) atg2_b_pump_group2 = src.atg2_b_pump_group2
        if (atg2_b_pump_group3.isBlank()) atg2_b_pump_group3 = src.atg2_b_pump_group3
        if (atg2_b_pump_group4.isBlank()) atg2_b_pump_group4 = src.atg2_b_pump_group4

        // 2 АТГ ф.А
        if (atg2_a_oil_tank.isBlank()) atg2_a_oil_tank = src.atg2_a_oil_tank
        if (atg2_a_oil_rpn.isBlank()) atg2_a_oil_rpn = src.atg2_a_oil_rpn
        if (atg2_a_pressure_500.isBlank()) atg2_a_pressure_500 = src.atg2_a_pressure_500
        if (atg2_a_pressure_220.isBlank()) atg2_a_pressure_220 = src.atg2_a_pressure_220
        if (atg2_a_temp_ts1.isBlank()) atg2_a_temp_ts1 = src.atg2_a_temp_ts1
        if (atg2_a_temp_ts2.isBlank()) atg2_a_temp_ts2 = src.atg2_a_temp_ts2
        if (atg2_a_pump_group1.isBlank()) atg2_a_pump_group1 = src.atg2_a_pump_group1
        if (atg2_a_pump_group2.isBlank()) atg2_a_pump_group2 = src.atg2_a_pump_group2
        if (atg2_a_pump_group3.isBlank()) atg2_a_pump_group3 = src.atg2_a_pump_group3
        if (atg2_a_pump_group4.isBlank()) atg2_a_pump_group4 = src.atg2_a_pump_group4

        // АТГ резервная
        if (atg_reserve_oil_tank.isBlank()) atg_reserve_oil_tank = src.atg_reserve_oil_tank
        if (atg_reserve_oil_rpn.isBlank()) atg_reserve_oil_rpn = src.atg_reserve_oil_rpn
        if (atg_reserve_pressure_500.isBlank()) atg_reserve_pressure_500 = src.atg_reserve_pressure_500
        if (atg_reserve_pressure_220.isBlank()) atg_reserve_pressure_220 = src.atg_reserve_pressure_220
        if (atg_reserve_temp_ts1.isBlank()) atg_reserve_temp_ts1 = src.atg_reserve_temp_ts1
        if (atg_reserve_temp_ts2.isBlank()) atg_reserve_temp_ts2 = src.atg_reserve_temp_ts2
        if (atg_reserve_pump_group1.isBlank()) atg_reserve_pump_group1 = src.atg_reserve_pump_group1
        if (atg_reserve_pump_group2.isBlank()) atg_reserve_pump_group2 = src.atg_reserve_pump_group2
        if (atg_reserve_pump_group3.isBlank()) atg_reserve_pump_group3 = src.atg_reserve_pump_group3
        if (atg_reserve_pump_group4.isBlank()) atg_reserve_pump_group4 = src.atg_reserve_pump_group4

        // 3 АТГ ф.С
        if (atg3_c_oil_tank.isBlank()) atg3_c_oil_tank = src.atg3_c_oil_tank
        if (atg3_c_oil_rpn.isBlank()) atg3_c_oil_rpn = src.atg3_c_oil_rpn
        if (atg3_c_pressure_500.isBlank()) atg3_c_pressure_500 = src.atg3_c_pressure_500
        if (atg3_c_pressure_220.isBlank()) atg3_c_pressure_220 = src.atg3_c_pressure_220
        if (atg3_c_temp_ts1.isBlank()) atg3_c_temp_ts1 = src.atg3_c_temp_ts1
        if (atg3_c_temp_ts2.isBlank()) atg3_c_temp_ts2 = src.atg3_c_temp_ts2
        if (atg3_c_pump_group1.isBlank()) atg3_c_pump_group1 = src.atg3_c_pump_group1
        if (atg3_c_pump_group2.isBlank()) atg3_c_pump_group2 = src.atg3_c_pump_group2
        if (atg3_c_pump_group3.isBlank()) atg3_c_pump_group3 = src.atg3_c_pump_group3
        if (atg3_c_pump_group4.isBlank()) atg3_c_pump_group4 = src.atg3_c_pump_group4

        // 3 АТГ ф.В
        if (atg3_b_oil_tank.isBlank()) atg3_b_oil_tank = src.atg3_b_oil_tank
        if (atg3_b_oil_rpn.isBlank()) atg3_b_oil_rpn = src.atg3_b_oil_rpn
        if (atg3_b_pressure_500.isBlank()) atg3_b_pressure_500 = src.atg3_b_pressure_500
        if (atg3_b_pressure_220.isBlank()) atg3_b_pressure_220 = src.atg3_b_pressure_220
        if (atg3_b_temp_ts1.isBlank()) atg3_b_temp_ts1 = src.atg3_b_temp_ts1
        if (atg3_b_temp_ts2.isBlank()) atg3_b_temp_ts2 = src.atg3_b_temp_ts2
        if (atg3_b_pump_group1.isBlank()) atg3_b_pump_group1 = src.atg3_b_pump_group1
        if (atg3_b_pump_group2.isBlank()) atg3_b_pump_group2 = src.atg3_b_pump_group2
        if (atg3_b_pump_group3.isBlank()) atg3_b_pump_group3 = src.atg3_b_pump_group3
        if (atg3_b_pump_group4.isBlank()) atg3_b_pump_group4 = src.atg3_b_pump_group4

        // 3 АТГ ф.А
        if (atg3_a_oil_tank.isBlank()) atg3_a_oil_tank = src.atg3_a_oil_tank
        if (atg3_a_oil_rpn.isBlank()) atg3_a_oil_rpn = src.atg3_a_oil_rpn
        if (atg3_a_pressure_500.isBlank()) atg3_a_pressure_500 = src.atg3_a_pressure_500
        if (atg3_a_pressure_220.isBlank()) atg3_a_pressure_220 = src.atg3_a_pressure_220
        if (atg3_a_temp_ts1.isBlank()) atg3_a_temp_ts1 = src.atg3_a_temp_ts1
        if (atg3_a_temp_ts2.isBlank()) atg3_a_temp_ts2 = src.atg3_a_temp_ts2
        if (atg3_a_pump_group1.isBlank()) atg3_a_pump_group1 = src.atg3_a_pump_group1
        if (atg3_a_pump_group2.isBlank()) atg3_a_pump_group2 = src.atg3_a_pump_group2
        if (atg3_a_pump_group3.isBlank()) atg3_a_pump_group3 = src.atg3_a_pump_group3
        if (atg3_a_pump_group4.isBlank()) atg3_a_pump_group4 = src.atg3_a_pump_group4

        // Реакторы
        if (reactor_c_oil_tank.isBlank()) reactor_c_oil_tank = src.reactor_c_oil_tank
        if (reactor_c_pressure_500.isBlank()) reactor_c_pressure_500 = src.reactor_c_pressure_500
        if (reactor_c_temp_ts.isBlank()) reactor_c_temp_ts = src.reactor_c_temp_ts
        if (reactor_c_pump_group1.isBlank()) reactor_c_pump_group1 = src.reactor_c_pump_group1
        if (reactor_c_pump_group2.isBlank()) reactor_c_pump_group2 = src.reactor_c_pump_group2
        if (reactor_c_pump_group3.isBlank()) reactor_c_pump_group3 = src.reactor_c_pump_group3
        if (reactor_c_tt_neutral.isBlank()) reactor_c_tt_neutral = src.reactor_c_tt_neutral

        if (reactor_b_oil_tank.isBlank()) reactor_b_oil_tank = src.reactor_b_oil_tank
        if (reactor_b_pressure_500.isBlank()) reactor_b_pressure_500 = src.reactor_b_pressure_500
        if (reactor_b_temp_ts.isBlank()) reactor_b_temp_ts = src.reactor_b_temp_ts
        if (reactor_b_pump_group1.isBlank()) reactor_b_pump_group1 = src.reactor_b_pump_group1
        if (reactor_b_pump_group2.isBlank()) reactor_b_pump_group2 = src.reactor_b_pump_group2
        if (reactor_b_pump_group3.isBlank()) reactor_b_pump_group3 = src.reactor_b_pump_group3
        if (reactor_b_tt_neutral.isBlank()) reactor_b_tt_neutral = src.reactor_b_tt_neutral

        if (reactor_a_oil_tank.isBlank()) reactor_a_oil_tank = src.reactor_a_oil_tank
        if (reactor_a_pressure_500.isBlank()) reactor_a_pressure_500 = src.reactor_a_pressure_500
        if (reactor_a_temp_ts.isBlank()) reactor_a_temp_ts = src.reactor_a_temp_ts
        if (reactor_a_pump_group1.isBlank()) reactor_a_pump_group1 = src.reactor_a_pump_group1
        if (reactor_a_pump_group2.isBlank()) reactor_a_pump_group2 = src.reactor_a_pump_group2
        if (reactor_a_pump_group3.isBlank()) reactor_a_pump_group3 = src.reactor_a_pump_group3
        if (reactor_a_tt_neutral.isBlank()) reactor_a_tt_neutral = src.reactor_a_tt_neutral

        // ТН-35
        if (tn352atg.isBlank()) tn352atg = src.tn352atg
        if (tn353atg.isBlank()) tn353atg = src.tn353atg
    }
}

// АТГ — только группы насосов (давления маслонасосов)
fun SharedInspectionViewModel.mergeATGPressuresOnly(src: InspectionATGData) {
    updateATGData {
        // 2 АТГ ф.С
        if (atg2_c_pump_group1.isBlank()) atg2_c_pump_group1 = src.atg2_c_pump_group1
        if (atg2_c_pump_group2.isBlank()) atg2_c_pump_group2 = src.atg2_c_pump_group2
        if (atg2_c_pump_group3.isBlank()) atg2_c_pump_group3 = src.atg2_c_pump_group3
        if (atg2_c_pump_group4.isBlank()) atg2_c_pump_group4 = src.atg2_c_pump_group4

        // 2 АТГ ф.В
        if (atg2_b_pump_group1.isBlank()) atg2_b_pump_group1 = src.atg2_b_pump_group1
        if (atg2_b_pump_group2.isBlank()) atg2_b_pump_group2 = src.atg2_b_pump_group2
        if (atg2_b_pump_group3.isBlank()) atg2_b_pump_group3 = src.atg2_b_pump_group3
        if (atg2_b_pump_group4.isBlank()) atg2_b_pump_group4 = src.atg2_b_pump_group4

        // 2 АТГ ф.А
        if (atg2_a_pump_group1.isBlank()) atg2_a_pump_group1 = src.atg2_a_pump_group1
        if (atg2_a_pump_group2.isBlank()) atg2_a_pump_group2 = src.atg2_a_pump_group2
        if (atg2_a_pump_group3.isBlank()) atg2_a_pump_group3 = src.atg2_a_pump_group3
        if (atg2_a_pump_group4.isBlank()) atg2_a_pump_group4 = src.atg2_a_pump_group4

        // АТГ резервная
        if (atg_reserve_pump_group1.isBlank()) atg_reserve_pump_group1 = src.atg_reserve_pump_group1
        if (atg_reserve_pump_group2.isBlank()) atg_reserve_pump_group2 = src.atg_reserve_pump_group2
        if (atg_reserve_pump_group3.isBlank()) atg_reserve_pump_group3 = src.atg_reserve_pump_group3
        if (atg_reserve_pump_group4.isBlank()) atg_reserve_pump_group4 = src.atg_reserve_pump_group4

        // 3 АТГ ф.С
        if (atg3_c_pump_group1.isBlank()) atg3_c_pump_group1 = src.atg3_c_pump_group1
        if (atg3_c_pump_group2.isBlank()) atg3_c_pump_group2 = src.atg3_c_pump_group2
        if (atg3_c_pump_group3.isBlank()) atg3_c_pump_group3 = src.atg3_c_pump_group3
        if (atg3_c_pump_group4.isBlank()) atg3_c_pump_group4 = src.atg3_c_pump_group4

        // 3 АТГ ф.В
        if (atg3_b_pump_group1.isBlank()) atg3_b_pump_group1 = src.atg3_b_pump_group1
        if (atg3_b_pump_group2.isBlank()) atg3_b_pump_group2 = src.atg3_b_pump_group2
        if (atg3_b_pump_group3.isBlank()) atg3_b_pump_group3 = src.atg3_b_pump_group3
        if (atg3_b_pump_group4.isBlank()) atg3_b_pump_group4 = src.atg3_b_pump_group4

        // 3 АТГ ф.А
        if (atg3_a_pump_group1.isBlank()) atg3_a_pump_group1 = src.atg3_a_pump_group1
        if (atg3_a_pump_group2.isBlank()) atg3_a_pump_group2 = src.atg3_a_pump_group2
        if (atg3_a_pump_group3.isBlank()) atg3_a_pump_group3 = src.atg3_a_pump_group3
        if (atg3_a_pump_group4.isBlank()) atg3_a_pump_group4 = src.atg3_a_pump_group4

        // Реакторы
        if (reactor_c_pump_group1.isBlank()) reactor_c_pump_group1 = src.reactor_c_pump_group1
        if (reactor_c_pump_group2.isBlank()) reactor_c_pump_group2 = src.reactor_c_pump_group2
        if (reactor_c_pump_group3.isBlank()) reactor_c_pump_group3 = src.reactor_c_pump_group3

        if (reactor_b_pump_group1.isBlank()) reactor_b_pump_group1 = src.reactor_b_pump_group1
        if (reactor_b_pump_group2.isBlank()) reactor_b_pump_group2 = src.reactor_b_pump_group2
        if (reactor_b_pump_group3.isBlank()) reactor_b_pump_group3 = src.reactor_b_pump_group3

        if (reactor_a_pump_group1.isBlank()) reactor_a_pump_group1 = src.reactor_a_pump_group1
        if (reactor_a_pump_group2.isBlank()) reactor_a_pump_group2 = src.reactor_a_pump_group2
        if (reactor_a_pump_group3.isBlank()) reactor_a_pump_group3 = src.reactor_a_pump_group3
    }
}