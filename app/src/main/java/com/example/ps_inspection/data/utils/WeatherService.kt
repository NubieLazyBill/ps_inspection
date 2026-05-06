package com.example.ps_inspection.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.io.IOException

data class WeatherData(
    val temperature: Double,  // Температура воздуха
    val windSpeed: Double,    // Скорость ветра
    val condition: String     // Погодные условия
)

class WeatherService {
    private val client = OkHttpClient()
    private val gson = Gson()

    /**
     * Получает текущую погоду по координатам
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData? {
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
                    parseWeatherResponse(json)
                } else {
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseWeatherResponse(json: String?): WeatherData? {
        if (json == null) return null
        try {
            val root = gson.fromJson(json, Map::class.java)
            val current = root["current"] as? Map<*, *> ?: return null

            val temperature = (current["temperature_2m"] as? Double) ?: return null
            val windSpeed = (current["wind_speed_10m"] as? Double) ?: 0.0
            val weatherCode = (current["weather_code"] as? Double)?.toInt() ?: 0
            val condition = getWeatherCondition(weatherCode)

            return WeatherData(
                temperature = temperature,
                windSpeed = windSpeed,
                condition = condition
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
}