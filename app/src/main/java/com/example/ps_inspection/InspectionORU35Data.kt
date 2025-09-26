// InspectionORU35Data.kt
package com.example.ps_inspection

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

    // ТН-35
    var tn352atg: String = "",
    var tn353atg: String = ""
)