package com.example.ps_inspection.data.utils

data class VoiceParsedResult(
    val fieldName: String,
    val value: String
)

object ORU35MassVoiceParser {

    // Маппинг ключевых слов на поля (для точных совпадений)
    private val fieldMapping = listOf(
        // ТТ-35 2ТСН
        mapOf("patterns" to listOf("ТТ-35 2ТСН А", "тт 35 2 тсн а", "2 тсн тт а", "тт 2 тсн а", "тт 35 2 тсн а", "тт 35 2а", "тт 2а"), "field" to "tt352tsnA"),
        mapOf("patterns" to listOf("ТТ-35 2ТСН В", "тт 35 2 тсн в", "2 тсн тт в", "тт 2 тсн в", "тт 35 2 тсн в", "тт 35 2в", "тт 2в"), "field" to "tt352tsnB"),
        mapOf("patterns" to listOf("ТТ-35 2ТСН С", "тт 35 2 тсн с", "2 тсн тт с", "тт 2 тсн с", "тт 35 2 тсн с", "тт 35 2с", "тт 2с"), "field" to "tt352tsnC"),

        // ТТ-35 3ТСН
        mapOf("patterns" to listOf("ТТ-35 3ТСН А", "тт 35 3 тсн а", "3 тсн тт а", "тт 3 тсн а", "тт 35 3 тсн а", "тт 35 3а", "тт 3а"), "field" to "tt353tsnA"),
        mapOf("patterns" to listOf("ТТ-35 3ТСН В", "тт 35 3 тсн в", "3 тсн тт в", "тт 3 тсн в", "тт 35 3 тсн в", "тт 35 3в", "тт 3в"), "field" to "tt353tsnB"),
        mapOf("patterns" to listOf("ТТ-35 3ТСН С", "тт 35 3 тсн с", "3 тсн тт с", "тт 3 тсн с", "тт 35 3 тсн с", "тт 35 3с", "тт 3с"), "field" to "tt353tsnC"),

        // В-35 2ТСН
        mapOf("patterns" to listOf("В-35 2ТСН А", "в 35 2 тсн а", "2 тсн в а", "в 2 тсн а", "в 35 2 тсн а", "в 35 2а", "в 2а"), "field" to "v352tsnA"),
        mapOf("patterns" to listOf("В-35 2ТСН В", "в 35 2 тсн в", "2 тсн в в", "в 2 тсн в", "в 35 2 тсн в", "в 35 2в", "в 2в"), "field" to "v352tsnB"),
        mapOf("patterns" to listOf("В-35 2ТСН С", "в 35 2 тсн с", "2 тсн в с", "в 2 тсн с", "в 35 2 тсн с", "в 35 2с", "в 2с"), "field" to "v352tsnC"),

        // В-35 3ТСН
        mapOf("patterns" to listOf("В-35 3ТСН А", "в 35 3 тсн а", "3 тсн в а", "в 3 тсн а", "в 35 3 тсн а", "в 35 3а", "в 3а"), "field" to "v353tsnA"),
        mapOf("patterns" to listOf("В-35 3ТСН В", "в 35 3 тсн в", "3 тсн в в", "в 3 тсн в", "в 35 3 тсн в", "в 35 3в", "в 3в"), "field" to "v353tsnB"),
        mapOf("patterns" to listOf("В-35 3ТСН С", "в 35 3 тсн с", "3 тсн в с", "в 3 тсн с", "в 35 3 тсн с", "в 35 3с", "в 3с"), "field" to "v353tsnC")
    )

    // Паттерн для поиска значения после оборудования
    private val valuePattern = Regex(
        """(-?\d+(?:[.,]\d+)?)|(\d+/\d+)|(полтора|ноль\s*пять|пол|минус\s*полтора)""",
        RegexOption.IGNORE_CASE
    )

    // Динамический паттерн для ТСН (любая цифра или слово перед ТСН)
    private val tsnPattern = Regex(
        """(?:два|три|четыре|2|3|4|второй|третий|четвертый|четвёртый)\s*(?:тсн|ТСН|тэсэн)""",
        RegexOption.IGNORE_CASE
    )

    // Конвертер словесных и дробных значений в число
    private fun convertToNumber(raw: String): String? {
        val lower = raw.lowercase().trim()

        return when {
            lower.contains("/") -> {
                val parts = lower.split("/")
                if (parts.size == 2) {
                    val numerator = parts[0].toDoubleOrNull()
                    val denominator = parts[1].toDoubleOrNull()
                    if (numerator != null && denominator != null && denominator != 0.0) {
                        (numerator / denominator).toString()
                    } else null
                } else null
            }
            lower == "полтора" -> "1.5"
            lower == "минус полтора" -> "-1.5"
            lower.matches(Regex("ноль\\s+пять")) -> "0.5"
            lower == "ноль пять" -> "0.5"
            lower == "пол" -> "0.5"
            else -> {
                val num = lower.toDoubleOrNull()
                num?.toString()?.replace(",", ".")
            }
        }
    }

