package com.example.ps_inspection.ui.fragments.inspections

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.R
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentInspectionAtgBinding
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionATG : Fragment() {

    private var _binding: FragmentInspectionAtgBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    private var isUpdatingUIFromViewModel = false

    private lateinit var mediaManager: InspectionMediaManager

    // Маппинг для кнопок комментариев
    private val commentButtons = mutableMapOf<ImageButton, String>()
    // Маппинг для кнопок фото
    private val mediaButtons = mutableMapOf<ImageButton, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionAtgBinding.inflate(inflater, container, false)
        mediaManager = InspectionMediaManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.atgData.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.atgComments.collectLatest { comments ->
                updateCommentButtonsState(comments)
            }
        }

        setupInputListeners()
        setupMediaButtons()
        updatePhotoButtonsState()
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private fun registerMediaCommentPair(mediaBtn: ImageButton?, commentBtn: ImageButton?, equipmentKey: String) {
        mediaBtn?.let {
            mediaButtons[it] = equipmentKey
            it.setOnClickListener {
                MediaDialogFragment.newInstance("current_inspection", equipmentKey)
                    .show(childFragmentManager, "media_${equipmentKey.replace(" ", "_")}")
            }
        }
        commentBtn?.let {
            commentButtons[it] = equipmentKey
            it.setOnClickListener {
                CommentsDialogFragment.newInstance(equipmentKey, "ATG")
                    .show(parentFragmentManager, "comment_${equipmentKey.replace(" ", "_")}")
            }
        }
    }

    private fun loadComment(button: ImageButton, commentText: String, equipmentKey: String) {
        button.tag = equipmentKey
        commentButtons[button] = equipmentKey
        button.setOnClickListener {
            CommentsDialogFragment.newInstance(equipmentKey, "ATG")
                .show(parentFragmentManager, "comment_${equipmentKey.replace(" ", "_")}")
        }
    }

    // ========== ОБНОВЛЕНИЕ СОСТОЯНИЙ ==========

    private fun updatePhotoButtonsState() {
        val inspectionId = "current_inspection"
        val photoButtons = mapOf(
            binding.btnMediaAtg2C to "2 АТГ ф.С",
            binding.btnMediaAtg2B to "2 АТГ ф.В",
            binding.btnMediaAtg2A to "2 АТГ ф.А",
            binding.btnMediaAtgReserve to "АТГ резервная",
            binding.btnMediaAtg3C to "3 АТГ ф.С",
            binding.btnMediaAtg3B to "3 АТГ ф.В",
            binding.btnMediaAtg3A to "3 АТГ ф.А",
            binding.btnMediaReactorC to "Реактор ф.С",
            binding.btnMediaReactorB to "Реактор ф.В",
            binding.btnMediaReactorA to "Реактор ф.А",
            binding.btnMediaTn35 to "ТН-35"
        )

        photoButtons.forEach { (button, name) ->
            val hasPhotos = mediaManager.hasPhotos(inspectionId, name)
            val color = if (hasPhotos) {
                ContextCompat.getColor(requireContext(), R.color.green)
            } else {
                ContextCompat.getColor(requireContext(), R.color.gray)
            }
            button.setColorFilter(color)
        }
    }

    fun updateCommentButtonsState(comments: Map<String, List<String>>) {
        val buttonKeys = mapOf(
            binding.btnCommentAtg2C to "2 АТГ ф.С",
            binding.btnCommentAtg2B to "2 АТГ ф.В",
            binding.btnCommentAtg2A to "2 АТГ ф.А",
            binding.btnCommentAtgReserve to "АТГ резервная",
            binding.btnCommentAtg3C to "3 АТГ ф.С",
            binding.btnCommentAtg3B to "3 АТГ ф.В",
            binding.btnCommentAtg3A to "3 АТГ ф.А",
            binding.btnCommentReactorC to "Реактор ф.С",
            binding.btnCommentReactorB to "Реактор ф.В",
            binding.btnCommentReactorA to "Реактор ф.А",
            binding.btnCommentTn35 to "ТН-35",
        )

        buttonKeys.forEach { (button, key) ->
            val hasComment = comments[key].isNullOrEmpty().not()
            val color = if (hasComment) {
                Color.parseColor("#4CAF50")
            } else {
                Color.parseColor("#9E9E9E")
            }
            button.setColorFilter(color)
        }
    }

    fun refreshPhotoButtonsState() {
        updatePhotoButtonsState()
    }

    private fun setupMediaButtons() {
        // Регистрируем все пары (фото + комментарий) для существующих секций
        registerMediaCommentPair(binding.btnMediaAtg2C, binding.btnCommentAtg2C, "2 АТГ ф.С")
        registerMediaCommentPair(binding.btnMediaAtg2B, binding.btnCommentAtg2B, "2 АТГ ф.В")
        registerMediaCommentPair(binding.btnMediaAtg2A, binding.btnCommentAtg2A, "2 АТГ ф.А")
        registerMediaCommentPair(binding.btnMediaAtgReserve, binding.btnCommentAtgReserve, "АТГ резервная")
        registerMediaCommentPair(binding.btnMediaAtg3C, binding.btnCommentAtg3C, "3 АТГ ф.С")
        registerMediaCommentPair(binding.btnMediaAtg3B, binding.btnCommentAtg3B, "3 АТГ ф.В")
        registerMediaCommentPair(binding.btnMediaAtg3A, binding.btnCommentAtg3A, "3 АТГ ф.А")
        registerMediaCommentPair(binding.btnMediaReactorC, binding.btnCommentReactorC, "Реактор ф.С")
        registerMediaCommentPair(binding.btnMediaReactorB, binding.btnCommentReactorB, "Реактор ф.В")
        registerMediaCommentPair(binding.btnMediaReactorA, binding.btnCommentReactorA, "Реактор ф.А")

        // ТН-35
        registerMediaCommentPair(binding.btnMediaTn35, binding.btnCommentTn35, "ТН-35")
    }

    // ========== ОБНОВЛЕНИЕ UI ИЗ DATA ==========

    private fun updateUIFromData(data: InspectionATGData) {
        isUpdatingUIFromViewModel = true

        // 2 АТГ ф.С
        updateEditTextIfNeeded(binding.atg2COilTank, data.atg2_c_oil_tank)
        updateEditTextIfNeeded(binding.atg2COilRpn, data.atg2_c_oil_rpn)
        updateEditTextIfNeeded(binding.atg2CPressure500, data.atg2_c_pressure_500)
        updateEditTextIfNeeded(binding.atg2CPressure220, data.atg2_c_pressure_220)
        updateEditTextIfNeeded(binding.atg2CTempTs1, data.atg2_c_temp_ts1)
        updateEditTextIfNeeded(binding.atg2CTempTs2, data.atg2_c_temp_ts2)
        updateEditTextIfNeeded(binding.atg2CPumpGroup1, data.atg2_c_pump_group1)
        updateEditTextIfNeeded(binding.atg2CPumpGroup2, data.atg2_c_pump_group2)
        updateEditTextIfNeeded(binding.atg2CPumpGroup3, data.atg2_c_pump_group3)
        updateEditTextIfNeeded(binding.atg2CPumpGroup4, data.atg2_c_pump_group4)

        // 2 АТГ ф.В
        updateEditTextIfNeeded(binding.atg2BOilTank, data.atg2_b_oil_tank)
        updateEditTextIfNeeded(binding.atg2BOilRpn, data.atg2_b_oil_rpn)
        updateEditTextIfNeeded(binding.atg2BPressure500, data.atg2_b_pressure_500)
        updateEditTextIfNeeded(binding.atg2BPressure220, data.atg2_b_pressure_220)
        updateEditTextIfNeeded(binding.atg2BTempTs1, data.atg2_b_temp_ts1)
        updateEditTextIfNeeded(binding.atg2BTempTs2, data.atg2_b_temp_ts2)
        updateEditTextIfNeeded(binding.atg2BPumpGroup1, data.atg2_b_pump_group1)
        updateEditTextIfNeeded(binding.atg2BPumpGroup2, data.atg2_b_pump_group2)
        updateEditTextIfNeeded(binding.atg2BPumpGroup3, data.atg2_b_pump_group3)
        updateEditTextIfNeeded(binding.atg2BPumpGroup4, data.atg2_b_pump_group4)

        // 2 АТГ ф.А
        updateEditTextIfNeeded(binding.atg2AOilTank, data.atg2_a_oil_tank)
        updateEditTextIfNeeded(binding.atg2AOilRpn, data.atg2_a_oil_rpn)
        updateEditTextIfNeeded(binding.atg2APressure500, data.atg2_a_pressure_500)
        updateEditTextIfNeeded(binding.atg2APressure220, data.atg2_a_pressure_220)
        updateEditTextIfNeeded(binding.atg2ATempTs1, data.atg2_a_temp_ts1)
        updateEditTextIfNeeded(binding.atg2ATempTs2, data.atg2_a_temp_ts2)
        updateEditTextIfNeeded(binding.atg2APumpGroup1, data.atg2_a_pump_group1)
        updateEditTextIfNeeded(binding.atg2APumpGroup2, data.atg2_a_pump_group2)
        updateEditTextIfNeeded(binding.atg2APumpGroup3, data.atg2_a_pump_group3)
        updateEditTextIfNeeded(binding.atg2APumpGroup4, data.atg2_a_pump_group4)

        // АТГ резервная фаза
        updateEditTextIfNeeded(binding.atgReserveOilTank, data.atg_reserve_oil_tank)
        updateEditTextIfNeeded(binding.atgReserveOilRpn, data.atg_reserve_oil_rpn)
        updateEditTextIfNeeded(binding.atgReservePressure500, data.atg_reserve_pressure_500)
        updateEditTextIfNeeded(binding.atgReservePressure220, data.atg_reserve_pressure_220)
        updateEditTextIfNeeded(binding.atgReserveTempTs1, data.atg_reserve_temp_ts1)
        updateEditTextIfNeeded(binding.atgReserveTempTs2, data.atg_reserve_temp_ts2)
        updateEditTextIfNeeded(binding.atgReservePumpGroup1, data.atg_reserve_pump_group1)
        updateEditTextIfNeeded(binding.atgReservePumpGroup2, data.atg_reserve_pump_group2)
        updateEditTextIfNeeded(binding.atgReservePumpGroup3, data.atg_reserve_pump_group3)
        updateEditTextIfNeeded(binding.atgReservePumpGroup4, data.atg_reserve_pump_group4)

        // ТН-35
        updateEditTextIfNeeded(binding.etTn352atg, data.tn352atg)
        updateEditTextIfNeeded(binding.etTn353atg, data.tn353atg)
        // Комментарий для ТН-35 уже загрузится через подписку, но кнопка должна быть зарегистрирована

        // 3 АТГ ф.С
        updateEditTextIfNeeded(binding.atg3COilTank, data.atg3_c_oil_tank)
        updateEditTextIfNeeded(binding.atg3COilRpn, data.atg3_c_oil_rpn)
        updateEditTextIfNeeded(binding.atg3CPressure500, data.atg3_c_pressure_500)
        updateEditTextIfNeeded(binding.atg3CPressure220, data.atg3_c_pressure_220)
        updateEditTextIfNeeded(binding.atg3CTempTs1, data.atg3_c_temp_ts1)
        updateEditTextIfNeeded(binding.atg3CTempTs2, data.atg3_c_temp_ts2)
        updateEditTextIfNeeded(binding.atg3CPumpGroup1, data.atg3_c_pump_group1)
        updateEditTextIfNeeded(binding.atg3CPumpGroup2, data.atg3_c_pump_group2)
        updateEditTextIfNeeded(binding.atg3CPumpGroup3, data.atg3_c_pump_group3)
        updateEditTextIfNeeded(binding.atg3CPumpGroup4, data.atg3_c_pump_group4)

        // 3 АТГ ф.В
        updateEditTextIfNeeded(binding.atg3BOilTank, data.atg3_b_oil_tank)
        updateEditTextIfNeeded(binding.atg3BOilRpn, data.atg3_b_oil_rpn)
        updateEditTextIfNeeded(binding.atg3BPressure500, data.atg3_b_pressure_500)
        updateSpinnerIfNeeded(binding.atg3BPressure220, data.atg3_b_pressure_220)
        updateEditTextIfNeeded(binding.atg3BTempTs1, data.atg3_b_temp_ts1)
        updateEditTextIfNeeded(binding.atg3BTempTs2, data.atg3_b_temp_ts2)
        updateEditTextIfNeeded(binding.atg3BPumpGroup1, data.atg3_b_pump_group1)
        updateEditTextIfNeeded(binding.atg3BPumpGroup2, data.atg3_b_pump_group2)
        updateEditTextIfNeeded(binding.atg3BPumpGroup3, data.atg3_b_pump_group3)
        updateEditTextIfNeeded(binding.atg3BPumpGroup4, data.atg3_b_pump_group4)

        // 3 АТГ ф.А
        updateEditTextIfNeeded(binding.atg3AOilTank, data.atg3_a_oil_tank)
        updateEditTextIfNeeded(binding.atg3AOilRpn, data.atg3_a_oil_rpn)
        updateEditTextIfNeeded(binding.atg3APressure500, data.atg3_a_pressure_500)
        updateEditTextIfNeeded(binding.atg3APressure220, data.atg3_a_pressure_220)
        updateEditTextIfNeeded(binding.atg3ATempTs1, data.atg3_a_temp_ts1)
        updateEditTextIfNeeded(binding.atg3ATempTs2, data.atg3_a_temp_ts2)
        updateEditTextIfNeeded(binding.atg3APumpGroup1, data.atg3_a_pump_group1)
        updateEditTextIfNeeded(binding.atg3APumpGroup2, data.atg3_a_pump_group2)
        updateEditTextIfNeeded(binding.atg3APumpGroup3, data.atg3_a_pump_group3)
        updateEditTextIfNeeded(binding.atg3APumpGroup4, data.atg3_a_pump_group4)

        // Реакторы
        updateEditTextIfNeeded(binding.reactorCOilTank, data.reactor_c_oil_tank)
        updateEditTextIfNeeded(binding.reactorCPressure500, data.reactor_c_pressure_500)
        updateEditTextIfNeeded(binding.reactorCTempTs, data.reactor_c_temp_ts)
        updateEditTextIfNeeded(binding.reactorCPumpGroup1, data.reactor_c_pump_group1)
        updateEditTextIfNeeded(binding.reactorCPumpGroup2, data.reactor_c_pump_group2)
        updateEditTextIfNeeded(binding.reactorCPumpGroup3, data.reactor_c_pump_group3)
        updateSpinnerIfNeeded(binding.reactorCTtNeutral, data.reactor_c_tt_neutral)

        updateEditTextIfNeeded(binding.reactorBOilTank, data.reactor_b_oil_tank)
        updateEditTextIfNeeded(binding.reactorBPressure500, data.reactor_b_pressure_500)
        updateEditTextIfNeeded(binding.reactorBTempTs, data.reactor_b_temp_ts)
        updateEditTextIfNeeded(binding.reactorBPumpGroup1, data.reactor_b_pump_group1)
        updateEditTextIfNeeded(binding.reactorBPumpGroup2, data.reactor_b_pump_group2)
        updateEditTextIfNeeded(binding.reactorBPumpGroup3, data.reactor_b_pump_group3)
        updateSpinnerIfNeeded(binding.reactorBTtNeutral, data.reactor_b_tt_neutral)

        updateEditTextIfNeeded(binding.reactorAOilTank, data.reactor_a_oil_tank)
        updateEditTextIfNeeded(binding.reactorAPressure500, data.reactor_a_pressure_500)
        updateEditTextIfNeeded(binding.reactorATempTs, data.reactor_a_temp_ts)
        updateEditTextIfNeeded(binding.reactorAPumpGroup1, data.reactor_a_pump_group1)
        updateEditTextIfNeeded(binding.reactorAPumpGroup2, data.reactor_a_pump_group2)
        updateEditTextIfNeeded(binding.reactorAPumpGroup3, data.reactor_a_pump_group3)
        updateSpinnerIfNeeded(binding.reactorATtNeutral, data.reactor_a_tt_neutral)

        isUpdatingUIFromViewModel = false
    }

    private fun updateEditTextIfNeeded(editText: EditText, newValue: String) {
        val currentText = editText.text.toString()
        if (currentText != newValue) {
            editText.setText(newValue)
            editText.setSelection(editText.text.length)
        }
    }

    private fun updateSpinnerIfNeeded(spinner: Spinner, value: String?) {
        if (!value.isNullOrEmpty()) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    if (spinner.selectedItemPosition != i) {
                        spinner.setSelection(i)
                    }
                    break
                }
            }
        }
    }

    // ========== НАСТРОЙКА СЛУШАТЕЛЕЙ ==========

    private fun setupInputListeners() {
        // 2 АТГ ф.С
        setupEditTextListener(binding.atg2COilTank) { text ->
            sharedViewModel.updateATGData { atg2_c_oil_tank = text }
        }
        setupEditTextListener(binding.atg2COilRpn) { text ->
            sharedViewModel.updateATGData { atg2_c_oil_rpn = text }
        }
        setupEditTextListener(binding.atg2CPressure500) { text ->
            sharedViewModel.updateATGData { atg2_c_pressure_500 = text }
        }
        setupEditTextListener(binding.atg2CPressure220) { text ->
            sharedViewModel.updateATGData { atg2_c_pressure_220 = text }
        }
        setupEditTextListener(binding.atg2CTempTs1) { text ->
            sharedViewModel.updateATGData { atg2_c_temp_ts1 = text }
        }
        setupEditTextListener(binding.atg2CTempTs2) { text ->
            sharedViewModel.updateATGData { atg2_c_temp_ts2 = text }
        }
        setupEditTextListener(binding.atg2CPumpGroup1) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group1 = text }
        }
        setupEditTextListener(binding.atg2CPumpGroup2) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group2 = text }
        }
        setupEditTextListener(binding.atg2CPumpGroup3) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group3 = text }
        }
        setupEditTextListener(binding.atg2CPumpGroup4) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group4 = text }
        }

        // 2 АТГ ф.В
        setupEditTextListener(binding.atg2BOilTank) { text ->
            sharedViewModel.updateATGData { atg2_b_oil_tank = text }
        }
        setupEditTextListener(binding.atg2BOilRpn) { text ->
            sharedViewModel.updateATGData { atg2_b_oil_rpn = text }
        }
        setupEditTextListener(binding.atg2BPressure500) { text ->
            sharedViewModel.updateATGData { atg2_b_pressure_500 = text }
        }
        setupEditTextListener(binding.atg2BPressure220) { text ->
            sharedViewModel.updateATGData { atg2_b_pressure_220 = text }
        }
        setupEditTextListener(binding.atg2BTempTs1) { text ->
            sharedViewModel.updateATGData { atg2_b_temp_ts1 = text }
        }
        setupEditTextListener(binding.atg2BTempTs2) { text ->
            sharedViewModel.updateATGData { atg2_b_temp_ts2 = text }
        }
        setupEditTextListener(binding.atg2BPumpGroup1) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group1 = text }
        }
        setupEditTextListener(binding.atg2BPumpGroup2) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group2 = text }
        }
        setupEditTextListener(binding.atg2BPumpGroup3) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group3 = text }
        }
        setupEditTextListener(binding.atg2BPumpGroup4) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group4 = text }
        }

        // 2 АТГ ф.А
        setupEditTextListener(binding.atg2AOilTank) { text ->
            sharedViewModel.updateATGData { atg2_a_oil_tank = text }
        }
        setupEditTextListener(binding.atg2AOilRpn) { text ->
            sharedViewModel.updateATGData { atg2_a_oil_rpn = text }
        }
        setupEditTextListener(binding.atg2APressure500) { text ->
            sharedViewModel.updateATGData { atg2_a_pressure_500 = text }
        }
        setupEditTextListener(binding.atg2APressure220) { text ->
            sharedViewModel.updateATGData { atg2_a_pressure_220 = text }
        }
        setupEditTextListener(binding.atg2ATempTs1) { text ->
            sharedViewModel.updateATGData { atg2_a_temp_ts1 = text }
        }
        setupEditTextListener(binding.atg2ATempTs2) { text ->
            sharedViewModel.updateATGData { atg2_a_temp_ts2 = text }
        }
        setupEditTextListener(binding.atg2APumpGroup1) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group1 = text }
        }
        setupEditTextListener(binding.atg2APumpGroup2) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group2 = text }
        }
        setupEditTextListener(binding.atg2APumpGroup3) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group3 = text }
        }
        setupEditTextListener(binding.atg2APumpGroup4) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group4 = text }
        }

        // АТГ резервная
        setupEditTextListener(binding.atgReserveOilTank) { text ->
            sharedViewModel.updateATGData { atg_reserve_oil_tank = text }
        }
        setupEditTextListener(binding.atgReserveOilRpn) { text ->
            sharedViewModel.updateATGData { atg_reserve_oil_rpn = text }
        }
        setupEditTextListener(binding.atgReservePressure500) { text ->
            sharedViewModel.updateATGData { atg_reserve_pressure_500 = text }
        }
        setupEditTextListener(binding.atgReservePressure220) { text ->
            sharedViewModel.updateATGData { atg_reserve_pressure_220 = text }
        }
        setupEditTextListener(binding.atgReserveTempTs1) { text ->
            sharedViewModel.updateATGData { atg_reserve_temp_ts1 = text }
        }
        setupEditTextListener(binding.atgReserveTempTs2) { text ->
            sharedViewModel.updateATGData { atg_reserve_temp_ts2 = text }
        }
        setupEditTextListener(binding.atgReservePumpGroup1) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group1 = text }
        }
        setupEditTextListener(binding.atgReservePumpGroup2) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group2 = text }
        }
        setupEditTextListener(binding.atgReservePumpGroup3) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group3 = text }
        }
        setupEditTextListener(binding.atgReservePumpGroup4) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group4 = text }
        }

        // ТН-35
        setupEditTextListener(binding.etTn352atg) { text ->
            sharedViewModel.updateATGData { tn352atg = text }
        }
        setupEditTextListener(binding.etTn353atg) { text ->
            sharedViewModel.updateATGData { tn353atg = text }
        }

        // 3 АТГ ф.С
        setupEditTextListener(binding.atg3COilTank) { text ->
            sharedViewModel.updateATGData { atg3_c_oil_tank = text }
        }
        setupEditTextListener(binding.atg3COilRpn) { text ->
            sharedViewModel.updateATGData { atg3_c_oil_rpn = text }
        }
        setupEditTextListener(binding.atg3CPressure500) { text ->
            sharedViewModel.updateATGData { atg3_c_pressure_500 = text }
        }
        setupEditTextListener(binding.atg3CPressure220) { text ->
            sharedViewModel.updateATGData { atg3_c_pressure_220 = text }
        }
        setupEditTextListener(binding.atg3CTempTs1) { text ->
            sharedViewModel.updateATGData { atg3_c_temp_ts1 = text }
        }
        setupEditTextListener(binding.atg3CTempTs2) { text ->
            sharedViewModel.updateATGData { atg3_c_temp_ts2 = text }
        }
        setupEditTextListener(binding.atg3CPumpGroup1) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group1 = text }
        }
        setupEditTextListener(binding.atg3CPumpGroup2) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group2 = text }
        }
        setupEditTextListener(binding.atg3CPumpGroup3) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group3 = text }
        }
        setupEditTextListener(binding.atg3CPumpGroup4) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group4 = text }
        }

        // 3 АТГ ф.В
        setupEditTextListener(binding.atg3BOilTank) { text ->
            sharedViewModel.updateATGData { atg3_b_oil_tank = text }
        }
        setupEditTextListener(binding.atg3BOilRpn) { text ->
            sharedViewModel.updateATGData { atg3_b_oil_rpn = text }
        }
        setupEditTextListener(binding.atg3BPressure500) { text ->
            sharedViewModel.updateATGData { atg3_b_pressure_500 = text }
        }
        setupSpinnerListener(binding.atg3BPressure220) { text ->
            sharedViewModel.updateATGData { atg3_b_pressure_220 = text }
        }
        setupEditTextListener(binding.atg3BTempTs1) { text ->
            sharedViewModel.updateATGData { atg3_b_temp_ts1 = text }
        }
        setupEditTextListener(binding.atg3BTempTs2) { text ->
            sharedViewModel.updateATGData { atg3_b_temp_ts2 = text }
        }
        setupEditTextListener(binding.atg3BPumpGroup1) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group1 = text }
        }
        setupEditTextListener(binding.atg3BPumpGroup2) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group2 = text }
        }
        setupEditTextListener(binding.atg3BPumpGroup3) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group3 = text }
        }
        setupEditTextListener(binding.atg3BPumpGroup4) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group4 = text }
        }

        // 3 АТГ ф.А
        setupEditTextListener(binding.atg3AOilTank) { text ->
            sharedViewModel.updateATGData { atg3_a_oil_tank = text }
        }
        setupEditTextListener(binding.atg3AOilRpn) { text ->
            sharedViewModel.updateATGData { atg3_a_oil_rpn = text }
        }
        setupEditTextListener(binding.atg3APressure500) { text ->
            sharedViewModel.updateATGData { atg3_a_pressure_500 = text }
        }
        setupEditTextListener(binding.atg3APressure220) { text ->
            sharedViewModel.updateATGData { atg3_a_pressure_220 = text }
        }
        setupEditTextListener(binding.atg3ATempTs1) { text ->
            sharedViewModel.updateATGData { atg3_a_temp_ts1 = text }
        }
        setupEditTextListener(binding.atg3ATempTs2) { text ->
            sharedViewModel.updateATGData { atg3_a_temp_ts2 = text }
        }
        setupEditTextListener(binding.atg3APumpGroup1) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group1 = text }
        }
        setupEditTextListener(binding.atg3APumpGroup2) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group2 = text }
        }
        setupEditTextListener(binding.atg3APumpGroup3) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group3 = text }
        }
        setupEditTextListener(binding.atg3APumpGroup4) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group4 = text }
        }

        // Реакторы
        setupEditTextListener(binding.reactorCOilTank) { text ->
            sharedViewModel.updateATGData { reactor_c_oil_tank = text }
        }
        setupEditTextListener(binding.reactorCPressure500) { text ->
            sharedViewModel.updateATGData { reactor_c_pressure_500 = text }
        }
        setupEditTextListener(binding.reactorCTempTs) { text ->
            sharedViewModel.updateATGData { reactor_c_temp_ts = text }
        }
        setupEditTextListener(binding.reactorCPumpGroup1) { text ->
            sharedViewModel.updateATGData { reactor_c_pump_group1 = text }
        }
        setupEditTextListener(binding.reactorCPumpGroup2) { text ->
            sharedViewModel.updateATGData { reactor_c_pump_group2 = text }
        }
        setupEditTextListener(binding.reactorCPumpGroup3) { text ->
            sharedViewModel.updateATGData { reactor_c_pump_group3 = text }
        }
        setupSpinnerListener(binding.reactorCTtNeutral) { text ->
            sharedViewModel.updateATGData { reactor_c_tt_neutral = text }
        }

        setupEditTextListener(binding.reactorBOilTank) { text ->
            sharedViewModel.updateATGData { reactor_b_oil_tank = text }
        }
        setupEditTextListener(binding.reactorBPressure500) { text ->
            sharedViewModel.updateATGData { reactor_b_pressure_500 = text }
        }
        setupEditTextListener(binding.reactorBTempTs) { text ->
            sharedViewModel.updateATGData { reactor_b_temp_ts = text }
        }
        setupEditTextListener(binding.reactorBPumpGroup1) { text ->
            sharedViewModel.updateATGData { reactor_b_pump_group1 = text }
        }
        setupEditTextListener(binding.reactorBPumpGroup2) { text ->
            sharedViewModel.updateATGData { reactor_b_pump_group2 = text }
        }
        setupEditTextListener(binding.reactorBPumpGroup3) { text ->
            sharedViewModel.updateATGData { reactor_b_pump_group3 = text }
        }
        setupSpinnerListener(binding.reactorBTtNeutral) { text ->
            sharedViewModel.updateATGData { reactor_b_tt_neutral = text }
        }

        setupEditTextListener(binding.reactorAOilTank) { text ->
            sharedViewModel.updateATGData { reactor_a_oil_tank = text }
        }
        setupEditTextListener(binding.reactorAPressure500) { text ->
            sharedViewModel.updateATGData { reactor_a_pressure_500 = text }
        }
        setupEditTextListener(binding.reactorATempTs) { text ->
            sharedViewModel.updateATGData { reactor_a_temp_ts = text }
        }
        setupEditTextListener(binding.reactorAPumpGroup1) { text ->
            sharedViewModel.updateATGData { reactor_a_pump_group1 = text }
        }
        setupEditTextListener(binding.reactorAPumpGroup2) { text ->
            sharedViewModel.updateATGData { reactor_a_pump_group2 = text }
        }
        setupEditTextListener(binding.reactorAPumpGroup3) { text ->
            sharedViewModel.updateATGData { reactor_a_pump_group3 = text }
        }
        setupSpinnerListener(binding.reactorATtNeutral) { text ->
            sharedViewModel.updateATGData { reactor_a_tt_neutral = text }
        }
    }

    private fun setupEditTextListener(editText: EditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                val newText = s?.toString() ?: ""
                onTextChanged(newText)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSpinnerListener(spinner: Spinner, onItemSelected: (String) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0 && !isUpdatingUIFromViewModel) {
                    val text = parent.getItemAtPosition(position).toString()
                    onItemSelected(text)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = InspectionATG()
    }
}