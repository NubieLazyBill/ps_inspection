package com.example.ps_inspection.data.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class WeatherData(
    val temperature: Double,
    val windSpeed: Double,
    val condition: String
)

class WeatherService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Получает погоду с fallback-механизмом:
     * 1. Сначала пробует Open-Meteo (API)
     * 2. Если не работает — парсит Яндекс.Погоду
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData? {
        // 1️⃣ Пробуем Open-Meteo
        Log.d("WeatherService", "🌤️ Попытка 1: Open-Meteo API")
        val openMeteoWeather = getWeatherFromOpenMeteo(latitude, longitude)
        if (openMeteoWeather != null) {
            Log.d("WeatherService", "✅ Open-Meteo успешно: ${openMeteoWeather.temperature}°C")
            return openMeteoWeather
        }

        // 2️⃣ Если Open-Meteo не работает — пробуем Яндекс
        Log.d("WeatherService", "🌤️ Попытка 2: Яндекс.Погода (парсинг)")
        val yandexWeather = getWeatherFromYandex(latitude, longitude)
        if (yandexWeather != null) {
            Log.d("WeatherService", "✅ Яндекс успешно: ${yandexWeather.temperature}°C")
            return yandexWeather
        }

        Log.e("WeatherService", "❌ Все источники погоды недоступны")
        return null
    }

    // ==================== 1. Open-Meteo API ====================

    private suspend fun getWeatherFromOpenMeteo(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$latitude&longitude=$longitude" +
                        "&current=temperature_2m,wind_speed_10m,weather_code" +
                        "&timezone=auto"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val json = response.body?.string()
                    parseOpenMeteoResponse(json)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("WeatherService", "Open-Meteo ошибка: ${e.message}")
                null
            }
        }
    }

    private fun parseOpenMeteoResponse(json: String?): WeatherData? {
        if (json == null) return null
        try {
            val root = JSONObject(json)
            val current = root.optJSONObject("current") ?: return null

            val temperature = current.optDouble("temperature_2m", Double.NaN)
            if (temperature.isNaN()) return null

            val windSpeed = current.optDouble("wind_speed_10m", 0.0)
            val weatherCode = current.optInt("weather_code", 0)
            val condition = getWeatherCondition(weatherCode)

            return WeatherData(
                temperature = temperature,
                windSpeed = windSpeed,
                condition = condition
            )
        } catch (e: Exception) {
            Log.e("WeatherService", "Ошибка парсинга Open-Meteo", e)
            return null
        }
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Ясно ☀️"
            1, 2, 3 -> "Переменная облачность ⛅"
            45, 48 -> "Туман 🌫️"
            51, 53, 55 -> "Морось 🌧️"
            61, 63, 65 -> "Дождь 🌧️"
            71, 73, 75 -> "Снег 🌨️"
            80, 81, 82 -> "Ливень ⛈️"
            95, 96, 99 -> "Гроза ⚡"
            else -> "Облачно ☁️"
        }
    }

    // ==================== 2. Яндекс.Погода (парсинг) ====================

    private suspend fun getWeatherFromYandex(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://yandex.ru/pogoda/?lat=$latitude&lon=$longitude"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val html = response.body?.string()
                    parseYandexWeather(html)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("WeatherService", "Яндекс ошибка: ${e.message}")
                null
            }
        }
    }

    private fun parseYandexWeather(html: String?): WeatherData? {
        if (html == null) return null

        return try {
            // Температура
            val tempPattern = Pattern.compile("""Температура воздуха (\+?\-?\d+)°""")
            val tempMatcher = tempPattern.matcher(html)
            val temperature = if (tempMatcher.find()) {
                tempMatcher.group(1).toDouble()
            } else {
                val fallbackPattern = Pattern.compile("""(\+?\-?\d+)°""")
                val fallbackMatcher = fallbackPattern.matcher(html)
                if (fallbackMatcher.find()) {
                    fallbackMatcher.group(1).toDouble()
                } else {
                    0.0
                }
            }

            // Состояние погоды
            val conditionPattern = Pattern.compile("""погода сейчас: (.+?)[.,]""")
            val conditionMatcher = conditionPattern.matcher(html)
            val condition = if (conditionMatcher.find()) {
                conditionMatcher.group(1).trim()
            } else {
                "Неизвестно"
            }

            WeatherData(
                temperature = temperature,
                windSpeed = 0.0,
                condition = condition
            )
        } catch (e: Exception) {
            Log.e("WeatherService", "Ошибка парсинга Яндекса", e)
            null
        }
    }
}