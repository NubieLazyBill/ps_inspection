package com.example.ps_inspection

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.ps_inspection.ui.MainActivity

class SplashActivity : AppCompatActivity() {

    private val splashDelay = 2500L

    // Список всех splash-картинок
    private val splashImages = listOf(
        R.drawable.splash_kustovaya,
        R.drawable.splash_kustovaya1,
        R.drawable.splash_kustovaya2,
        R.drawable.splash_kustovaya3,
        R.drawable.splash_kustovaya4,
        R.drawable.splash_kustovaya5,
        R.drawable.splash_kustovaya6,
        // Добавьте все ваши картинки
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Выбираем случайную картинку
        val randomImageId = splashImages.random()

        // Устанавливаем картинку
        val ivSplash = findViewById<ImageView>(R.id.ivSplash)
        ivSplash.setImageResource(randomImageId)

        // Анализируем цвета картинки и меняем фон
        extractColorsAndSetBackground(randomImageId)

        // Задержка перед переходом
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashDelay)
    }

    private fun extractColorsAndSetBackground(imageResId: Int) {
        try {
            // Загружаем картинку в Bitmap
            val bitmap = BitmapFactory.decodeResource(resources, imageResId)

            // Анализируем палитру цветов
            Palette.from(bitmap).generate { palette ->
                if (palette != null) {
                    // Выбираем подходящий цвет фона
                    val backgroundColor = selectBestBackgroundColor(palette)

                    // Применяем фон
                    val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
                    rootLayout.setBackgroundColor(backgroundColor)
                }
            }
        } catch (e: Exception) {
            // В случае ошибки используем чёрный фон
            findViewById<RelativeLayout>(R.id.rootLayout).setBackgroundColor(Color.BLACK)
        }
    }

    private fun selectBestBackgroundColor(palette: Palette): Int {
        // Пробуем получить разные цвета из палитры в порядке приоритета

        // 1. Тёмный цвет (светлая картинка -> тёмный фон)
        val darkVibrantColor = palette.getDarkVibrantColor(0)
        if (darkVibrantColor != 0 && isDarkColor(darkVibrantColor)) {
            return darkVibrantColor
        }

        // 2. Тёмный мягкий цвет
        val darkMutedColor = palette.getDarkMutedColor(0)
        if (darkMutedColor != 0 && isDarkColor(darkMutedColor)) {
            return darkMutedColor
        }

        // 3. Яркий цвет (если картинка тёмная)
        val vibrantColor = palette.getVibrantColor(0)
        if (vibrantColor != 0 && !isDarkColor(vibrantColor)) {
            return vibrantColor
        }

        // 4. Мягкий цвет
        val mutedColor = palette.getMutedColor(0)
        if (mutedColor != 0) {
            return mutedColor
        }

        // 5. По умолчанию — чёрный
        return Color.BLACK
    }

    // Функция для определения тёмный ли цвет
    private fun isDarkColor(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        // Формула яркости (стандартная)
        val brightness = (0.299 * red + 0.587 * green + 0.114 * blue)

        // Если яркость < 128, цвет тёмный
        return brightness < 128
    }
}