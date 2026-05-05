package com.example.ps_inspection.ui

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.ps_inspection.data.repositories.AutoSaveManager
import com.example.ps_inspection.data.repositories.LastInspectionManager
import com.example.ps_inspection.R
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView
    private lateinit var autoSaveManager: AutoSaveManager
    private lateinit var lastInspectionManager: LastInspectionManager
    private val sharedViewModel: SharedInspectionViewModel by lazy {
        ViewModelProvider(this)[SharedInspectionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Добавь ДО super.onCreate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализируем менеджеры
        autoSaveManager = AutoSaveManager(this)
        lastInspectionManager = LastInspectionManager(this)

        // Инициализируем хранилище комментариев (загружает ВСЕ комментарии)
        sharedViewModel.initCommentStorage(this)

        // Находим кастомный TextView
        toolbarTitle = findViewById(R.id.toolbar_title)

        // Настройка Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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

        // Проверяем наличие автосохранения
        checkForAutoSave()
    }

    override fun onPause() {
        super.onPause()
        // 🔒 ТРОЙНОЕ СОХРАНЕНИЕ при сворачивании
        val oru35 = sharedViewModel.oru35Data.value
        val oru220 = sharedViewModel.oru220Data.value
        val atg = sharedViewModel.atgData.value
        val oru500 = sharedViewModel.oru500Data.value
        val buildings = sharedViewModel.buildingsData.value

        // 1. Автосохранение (JSON файл)
        autoSaveManager.saveAllData(oru35, oru220, atg, oru500, buildings)

        // 2. LastInspection (SharedPreferences)
        try {
            lastInspectionManager.saveLastInspection(oru35, oru220, atg, oru500, buildings)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 🔒 НЕ удаляем автосохранение при закрытии!
        // Оно удалится только когда пользователь ЯВНО начнёт новый осмотр
    }

    private fun checkForAutoSave() {
        if (autoSaveManager.hasAutoSave()) {
            val autoSave = autoSaveManager.loadAllData()
            if (autoSave != null) {
                // Кастомный диалог
                val dialogView = layoutInflater.inflate(R.layout.dialog_restore_inspection, null)
                val tvMessage = dialogView.findViewById<TextView>(R.id.tvRestoreMessage)
                val btnRestore = dialogView.findViewById<Button>(R.id.btnRestore)
                val btnNew = dialogView.findViewById<Button>(R.id.btnNewInspection)

                tvMessage.text = "Обнаружены несохранённые данные осмотра\nот ${autoSave.displayDate}"

                val dialog = AlertDialog.Builder(this, R.style.TransparentDialog)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create()

                btnRestore.setOnClickListener {
                    dialog.dismiss()
                    restoreAutoSave(autoSave)
                    sharedViewModel.initCommentStorage(this)
                    Toast.makeText(this, "✅ Данные восстановлены", Toast.LENGTH_LONG).show()
                }

                btnNew.setOnClickListener {
                    dialog.dismiss()
                    // Кастомный диалог подтверждения
                    val confirmView = layoutInflater.inflate(R.layout.dialog_confirm_new_inspection, null)
                    val btnConfirmYes = confirmView.findViewById<Button>(R.id.btnConfirmYes)
                    val btnConfirmNo = confirmView.findViewById<Button>(R.id.btnConfirmNo)

                    val confirmDialog = AlertDialog.Builder(this@MainActivity, R.style.TransparentDialog)
                        .setView(confirmView)
                        .setCancelable(false)
                        .create()

                    btnConfirmYes.setOnClickListener {
                        confirmDialog.dismiss()
                        autoSaveManager.clearAutoSave()
                        sharedViewModel.clearAllData()
                        sharedViewModel.initCommentStorage(this@MainActivity)
                        Toast.makeText(this@MainActivity, "Начат новый осмотр", Toast.LENGTH_SHORT).show()
                    }

                    btnConfirmNo.setOnClickListener {
                        confirmDialog.dismiss()
                    }

                    confirmDialog.show()
                }

                dialog.show()
            }
        } else {
            sharedViewModel.initCommentStorage(this)
        }
    }

    private fun restoreAutoSave(autoSave: AutoSaveManager.AutoSaveData) {

        // Восстанавливаем температуру

        sharedViewModel.updateOutdoorTemp(autoSave.outdoorTemp)
        // Восстанавливаем ORU35 данные
        sharedViewModel.updateORU35Data {
            tsn2 = autoSave.oru35.tsn2
            tsn3 = autoSave.oru35.tsn3
            tsn4 = autoSave.oru35.tsn4
            tt352tsnA = autoSave.oru35.tt352tsnA
            tt352tsnB = autoSave.oru35.tt352tsnB
            tt352tsnC = autoSave.oru35.tt352tsnC
            tt353tsnA = autoSave.oru35.tt353tsnA
            tt353tsnB = autoSave.oru35.tt353tsnB
            tt353tsnC = autoSave.oru35.tt353tsnC
            v352tsnA = autoSave.oru35.v352tsnA
            v352tsnB = autoSave.oru35.v352tsnB
            v352tsnC = autoSave.oru35.v352tsnC
            v353tsnA = autoSave.oru35.v353tsnA
            v353tsnB = autoSave.oru35.v353tsnB
            v353tsnC = autoSave.oru35.v353tsnC

            // 🔒 Восстанавливаем комментарии
            commentTsn = autoSave.oru35.commentTsn
            commentTt352 = autoSave.oru35.commentTt352
            commentTt353 = autoSave.oru35.commentTt353
            commentV352 = autoSave.oru35.commentV352
            commentV353 = autoSave.oru35.commentV353

            // 🔒 Восстанавливаем фото
            oru35PhotoFiles = autoSave.oru35.oru35PhotoFiles
        }

        // Восстанавливаем ORU220 данные
        sharedViewModel.updateORU220Data {
            purgingMirnayaA = autoSave.oru220.purgingMirnayaA
            purgingMirnayaB = autoSave.oru220.purgingMirnayaB
            purgingMirnayaC = autoSave.oru220.purgingMirnayaC
            oilMirnayaA = autoSave.oru220.oilMirnayaA
            oilMirnayaB = autoSave.oru220.oilMirnayaB
            oilMirnayaC = autoSave.oru220.oilMirnayaC
            purgingTopazA = autoSave.oru220.purgingTopazA
            purgingTopazB = autoSave.oru220.purgingTopazB
            purgingTopazC = autoSave.oru220.purgingTopazC
            oilTopazA = autoSave.oru220.oilTopazA
            oilTopazB = autoSave.oru220.oilTopazB
            oilTopazC = autoSave.oru220.oilTopazC
            purgingOvA = autoSave.oru220.purgingOvA
            purgingOvB = autoSave.oru220.purgingOvB
            purgingOvC = autoSave.oru220.purgingOvC
            oilOvA = autoSave.oru220.oilOvA
            oilOvB = autoSave.oru220.oilOvB
            oilOvC = autoSave.oru220.oilOvC
            tnOsshFvUpper = autoSave.oru220.tnOsshFvUpper
            tnOsshFvLower = autoSave.oru220.tnOsshFvLower
            purgingV2atgA = autoSave.oru220.purgingV2atgA
            purgingV2atgB = autoSave.oru220.purgingV2atgB
            purgingV2atgC = autoSave.oru220.purgingV2atgC
            oilTt2atgA = autoSave.oru220.oilTt2atgA
            oilTt2atgB = autoSave.oru220.oilTt2atgB
            oilTt2atgC = autoSave.oru220.oilTt2atgC
            purgingShSV220A = autoSave.oru220.purgingShSV220A
            purgingShSV220B = autoSave.oru220.purgingShSV220B
            purgingShSV220C = autoSave.oru220.purgingShSV220C
            oilTtShSV220A = autoSave.oru220.oilTtShSV220A
            oilTtShSV220B = autoSave.oru220.oilTtShSV220B
            oilTtShSV220C = autoSave.oru220.oilTtShSV220C
            purgingV3atgA = autoSave.oru220.purgingV3atgA
            purgingV3atgB = autoSave.oru220.purgingV3atgB
            purgingV3atgC = autoSave.oru220.purgingV3atgC
            oilTt3atgA = autoSave.oru220.oilTt3atgA
            oilTt3atgB = autoSave.oru220.oilTt3atgB
            oilTt3atgC = autoSave.oru220.oilTt3atgC
            purgingOrbitaA = autoSave.oru220.purgingOrbitaA
            purgingOrbitaB = autoSave.oru220.purgingOrbitaB
            purgingOrbitaC = autoSave.oru220.purgingOrbitaC
            oilOrbitaA = autoSave.oru220.oilOrbitaA
            oilOrbitaB = autoSave.oru220.oilOrbitaB
            oilOrbitaC = autoSave.oru220.oilOrbitaC
            purgingFakelA = autoSave.oru220.purgingFakelA
            purgingFakelB = autoSave.oru220.purgingFakelB
            purgingFakelC = autoSave.oru220.purgingFakelC
            oilFakelA = autoSave.oru220.oilFakelA
            oilFakelB = autoSave.oru220.oilFakelB
            oilFakelC = autoSave.oru220.oilFakelC
            purgingCometa1A = autoSave.oru220.purgingCometa1A
            purgingCometa1B = autoSave.oru220.purgingCometa1B
            purgingCometa1C = autoSave.oru220.purgingCometa1C
            oilCometa1A = autoSave.oru220.oilCometa1A
            oilCometa1B = autoSave.oru220.oilCometa1B
            oilCometa1C = autoSave.oru220.oilCometa1C
            purgingCometa2A = autoSave.oru220.purgingCometa2A
            purgingCometa2B = autoSave.oru220.purgingCometa2B
            purgingCometa2C = autoSave.oru220.purgingCometa2C
            oilCometa2A = autoSave.oru220.oilCometa2A
            oilCometa2B = autoSave.oru220.oilCometa2B
            oilCometa2C = autoSave.oru220.oilCometa2C
            tn1UpperA = autoSave.oru220.tn1UpperA
            tn1UpperB = autoSave.oru220.tn1UpperB
            tn1UpperC = autoSave.oru220.tn1UpperC
            tn1LowerA = autoSave.oru220.tn1LowerA
            tn1LowerB = autoSave.oru220.tn1LowerB
            tn1LowerC = autoSave.oru220.tn1LowerC
            tn2UpperA = autoSave.oru220.tn2UpperA
            tn2UpperB = autoSave.oru220.tn2UpperB
            tn2UpperC = autoSave.oru220.tn2UpperC
            tn2LowerA = autoSave.oru220.tn2LowerA
            tn2LowerB = autoSave.oru220.tn2LowerB
            tn2LowerC = autoSave.oru220.tn2LowerC

            // 🔒 Восстанавливаем комментарии ORU220
            commentMirnaya = autoSave.oru220.commentMirnaya
            commentMirnayaTT = autoSave.oru220.commentMirnayaTT
            commentTopaz = autoSave.oru220.commentTopaz
            commentTopazTT = autoSave.oru220.commentTopazTT
            commentOv = autoSave.oru220.commentOv
            commentOvTT = autoSave.oru220.commentOvTT
            commentOssh = autoSave.oru220.commentOssh
            commentV2atg = autoSave.oru220.commentV2atg
            commentV2atgTT = autoSave.oru220.commentV2atgTT
            commentShsv = autoSave.oru220.commentShsv
            commentShsvTT = autoSave.oru220.commentShsvTT
            commentV3atg = autoSave.oru220.commentV3atg
            commentV3atgTT = autoSave.oru220.commentV3atgTT
            commentOrbita = autoSave.oru220.commentOrbita
            commentOrbitaTT = autoSave.oru220.commentOrbitaTT
            commentFakel = autoSave.oru220.commentFakel
            commentFakelTT = autoSave.oru220.commentFakelTT
            commentCometa1 = autoSave.oru220.commentCometa1
            commentCometa1TT = autoSave.oru220.commentCometa1TT
            commentCometa2 = autoSave.oru220.commentCometa2
            commentCometa2TT = autoSave.oru220.commentCometa2TT
            commentTn1 = autoSave.oru220.commentTn1
            commentTn2 = autoSave.oru220.commentTn2
        }

        // Восстанавливаем ATG данные
        sharedViewModel.updateATGData {
            atg2_c_oil_tank = autoSave.atg.atg2_c_oil_tank
            atg2_c_oil_rpn = autoSave.atg.atg2_c_oil_rpn
            atg2_c_pressure_500 = autoSave.atg.atg2_c_pressure_500
            atg2_c_pressure_220 = autoSave.atg.atg2_c_pressure_220
            atg2_c_temp_ts1 = autoSave.atg.atg2_c_temp_ts1
            atg2_c_temp_ts2 = autoSave.atg.atg2_c_temp_ts2
            atg2_c_pump_group1 = autoSave.atg.atg2_c_pump_group1
            atg2_c_pump_group2 = autoSave.atg.atg2_c_pump_group2
            atg2_c_pump_group3 = autoSave.atg.atg2_c_pump_group3
            atg2_c_pump_group4 = autoSave.atg.atg2_c_pump_group4
            atg2_b_oil_tank = autoSave.atg.atg2_b_oil_tank
            atg2_b_oil_rpn = autoSave.atg.atg2_b_oil_rpn
            atg2_b_pressure_500 = autoSave.atg.atg2_b_pressure_500
            atg2_b_pressure_220 = autoSave.atg.atg2_b_pressure_220
            atg2_b_temp_ts1 = autoSave.atg.atg2_b_temp_ts1
            atg2_b_temp_ts2 = autoSave.atg.atg2_b_temp_ts2
            atg2_b_pump_group1 = autoSave.atg.atg2_b_pump_group1
            atg2_b_pump_group2 = autoSave.atg.atg2_b_pump_group2
            atg2_b_pump_group3 = autoSave.atg.atg2_b_pump_group3
            atg2_b_pump_group4 = autoSave.atg.atg2_b_pump_group4
            atg2_a_oil_tank = autoSave.atg.atg2_a_oil_tank
            atg2_a_oil_rpn = autoSave.atg.atg2_a_oil_rpn
            atg2_a_pressure_500 = autoSave.atg.atg2_a_pressure_500
            atg2_a_pressure_220 = autoSave.atg.atg2_a_pressure_220
            atg2_a_temp_ts1 = autoSave.atg.atg2_a_temp_ts1
            atg2_a_temp_ts2 = autoSave.atg.atg2_a_temp_ts2
            atg2_a_pump_group1 = autoSave.atg.atg2_a_pump_group1
            atg2_a_pump_group2 = autoSave.atg.atg2_a_pump_group2
            atg2_a_pump_group3 = autoSave.atg.atg2_a_pump_group3
            atg2_a_pump_group4 = autoSave.atg.atg2_a_pump_group4
            atg_reserve_oil_tank = autoSave.atg.atg_reserve_oil_tank
            atg_reserve_oil_rpn = autoSave.atg.atg_reserve_oil_rpn
            atg_reserve_pressure_500 = autoSave.atg.atg_reserve_pressure_500
            atg_reserve_pressure_220 = autoSave.atg.atg_reserve_pressure_220
            atg_reserve_temp_ts1 = autoSave.atg.atg_reserve_temp_ts1
            atg_reserve_temp_ts2 = autoSave.atg.atg_reserve_temp_ts2
            atg_reserve_pump_group1 = autoSave.atg.atg_reserve_pump_group1
            atg_reserve_pump_group2 = autoSave.atg.atg_reserve_pump_group2
            atg_reserve_pump_group3 = autoSave.atg.atg_reserve_pump_group3
            atg_reserve_pump_group4 = autoSave.atg.atg_reserve_pump_group4
            tn352atg = autoSave.atg.tn352atg
            tn353atg = autoSave.atg.tn353atg
            atg3_c_oil_tank = autoSave.atg.atg3_c_oil_tank
            atg3_c_oil_rpn = autoSave.atg.atg3_c_oil_rpn
            atg3_c_pressure_500 = autoSave.atg.atg3_c_pressure_500
            atg3_c_pressure_220 = autoSave.atg.atg3_c_pressure_220
            atg3_c_temp_ts1 = autoSave.atg.atg3_c_temp_ts1
            atg3_c_temp_ts2 = autoSave.atg.atg3_c_temp_ts2
            atg3_c_pump_group1 = autoSave.atg.atg3_c_pump_group1
            atg3_c_pump_group2 = autoSave.atg.atg3_c_pump_group2
            atg3_c_pump_group3 = autoSave.atg.atg3_c_pump_group3
            atg3_c_pump_group4 = autoSave.atg.atg3_c_pump_group4
            atg3_b_oil_tank = autoSave.atg.atg3_b_oil_tank
            atg3_b_oil_rpn = autoSave.atg.atg3_b_oil_rpn
            atg3_b_pressure_500 = autoSave.atg.atg3_b_pressure_500
            atg3_b_pressure_220 = autoSave.atg.atg3_b_pressure_220
            atg3_b_temp_ts1 = autoSave.atg.atg3_b_temp_ts1
            atg3_b_temp_ts2 = autoSave.atg.atg3_b_temp_ts2
            atg3_b_pump_group1 = autoSave.atg.atg3_b_pump_group1
            atg3_b_pump_group2 = autoSave.atg.atg3_b_pump_group2
            atg3_b_pump_group3 = autoSave.atg.atg3_b_pump_group3
            atg3_b_pump_group4 = autoSave.atg.atg3_b_pump_group4
            atg3_a_oil_tank = autoSave.atg.atg3_a_oil_tank
            atg3_a_oil_rpn = autoSave.atg.atg3_a_oil_rpn
            atg3_a_pressure_500 = autoSave.atg.atg3_a_pressure_500
            atg3_a_pressure_220 = autoSave.atg.atg3_a_pressure_220
            atg3_a_temp_ts1 = autoSave.atg.atg3_a_temp_ts1
            atg3_a_temp_ts2 = autoSave.atg.atg3_a_temp_ts2
            atg3_a_pump_group1 = autoSave.atg.atg3_a_pump_group1
            atg3_a_pump_group2 = autoSave.atg.atg3_a_pump_group2
            atg3_a_pump_group3 = autoSave.atg.atg3_a_pump_group3
            atg3_a_pump_group4 = autoSave.atg.atg3_a_pump_group4
            reactor_c_oil_tank = autoSave.atg.reactor_c_oil_tank
            reactor_c_pressure_500 = autoSave.atg.reactor_c_pressure_500
            reactor_c_temp_ts = autoSave.atg.reactor_c_temp_ts
            reactor_c_pump_group1 = autoSave.atg.reactor_c_pump_group1
            reactor_c_pump_group2 = autoSave.atg.reactor_c_pump_group2
            reactor_c_pump_group3 = autoSave.atg.reactor_c_pump_group3
            reactor_c_tt_neutral = autoSave.atg.reactor_c_tt_neutral
            reactor_b_oil_tank = autoSave.atg.reactor_b_oil_tank
            reactor_b_pressure_500 = autoSave.atg.reactor_b_pressure_500
            reactor_b_temp_ts = autoSave.atg.reactor_b_temp_ts
            reactor_b_pump_group1 = autoSave.atg.reactor_b_pump_group1
            reactor_b_pump_group2 = autoSave.atg.reactor_b_pump_group2
            reactor_b_pump_group3 = autoSave.atg.reactor_b_pump_group3
            reactor_b_tt_neutral = autoSave.atg.reactor_b_tt_neutral
            reactor_a_oil_tank = autoSave.atg.reactor_a_oil_tank
            reactor_a_pressure_500 = autoSave.atg.reactor_a_pressure_500
            reactor_a_temp_ts = autoSave.atg.reactor_a_temp_ts
            reactor_a_pump_group1 = autoSave.atg.reactor_a_pump_group1
            reactor_a_pump_group2 = autoSave.atg.reactor_a_pump_group2
            reactor_a_pump_group3 = autoSave.atg.reactor_a_pump_group3
            reactor_a_tt_neutral = autoSave.atg.reactor_a_tt_neutral

            // 🔒 Восстанавливаем комментарии ATG
            commentAtg2C = autoSave.atg.commentAtg2C
            commentAtg2B = autoSave.atg.commentAtg2B
            commentAtg2A = autoSave.atg.commentAtg2A
            commentAtgReserve = autoSave.atg.commentAtgReserve
            commentAtg3C = autoSave.atg.commentAtg3C
            commentAtg3B = autoSave.atg.commentAtg3B
            commentAtg3A = autoSave.atg.commentAtg3A
            commentReactorC = autoSave.atg.commentReactorC
            commentReactorB = autoSave.atg.commentReactorB
            commentReactorA = autoSave.atg.commentReactorA
            commentTn35 = autoSave.atg.commentTn35

            // 🔒 Восстанавливаем фото ATG
            atgPhotoFiles = autoSave.atg.atgPhotoFiles
        }

        // Восстанавливаем ORU500 данные
        sharedViewModel.updateORU500Data {
            purgingR5002sA1 = autoSave.oru500.purgingR5002sA1
            purgingR5002sB1 = autoSave.oru500.purgingR5002sB1
            purgingR5002sC1 = autoSave.oru500.purgingR5002sC1
            purgingR5002sA2 = autoSave.oru500.purgingR5002sA2
            purgingR5002sB2 = autoSave.oru500.purgingR5002sB2
            purgingR5002sC2 = autoSave.oru500.purgingR5002sC2
            gasPressureVsht31A = autoSave.oru500.gasPressureVsht31A
            gasPressureVsht31B = autoSave.oru500.gasPressureVsht31B
            gasPressureVsht31C = autoSave.oru500.gasPressureVsht31C
            oilTtVsht31A = autoSave.oru500.oilTtVsht31A
            oilTtVsht31B = autoSave.oru500.oilTtVsht31B
            oilTtVsht31C = autoSave.oru500.oilTtVsht31C
            gasPressureVlt30A = autoSave.oru500.gasPressureVlt30A
            gasPressureVlt30B = autoSave.oru500.gasPressureVlt30B
            gasPressureVlt30C = autoSave.oru500.gasPressureVlt30C
            oilTtVlt30A = autoSave.oru500.oilTtVlt30A
            oilTtVlt30B = autoSave.oru500.oilTtVlt30B
            oilTtVlt30C = autoSave.oru500.oilTtVlt30C
            purgingVshl32A1 = autoSave.oru500.purgingVshl32A1
            purgingVshl32B1 = autoSave.oru500.purgingVshl32B1
            purgingVshl32C1 = autoSave.oru500.purgingVshl32C1
            purgingVshl32A2 = autoSave.oru500.purgingVshl32A2
            purgingVshl32B2 = autoSave.oru500.purgingVshl32B2
            purgingVshl32C2 = autoSave.oru500.purgingVshl32C2
            oilTtVshl32A = autoSave.oru500.oilTtVshl32A
            oilTtVshl32B = autoSave.oru500.oilTtVshl32B
            oilTtVshl32C = autoSave.oru500.oilTtVshl32C
            purgingVshl21A1 = autoSave.oru500.purgingVshl21A1
            purgingVshl21B1 = autoSave.oru500.purgingVshl21B1
            purgingVshl21C1 = autoSave.oru500.purgingVshl21C1
            purgingVshl21A2 = autoSave.oru500.purgingVshl21A2
            purgingVshl21B2 = autoSave.oru500.purgingVshl21B2
            purgingVshl21C2 = autoSave.oru500.purgingVshl21C2
            oilTtVshl21A = autoSave.oru500.oilTtVshl21A
            oilTtVshl21B = autoSave.oru500.oilTtVshl21B
            oilTtVshl21C = autoSave.oru500.oilTtVshl21C
            purgingVsht22A1 = autoSave.oru500.purgingVsht22A1
            purgingVsht22B1 = autoSave.oru500.purgingVsht22B1
            purgingVsht22C1 = autoSave.oru500.purgingVsht22C1
            purgingVsht22A2 = autoSave.oru500.purgingVsht22A2
            purgingVsht22B2 = autoSave.oru500.purgingVsht22B2
            purgingVsht22C2 = autoSave.oru500.purgingVsht22C2
            oilTtVsht22A = autoSave.oru500.oilTtVsht22A
            oilTtVsht22B = autoSave.oru500.oilTtVsht22B
            oilTtVsht22C = autoSave.oru500.oilTtVsht22C
            purgingVlt20A1 = autoSave.oru500.purgingVlt20A1
            purgingVlt20B1 = autoSave.oru500.purgingVlt20B1
            purgingVlt20C1 = autoSave.oru500.purgingVlt20C1
            purgingVlt20A2 = autoSave.oru500.purgingVlt20A2
            purgingVlt20B2 = autoSave.oru500.purgingVlt20B2
            purgingVlt20C2 = autoSave.oru500.purgingVlt20C2
            oilTtVlt20A = autoSave.oru500.oilTtVlt20A
            oilTtVlt20B = autoSave.oru500.oilTtVlt20B
            oilTtVlt20C = autoSave.oru500.oilTtVlt20C
            purgingVsht11A1 = autoSave.oru500.purgingVsht11A1
            purgingVsht11B1 = autoSave.oru500.purgingVsht11B1
            purgingVsht11C1 = autoSave.oru500.purgingVsht11C1
            purgingVsht11A2 = autoSave.oru500.purgingVsht11A2
            purgingVsht11B2 = autoSave.oru500.purgingVsht11B2
            purgingVsht11C2 = autoSave.oru500.purgingVsht11C2
            oilTtVsht11A = autoSave.oru500.oilTtVsht11A
            oilTtVsht11B = autoSave.oru500.oilTtVsht11B
            oilTtVsht11C = autoSave.oru500.oilTtVsht11C
            purgingVshl12A1 = autoSave.oru500.purgingVshl12A1
            purgingVshl12B1 = autoSave.oru500.purgingVshl12B1
            purgingVshl12C1 = autoSave.oru500.purgingVshl12C1
            purgingVshl12A2 = autoSave.oru500.purgingVshl12A2
            purgingVshl12B2 = autoSave.oru500.purgingVshl12B2
            purgingVshl12C2 = autoSave.oru500.purgingVshl12C2
            oilTtVshl12A = autoSave.oru500.oilTtVshl12A
            oilTtVshl12B = autoSave.oru500.oilTtVshl12B
            oilTtVshl12C = autoSave.oru500.oilTtVshl12C
            tn1500Cascade1A = autoSave.oru500.tn1500Cascade1A
            tn1500Cascade1B = autoSave.oru500.tn1500Cascade1B
            tn1500Cascade1C = autoSave.oru500.tn1500Cascade1C
            tn1500Cascade2A = autoSave.oru500.tn1500Cascade2A
            tn1500Cascade2B = autoSave.oru500.tn1500Cascade2B
            tn1500Cascade2C = autoSave.oru500.tn1500Cascade2C
            tn1500Cascade3A = autoSave.oru500.tn1500Cascade3A
            tn1500Cascade3B = autoSave.oru500.tn1500Cascade3B
            tn1500Cascade3C = autoSave.oru500.tn1500Cascade3C
            tn1500Cascade4A = autoSave.oru500.tn1500Cascade4A
            tn1500Cascade4B = autoSave.oru500.tn1500Cascade4B
            tn1500Cascade4C = autoSave.oru500.tn1500Cascade4C
            tn2500Cascade1A = autoSave.oru500.tn2500Cascade1A
            tn2500Cascade1B = autoSave.oru500.tn2500Cascade1B
            tn2500Cascade1C = autoSave.oru500.tn2500Cascade1C
            tn2500Cascade2A = autoSave.oru500.tn2500Cascade2A
            tn2500Cascade2B = autoSave.oru500.tn2500Cascade2B
            tn2500Cascade2C = autoSave.oru500.tn2500Cascade2C
            tn2500Cascade3A = autoSave.oru500.tn2500Cascade3A
            tn2500Cascade3B = autoSave.oru500.tn2500Cascade3B
            tn2500Cascade3C = autoSave.oru500.tn2500Cascade3C
            tn2500Cascade4A = autoSave.oru500.tn2500Cascade4A
            tn2500Cascade4B = autoSave.oru500.tn2500Cascade4B
            tn2500Cascade4C = autoSave.oru500.tn2500Cascade4C
            tn500Sgres1Cascade1A = autoSave.oru500.tn500Sgres1Cascade1A
            tn500Sgres1Cascade1B = autoSave.oru500.tn500Sgres1Cascade1B
            tn500Sgres1Cascade1C = autoSave.oru500.tn500Sgres1Cascade1C
            tn500Sgres1Cascade2A = autoSave.oru500.tn500Sgres1Cascade2A
            tn500Sgres1Cascade2B = autoSave.oru500.tn500Sgres1Cascade2B
            tn500Sgres1Cascade2C = autoSave.oru500.tn500Sgres1Cascade2C
            tn500Sgres1Cascade3A = autoSave.oru500.tn500Sgres1Cascade3A
            tn500Sgres1Cascade3B = autoSave.oru500.tn500Sgres1Cascade3B
            tn500Sgres1Cascade3C = autoSave.oru500.tn500Sgres1Cascade3C
            tn500Sgres1Cascade4A = autoSave.oru500.tn500Sgres1Cascade4A
            tn500Sgres1Cascade4B = autoSave.oru500.tn500Sgres1Cascade4B
            tn500Sgres1Cascade4C = autoSave.oru500.tn500Sgres1Cascade4C
            oilTtTrachukovskayaA = autoSave.oru500.oilTtTrachukovskayaA
            oilTtTrachukovskayaB = autoSave.oru500.oilTtTrachukovskayaB
            oilTtTrachukovskayaC = autoSave.oru500.oilTtTrachukovskayaC
            oil2tnTrachukovskayaA = autoSave.oru500.oil2tnTrachukovskayaA
            oil2tnTrachukovskayaB = autoSave.oru500.oil2tnTrachukovskayaB
            oil2tnTrachukovskayaC = autoSave.oru500.oil2tnTrachukovskayaC
            oil1tnTrachukovskayaA = autoSave.oru500.oil1tnTrachukovskayaA
            oil1tnTrachukovskayaB = autoSave.oru500.oil1tnTrachukovskayaB
            oil1tnTrachukovskayaC = autoSave.oru500.oil1tnTrachukovskayaC
            oil2tnBelozernayaA = autoSave.oru500.oil2tnBelozernayaA
            oil2tnBelozernayaB = autoSave.oru500.oil2tnBelozernayaB
            oil2tnBelozernayaC = autoSave.oru500.oil2tnBelozernayaC

            // 🔒 Восстанавливаем комментарии ORU500
            commentR5002s = autoSave.oru500.commentR5002s
            commentVsht31 = autoSave.oru500.commentVsht31
            commentVlt30 = autoSave.oru500.commentVlt30
            commentVshl32 = autoSave.oru500.commentVshl32
            commentVshl21 = autoSave.oru500.commentVshl21
            commentVsht22 = autoSave.oru500.commentVsht22
            commentVlt20 = autoSave.oru500.commentVlt20
            commentVsht11 = autoSave.oru500.commentVsht11
            commentVshl12 = autoSave.oru500.commentVshl12
            commentTtVsht31 = autoSave.oru500.commentTtVsht31
            commentTtVlt30 = autoSave.oru500.commentTtVlt30
            commentTtVshl32 = autoSave.oru500.commentTtVshl32
            commentTtVshl21 = autoSave.oru500.commentTtVshl21
            commentTtVsht22 = autoSave.oru500.commentTtVsht22
            commentTtVlt20 = autoSave.oru500.commentTtVlt20
            commentTtVsht11 = autoSave.oru500.commentTtVsht11
            commentTtVshl12 = autoSave.oru500.commentTtVshl12
            commentTn1500 = autoSave.oru500.commentTn1500
            commentTn2500 = autoSave.oru500.commentTn2500
            commentTn500Sgres1 = autoSave.oru500.commentTn500Sgres1
            commentTrachukovskayaTt = autoSave.oru500.commentTrachukovskayaTt
            commentTrachukovskaya2tn = autoSave.oru500.commentTrachukovskaya2tn
            commentTrachukovskaya1tn = autoSave.oru500.commentTrachukovskaya1tn
            commentBelozernaya2tn = autoSave.oru500.commentBelozernaya2tn
        }

        // Восстанавливаем Buildings данные
        sharedViewModel.updateBuildingsData {
            compressor1Valve = autoSave.buildings.compressor1Valve
            compressor1Heating = autoSave.buildings.compressor1Heating
            compressor1Temp = autoSave.buildings.compressor1Temp
            ballroom1Valve = autoSave.buildings.ballroom1Valve
            ballroom1Heating = autoSave.buildings.ballroom1Heating
            ballroom1Temp = autoSave.buildings.ballroom1Temp
            compressor2Valve = autoSave.buildings.compressor2Valve
            compressor2Heating = autoSave.buildings.compressor2Heating
            compressor2Temp = autoSave.buildings.compressor2Temp
            ballroom2Valve = autoSave.buildings.ballroom2Valve
            ballroom2Heating = autoSave.buildings.ballroom2Heating
            ballroom2Temp = autoSave.buildings.ballroom2Temp
            kpzOpuValve = autoSave.buildings.kpzOpuValve
            kpzOpuHeating = autoSave.buildings.kpzOpuHeating
            kpzOpuTemp = autoSave.buildings.kpzOpuTemp
            kpz2Valve = autoSave.buildings.kpz2Valve
            kpz2Heating = autoSave.buildings.kpz2Heating
            kpz2Temp = autoSave.buildings.kpz2Temp
            firePumpValve = autoSave.buildings.firePumpValve
            firePumpHeating = autoSave.buildings.firePumpHeating
            firePumpTemp = autoSave.buildings.firePumpTemp
            workshopHeating = autoSave.buildings.workshopHeating
            workshopTemp = autoSave.buildings.workshopTemp
            artWellHeating = autoSave.buildings.artWellHeating
            artesianWellHeating = autoSave.buildings.artesianWellHeating
            roomAbHeating = autoSave.buildings.roomAbHeating
            roomAbTemp = autoSave.buildings.roomAbTemp
            basementHeating = autoSave.buildings.basementHeating
            basementTemp = autoSave.buildings.basementTemp

            // 🔒 Восстанавливаем комментарии Buildings
            commentCompressor1 = autoSave.buildings.commentCompressor1
            commentBallroom1 = autoSave.buildings.commentBallroom1
            commentCompressor2 = autoSave.buildings.commentCompressor2
            commentBallroom2 = autoSave.buildings.commentBallroom2
            commentKpzOpu = autoSave.buildings.commentKpzOpu
            commentKpz2 = autoSave.buildings.commentKpz2
            commentFirePump = autoSave.buildings.commentFirePump
            commentWorkshop = autoSave.buildings.commentWorkshop
            commentArtWell = autoSave.buildings.commentArtWell
            commentArtesianWell = autoSave.buildings.commentArtesianWell
            commentRoomAb = autoSave.buildings.commentRoomAb
            commentBasement = autoSave.buildings.commentBasement

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}