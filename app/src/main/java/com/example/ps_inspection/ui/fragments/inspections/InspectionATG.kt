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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.R
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.data.models.InspectionATGData
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentInspectionAtgBinding
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Context
import android.widget.TextView
import com.example.ps_inspection.data.utils.InputValidator

class InspectionATG : Fragment() {

    private var currentToast: Toast? = null
    private lateinit var layoutInflater: LayoutInflater

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
        layoutInflater = inflater
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

    private fun updatePhotoButtonsState() {
        val inspectionId = "current_inspection"

        val photoButtons = mapOf(
            // Основные секции
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
            binding.btnMediaTn35 to "ТН-35",

            // Манометры 2 АТГ ф.С
            binding.btnMediaAtg2CGroup1 to "2 АТГ ф.С Группа 1",
            binding.btnMediaAtg2CGroup2 to "2 АТГ ф.С Группа 2",
            binding.btnMediaAtg2CGroup3 to "2 АТГ ф.С Группа 3",
            binding.btnMediaAtg2CGroup4 to "2 АТГ ф.С Группа 4",

            // Манометры 2 АТГ ф.В
            binding.btnMediaAtg2BGroup1 to "2 АТГ ф.В Группа 1",
            binding.btnMediaAtg2BGroup2 to "2 АТГ ф.В Группа 2",
            binding.btnMediaAtg2BGroup3 to "2 АТГ ф.В Группа 3",
            binding.btnMediaAtg2BGroup4 to "2 АТГ ф.В Группа 4",

            // Манометры 2 АТГ ф.А
            binding.btnMediaAtg2AGroup1 to "2 АТГ ф.А Группа 1",
            binding.btnMediaAtg2AGroup2 to "2 АТГ ф.А Группа 2",
            binding.btnMediaAtg2AGroup3 to "2 АТГ ф.А Группа 3",
            binding.btnMediaAtg2AGroup4 to "2 АТГ ф.А Группа 4",

            // Манометры АТГ резервная
            binding.btnMediaAtgReserveGroup1 to "АТГ резервная Группа 1",
            binding.btnMediaAtgReserveGroup2 to "АТГ резервная Группа 2",
            binding.btnMediaAtgReserveGroup3 to "АТГ резервная Группа 3",
            binding.btnMediaAtgReserveGroup4 to "АТГ резервная Группа 4",

            // Манометры 3 АТГ ф.С
            binding.btnMediaAtg3CGroup1 to "3 АТГ ф.С Группа 1",
            binding.btnMediaAtg3CGroup2 to "3 АТГ ф.С Группа 2",
            binding.btnMediaAtg3CGroup3 to "3 АТГ ф.С Группа 3",
            binding.btnMediaAtg3CGroup4 to "3 АТГ ф.С Группа 4",

            // Манометры 3 АТГ ф.В
            binding.btnMediaAtg3BGroup1 to "3 АТГ ф.В Группа 1",
            binding.btnMediaAtg3BGroup2 to "3 АТГ ф.В Группа 2",
            binding.btnMediaAtg3BGroup3 to "3 АТГ ф.В Группа 3",
            binding.btnMediaAtg3BGroup4 to "3 АТГ ф.В Группа 4",

            // Манометры 3 АТГ ф.А
            binding.btnMediaAtg3AGroup1 to "3 АТГ ф.А Группа 1",
            binding.btnMediaAtg3AGroup2 to "3 АТГ ф.А Группа 2",
            binding.btnMediaAtg3AGroup3 to "3 АТГ ф.А Группа 3",
            binding.btnMediaAtg3AGroup4 to "3 АТГ ф.А Группа 4",

            // Манометры Реактор ф.С
            binding.btnMediaReactorCGroup1 to "Реактор ф.С Группа 1",
            binding.btnMediaReactorCGroup2 to "Реактор ф.С Группа 2",
            binding.btnMediaReactorCGroup3 to "Реактор ф.С Группа 3",

            // Манометры Реактор ф.В
            binding.btnMediaReactorBGroup1 to "Реактор ф.В Группа 1",
            binding.btnMediaReactorBGroup2 to "Реактор ф.В Группа 2",
            binding.btnMediaReactorBGroup3 to "Реактор ф.В Группа 3",

            // Манометры Реактор ф.А
            binding.btnMediaReactorAGroup1 to "Реактор ф.А Группа 1",
            binding.btnMediaReactorAGroup2 to "Реактор ф.А Группа 2",
            binding.btnMediaReactorAGroup3 to "Реактор ф.А Группа 3"
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

    fun updateCommentButtonsState(comments: Map<String, List<Comment>>) {
        val buttonKeys = mapOf(
            // Основные секции
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

            // Манометры 2 АТГ ф.С
            binding.btnCommentAtg2CGroup1 to "2 АТГ ф.С Группа 1",
            binding.btnCommentAtg2CGroup2 to "2 АТГ ф.С Группа 2",
            binding.btnCommentAtg2CGroup3 to "2 АТГ ф.С Группа 3",
            binding.btnCommentAtg2CGroup4 to "2 АТГ ф.С Группа 4",

            // Манометры 2 АТГ ф.В
            binding.btnCommentAtg2BGroup1 to "2 АТГ ф.В Группа 1",
            binding.btnCommentAtg2BGroup2 to "2 АТГ ф.В Группа 2",
            binding.btnCommentAtg2BGroup3 to "2 АТГ ф.В Группа 3",
            binding.btnCommentAtg2BGroup4 to "2 АТГ ф.В Группа 4",

            // Манометры 2 АТГ ф.А
            binding.btnCommentAtg2AGroup1 to "2 АТГ ф.А Группа 1",
            binding.btnCommentAtg2AGroup2 to "2 АТГ ф.А Группа 2",
            binding.btnCommentAtg2AGroup3 to "2 АТГ ф.А Группа 3",
            binding.btnCommentAtg2AGroup4 to "2 АТГ ф.А Группа 4",

            // Манометры АТГ резервная
            binding.btnCommentAtgReserveGroup1 to "АТГ резервная Группа 1",
            binding.btnCommentAtgReserveGroup2 to "АТГ резервная Группа 2",
            binding.btnCommentAtgReserveGroup3 to "АТГ резервная Группа 3",
            binding.btnCommentAtgReserveGroup4 to "АТГ резервная Группа 4",

            // Манометры 3 АТГ ф.С
            binding.btnCommentAtg3CGroup1 to "3 АТГ ф.С Группа 1",
            binding.btnCommentAtg3CGroup2 to "3 АТГ ф.С Группа 2",
            binding.btnCommentAtg3CGroup3 to "3 АТГ ф.С Группа 3",
            binding.btnCommentAtg3CGroup4 to "3 АТГ ф.С Группа 4",

            // Манометры 3 АТГ ф.В
            binding.btnCommentAtg3BGroup1 to "3 АТГ ф.В Группа 1",
            binding.btnCommentAtg3BGroup2 to "3 АТГ ф.В Группа 2",
            binding.btnCommentAtg3BGroup3 to "3 АТГ ф.В Группа 3",
            binding.btnCommentAtg3BGroup4 to "3 АТГ ф.В Группа 4",

            // Манометры 3 АТГ ф.А
            binding.btnCommentAtg3AGroup1 to "3 АТГ ф.А Группа 1",
            binding.btnCommentAtg3AGroup2 to "3 АТГ ф.А Группа 2",
            binding.btnCommentAtg3AGroup3 to "3 АТГ ф.А Группа 3",
            binding.btnCommentAtg3AGroup4 to "3 АТГ ф.А Группа 4",

            // Манометры Реактор ф.С
            binding.btnCommentReactorCGroup1 to "Реактор ф.С Группа 1",
            binding.btnCommentReactorCGroup2 to "Реактор ф.С Группа 2",
            binding.btnCommentReactorCGroup3 to "Реактор ф.С Группа 3",

            // Манометры Реактор ф.В
            binding.btnCommentReactorBGroup1 to "Реактор ф.В Группа 1",
            binding.btnCommentReactorBGroup2 to "Реактор ф.В Группа 2",
            binding.btnCommentReactorBGroup3 to "Реактор ф.В Группа 3",

            // Манометры Реактор ф.А
            binding.btnCommentReactorAGroup1 to "Реактор ф.А Группа 1",
            binding.btnCommentReactorAGroup2 to "Реактор ф.А Группа 2",
            binding.btnCommentReactorAGroup3 to "Реактор ф.А Группа 3"
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

    private fun setupMediaButtons() {
        // Основные секции
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
        registerMediaCommentPair(binding.btnMediaTn35, binding.btnCommentTn35, "ТН-35")

        // Манометры 2 АТГ ф.С
        registerMediaCommentPair(binding.btnMediaAtg2CGroup1, binding.btnCommentAtg2CGroup1, "2 АТГ ф.С Группа 1")
        registerMediaCommentPair(binding.btnMediaAtg2CGroup2, binding.btnCommentAtg2CGroup2, "2 АТГ ф.С Группа 2")
        registerMediaCommentPair(binding.btnMediaAtg2CGroup3, binding.btnCommentAtg2CGroup3, "2 АТГ ф.С Группа 3")
        registerMediaCommentPair(binding.btnMediaAtg2CGroup4, binding.btnCommentAtg2CGroup4, "2 АТГ ф.С Группа 4")

        // Манометры 2 АТГ ф.В
        registerMediaCommentPair(binding.btnMediaAtg2BGroup1, binding.btnCommentAtg2BGroup1, "2 АТГ ф.В Группа 1")
        registerMediaCommentPair(binding.btnMediaAtg2BGroup2, binding.btnCommentAtg2BGroup2, "2 АТГ ф.В Группа 2")
        registerMediaCommentPair(binding.btnMediaAtg2BGroup3, binding.btnCommentAtg2BGroup3, "2 АТГ ф.В Группа 3")
        registerMediaCommentPair(binding.btnMediaAtg2BGroup4, binding.btnCommentAtg2BGroup4, "2 АТГ ф.В Группа 4")

        // Манометры 2 АТГ ф.А
        registerMediaCommentPair(binding.btnMediaAtg2AGroup1, binding.btnCommentAtg2AGroup1, "2 АТГ ф.А Группа 1")
        registerMediaCommentPair(binding.btnMediaAtg2AGroup2, binding.btnCommentAtg2AGroup2, "2 АТГ ф.А Группа 2")
        registerMediaCommentPair(binding.btnMediaAtg2AGroup3, binding.btnCommentAtg2AGroup3, "2 АТГ ф.А Группа 3")
        registerMediaCommentPair(binding.btnMediaAtg2AGroup4, binding.btnCommentAtg2AGroup4, "2 АТГ ф.А Группа 4")

        // Манометры АТГ резервная
        registerMediaCommentPair(binding.btnMediaAtgReserveGroup1, binding.btnCommentAtgReserveGroup1, "АТГ резервная Группа 1")
        registerMediaCommentPair(binding.btnMediaAtgReserveGroup2, binding.btnCommentAtgReserveGroup2, "АТГ резервная Группа 2")
        registerMediaCommentPair(binding.btnMediaAtgReserveGroup3, binding.btnCommentAtgReserveGroup3, "АТГ резервная Группа 3")
        registerMediaCommentPair(binding.btnMediaAtgReserveGroup4, binding.btnCommentAtgReserveGroup4, "АТГ резервная Группа 4")

        // Манометры 3 АТГ ф.С
        registerMediaCommentPair(binding.btnMediaAtg3CGroup1, binding.btnCommentAtg3CGroup1, "3 АТГ ф.С Группа 1")
        registerMediaCommentPair(binding.btnMediaAtg3CGroup2, binding.btnCommentAtg3CGroup2, "3 АТГ ф.С Группа 2")
        registerMediaCommentPair(binding.btnMediaAtg3CGroup3, binding.btnCommentAtg3CGroup3, "3 АТГ ф.С Группа 3")
        registerMediaCommentPair(binding.btnMediaAtg3CGroup4, binding.btnCommentAtg3CGroup4, "3 АТГ ф.С Группа 4")

        // Манометры 3 АТГ ф.В
        registerMediaCommentPair(binding.btnMediaAtg3BGroup1, binding.btnCommentAtg3BGroup1, "3 АТГ ф.В Группа 1")
        registerMediaCommentPair(binding.btnMediaAtg3BGroup2, binding.btnCommentAtg3BGroup2, "3 АТГ ф.В Группа 2")
        registerMediaCommentPair(binding.btnMediaAtg3BGroup3, binding.btnCommentAtg3BGroup3, "3 АТГ ф.В Группа 3")
        registerMediaCommentPair(binding.btnMediaAtg3BGroup4, binding.btnCommentAtg3BGroup4, "3 АТГ ф.В Группа 4")

        // Манометры 3 АТГ ф.А
        registerMediaCommentPair(binding.btnMediaAtg3AGroup1, binding.btnCommentAtg3AGroup1, "3 АТГ ф.А Группа 1")
        registerMediaCommentPair(binding.btnMediaAtg3AGroup2, binding.btnCommentAtg3AGroup2, "3 АТГ ф.А Группа 2")
        registerMediaCommentPair(binding.btnMediaAtg3AGroup3, binding.btnCommentAtg3AGroup3, "3 АТГ ф.А Группа 3")
        registerMediaCommentPair(binding.btnMediaAtg3AGroup4, binding.btnCommentAtg3AGroup4, "3 АТГ ф.А Группа 4")

        // Манометры Реактор ф.С
        registerMediaCommentPair(binding.btnMediaReactorCGroup1, binding.btnCommentReactorCGroup1, "Реактор ф.С Группа 1")
        registerMediaCommentPair(binding.btnMediaReactorCGroup2, binding.btnCommentReactorCGroup2, "Реактор ф.С Группа 2")
        registerMediaCommentPair(binding.btnMediaReactorCGroup3, binding.btnCommentReactorCGroup3, "Реактор ф.С Группа 3")

        // Манометры Реактор ф.В
        registerMediaCommentPair(binding.btnMediaReactorBGroup1, binding.btnCommentReactorBGroup1, "Реактор ф.В Группа 1")
        registerMediaCommentPair(binding.btnMediaReactorBGroup2, binding.btnCommentReactorBGroup2, "Реактор ф.В Группа 2")
        registerMediaCommentPair(binding.btnMediaReactorBGroup3, binding.btnCommentReactorBGroup3, "Реактор ф.В Группа 3")

        // Манометры Реактор ф.А
        registerMediaCommentPair(binding.btnMediaReactorAGroup1, binding.btnCommentReactorAGroup1, "Реактор ф.А Группа 1")
        registerMediaCommentPair(binding.btnMediaReactorAGroup2, binding.btnCommentReactorAGroup2, "Реактор ф.А Группа 2")
        registerMediaCommentPair(binding.btnMediaReactorAGroup3, binding.btnCommentReactorAGroup3, "Реактор ф.А Группа 3")
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
        // ===== 2 АТГ ф.С =====
        // Уровень масла (бак и РПН)
        setupEditTextListenerWithValidation(binding.atg2COilTank, "2 АТГ ф.С уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atg2COilRpn, "2 АТГ ф.С уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_oil_rpn = text }
        }

        // Давление на вводах
        setupEditTextListenerWithValidation(binding.atg2CPressure500, "2 АТГ ф.С давление 500 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_pressure_500 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2CPressure220, "2 АТГ ф.С давление 220 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_pressure_220 = text }
        }

        // Температура
        setupEditTextListenerWithValidation(binding.atg2CTempTs1, "2 АТГ ф.С температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2CTempTs2, "2 АТГ ф.С температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_temp_ts2 = text }
        }

        // Давление маслонасосов (группы 1-4) для АТГ
        setupEditTextListenerWithValidation(binding.atg2CPumpGroup1, "2 АТГ ф.С группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2CPumpGroup2, "2 АТГ ф.С группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2CPumpGroup3, "2 АТГ ф.С группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2CPumpGroup4, "2 АТГ ф.С группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_c_pump_group4 = text }
        }

        // ===== 2 АТГ ф.В ===== (аналогично)
        setupEditTextListenerWithValidation(binding.atg2BOilTank, "2 АТГ ф.В уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BOilRpn, "2 АТГ ф.В уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_oil_rpn = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BPressure500, "2 АТГ ф.В давление 500 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_pressure_500 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BPressure220, "2 АТГ ф.В давление 220 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_pressure_220 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BTempTs1, "2 АТГ ф.В температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BTempTs2, "2 АТГ ф.В температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_temp_ts2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BPumpGroup1, "2 АТГ ф.В группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BPumpGroup2, "2 АТГ ф.В группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BPumpGroup3, "2 АТГ ф.В группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2BPumpGroup4, "2 АТГ ф.В группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_b_pump_group4 = text }
        }

