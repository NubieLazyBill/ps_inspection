// InspectionBuildings.kt
package com.example.ps_inspection

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InspectionBuildings : Fragment() {

    // Переименовываем свойство, чтобы избежать конфликта с getView() из Fragment
    private var _rootView: View? = null
    private val rootView get() = _rootView!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private var isUpdatingUIFromViewModel = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _rootView = inflater.inflate(R.layout.fragment_inspection_buildings, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.buildingsData.collectLatest { data ->
                updateUIFromData(data)
            }
        }

        setupInputListeners()
    }

    private fun updateUIFromData(data: InspectionBuildingsData) {
        isUpdatingUIFromViewModel = true

        // Компрессорная №1
        updateButtonState(rootView.findViewById(R.id.btnCompressor1Valve), data.compressor1Valve)
        updateButtonState(rootView.findViewById(R.id.btnCompressor1Heating), data.compressor1Heating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etCompressor1Temp), data.compressor1Temp)

        // Баллоная №1
        updateButtonState(rootView.findViewById(R.id.btnBallroom1Valve), data.ballroom1Valve)
        updateButtonState(rootView.findViewById(R.id.btnBallroom1Heating), data.ballroom1Heating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etBallroom1Temp), data.ballroom1Temp)

        // Компрессорная №2
        updateButtonState(rootView.findViewById(R.id.btnCompressor2Valve), data.compressor2Valve)
        updateButtonState(rootView.findViewById(R.id.btnCompressor2Heating), data.compressor2Heating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etCompressor2Temp), data.compressor2Temp)

        // Баллоная №2
        updateButtonState(rootView.findViewById(R.id.btnBallroom2Valve), data.ballroom2Valve)
        updateButtonState(rootView.findViewById(R.id.btnBallroom2Heating), data.ballroom2Heating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etBallroom2Temp), data.ballroom2Temp)

        // КПЗ ОПУ
        updateButtonState(rootView.findViewById(R.id.btnKpzOpuValve), data.kpzOpuValve)
        updateButtonState(rootView.findViewById(R.id.btnKpzOpuHeating), data.kpzOpuHeating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etKpzOpuTemp), data.kpzOpuTemp)

        // КПЗ-2
        updateButtonState(rootView.findViewById(R.id.btnKpz2Valve), data.kpz2Valve)
        updateButtonState(rootView.findViewById(R.id.btnKpz2Heating), data.kpz2Heating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etKpz2Temp), data.kpz2Temp)

        // Насосная пожаротушения
        updateButtonState(rootView.findViewById(R.id.btnFirePumpValve), data.firePumpValve)
        updateButtonState(rootView.findViewById(R.id.btnFirePumpHeating), data.firePumpHeating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etFirePumpTemp), data.firePumpTemp)

        // Мастерская по ремонту ВВ
        updateButtonState(rootView.findViewById(R.id.btnWorkshopHeating), data.workshopHeating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etWorkshopTemp), data.workshopTemp)

        // Артскважина
        updateButtonState(rootView.findViewById(R.id.btnArtWellHeating), data.artWellHeating)

        // Здание артезианской скважины
        updateButtonState(rootView.findViewById(R.id.btnArtesianWellHeating), data.artesianWellHeating)

        // Помещение 1 (2) АБ
        updateButtonState(rootView.findViewById(R.id.btnRoomAbHeating), data.roomAbHeating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etRoomAbTemp), data.roomAbTemp)

        // Помещение п/этажа №1,2,3
        updateButtonState(rootView.findViewById(R.id.btnBasementHeating), data.basementHeating)
        updateEditTextIfNeeded(rootView.findViewById(R.id.etBasementTemp), data.basementTemp)

        isUpdatingUIFromViewModel = false
    }

    private fun updateButtonState(button: Button, state: String) {
        if (button.text.toString() != state) {
            button.text = state
            // Устанавливаем цвет в зависимости от состояния
            when (state) {
                "+" -> button.setTextColor(Color.GREEN)
                "−" -> button.setTextColor(Color.RED)
                else -> button.setTextColor(Color.GRAY)
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
        val button = rootView.findViewById<Button>(buttonId)
        button.setOnClickListener {
            if (isUpdatingUIFromViewModel) return@setOnClickListener

            val currentState = button.text.toString()
            val newState = when (currentState) {
                "○" -> "+"
                "+" -> "−"
                else -> "○"
            }

            button.text = newState
            when (newState) {
                "+" -> button.setTextColor(Color.GREEN)
                "−" -> button.setTextColor(Color.RED)
                else -> button.setTextColor(Color.GRAY)
            }

            onStateChanged(newState)
        }
    }

    private fun setupEditTextListener(editTextId: Int, onTextChanged: (String) -> Unit) {
        val editText = rootView.findViewById<EditText>(editTextId)
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingUIFromViewModel) return
                onTextChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
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