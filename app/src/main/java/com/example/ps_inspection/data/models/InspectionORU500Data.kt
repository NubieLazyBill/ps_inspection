package com.example.ps_inspection.data.models

data class InspectionORU500Data(
    // Ячейка 1: В-500 Р-500 2С
    var purgingR5002sA1: String = "",
    var purgingR5002sB1: String = "",
    var purgingR5002sC1: String = "",
    var purgingR5002sA2: String = "",
    var purgingR5002sB2: String = "",
    var purgingR5002sC2: String = "",

    // Ячейка 2: В-500 ВШТ-31
    var gasPressureVsht31A: String = "",
    var gasPressureVsht31B: String = "",
    var gasPressureVsht31C: String = "",
    var oilTtVsht31A: String = "",
    var oilTtVsht31B: String = "",
    var oilTtVsht31C: String = "",

    // Ячейка 3: В-500 ВЛТ-30, Трачуки
    var gasPressureVlt30A: String = "",
    var gasPressureVlt30B: String = "",
    var gasPressureVlt30C: String = "",
    var oilTtVlt30A: String = "",
    var oilTtVlt30B: String = "",
    var oilTtVlt30C: String = "",
    var oilTtTrachukovskayaA: String = "",
    var oilTtTrachukovskayaB: String = "",
    var oilTtTrachukovskayaC: String = "",
    var oil2tnTrachukovskayaA: String = "",
    var oil2tnTrachukovskayaB: String = "",
    var oil2tnTrachukovskayaC: String = "",
    var oil1tnTrachukovskayaA: String = "",
    var oil1tnTrachukovskayaB: String = "",
    var oil1tnTrachukovskayaC: String = "",

    // Ячейка 4: В-500 ВШЛ-32
    var purgingVshl32A1: String = "",
    var purgingVshl32B1: String = "",
    var purgingVshl32C1: String = "",
    var purgingVshl32A2: String = "",
    var purgingVshl32B2: String = "",
    var purgingVshl32C2: String = "",
    var oilTtVshl32A: String = "",
    var oilTtVshl32B: String = "",
    var oilTtVshl32C: String = "",

    // Ячейка 5: В-500 ВШЛ-21
    var purgingVshl21A1: String = "",
    var purgingVshl21B1: String = "",
    var purgingVshl21C1: String = "",
    var purgingVshl21A2: String = "",
    var purgingVshl21B2: String = "",
    var purgingVshl21C2: String = "",
    var oilTtVshl21A: String = "",
    var oilTtVshl21B: String = "",
    var oilTtVshl21C: String = "",

    // Ячейка 6: В-500 ВШТ-22
    var purgingVsht22A1: String = "",
    var purgingVsht22B1: String = "",
    var purgingVsht22C1: String = "",
    var purgingVsht22A2: String = "",
    var purgingVsht22B2: String = "",
    var purgingVsht22C2: String = "",
    var oilTtVsht22A: String = "",
    var oilTtVsht22B: String = "",
    var oilTtVsht22C: String = "",

    //Ячейка В-500 ВЛТ-20
    var purgingVlt20A1: String = "",
    var purgingVlt20B1: String = "",
    var purgingVlt20C1: String = "",
    var purgingVlt20A2: String = "",
    var purgingVlt20B2: String = "",
    var purgingVlt20C2: String = "",
    var oilTtVlt20A: String = "",
    var oilTtVlt20B: String = "",
    var oilTtVlt20C: String = "",

    // Ячейка 7: В-500 ВШТ-11
    var purgingVsht11A1: String = "",
    var purgingVsht11B1: String = "",
    var purgingVsht11C1: String = "",
    var purgingVsht11A2: String = "",
    var purgingVsht11B2: String = "",
    var purgingVsht11C2: String = "",
    var oilTtVsht11A: String = "",
    var oilTtVsht11B: String = "",
    var oilTtVsht11C: String = "",

    // Ячейка 8: В-500 ВШЛ-12
    var purgingVshl12A1: String = "",
    var purgingVshl12B1: String = "",
    var purgingVshl12C1: String = "",
    var purgingVshl12A2: String = "",
    var purgingVshl12B2: String = "",
    var purgingVshl12C2: String = "",
    var oilTtVshl12A: String = "",
    var oilTtVshl12B: String = "",
    var oilTtVshl12C: String = "",

    //ТН-500 Белозёрная
    var oil2tnBelozernayaA: String = "",
    var oil2tnBelozernayaB: String = "",
    var oil2tnBelozernayaC: String = "",

    // 1ТН-500 Каскады
    var tn1500Cascade1A: String = "",
    var tn1500Cascade1B: String = "",
    var tn1500Cascade1C: String = "",
    var tn1500Cascade2A: String = "",
    var tn1500Cascade2B: String = "",
    var tn1500Cascade2C: String = "",
    var tn1500Cascade3A: String = "",
    var tn1500Cascade3B: String = "",
    var tn1500Cascade3C: String = "",
    var tn1500Cascade4A: String = "",
    var tn1500Cascade4B: String = "",
    var tn1500Cascade4C: String = "",

    // 2ТН-500 Каскады
    var tn2500Cascade1A: String = "",
    var tn2500Cascade1B: String = "",
    var tn2500Cascade1C: String = "",
    var tn2500Cascade2A: String = "",
    var tn2500Cascade2B: String = "",
    var tn2500Cascade2C: String = "",
    var tn2500Cascade3A: String = "",
    var tn2500Cascade3B: String = "",
    var tn2500Cascade3C: String = "",
    var tn2500Cascade4A: String = "",
    var tn2500Cascade4B: String = "",
    var tn2500Cascade4C: String = "",

    // ТН-500 СГРЭС-1 Каскады
    var tn500Sgres1Cascade1A: String = "",
    var tn500Sgres1Cascade1B: String = "",
    var tn500Sgres1Cascade1C: String = "",
    var tn500Sgres1Cascade2A: String = "",
    var tn500Sgres1Cascade2B: String = "",
    var tn500Sgres1Cascade2C: String = "",
    var tn500Sgres1Cascade3A: String = "",
    var tn500Sgres1Cascade3B: String = "",
    var tn500Sgres1Cascade3C: String = "",
    var tn500Sgres1Cascade4A: String = "",
    var tn500Sgres1Cascade4B: String = "",
    var tn500Sgres1Cascade4C: String = "",

    // ========== ФОТО ==========
    // Списки фото для каждого оборудования
    var oru500PhotoFiles: List<String> = emptyList(),  // общий список (для совместимости)

    // Фото для В-500 (выключатели)
    var photoR5002s: List<String> = emptyList(),
    var photoVsht31: List<String> = emptyList(),
    var photoVlt30: List<String> = emptyList(),
    var photoVshl32: List<String> = emptyList(),
    var photoVshl21: List<String> = emptyList(),
    var photoVsht22: List<String> = emptyList(),
    var photoVlt20: List<String> = emptyList(),
    var photoVsht11: List<String> = emptyList(),
    var photoVshl12: List<String> = emptyList(),

    // Фото для ТТ-500
    var photoTtVsht31: List<String> = emptyList(),
    var photoTtVlt30: List<String> = emptyList(),
    var photoTtVshl32: List<String> = emptyList(),
    var photoTtVshl21: List<String> = emptyList(),
    var photoTtVsht22: List<String> = emptyList(),
    var photoTtVlt20: List<String> = emptyList(),
    var photoTtVsht11: List<String> = emptyList(),
    var photoTtVshl12: List<String> = emptyList(),

    // Фото для ТН-500 (трансформаторы напряжения)
    var photoTn1500: List<String> = emptyList(),
    var photoTn2500: List<String> = emptyList(),
    var photoTn500Sgres1: List<String> = emptyList(),

    // Фото для Трачуковской
    var photoTrachukovskayaTt: List<String> = emptyList(),
    var photoTrachukovskaya2tn: List<String> = emptyList(),
    var photoTrachukovskaya1tn: List<String> = emptyList(),

    // Фото для Белозёрной
    var photoBelozernaya2tn: List<String> = emptyList(),

    // ========== КОММЕНТАРИИ (с разделителем |||) ==========
    // В-500 (выключатели)
    var commentR5002s: String = "",
    var commentVsht31: String = "",
    var commentVlt30: String = "",
    var commentVshl32: String = "",
    var commentVshl21: String = "",
    var commentVsht22: String = "",
    var commentVlt20: String = "",
    var commentVsht11: String = "",
    var commentVshl12: String = "",

    // ТТ-500
    var commentTtVsht31: String = "",
    var commentTtVlt30: String = "",
    var commentTtVshl32: String = "",
    var commentTtVshl21: String = "",
    var commentTtVsht22: String = "",
    var commentTtVlt20: String = "",
    var commentTtVsht11: String = "",
    var commentTtVshl12: String = "",

    // ТН-500 (трансформаторы напряжения)
    var commentTn1500: String = "",
    var commentTn2500: String = "",
    var commentTn500Sgres1: String = "",

    // Трачуковская
    var commentTrachukovskayaTt: String = "",
    var commentTrachukovskaya2tn: String = "",
    var commentTrachukovskaya1tn: String = "",

    // Белозёрная
    var commentBelozernaya2tn: String = ""
)