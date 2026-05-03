package com.example.ps_inspection.ui.fragments.inspections

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
import com.example.ps_inspection.data.models.InspectionORU500Data
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentInspectionORU500Binding
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionORU500 : Fragment() {

    private var _binding: FragmentInspectionORU500Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var mediaManager: InspectionMediaManager

    private var isUpdatingUIFromViewModel = false

    private val tn1500Spinners = mutableListOf<Spinner>()
    private val tn2500Spinners = mutableListOf<Spinner>()
    private val tn500Sgres1Spinners = mutableListOf<Spinner>()

    // Маппинг для комментариев
    private val commentButtons = mutableMapOf<ImageButton, String>()
    // Маппинг для фото
    private val mediaButtons = mutableMapOf<ImageButton, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInspectionORU500Binding.inflate(inflater, container, false)
        mediaManager = InspectionMediaManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписываемся на изменения данных
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru500Data.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        // Подписываемся на изменения комментариев
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.oru500Comments.collectLatest { comments ->
                updateCommentButtonsState(comments)
            }
        }

        setupInputListeners()
        initSpinnerLists()
        setupFillButtons()
        setupMediaButtons()

        // Обновляем состояние всех кнопок
        updatePhotoButtonsState()
        refreshAllStates()
    }

    private fun setupMediaButtons() {
        val inspectionId = "current_inspection"

        // Регистрируем все кнопки фото
        registerMediaButton(binding.btnMediaR5002s, "В-500 Р-500 2С")
        registerMediaButton(binding.btnMediaVsht31, "В-500 ВШТ-31")
        registerMediaButton(binding.btnMediaVlt30, "В-500 ВЛТ-30")
        registerMediaButton(binding.btnMediaVshl32, "В-500 ВШЛ-32")
        registerMediaButton(binding.btnMediaVshl21, "В-500 ВШЛ-21")
        registerMediaButton(binding.btnMediaVsht22, "В-500 ВШТ-22")
        registerMediaButton(binding.btnMediaVlt20, "В-500 ВЛТ-20")
        registerMediaButton(binding.btnMediaVsht11, "В-500 ВШТ-11")
        registerMediaButton(binding.btnMediaVshl12, "В-500 ВШЛ-12")

        registerMediaButton(binding.btnMediaTtVsht31, "ТТ-500 ВШТ-31")
        registerMediaButton(binding.btnMediaTtVlt30, "ТТ-500 ВЛТ-30")
        registerMediaButton(binding.btnMediaTtVshl32, "ТТ-500 ВШЛ-32")
        registerMediaButton(binding.btnMediaTtVshl21, "ТТ-500 ВШЛ-21")
        registerMediaButton(binding.btnMediaTtVsht22, "ТТ-500 ВШТ-22")
        registerMediaButton(binding.btnMediaTtVlt20, "ТТ-500 ВЛТ-20")
        registerMediaButton(binding.btnMediaTtVsht11, "ТТ-500 ВШТ-11")
        registerMediaButton(binding.btnMediaTtVshl12, "ТТ-500 ВШЛ-12")

        registerMediaButton(binding.btnMediaTn1500, "1ТН-500")
        registerMediaButton(binding.btnMediaTn2500, "2ТН-500")
        registerMediaButton(binding.btnMediaTn500Sgres1, "ТН-500 СГРЭС-1")

        registerMediaButton(binding.btnMediaTrachukovskayaTt, "Трачуковская ТТ")
        registerMediaButton(binding.btnMediaTrachukovskaya2tn, "Трачуковская 2ТН")
        registerMediaButton(binding.btnMediaTrachukovskaya1tn, "Трачуковская 1ТН")

        registerMediaButton(binding.btnMediaBelozernaya2tn, "Белозёрная 2ТН")

        // Регистрируем все кнопки комментариев
        registerCommentButton(binding.btnCommentR5002s, "В-500 Р-500 2С")
        registerCommentButton(binding.btnCommentVsht31, "В-500 ВШТ-31")
        registerCommentButton(binding.btnCommentVlt30, "В-500 ВЛТ-30")
        registerCommentButton(binding.btnCommentVshl32, "В-500 ВШЛ-32")
        registerCommentButton(binding.btnCommentVshl21, "В-500 ВШЛ-21")
        registerCommentButton(binding.btnCommentVsht22, "В-500 ВШТ-22")
        registerCommentButton(binding.btnCommentVlt20, "В-500 ВЛТ-20")
        registerCommentButton(binding.btnCommentVsht11, "В-500 ВШТ-11")
        registerCommentButton(binding.btnCommentVshl12, "В-500 ВШЛ-12")

        registerCommentButton(binding.btnCommentTtVsht31, "ТТ-500 ВШТ-31")
        registerCommentButton(binding.btnCommentTtVlt30, "ТТ-500 ВЛТ-30")
        registerCommentButton(binding.btnCommentTtVshl32, "ТТ-500 ВШЛ-32")
        registerCommentButton(binding.btnCommentTtVshl21, "ТТ-500 ВШЛ-21")
        registerCommentButton(binding.btnCommentTtVsht22, "ТТ-500 ВШТ-22")
        registerCommentButton(binding.btnCommentTtVlt20, "ТТ-500 ВЛТ-20")
        registerCommentButton(binding.btnCommentTtVsht11, "ТТ-500 ВШТ-11")
        registerCommentButton(binding.btnCommentTtVshl12, "ТТ-500 ВШЛ-12")

        registerCommentButton(binding.btnCommentTn1500, "1ТН-500")
        registerCommentButton(binding.btnCommentTn2500, "2ТН-500")
        registerCommentButton(binding.btnCommentTn500Sgres1, "ТН-500 СГРЭС-1")

        registerCommentButton(binding.btnCommentTrachukovskayaTt, "Трачуковская ТТ")
        registerCommentButton(binding.btnCommentTrachukovskaya2tn, "Трачуковская 2ТН")
        registerCommentButton(binding.btnCommentTrachukovskaya1tn, "Трачуковская 1ТН")

        registerCommentButton(binding.btnCommentBelozernaya2tn, "Белозёрная 2ТН")
    }

    private fun registerMediaButton(button: ImageButton?, equipmentKey: String) {
        button?.let {
            mediaButtons[it] = equipmentKey
            it.setOnClickListener {
                MediaDialogFragment.newInstance("current_inspection", equipmentKey)
                    .show(childFragmentManager, "media_${equipmentKey.replace(" ", "_")}")
            }
        }
    }

    private fun registerCommentButton(button: ImageButton?, equipmentKey: String) {
        button?.let {
            commentButtons[it] = equipmentKey
            it.setOnClickListener {
                CommentsDialogFragment.newInstance(equipmentKey, "ORU500")
                    .show(parentFragmentManager, "comment_${equipmentKey.replace(" ", "_")}")
            }
        }
    }

    private fun updatePhotoButtonsState() {
        val inspectionId = "current_inspection"
        mediaButtons.forEach { (button, key) ->
            val hasPhotos = mediaManager.hasPhotos(inspectionId, key)
            val color = if (hasPhotos) {
                ContextCompat.getColor(requireContext(), R.color.green)
            } else {
                ContextCompat.getColor(requireContext(), R.color.gray)
            }
            button.setColorFilter(color)
        }
    }

    // ИСПРАВЛЕНО: тип изменён с Map<String, List<String>> на Map<String, List<Comment>>
    fun updateCommentButtonsState(commentsMap: Map<String, List<Comment>>) {
        commentButtons.forEach { (button, key) ->
            val hasComments = commentsMap[key]?.isNotEmpty() == true
            val color = if (hasComments) {
                ContextCompat.getColor(requireContext(), R.color.green)
            } else {
                ContextCompat.getColor(requireContext(), R.color.gray)
            }
            button.setColorFilter(color)
        }
    }

    fun refreshPhotoButtonsState() {
        updatePhotoButtonsState()
    }

    private fun initSpinnerLists() {
        tn1500Spinners.addAll(listOf(
            binding.tn1500Cascade1A, binding.tn1500Cascade1B, binding.tn1500Cascade1C,
            binding.tn1500Cascade2A, binding.tn1500Cascade2B, binding.tn1500Cascade2C,
            binding.tn1500Cascade3A, binding.tn1500Cascade3B, binding.tn1500Cascade3C,
            binding.tn1500Cascade4A, binding.tn1500Cascade4B, binding.tn1500Cascade4C
        ))

        tn2500Spinners.addAll(listOf(
            binding.tn2500Cascade1A, binding.tn2500Cascade1B, binding.tn2500Cascade1C,
            binding.tn2500Cascade2A, binding.tn2500Cascade2B, binding.tn2500Cascade2C,
            binding.tn2500Cascade3A, binding.tn2500Cascade3B, binding.tn2500Cascade3C,
            binding.tn2500Cascade4A, binding.tn2500Cascade4B, binding.tn2500Cascade4C
        ))

        tn500Sgres1Spinners.addAll(listOf(
            binding.tn500Sgres1Cascade1A, binding.tn500Sgres1Cascade1B, binding.tn500Sgres1Cascade1C,
            binding.tn500Sgres1Cascade2A, binding.tn500Sgres1Cascade2B, binding.tn500Sgres1Cascade2C,
            binding.tn500Sgres1Cascade3A, binding.tn500Sgres1Cascade3B, binding.tn500Sgres1Cascade3C,
            binding.tn500Sgres1Cascade4A, binding.tn500Sgres1Cascade4B, binding.tn500Sgres1Cascade4C
        ))
    }

    private fun setupFillButtons() {
        binding.btnFillAllTn1500.setOnClickListener {
            fillAllSpinners(tn1500Spinners, "1ТН-500") { value ->
                updateTn1500ViewModel(value)
            }
        }

        binding.btnFillAllTn2500.setOnClickListener {
            fillAllSpinners(tn2500Spinners, "2ТН-500") { value ->
                updateTn2500ViewModel(value)
            }
        }

        binding.btnFillAllTn500Sgres1.setOnClickListener {
            fillAllSpinners(tn500Sgres1Spinners, "ТН-500 СГРЭС-1") { value ->
                updateTn500Sgres1ViewModel(value)
            }
        }

        binding.btnFillAllOilTtVsht31.setOnClickListener {
            fillTtSpinners(binding.oilTtVsht31A, binding.oilTtVsht31B, binding.oilTtVsht31C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVsht31B = value
                    oilTtVsht31C = value
                }
            }
        }

        binding.btnFillAllOilTtVlt30.setOnClickListener {
            fillTtSpinners(binding.oilTtVlt30A, binding.oilTtVlt30B, binding.oilTtVlt30C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVlt30B = value
                    oilTtVlt30C = value
                }
            }
        }

        binding.btnFillAllOilTtVshl32.setOnClickListener {
            fillTtSpinners(binding.oilTtVshl32A, binding.oilTtVshl32B, binding.oilTtVshl32C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVshl32B = value
                    oilTtVshl32C = value
                }
            }
        }

        binding.btnFillAllOilTtVshl21.setOnClickListener {
            fillTtSpinners(binding.oilTtVshl21A, binding.oilTtVshl21B, binding.oilTtVshl21C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVshl21B = value
                    oilTtVshl21C = value
                }
            }
        }

        binding.btnFillAllOilTtVsht22.setOnClickListener {
            fillTtSpinners(binding.oilTtVsht22A, binding.oilTtVsht22B, binding.oilTtVsht22C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVsht22B = value
                    oilTtVsht22C = value
                }
            }
        }

        binding.btnFillAllOilTtVlt20.setOnClickListener {
            fillTtSpinners(binding.oilTtVlt20A, binding.oilTtVlt20B, binding.oilTtVlt20C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVlt20B = value
                    oilTtVlt20C = value
                }
            }
        }

        binding.btnFillAllOilTtVsht11.setOnClickListener {
            fillTtSpinners(binding.oilTtVsht11A, binding.oilTtVsht11B, binding.oilTtVsht11C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVsht11B = value
                    oilTtVsht11C = value
                }
            }
        }

        binding.btnFillAllOilTtVshl12.setOnClickListener {
            fillTtSpinners(binding.oilTtVshl12A, binding.oilTtVshl12B, binding.oilTtVshl12C) { value ->
                sharedViewModel.updateORU500Data {
                    oilTtVshl12B = value
                    oilTtVshl12C = value
                }
            }
        }
    }

    private fun fillAllSpinners(spinners: List<Spinner>, title: String, onUpdate: (String) -> Unit) {
        val firstSpinner = spinners.firstOrNull() ?: return
        val value = firstSpinner.selectedItem?.toString() ?: return

        if (value.isEmpty() || value == "Выберите") {
            Toast.makeText(requireContext(), "Сначала выберите значение в первом поле", Toast.LENGTH_SHORT).show()
            return
        }

        isUpdatingUIFromViewModel = true

        for (spinner in spinners) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    spinner.setSelection(i, false)
                    break
                }
            }
        }

        isUpdatingUIFromViewModel = false
        onUpdate(value)

        Toast.makeText(requireContext(), "Все каскады $title заполнены", Toast.LENGTH_SHORT).show()
    }

    private fun fillTtSpinners(
        spinnerA: Spinner,
        spinnerB: Spinner,
        spinnerC: Spinner,
        onUpdate: (String) -> Unit
    ) {
        val value = spinnerA.selectedItem?.toString()
        if (value.isNullOrEmpty() || value == "Выберите") {
            Toast.makeText(requireContext(), "Сначала выберите значение в фазе А", Toast.LENGTH_SHORT).show()
            return
        }

        isUpdatingUIFromViewModel = true

        setSpinnerSilently(spinnerB, value)
        setSpinnerSilently(spinnerC, value)

        isUpdatingUIFromViewModel = false
        onUpdate(value)

        Toast.makeText(requireContext(), "Фазы B и C заполнены", Toast.LENGTH_SHORT).show()
    }

    private fun setSpinnerSilently(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i, false)
                break
            }
        }
    }

    private fun updateTn1500ViewModel(value: String) {
        sharedViewModel.updateORU500Data {
            tn1500Cascade1A = value
            tn1500Cascade1B = value
            tn1500Cascade1C = value
            tn1500Cascade2A = value
            tn1500Cascade2B = value
            tn1500Cascade2C = value
            tn1500Cascade3A = value
            tn1500Cascade3B = value
            tn1500Cascade3C = value
            tn1500Cascade4A = value
            tn1500Cascade4B = value
            tn1500Cascade4C = value
        }
    }

    private fun updateTn2500ViewModel(value: String) {
        sharedViewModel.updateORU500Data {
            tn2500Cascade1A = value
            tn2500Cascade1B = value
            tn2500Cascade1C = value
            tn2500Cascade2A = value
            tn2500Cascade2B = value
            tn2500Cascade2C = value
            tn2500Cascade3A = value
            tn2500Cascade3B = value
            tn2500Cascade3C = value
            tn2500Cascade4A = value
            tn2500Cascade4B = value
            tn2500Cascade4C = value
        }
    }

    private fun updateTn500Sgres1ViewModel(value: String) {
        sharedViewModel.updateORU500Data {
            tn500Sgres1Cascade1A = value
            tn500Sgres1Cascade1B = value
            tn500Sgres1Cascade1C = value
            tn500Sgres1Cascade2A = value
            tn500Sgres1Cascade2B = value
            tn500Sgres1Cascade2C = value
            tn500Sgres1Cascade3A = value
            tn500Sgres1Cascade3B = value
            tn500Sgres1Cascade3C = value
            tn500Sgres1Cascade4A = value
            tn500Sgres1Cascade4B = value
            tn500Sgres1Cascade4C = value
        }
    }

    private fun updateUIFromData(data: InspectionORU500Data) {
        isUpdatingUIFromViewModel = true

        setSpinnerSelection(binding.purgingR5002sA1, data.purgingR5002sA1)
        setSpinnerSelection(binding.purgingR5002sB1, data.purgingR5002sB1)
        setSpinnerSelection(binding.purgingR5002sC1, data.purgingR5002sC1)
        setSpinnerSelection(binding.purgingR5002sA2, data.purgingR5002sA2)
        setSpinnerSelection(binding.purgingR5002sB2, data.purgingR5002sB2)
        setSpinnerSelection(binding.purgingR5002sC2, data.purgingR5002sC2)

        updateEditTextIfNeeded(binding.gasPressureVsht31A, data.gasPressureVsht31A)
        updateEditTextIfNeeded(binding.gasPressureVsht31B, data.gasPressureVsht31B)
        updateEditTextIfNeeded(binding.gasPressureVsht31C, data.gasPressureVsht31C)
        setSpinnerSelection(binding.oilTtVsht31A, data.oilTtVsht31A)
        setSpinnerSelection(binding.oilTtVsht31B, data.oilTtVsht31B)
        setSpinnerSelection(binding.oilTtVsht31C, data.oilTtVsht31C)

        updateEditTextIfNeeded(binding.gasPressureVlt30A, data.gasPressureVlt30A)
        updateEditTextIfNeeded(binding.gasPressureVlt30B, data.gasPressureVlt30B)
        updateEditTextIfNeeded(binding.gasPressureVlt30C, data.gasPressureVlt30C)
        setSpinnerSelection(binding.oilTtVlt30A, data.oilTtVlt30A)
        setSpinnerSelection(binding.oilTtVlt30B, data.oilTtVlt30B)
        setSpinnerSelection(binding.oilTtVlt30C, data.oilTtVlt30C)
        setSpinnerSelection(binding.oilTtTrachukovskayaA, data.oilTtTrachukovskayaA)
        setSpinnerSelection(binding.oilTtTrachukovskayaB, data.oilTtTrachukovskayaB)
        setSpinnerSelection(binding.oilTtTrachukovskayaC, data.oilTtTrachukovskayaC)
        setSpinnerSelection(binding.oil2tnTrachukovskayaA, data.oil2tnTrachukovskayaA)
        setSpinnerSelection(binding.oil2tnTrachukovskayaB, data.oil2tnTrachukovskayaB)
        setSpinnerSelection(binding.oil2tnTrachukovskayaC, data.oil2tnTrachukovskayaC)
        setSpinnerSelection(binding.oil1tnTrachukovskayaA, data.oil1tnTrachukovskayaA)
        setSpinnerSelection(binding.oil1tnTrachukovskayaB, data.oil1tnTrachukovskayaB)
        setSpinnerSelection(binding.oil1tnTrachukovskayaC, data.oil1tnTrachukovskayaC)

        setSpinnerSelection(binding.purgingVshl32A1, data.purgingVshl32A1)
        setSpinnerSelection(binding.purgingVshl32B1, data.purgingVshl32B1)
        setSpinnerSelection(binding.purgingVshl32C1, data.purgingVshl32C1)
        setSpinnerSelection(binding.purgingVshl32A2, data.purgingVshl32A2)
        setSpinnerSelection(binding.purgingVshl32B2, data.purgingVshl32B2)
        setSpinnerSelection(binding.purgingVshl32C2, data.purgingVshl32C2)
        setSpinnerSelection(binding.oilTtVshl32A, data.oilTtVshl32A)
        setSpinnerSelection(binding.oilTtVshl32B, data.oilTtVshl32B)
        setSpinnerSelection(binding.oilTtVshl32C, data.oilTtVshl32C)

        setSpinnerSelection(binding.purgingVshl21A1, data.purgingVshl21A1)
        setSpinnerSelection(binding.purgingVshl21B1, data.purgingVshl21B1)
        setSpinnerSelection(binding.purgingVshl21C1, data.purgingVshl21C1)
        setSpinnerSelection(binding.purgingVshl21A2, data.purgingVshl21A2)
        setSpinnerSelection(binding.purgingVshl21B2, data.purgingVshl21B2)
        setSpinnerSelection(binding.purgingVshl21C2, data.purgingVshl21C2)
        setSpinnerSelection(binding.oilTtVshl21A, data.oilTtVshl21A)
        setSpinnerSelection(binding.oilTtVshl21B, data.oilTtVshl21B)
        setSpinnerSelection(binding.oilTtVshl21C, data.oilTtVshl21C)

        setSpinnerSelection(binding.purgingVsht22A1, data.purgingVsht22A1)
        setSpinnerSelection(binding.purgingVsht22B1, data.purgingVsht22B1)
        setSpinnerSelection(binding.purgingVsht22C1, data.purgingVsht22C1)
        setSpinnerSelection(binding.purgingVsht22A2, data.purgingVsht22A2)
        setSpinnerSelection(binding.purgingVsht22B2, data.purgingVsht22B2)
        setSpinnerSelection(binding.purgingVsht22C2, data.purgingVsht22C2)
        setSpinnerSelection(binding.oilTtVsht22A, data.oilTtVsht22A)
        setSpinnerSelection(binding.oilTtVsht22B, data.oilTtVsht22B)
        setSpinnerSelection(binding.oilTtVsht22C, data.oilTtVsht22C)

        setSpinnerSelection(binding.purgingVlt20A1, data.purgingVlt20A1)
        setSpinnerSelection(binding.purgingVlt20B1, data.purgingVlt20B1)
        setSpinnerSelection(binding.purgingVlt20C1, data.purgingVlt20C1)
        setSpinnerSelection(binding.purgingVlt20A2, data.purgingVlt20A2)
        setSpinnerSelection(binding.purgingVlt20B2, data.purgingVlt20B2)
        setSpinnerSelection(binding.purgingVlt20C2, data.purgingVlt20C2)
        setSpinnerSelection(binding.oilTtVlt20A, data.oilTtVlt20A)
        setSpinnerSelection(binding.oilTtVlt20B, data.oilTtVlt20B)
        setSpinnerSelection(binding.oilTtVlt20C, data.oilTtVlt20C)

        setSpinnerSelection(binding.purgingVsht11A1, data.purgingVsht11A1)
        setSpinnerSelection(binding.purgingVsht11B1, data.purgingVsht11B1)
        setSpinnerSelection(binding.purgingVsht11C1, data.purgingVsht11C1)
        setSpinnerSelection(binding.purgingVsht11A2, data.purgingVsht11A2)
        setSpinnerSelection(binding.purgingVsht11B2, data.purgingVsht11B2)
        setSpinnerSelection(binding.purgingVsht11C2, data.purgingVsht11C2)
        setSpinnerSelection(binding.oilTtVsht11A, data.oilTtVsht11A)
        setSpinnerSelection(binding.oilTtVsht11B, data.oilTtVsht11B)
        setSpinnerSelection(binding.oilTtVsht11C, data.oilTtVsht11C)

        setSpinnerSelection(binding.purgingVshl12A1, data.purgingVshl12A1)
        setSpinnerSelection(binding.purgingVshl12B1, data.purgingVshl12B1)
        setSpinnerSelection(binding.purgingVshl12C1, data.purgingVshl12C1)
        setSpinnerSelection(binding.purgingVshl12A2, data.purgingVshl12A2)
        setSpinnerSelection(binding.purgingVshl12B2, data.purgingVshl12B2)
        setSpinnerSelection(binding.purgingVshl12C2, data.purgingVshl12C2)
        setSpinnerSelection(binding.oilTtVshl12A, data.oilTtVshl12A)
        setSpinnerSelection(binding.oilTtVshl12B, data.oilTtVshl12B)
        setSpinnerSelection(binding.oilTtVshl12C, data.oilTtVshl12C)
        setSpinnerSelection(binding.oil2tnBelozernayaA, data.oil2tnBelozernayaA)
        setSpinnerSelection(binding.oil2tnBelozernayaB, data.oil2tnBelozernayaB)
        setSpinnerSelection(binding.oil2tnBelozernayaC, data.oil2tnBelozernayaC)

        setSpinnerSelection(binding.tn1500Cascade1A, data.tn1500Cascade1A)
        setSpinnerSelection(binding.tn1500Cascade1B, data.tn1500Cascade1B)
        setSpinnerSelection(binding.tn1500Cascade1C, data.tn1500Cascade1C)
        setSpinnerSelection(binding.tn1500Cascade2A, data.tn1500Cascade2A)
        setSpinnerSelection(binding.tn1500Cascade2B, data.tn1500Cascade2B)
        setSpinnerSelection(binding.tn1500Cascade2C, data.tn1500Cascade2C)
        setSpinnerSelection(binding.tn1500Cascade3A, data.tn1500Cascade3A)
        setSpinnerSelection(binding.tn1500Cascade3B, data.tn1500Cascade3B)
        setSpinnerSelection(binding.tn1500Cascade3C, data.tn1500Cascade3C)
        setSpinnerSelection(binding.tn1500Cascade4A, data.tn1500Cascade4A)
        setSpinnerSelection(binding.tn1500Cascade4B, data.tn1500Cascade4B)
        setSpinnerSelection(binding.tn1500Cascade4C, data.tn1500Cascade4C)

        setSpinnerSelection(binding.tn2500Cascade1A, data.tn2500Cascade1A)
        setSpinnerSelection(binding.tn2500Cascade1B, data.tn2500Cascade1B)
        setSpinnerSelection(binding.tn2500Cascade1C, data.tn2500Cascade1C)
        setSpinnerSelection(binding.tn2500Cascade2A, data.tn2500Cascade2A)
        setSpinnerSelection(binding.tn2500Cascade2B, data.tn2500Cascade2B)
        setSpinnerSelection(binding.tn2500Cascade2C, data.tn2500Cascade2C)
        setSpinnerSelection(binding.tn2500Cascade3A, data.tn2500Cascade3A)
        setSpinnerSelection(binding.tn2500Cascade3B, data.tn2500Cascade3B)
        setSpinnerSelection(binding.tn2500Cascade3C, data.tn2500Cascade3C)
        setSpinnerSelection(binding.tn2500Cascade4A, data.tn2500Cascade4A)
        setSpinnerSelection(binding.tn2500Cascade4B, data.tn2500Cascade4B)
        setSpinnerSelection(binding.tn2500Cascade4C, data.tn2500Cascade4C)

        setSpinnerSelection(binding.tn500Sgres1Cascade1A, data.tn500Sgres1Cascade1A)
        setSpinnerSelection(binding.tn500Sgres1Cascade1B, data.tn500Sgres1Cascade1B)
        setSpinnerSelection(binding.tn500Sgres1Cascade1C, data.tn500Sgres1Cascade1C)
        setSpinnerSelection(binding.tn500Sgres1Cascade2A, data.tn500Sgres1Cascade2A)
        setSpinnerSelection(binding.tn500Sgres1Cascade2B, data.tn500Sgres1Cascade2B)
        setSpinnerSelection(binding.tn500Sgres1Cascade2C, data.tn500Sgres1Cascade2C)
        setSpinnerSelection(binding.tn500Sgres1Cascade3A, data.tn500Sgres1Cascade3A)
        setSpinnerSelection(binding.tn500Sgres1Cascade3B, data.tn500Sgres1Cascade3B)
        setSpinnerSelection(binding.tn500Sgres1Cascade3C, data.tn500Sgres1Cascade3C)
        setSpinnerSelection(binding.tn500Sgres1Cascade4A, data.tn500Sgres1Cascade4A)
        setSpinnerSelection(binding.tn500Sgres1Cascade4B, data.tn500Sgres1Cascade4B)
        setSpinnerSelection(binding.tn500Sgres1Cascade4C, data.tn500Sgres1Cascade4C)

        isUpdatingUIFromViewModel = false
    }

    private fun updateEditTextIfNeeded(editText: EditText, newValue: String) {
        val currentText = editText.text.toString()
        if (currentText != newValue) {
            editText.setText(newValue)
            editText.setSelection(editText.text.length)
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        if (value.isNotEmpty()) {
            val adapter = spinner.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == value) {
                    if (spinner.selectedItemPosition != i) {
                        spinner.setSelection(i, false)
                    }
                    break
                }
            }
        }
    }

    private fun setupInputListeners() {
        setupEditTextListener(binding.gasPressureVsht31A) { text ->
            sharedViewModel.updateORU500Data { gasPressureVsht31A = text }
        }
        setupEditTextListener(binding.gasPressureVsht31B) { text ->
            sharedViewModel.updateORU500Data { gasPressureVsht31B = text }
        }
        setupEditTextListener(binding.gasPressureVsht31C) { text ->
            sharedViewModel.updateORU500Data { gasPressureVsht31C = text }
        }
        setupEditTextListener(binding.gasPressureVlt30A) { text ->
            sharedViewModel.updateORU500Data { gasPressureVlt30A = text }
        }
        setupEditTextListener(binding.gasPressureVlt30B) { text ->
            sharedViewModel.updateORU500Data { gasPressureVlt30B = text }
        }
        setupEditTextListener(binding.gasPressureVlt30C) { text ->
            sharedViewModel.updateORU500Data { gasPressureVlt30C = text }
        }

        setupSpinnerListener(binding.purgingR5002sA1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sA1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sB1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sB1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sC1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sC1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sA2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sA2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sB2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sB2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingR5002sC2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingR5002sC2 = selectedItem.toString() }
        }

        setupSpinnerListener(binding.oilTtVsht31A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht31A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht31B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht31B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht31C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht31C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.oilTtVlt30A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt30A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt30B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt30B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt30C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt30C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtTrachukovskayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtTrachukovskayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtTrachukovskayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtTrachukovskayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtTrachukovskayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtTrachukovskayaC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnTrachukovskayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnTrachukovskayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnTrachukovskayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnTrachukovskayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnTrachukovskayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnTrachukovskayaC = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil1tnTrachukovskayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oil1tnTrachukovskayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil1tnTrachukovskayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oil1tnTrachukovskayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil1tnTrachukovskayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oil1tnTrachukovskayaC = selectedItem.toString() }
        }

        setupSpinnerListener(binding.purgingVshl32A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl32C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl32C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl32A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl32A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl32B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl32B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl32C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl32C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.purgingVshl21A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl21C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl21C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl21A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl21A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl21B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl21B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl21C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl21C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.purgingVsht22A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht22C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht22C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht22A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht22A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht22B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht22B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht22C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht22C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.purgingVlt20A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVlt20A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVlt20B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVlt20B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVlt20C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVlt20C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVlt20A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVlt20A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVlt20B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVlt20B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVlt20C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVlt20C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt20A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt20A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt20B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt20B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVlt20C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVlt20C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.purgingVsht11A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVsht11C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVsht11C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht11A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht11A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht11B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht11B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVsht11C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVsht11C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.purgingVshl12A1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12A1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12B1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12B1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12C1) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12C1 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12A2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12A2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12B2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12B2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.purgingVshl12C2) { selectedItem ->
            sharedViewModel.updateORU500Data { purgingVshl12C2 = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl12A) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl12A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl12B) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl12B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oilTtVshl12C) { selectedItem ->
            sharedViewModel.updateORU500Data { oilTtVshl12C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnBelozernayaA) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnBelozernayaA = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnBelozernayaB) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnBelozernayaB = selectedItem.toString() }
        }
        setupSpinnerListener(binding.oil2tnBelozernayaC) { selectedItem ->
            sharedViewModel.updateORU500Data { oil2tnBelozernayaC = selectedItem.toString() }
        }

        setupSpinnerListener(binding.tn1500Cascade1A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade1B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade1C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade2A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade2B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade2C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade3A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade3A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade3B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade3B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade3C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade3C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade4A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade4A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade4B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade4B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn1500Cascade4C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn1500Cascade4C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.tn2500Cascade1A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade1B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade1C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade2A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade2B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade2C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade3A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade3A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade3B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade3B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade3C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade3C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade4A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade4A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade4B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade4B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn2500Cascade4C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn2500Cascade4C = selectedItem.toString() }
        }

        setupSpinnerListener(binding.tn500Sgres1Cascade1A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade1A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade1B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade1B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade1C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade1C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade2A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade2A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade2B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade2B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade2C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade2C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade3A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade3A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade3B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade3B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade3C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade3C = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade4A) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade4A = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade4B) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade4B = selectedItem.toString() }
        }
        setupSpinnerListener(binding.tn500Sgres1Cascade4C) { selectedItem ->
            sharedViewModel.updateORU500Data { tn500Sgres1Cascade4C = selectedItem.toString() }
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

    private fun setupSpinnerListener(spinner: Spinner, onItemSelected: (Any?) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && !isUpdatingUIFromViewModel) {
                    onItemSelected(parent?.getItemAtPosition(position))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun refreshAllStates() {
        updatePhotoButtonsState()
        updateCommentButtonsState(sharedViewModel.oru500Comments.value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = InspectionORU500()
    }
}