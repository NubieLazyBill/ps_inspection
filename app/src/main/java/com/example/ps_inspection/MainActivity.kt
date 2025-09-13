package com.example.ps_inspection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим кастомный TextView
        toolbarTitle = findViewById(R.id.toolbar_title)

        // Настройка Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ОТКЛЮЧАЕМ стандартный заголовок ActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Настройка Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Обновляем заголовок при изменении destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            toolbarTitle.text = destination.label ?: "Осмотр ПС"
        }

        // Связываем ActionBar с NavController (для кнопки "Назад")
        setupActionBarWithNavController(navController)
    }

    // Обрабатываем нажатие кнопки "Назад"
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}