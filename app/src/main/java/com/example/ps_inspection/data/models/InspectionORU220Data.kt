package com.example.ps_inspection.data.models

data class InspectionORU220Data(
    // Мирная - Выключатель
    var purgingMirnayaA: String = "",
    var purgingMirnayaB: String = "",
    var purgingMirnayaC: String = "",

    // Мирная - ТТ
    var oilMirnayaA: String = "",
    var oilMirnayaB: String = "",
    var oilMirnayaC: String = "",

    // Топаз - Выключатель
    var purgingTopazA: String = "",
    var purgingTopazB: String = "",
    var purgingTopazC: String = "",

    // Топаз - ТТ
    var oilTopazA: String = "",
    var oilTopazB: String = "",
    var oilTopazC: String = "",

    // ОВ - Выключатель
    var purgingOvA: String = "",
    var purgingOvB: String = "",
    var purgingOvC: String = "",

    // ОВ - ТТ
    var oilOvA: String = "",
    var oilOvB: String = "",
    var oilOvC: String = "",

    // ТН-220 ОСШ ф.В
    var tnOsshFvUpper: String = "",
    var tnOsshFvLower: String = "",

    // В-220 2АТГ - Выключатель
    var purgingV2atgA: String = "",
    var purgingV2atgB: String = "",
    var purgingV2atgC: String = "",

    // ТТ-220 2АТГ
    var oilTt2atgA: String = "",
    var oilTt2atgB: String = "",
    var oilTt2atgC: String = "",

    // ШСВ-220 - Выключатель
    var purgingShSV220A: String = "",
    var purgingShSV220B: String = "",
    var purgingShSV220C: String = "",

    // ТТ-220 ШСВ
    var oilTtShSV220A: String = "",
    var oilTtShSV220B: String = "",
    var oilTtShSV220C: String = "",

    // В-220 3АТГ - Выключатель
    var purgingV3atgA: String = "",
    var purgingV3atgB: String = "",
    var purgingV3atgC: String = "",

    // ТТ-220 3АТГ
    var oilTt3atgA: String = "",
    var oilTt3atgB: String = "",
    var oilTt3atgC: String = "",

    // Орбита - Выключатель
    var purgingOrbitaA: String = "",
    var purgingOrbitaB: String = "",
    var purgingOrbitaC: String = "",

    // Орбита - ТТ
    var oilOrbitaA: String = "",
    var oilOrbitaB: String = "",
    var oilOrbitaC: String = "",

    // Факел - Выключатель
    var purgingFakelA: String = "",
    var purgingFakelB: String = "",
    var purgingFakelC: String = "",

    // Факел - ТТ
    var oilFakelA: String = "",
    var oilFakelB: String = "",
    var oilFakelC: String = "",

    // Комета-1 - Выключатель
    var purgingCometa1A: String = "",
    var purgingCometa1B: String = "",
    var purgingCometa1C: String = "",

    // Комета-1 - ТТ
    var oilCometa1A: String = "",
    var oilCometa1B: String = "",
    var oilCometa1C: String = "",

    // Комета-2 - Выключатель
    var purgingCometa2A: String = "",
    var purgingCometa2B: String = "",
    var purgingCometa2C: String = "",

    // Комета-2 - ТТ
    var oilCometa2A: String = "",
    var oilCometa2B: String = "",
    var oilCometa2C: String = "",

    // 1ТН-220
    var tn1UpperA: String = "",
    var tn1UpperB: String = "",
    var tn1UpperC: String = "",
    var tn1LowerA: String = "",
    var tn1LowerB: String = "",
    var tn1LowerC: String = "",

    // 2ТН-220
    var tn2UpperA: String = "",
    var tn2UpperB: String = "",
    var tn2UpperC: String = "",
    var tn2LowerA: String = "",
    var tn2LowerB: String = "",
    var tn2LowerC: String = "",

    // ========== ФОТО ==========
    var oru220PhotoFiles: List<String> = emptyList(),

    // ========== КОММЕНТАРИИ (для всех 23 единиц) ==========
    var commentMirnaya: String = "",           // 1
    var commentMirnayaTT: String = "",         // 2
    var commentTopaz: String = "",             // 3
    var commentTopazTT: String = "",           // 4
    var commentOv: String = "",                // 5
    var commentOvTT: String = "",              // 6
    var commentOssh: String = "",              // 7
    var commentV2atg: String = "",             // 8
    var commentV2atgTT: String = "",           // 9
    var commentShsv: String = "",              // 10
    var commentShsvTT: String = "",            // 11
    var commentV3atg: String = "",             // 12
    var commentV3atgTT: String = "",           // 13
    var commentOrbita: String = "",            // 14
    var commentOrbitaTT: String = "",          // 15
    var commentFakel: String = "",             // 16
    var commentFakelTT: String = "",           // 17
    var commentCometa1: String = "",           // 18
    var commentCometa1TT: String = "",         // 19
    var commentCometa2: String = "",           // 20
    var commentCometa2TT: String = "",         // 21
    var commentTn1: String = "",               // 22
    var commentTn2: String = ""                // 23
)