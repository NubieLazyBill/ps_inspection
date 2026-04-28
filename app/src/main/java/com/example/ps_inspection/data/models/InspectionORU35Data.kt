package com.example.ps_inspection.data.models

data class InspectionORU35Data(
    // ТСН
    var tsn2: String = "",
    var tsn3: String = "",
    var tsn4: String = "",

    // ТТ-35 2ТСН
    var tt352tsnA: String = "",
    var tt352tsnB: String = "",
    var tt352tsnC: String = "",

    // ТТ-35 3ТСН
    var tt353tsnA: String = "",
    var tt353tsnB: String = "",
    var tt353tsnC: String = "",

    // В-35 2ТСН
    var v352tsnA: String = "",
    var v352tsnB: String = "",
    var v352tsnC: String = "",

    // В-35 3ТСН
    var v353tsnA: String = "",
    var v353tsnB: String = "",
    var v353tsnC: String = "",

    // Фото ОРУ-35
    var oru35PhotoFiles: List<String> = emptyList(),

    // 💬 Комментарии по блокам
    var commentTsn: String = "",           // комментарий для блока ТСН
    var commentTt352: String = "",         // комментарий для ТТ-35 2ТСН
    var commentTt353: String = "",         // комментарий для ТТ-35 3ТСН
    var commentV352: String = "",          // комментарий для В-35 2ТСН
    var commentV353: String = ""           // комментарий для В-35 3ТСН
)