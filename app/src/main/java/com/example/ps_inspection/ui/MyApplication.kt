package com.example.ps_inspection

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Отключаем ночной режим на уровне всего приложения
        // MODE_NIGHT_NO = принудительно светлая тема всегда
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}