    // Нормализация текста (цифры словами → цифрами)
    private fun normalizeText(text: String): String {
        var result = text
        result = result.replace(Regex("\\bдва\\b"), "2")
        result = result.replace(Regex("\\bтри\\b"), "3")
        result = result.replace(Regex("\\bчетыре\\b"), "4")
        result = result.replace(Regex("\\bвторой\\b"), "2")
        result = result.replace(Regex("\\bтретий\\b"), "3")
        result = result.replace(Regex("\\bчетвертый\\b|\\bчетвёртый\\b"), "4")
        result = result.replace(Regex("тэсэн", RegexOption.IGNORE_CASE), "тсн")
        return result
    }

    // Определение поля для ТСН по номеру
    private fun getTsnField(number: String): String? {
        return when (number) {
            "2", "два", "второй" -> "tsn2"
            "3", "три", "третий" -> "tsn3"
            "4", "четыре", "четвертый", "четвёртый" -> "tsn4"
            else -> null
        }
    }

    fun parse(spokenText: String): List<VoiceParsedResult> {
        val results = mutableListOf<VoiceParsedResult>()
        var normalized = spokenText.lowercase().trim()
        normalized = normalizeText(normalized)

        // ========== ОБРАБОТКА ТСН (динамическая) ==========
        val tsnMatch = tsnPattern.find(normalized)
        if (tsnMatch != null) {
            val tsnText = tsnMatch.value
            val numberMatch = Regex("""(2|3|4|два|три|четыре|второй|третий|четвертый|четвёртый)""").find(tsnText)
            if (numberMatch != null) {
                val field = getTsnField(numberMatch.value)
                if (field != null) {
                    val afterText = normalized.substring(tsnMatch.range.last + 1)
                    val valueMatch = valuePattern.find(afterText)
                    if (valueMatch != null) {
                        val converted = convertToNumber(valueMatch.value)
                        if (converted != null) {
                            results.add(VoiceParsedResult(field, converted))
                        }
                    }
                }
            }
        }

        // ========== ОБРАБОТКА ОСТАЛЬНОГО ОБОРУДОВАНИЯ ==========
        for (mapping in fieldMapping) {
            val patterns = mapping["patterns"] as List<String>
            val field = mapping["field"] as String

            for (pattern in patterns) {
                val patternLower = pattern.lowercase()
                if (normalized.contains(patternLower)) {
                    val index = normalized.indexOf(patternLower) + patternLower.length
                    val afterText = if (index < normalized.length) {
                        normalized.substring(index)
                    } else {
                        ""
                    }

                    val valueMatch = valuePattern.find(afterText)
                    if (valueMatch != null) {
                        val converted = convertToNumber(valueMatch.value)
                        if (converted != null) {
                            results.add(VoiceParsedResult(field, converted))
                        }
                    }
                    break
                }
            }
        }

        return results
    }

    fun formatConfirmationMessage(results: List<VoiceParsedResult>): String {
        if (results.isEmpty()) return "Не удалось распознать показания"

        val sb = StringBuilder()
        sb.append("🎤 Распознано:\n\n")

        for (r in results) {
            val prettyName = when (r.fieldName) {
                "tsn2" -> "2ТСН"
                "tsn3" -> "3ТСН"
                "tsn4" -> "4ТСН"
                "tt352tsnA" -> "ТТ-35 2ТСН А"
                "tt352tsnB" -> "ТТ-35 2ТСН В"
                "tt352tsnC" -> "ТТ-35 2ТСН С"
                "tt353tsnA" -> "ТТ-35 3ТСН А"
                "tt353tsnB" -> "ТТ-35 3ТСН В"
                "tt353tsnC" -> "ТТ-35 3ТСН С"
                "v352tsnA" -> "В-35 2ТСН А"
                "v352tsnB" -> "В-35 2ТСН В"
                "v352tsnC" -> "В-35 2ТСН С"
                "v353tsnA" -> "В-35 3ТСН А"
                "v353tsnB" -> "В-35 3ТСН В"
                "v353tsnC" -> "В-35 3ТСН С"
                else -> r.fieldName
            }
            sb.append("• $prettyName → ${r.value}\n")
        }
        sb.append("\nПрименить все изменения?")
        return sb.toString()
    }
}