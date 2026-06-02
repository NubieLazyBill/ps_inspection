package com.example.ps_inspection.data.utils

data class VoiceParsedResult(
    val fieldName: String,
    val value: String
)

object ORU35MassVoiceParser {

    // Маппинг оборудования с фазами
    private val phaseEquipmentMapping = listOf(
        // ТТ-35 2ТСН
        mapOf(
            "name" to "ТТ-35 2ТСН",
            "patterns" to listOf("ТТ-35 2ТСН", "тт 35 2 тсн", "2 тсн тт", "тт 2 тсн", "тт 35 2", "тт 2"),
            "fields" to mapOf("А" to "tt352tsnA", "В" to "tt352tsnB", "С" to "tt352tsnC")
        ),
        // ТТ-35 3ТСН
        mapOf(
            "name" to "ТТ-35 3ТСН",
            "patterns" to listOf("ТТ-35 3ТСН", "тт 35 3 тсн", "3 тсн тт", "тт 3 тсн", "тт 35 3", "тт 3"),
            "fields" to mapOf("А" to "tt353tsnA", "В" to "tt353tsnB", "С" to "tt353tsnC")
        ),
        // В-35 2ТСН
        mapOf(
            "name" to "В-35 2ТСН",
            "patterns" to listOf("В-35 2ТСН", "в 35 2 тсн", "2 тсн в", "в 2 тсн", "в 35 2", "в 2"),
            "fields" to mapOf("А" to "v352tsnA", "В" to "v352tsnB", "С" to "v352tsnC")
        ),
        // В-35 3ТСН
        mapOf(
            "name" to "В-35 3ТСН",
            "patterns" to listOf("В-35 3ТСН", "в 35 3 тсн", "3 тсн в", "в 3 тсн", "в 35 3", "в 3"),
            "fields" to mapOf("А" to "v353tsnA", "В" to "v353tsnB", "С" to "v353tsnC")
        )
    )

    // Фазы и их варианты произношения
    private val phaseMapping = mapOf(
        "А" to listOf("а", "фаза а", "а фаза", "а."),
        "В" to listOf("б", "бэ", "в", "фаза б", "фаза бэ", "фаза в", "б фаза", "бэ фаза", "в фаза", "б.", "в."),
        "С" to listOf("с", "эс", "ц", "фаза с", "фаза эс", "фаза ц", "с фаза", "эс фаза", "ц фаза", "с.", "ц.")
    )

    // Паттерн для поиска значения
    private val valuePattern = Regex(
        """(-?\d+(?:[.,]\d+)?)|(\d+/\d+)|(полтора|ноль\s*пять|пол|минус\s*полтора)""",
        RegexOption.IGNORE_CASE
    )

    // Динамический паттерн для ТСН
    private val tsnPattern = Regex(
        """(?:два|три|четыре|2|3|4|второй|третий|четвертый|четвёртый)\s*(?:тсн|ТСН|тэсэн)""",
        RegexOption.IGNORE_CASE
    )

    // Паттерн для поиска фразы "фаза Х значение"
    private val phaseValuePattern = Regex(
        """(?:фаза\s*)?([А-Яа-яA-Za-z])(?:\s*[-–—]?\s*)?(\d+(?:[.,]\d+)?|\d+/\d+|полтора|ноль\s*пять|пол)""",
        RegexOption.IGNORE_CASE
    )

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

    private fun normalizeText(text: String): String {
        var result = text.lowercase().trim()
        result = result.replace(Regex("\\bдва\\b"), "2")
        result = result.replace(Regex("\\bтри\\b"), "3")
        result = result.replace(Regex("\\bчетыре\\b"), "4")
        result = result.replace(Regex("\\bвторой\\b"), "2")
        result = result.replace(Regex("\\bтретий\\b"), "3")
        result = result.replace(Regex("\\bчетвертый\\b|\\bчетвёртый\\b"), "4")
        result = result.replace(Regex("тэсэн"), "тсн")
        result = result.replace(Regex("\\s+"), " ")
        return result
    }

    private fun getTsnField(number: String): String? {
        return when (number) {
            "2", "два", "второй" -> "tsn2"
            "3", "три", "третий" -> "tsn3"
            "4", "четыре", "четвертый", "четвёртый" -> "tsn4"
            else -> null
        }
    }

    private fun normalizePhase(input: String): String? {
        val lower = input.lowercase()
        for ((phase, variants) in phaseMapping) {
            if (variants.contains(lower) || variants.any { lower.contains(it) }) {
                return phase
            }
        }
        return null
    }

    private fun parsePhaseValues(text: String, afterEquipment: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        val searchText = afterEquipment.lowercase()

        // Ищем паттерны "А 0.7", "фаза Б 0.5", "С 0.6"
        val matches = phaseValuePattern.findAll(searchText)
        for (match in matches) {
            val phaseRaw = match.groupValues[1]
            val valueRaw = match.groupValues[2]
            val phase = normalizePhase(phaseRaw)
            val converted = convertToNumber(valueRaw)
            if (phase != null && converted != null) {
                results.add(Pair(phase, converted))
            }
        }

        return results
    }

    fun parse(spokenText: String): List<VoiceParsedResult> {
        val results = mutableListOf<VoiceParsedResult>()
        var normalized = normalizeText(spokenText)

        // ========== ОБРАБОТКА ТСН ==========
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

        // ========== ОБРАБОТКА ПОФАЗНОГО ОБОРУДОВАНИЯ ==========
        for (equipment in phaseEquipmentMapping) {
            val name = equipment["name"] as String
            val patterns = equipment["patterns"] as List<String>
            val fields = equipment["fields"] as Map<String, String>

            for (pattern in patterns) {
                val patternLower = pattern.lowercase()
                if (normalized.contains(patternLower)) {
                    val index = normalized.indexOf(patternLower) + patternLower.length
                    val afterText = if (index < normalized.length) {
                        normalized.substring(index)
                    } else {
                        ""
                    }

                    // Парсим фазы и значения
                    val phaseResults = parsePhaseValues(normalized, afterText)
                    for ((phase, value) in phaseResults) {
                        val field = fields[phase]
                        if (field != null) {
                            results.add(VoiceParsedResult(field, value))
                        }
                    }
                    break
                }
            }
        }

        return results.distinctBy { it.fieldName }
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