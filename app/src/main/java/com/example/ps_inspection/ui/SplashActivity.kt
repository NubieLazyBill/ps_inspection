package com.example.ps_inspection

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.example.ps_inspection.ui.MainActivity
import java.util.Calendar

class SplashActivity : AppCompatActivity() {

    private val splashDelay = 2500L
    private lateinit var prefs: SharedPreferences

    private val splashImages = listOf(
        R.drawable.splash_kustovaya,
        R.drawable.splash_kustovaya1,
        R.drawable.splash_kustovaya2,
        R.drawable.splash_kustovaya3,
        R.drawable.splash_kustovaya4,
        R.drawable.splash_kustovaya5,
        R.drawable.splash_kustovaya6,
        R.drawable.splash_kustovaya7,
        R.drawable.splash_kustovaya8,
        R.drawable.splash_kustovaya9,
        R.drawable.splash_kustovaya10,
        R.drawable.splash_kustovaya11,
        R.drawable.splash_kustovaya12,
        R.drawable.splash_kustovaya13,
        R.drawable.splash_kustovaya14,
        R.drawable.splash_kustovaya15,
        R.drawable.splash_kustovaya16,
        R.drawable.splash_kustovaya17,
        R.drawable.splash_kustovaya18,
        R.drawable.splash_kustovaya19,
        R.drawable.splash_kustovaya20,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("splash_prefs", MODE_PRIVATE)

        // Проверяем, нужно ли показывать сплеш сегодня
        if (shouldShowSplashToday()) {
            // Показываем сплеш
            setContentView(R.layout.activity_splash)

            val randomImageId = splashImages.random()
            val ivSplash = findViewById<ImageView>(R.id.ivSplash)
            ivSplash.setImageResource(randomImageId)

            extractColorsAndSetBackground(randomImageId)

            Handler(Looper.getMainLooper()).postDelayed({
                // Сохраняем дату показа
                saveSplashShownToday()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, splashDelay)
        } else {
            // Сегодня сплеш уже показывали — сразу переходим в MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /**
     * Проверяет, нужно ли показывать сплеш сегодня
     */
    private fun shouldShowSplashToday(): Boolean {
        val lastShownDate = prefs.getString("last_splash_date", null)
        val today = getCurrentDate()

        return lastShownDate != today
    }

    /**
     * Сохраняет дату сегодняшнего показа
     */
    private fun saveSplashShownToday() {
        val today = getCurrentDate()
        prefs.edit().putString("last_splash_date", today).apply()
    }

    /**
     * Возвращает текущую дату в формате "yyyy-MM-dd"
     */
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun extractColorsAndSetBackground(imageResId: Int) {
        try {
            val bitmap = BitmapFactory.decodeResource(resources, imageResId)

            Palette.from(bitmap).generate { palette ->
                if (palette != null) {
                    val backgroundColor = selectBestBackgroundColor(palette)
                    val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
                    rootLayout.setBackgroundColor(backgroundColor)
                }
            }
        } catch (e: Exception) {
            findViewById<RelativeLayout>(R.id.rootLayout).setBackgroundColor(Color.BLACK)
        }
    }

    private fun selectBestBackgroundColor(palette: Palette): Int {
        val darkVibrantColor = palette.getDarkVibrantColor(0)
        if (darkVibrantColor != 0 && isDarkColor(darkVibrantColor)) {
            return darkVibrantColor
        }

        val darkMutedColor = palette.getDarkMutedColor(0)
        if (darkMutedColor != 0 && isDarkColor(darkMutedColor)) {
            return darkMutedColor
        }

        val vibrantColor = palette.getVibrantColor(0)
        if (vibrantColor != 0 && !isDarkColor(vibrantColor)) {
            return vibrantColor
        }

        val mutedColor = palette.getMutedColor(0)
        if (mutedColor != 0) {
            return mutedColor
        }

        return Color.BLACK
    }

    private fun isDarkColor(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        val brightness = (0.299 * red + 0.587 * green + 0.114 * blue)
        return brightness < 128
    }
}