        // ===== 2 АТГ ф.А =====
        setupEditTextListenerWithValidation(binding.atg2AOilTank, "2 АТГ ф.А уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atg2AOilRpn, "2 АТГ ф.А уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_oil_rpn = text }
        }
        setupEditTextListenerWithValidation(binding.atg2APressure220, "2 АТГ ф.А давление 220 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_pressure_220 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2ATempTs1, "2 АТГ ф.А температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2ATempTs2, "2 АТГ ф.А температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_temp_ts2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2APumpGroup1, "2 АТГ ф.А группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2APumpGroup2, "2 АТГ ф.А группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2APumpGroup3, "2 АТГ ф.А группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atg2APumpGroup4, "2 АТГ ф.А группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg2_a_pump_group4 = text }
        }

        // ===== АТГ резервная =====
        setupEditTextListenerWithValidation(binding.atgReserveOilTank, "АТГ резервная уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atgReserveOilRpn, "АТГ резервная уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_oil_rpn = text }
        }
        setupEditTextListenerWithValidation(binding.atgReservePressure500, "АТГ резервная давление 500 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_pressure_500 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReservePressure220, "АТГ резервная давление 220 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_pressure_220 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReserveTempTs1, "АТГ резервная температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReserveTempTs2, "АТГ резервная температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_temp_ts2 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReservePumpGroup1, "АТГ резервная группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReservePumpGroup2, "АТГ резервная группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReservePumpGroup3, "АТГ резервная группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atgReservePumpGroup4, "АТГ резервная группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg_reserve_pump_group4 = text }
        }

        // ===== ТН-35 =====
        setupEditTextListenerWithValidation(binding.etTn352atg, "ТН-35 2АТГ",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { tn352atg = text }
        }
        setupEditTextListenerWithValidation(binding.etTn353atg, "ТН-35 3АТГ",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { tn353atg = text }
        }

        // ===== 3 АТГ ф.С =====
        setupEditTextListenerWithValidation(binding.atg3COilTank, "3 АТГ ф.С уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atg3COilRpn, "3 АТГ ф.С уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_oil_rpn = text }
        }
        setupEditTextListenerWithValidation(binding.atg3CTempTs1, "3 АТГ ф.С температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3CTempTs2, "3 АТГ ф.С температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_temp_ts2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3CPumpGroup1, "3 АТГ ф.С группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3CPumpGroup2, "3 АТГ ф.С группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3CPumpGroup3, "3 АТГ ф.С группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3CPumpGroup4, "3 АТГ ф.С группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_c_pump_group4 = text }
        }

        // ===== 3 АТГ ф.В =====
        setupEditTextListenerWithValidation(binding.atg3BOilTank, "3 АТГ ф.В уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BOilRpn, "3 АТГ ф.В уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_oil_rpn = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BTempTs1, "3 АТГ ф.В температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BTempTs2, "3 АТГ ф.В температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_temp_ts2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BPumpGroup1, "3 АТГ ф.В группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BPumpGroup2, "3 АТГ ф.В группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BPumpGroup3, "3 АТГ ф.В группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3BPumpGroup4, "3 АТГ ф.В группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_b_pump_group4 = text }
        }

        // ===== 3 АТГ ф.А =====
        setupEditTextListenerWithValidation(binding.atg3AOilTank, "3 АТГ ф.А уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.atg3AOilRpn, "3 АТГ ф.А уровень масла в РПН",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_oil_rpn = text }
        }
        setupEditTextListenerWithValidation(binding.atg3APressure220, "3 АТГ ф.А давление 220 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_pressure_220 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3ATempTs1, "3 АТГ ф.А температура ТS-1",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_temp_ts1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3ATempTs2, "3 АТГ ф.А температура ТS-2",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_temp_ts2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3APumpGroup1, "3 АТГ ф.А группа №1",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3APumpGroup2, "3 АТГ ф.А группа №2",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3APumpGroup3, "3 АТГ ф.А группа №3",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group3 = text }
        }
        setupEditTextListenerWithValidation(binding.atg3APumpGroup4, "3 АТГ ф.А группа №4",
            InputValidator.ATG.PUMP_PRESSURE_MIN, InputValidator.ATG.PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { atg3_a_pump_group4 = text }
        }

        // ===== Реакторы (группы 1-3, давление от 0.4 до 1.2) =====
        // Реактор ф.С
        setupEditTextListenerWithValidation(binding.reactorCOilTank, "Реактор ф.С уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { reactor_c_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.reactorCPressure500, "Реактор ф.С давление 500 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_c_pressure_500 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorCTempTs, "Реактор ф.С температура ТS",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { reactor_c_temp_ts = text }
        }
        setupEditTextListenerWithValidation(binding.reactorCPumpGroup1, "Реактор ф.С группа №1",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_c_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorCPumpGroup2, "Реактор ф.С группа №2",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_c_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorCPumpGroup3, "Реактор ф.С группа №3",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_c_pump_group3 = text }
        }

        // Реактор ф.В
        setupEditTextListenerWithValidation(binding.reactorBOilTank, "Реактор ф.В уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { reactor_b_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.reactorBPressure500, "Реактор ф.В давление 500 кВ",
            InputValidator.ATG.PRESSURE_MIN, InputValidator.ATG.PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_b_pressure_500 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorBTempTs, "Реактор ф.В температура ТS",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { reactor_b_temp_ts = text }
        }
        setupEditTextListenerWithValidation(binding.reactorBPumpGroup1, "Реактор ф.В группа №1",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_b_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorBPumpGroup2, "Реактор ф.В группа №2",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_b_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorBPumpGroup3, "Реактор ф.В группа №3",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_b_pump_group3 = text }
        }

        // Реактор ф.А
        setupEditTextListenerWithValidation(binding.reactorAOilTank, "Реактор ф.А уровень масла в баке",
            InputValidator.ATG.OIL_LEVEL_MIN, InputValidator.ATG.OIL_LEVEL_MAX) { text ->
            sharedViewModel.updateATGData { reactor_a_oil_tank = text }
        }
        setupEditTextListenerWithValidation(binding.reactorATempTs, "Реактор ф.А температура ТS",
            InputValidator.ATG.TEMP_MIN, InputValidator.ATG.TEMP_MAX) { text ->
            sharedViewModel.updateATGData { reactor_a_temp_ts = text }
        }
        setupEditTextListenerWithValidation(binding.reactorAPumpGroup1, "Реактор ф.А группа №1",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_a_pump_group1 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorAPumpGroup2, "Реактор ф.А группа №2",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_a_pump_group2 = text }
        }
        setupEditTextListenerWithValidation(binding.reactorAPumpGroup3, "Реактор ф.А группа №3",
            InputValidator.ATG.REACTOR_PUMP_PRESSURE_MIN, InputValidator.ATG.REACTOR_PUMP_PRESSURE_MAX) { text ->
            sharedViewModel.updateATGData { reactor_a_pump_group3 = text }
        }

        // Спиннеры без валидации
        setupSpinnerListener(binding.atg3BPressure220) { text ->
            sharedViewModel.updateATGData { atg3_b_pressure_220 = text }
        }
        setupSpinnerListener(binding.reactorCTtNeutral) { text ->
            sharedViewModel.updateATGData { reactor_c_tt_neutral = text }
        }
        setupSpinnerListener(binding.reactorBTtNeutral) { text ->
            sharedViewModel.updateATGData { reactor_b_tt_neutral = text }
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
                if (!isUpdatingUIFromViewModel) {
                    val text = parent.getItemAtPosition(position).toString()
                    onItemSelected(text)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ==================== ВАЛИДАЦИЯ ====================

    private fun validateEditText(editText: EditText, paramName: String, min: Double, max: Double): Boolean {
        val value = editText.text.toString()
        if (value.isBlank()) return true

        if (!InputValidator.isInRange(value, min, max)) {
            showValidationError(InputValidator.getRangeMessage(paramName, min, max))
            editText.setBackgroundResource(R.drawable.edittext_border_error)
            editText.requestFocus()
            return false
        }
        editText.setBackgroundResource(R.drawable.edittext_border)
        return true
    }

    private fun showValidationError(message: String) {
        currentToast?.cancel()

        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message
        text.setTextColor(Color.parseColor("#FF4444"))

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(android.view.Gravity.TOP, 0, 100)
        toast.show()

        currentToast = toast
    }

    private fun setupEditTextListenerWithValidation(
        editText: EditText,
        paramName: String,
        min: Double,
        max: Double,
        onTextChanged: (String) -> Unit
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                val newText = s?.toString() ?: ""

                if (validateEditText(editText, paramName, min, max)) {
                    onTextChanged(newText)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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