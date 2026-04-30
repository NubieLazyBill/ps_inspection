package com.example.ps_inspection.ui.fragments.inspections

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.ps_inspection.R
import com.example.ps_inspection.data.models.InspectionBuildingsData
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.ui.fragments.dialogs.CommentsDialogFragment
import com.example.ps_inspection.ui.fragments.dialogs.MediaDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionBuildings : Fragment() {

    private var _rootView: View? = null
    private val rootView get() = _rootView!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var mediaManager: InspectionMediaManager

    private var isUpdatingUIFromViewModel = false

    // Маппинг кнопок комментариев
    private val commentButtons = mutableMapOf<ImageButton, String>()
    // Маппинг кнопок фото
    private val mediaButtons = mutableMapOf<ImageButton, String>()
    // Маппинг кнопок-переключателей (арматура, обогрев)
    private val stateButtons = mutableMapOf<ImageButton, (String) -> Unit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _rootView = inflater.inflate(R.layout.fragment_inspection_buildings, container, false)
        mediaManager = InspectionMediaManager(requireContext())
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загружаем комментарии из хранилища
        sharedViewModel.loadBuildingsCommentsFromStorage()

        // Подписываемся на изменения данных
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.buildingsData.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        // Подписываемся на изменения комментариев
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.buildingsComments.collectLatest { comments ->
                updateCommentButtonsState(comments)
            }
        }

        setupInputListeners()
        setupMediaButtons()
        updatePhotoButtonsState()
        refreshAllStates()
    }

    private fun setupMediaButtons() {
        val inspectionId = "current_inspection"

        // Регистрируем все кнопки фото
        registerMediaButton(findImageButton(R.id.btnMediaCompressor1), "Компрессорная №1")
        registerMediaButton(findImageButton(R.id.btnMediaBallroom1), "Баллоная №1")
        registerMediaButton(findImageButton(R.id.btnMediaCompressor2), "Компрессорная №2")
        registerMediaButton(findImageButton(R.id.btnMediaBallroom2), "Баллоная №2")
        registerMediaButton(findImageButton(R.id.btnMediaKpzOpu), "КПЗ ОПУ")
        registerMediaButton(findImageButton(R.id.btnMediaKpz2), "КПЗ-2")
        registerMediaButton(findImageButton(R.id.btnMediaFirePump), "Насосная пожаротушения")
        registerMediaButton(findImageButton(R.id.btnMediaWorkshop), "Мастерская по ремонту ВВ")
        registerMediaButton(findImageButton(R.id.btnMediaArtWell), "Артскважина")
        registerMediaButton(findImageButton(R.id.btnMediaArtesianWell), "Здание артезианской скважины")
        registerMediaButton(findImageButton(R.id.btnMediaRoomAb), "Помещение 1 (2) АБ")
        registerMediaButton(findImageButton(R.id.btnMediaBasement), "Помещение п/этажа №1,2,3")

        // Регистрируем все кнопки комментариев
        registerCommentButton(findImageButton(R.id.btnCommentCompressor1), "Компрессорная №1")
        registerCommentButton(findImageButton(R.id.btnCommentBallroom1), "Баллоная №1")
        registerCommentButton(findImageButton(R.id.btnCommentCompressor2), "Компрессорная №2")
        registerCommentButton(findImageButton(R.id.btnCommentBallroom2), "Баллоная №2")
        registerCommentButton(findImageButton(R.id.btnCommentKpzOpu), "КПЗ ОПУ")
        registerCommentButton(findImageButton(R.id.btnCommentKpz2), "КПЗ-2")
        registerCommentButton(findImageButton(R.id.btnCommentFirePump), "Насосная пожаротушения")
        registerCommentButton(findImageButton(R.id.btnCommentWorkshop), "Мастерская по ремонту ВВ")
        registerCommentButton(findImageButton(R.id.btnCommentArtWell), "Артскважина")
        registerCommentButton(findImageButton(R.id.btnCommentArtesianWell), "Здание артезианской скважины")
        registerCommentButton(findImageButton(R.id.btnCommentRoomAb), "Помещение 1 (2) АБ")
        registerCommentButton(findImageButton(R.id.btnCommentBasement), "Помещение п/этажа №1,2,3")
    }

    private fun findImageButton(id: Int): ImageButton? {
        return try {
            rootView.findViewById<ImageButton>(id)
        } catch (e: Exception) {
            null
        }
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
                CommentsDialogFragment.newInstance(equipmentKey, "BUILDINGS")
                    .show(parentFragmentManager, "comment_${equipmentKey.replace(" ", "_")}")
            }
        }
    }

    private fun updatePhotoButtonsState() {
        val inspectionId = "current_inspection"
        val hasAnyPhotos = mediaManager.hasPhotos(inspectionId, "Buildings")
        mediaButtons.keys.forEach { button ->
            button.setColorFilter(if (hasAnyPhotos) {
                ContextCompat.getColor(requireContext(), R.color.green)
            } else {
                ContextCompat.getColor(requireContext(), R.color.gray)
            })
        }
    }

    fun updateCommentButtonsState(commentsMap: Map<String, List<String>>) {
        commentButtons.forEach { (button, key) ->
            val hasComments = commentsMap[key]?.isNotEmpty() == true
            button.setColorFilter(if (hasComments) {
                ContextCompat.getColor(requireContext(), R.color.green)
            } else {
                ContextCompat.getColor(requireContext(), R.color.gray)
            })
        }
    }

    fun refreshAllStates() {
        updatePhotoButtonsState()
        updateCommentButtonsState(sharedViewModel.buildingsComments.value)
    }

    private fun updateUIFromData(data: InspectionBuildingsData) {
        isUpdatingUIFromViewModel = true

        // Компрессорная №1
        updateButtonState(findImageButton(R.id.btnCompressor1Valve), data.compressor1Valve)
        updateButtonState(findImageButton(R.id.btnCompressor1Heating), data.compressor1Heating)
        updateEditTextIfNeeded(findEditText(R.id.etCompressor1Temp), data.compressor1Temp)

        // Баллоная №1
        updateButtonState(findImageButton(R.id.btnBallroom1Valve), data.ballroom1Valve)
        updateButtonState(findImageButton(R.id.btnBallroom1Heating), data.ballroom1Heating)
        updateEditTextIfNeeded(findEditText(R.id.etBallroom1Temp), data.ballroom1Temp)

        // Компрессорная №2
        updateButtonState(findImageButton(R.id.btnCompressor2Valve), data.compressor2Valve)
        updateButtonState(findImageButton(R.id.btnCompressor2Heating), data.compressor2Heating)
        updateEditTextIfNeeded(findEditText(R.id.etCompressor2Temp), data.compressor2Temp)

        // Баллоная №2
        updateButtonState(findImageButton(R.id.btnBallroom2Valve), data.ballroom2Valve)
        updateButtonState(findImageButton(R.id.btnBallroom2Heating), data.ballroom2Heating)
        updateEditTextIfNeeded(findEditText(R.id.etBallroom2Temp), data.ballroom2Temp)

        // КПЗ ОПУ
        updateButtonState(findImageButton(R.id.btnKpzOpuValve), data.kpzOpuValve)
        updateButtonState(findImageButton(R.id.btnKpzOpuHeating), data.kpzOpuHeating)
        updateEditTextIfNeeded(findEditText(R.id.etKpzOpuTemp), data.kpzOpuTemp)

        // КПЗ-2
        updateButtonState(findImageButton(R.id.btnKpz2Valve), data.kpz2Valve)
        updateButtonState(findImageButton(R.id.btnKpz2Heating), data.kpz2Heating)
        updateEditTextIfNeeded(findEditText(R.id.etKpz2Temp), data.kpz2Temp)

        // Насосная пожаротушения
        updateButtonState(findImageButton(R.id.btnFirePumpValve), data.firePumpValve)
        updateButtonState(findImageButton(R.id.btnFirePumpHeating), data.firePumpHeating)
        updateEditTextIfNeeded(findEditText(R.id.etFirePumpTemp), data.firePumpTemp)

        // Мастерская по ремонту ВВ
        updateButtonState(findImageButton(R.id.btnWorkshopHeating), data.workshopHeating)
        updateEditTextIfNeeded(findEditText(R.id.etWorkshopTemp), data.workshopTemp)

        // Артскважина
        updateButtonState(findImageButton(R.id.btnArtWellHeating), data.artWellHeating)

        // Здание артезианской скважины
        updateButtonState(findImageButton(R.id.btnArtesianWellHeating), data.artesianWellHeating)

        // Помещение 1 (2) АБ
        updateButtonState(findImageButton(R.id.btnRoomAbHeating), data.roomAbHeating)
        updateEditTextIfNeeded(findEditText(R.id.etRoomAbTemp), data.roomAbTemp)

        // Помещение п/этажа №1,2,3
        updateButtonState(findImageButton(R.id.btnBasementHeating), data.basementHeating)
        updateEditTextIfNeeded(findEditText(R.id.etBasementTemp), data.basementTemp)

        isUpdatingUIFromViewModel = false
    }

    private fun findEditText(id: Int): EditText = rootView.findViewById(id)

    private fun updateButtonState(button: ImageButton?, state: String) {
        button?.let {
            val currentState = it.tag as? String ?: "○"
            if (currentState != state) {
                it.tag = state
                when (state) {
                    "+" -> it.setColorFilter(Color.GREEN)
                    "−" -> it.setColorFilter(Color.RED)
                    else -> it.setColorFilter(Color.GRAY)
                }
            }
        }
    }

    private fun updateEditTextIfNeeded(editText: EditText, newValue: String) {
        val currentText = editText.text.toString()
        if (currentText != newValue) {
            editText.setText(newValue)
            editText.setSelection(editText.text.length)
        }
    }

    private fun setupInputListeners() {
        // Компрессорная №1
        setupButtonListener(R.id.btnCompressor1Valve) { state ->
            sharedViewModel.updateBuildingsData { compressor1Valve = state }
        }
        setupButtonListener(R.id.btnCompressor1Heating) { state ->
            sharedViewModel.updateBuildingsData { compressor1Heating = state }
        }
        setupEditTextListener(R.id.etCompressor1Temp) { text ->
            sharedViewModel.updateBuildingsData { compressor1Temp = text }
        }

        // Баллоная №1
        setupButtonListener(R.id.btnBallroom1Valve) { state ->
            sharedViewModel.updateBuildingsData { ballroom1Valve = state }
        }
        setupButtonListener(R.id.btnBallroom1Heating) { state ->
            sharedViewModel.updateBuildingsData { ballroom1Heating = state }
        }
        setupEditTextListener(R.id.etBallroom1Temp) { text ->
            sharedViewModel.updateBuildingsData { ballroom1Temp = text }
        }

        // Компрессорная №2
        setupButtonListener(R.id.btnCompressor2Valve) { state ->
            sharedViewModel.updateBuildingsData { compressor2Valve = state }
        }
        setupButtonListener(R.id.btnCompressor2Heating) { state ->
            sharedViewModel.updateBuildingsData { compressor2Heating = state }
        }
        setupEditTextListener(R.id.etCompressor2Temp) { text ->
            sharedViewModel.updateBuildingsData { compressor2Temp = text }
        }

        // Баллоная №2
        setupButtonListener(R.id.btnBallroom2Valve) { state ->
            sharedViewModel.updateBuildingsData { ballroom2Valve = state }
        }
        setupButtonListener(R.id.btnBallroom2Heating) { state ->
            sharedViewModel.updateBuildingsData { ballroom2Heating = state }
        }
        setupEditTextListener(R.id.etBallroom2Temp) { text ->
            sharedViewModel.updateBuildingsData { ballroom2Temp = text }
        }

        // КПЗ ОПУ
        setupButtonListener(R.id.btnKpzOpuValve) { state ->
            sharedViewModel.updateBuildingsData { kpzOpuValve = state }
        }
        setupButtonListener(R.id.btnKpzOpuHeating) { state ->
            sharedViewModel.updateBuildingsData { kpzOpuHeating = state }
        }
        setupEditTextListener(R.id.etKpzOpuTemp) { text ->
            sharedViewModel.updateBuildingsData { kpzOpuTemp = text }
        }

        // КПЗ-2
        setupButtonListener(R.id.btnKpz2Valve) { state ->
            sharedViewModel.updateBuildingsData { kpz2Valve = state }
        }
        setupButtonListener(R.id.btnKpz2Heating) { state ->
            sharedViewModel.updateBuildingsData { kpz2Heating = state }
        }
        setupEditTextListener(R.id.etKpz2Temp) { text ->
            sharedViewModel.updateBuildingsData { kpz2Temp = text }
        }

        // Насосная пожаротушения
        setupButtonListener(R.id.btnFirePumpValve) { state ->
            sharedViewModel.updateBuildingsData { firePumpValve = state }
        }
        setupButtonListener(R.id.btnFirePumpHeating) { state ->
            sharedViewModel.updateBuildingsData { firePumpHeating = state }
        }
        setupEditTextListener(R.id.etFirePumpTemp) { text ->
            sharedViewModel.updateBuildingsData { firePumpTemp = text }
        }

        // Мастерская по ремонту ВВ
        setupButtonListener(R.id.btnWorkshopHeating) { state ->
            sharedViewModel.updateBuildingsData { workshopHeating = state }
        }
        setupEditTextListener(R.id.etWorkshopTemp) { text ->
            sharedViewModel.updateBuildingsData { workshopTemp = text }
        }

        // Артскважина
        setupButtonListener(R.id.btnArtWellHeating) { state ->
            sharedViewModel.updateBuildingsData { artWellHeating = state }
        }

        // Здание артезианской скважины
        setupButtonListener(R.id.btnArtesianWellHeating) { state ->
            sharedViewModel.updateBuildingsData { artesianWellHeating = state }
        }

        // Помещение 1 (2) АБ
        setupButtonListener(R.id.btnRoomAbHeating) { state ->
            sharedViewModel.updateBuildingsData { roomAbHeating = state }
        }
        setupEditTextListener(R.id.etRoomAbTemp) { text ->
            sharedViewModel.updateBuildingsData { roomAbTemp = text }
        }

        // Помещение п/этажа №1,2,3
        setupButtonListener(R.id.btnBasementHeating) { state ->
            sharedViewModel.updateBuildingsData { basementHeating = state }
        }
        setupEditTextListener(R.id.etBasementTemp) { text ->
            sharedViewModel.updateBuildingsData { basementTemp = text }
        }
    }

    private fun setupButtonListener(buttonId: Int, onStateChanged: (String) -> Unit) {
        val button = rootView.findViewById<ImageButton>(buttonId)
        button.setOnClickListener {
            if (isUpdatingUIFromViewModel) return@setOnClickListener

            val currentState = button.tag as? String ?: "○"
            val newState = when (currentState) {
                "○" -> "+"
                "+" -> "−"
                else -> "○"
            }

            button.tag = newState
            when (newState) {
                "+" -> {
                    button.setImageResource(R.drawable.ic_state_ok)
                    button.setColorFilter(null)
                }
                "−" -> {
                    button.setImageResource(R.drawable.ic_state_error)
                    button.setColorFilter(null)
                }
                else -> {
                    button.setImageResource(R.drawable.ic_state_normal)
                    button.setColorFilter(null)
                }
            }

            onStateChanged(newState)
        }
        // Инициализация
        if (button.tag == null) {
            button.tag = "○"
            button.setImageResource(R.drawable.ic_state_normal)
            button.setColorFilter(null)
        }
    }

    private fun setupEditTextListener(editTextId: Int, onTextChanged: (String) -> Unit) {
        val editText = rootView.findViewById<EditText>(editTextId)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                onTextChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _rootView = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = InspectionBuildings()
    }
}