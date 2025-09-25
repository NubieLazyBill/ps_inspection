package com.example.ps_inspection

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class InspectionBuildings : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inspection_buildings, container, false)

        // Находим и настраиваем все кнопки
        setupToggleButtons(view)

        return view
    }

    private fun setupToggleButtons(view: View) {
        // Список всех ID кнопок, которые должны переключаться
        val toggleButtonIds = listOf(
            R.id.btnCompressor1Valve, R.id.btnCompressor1Heating,
            R.id.btnBallroom1Valve, R.id.btnBallroom1Heating,
            R.id.btnCompressor2Valve, R.id.btnCompressor2Heating,
            R.id.btnBallroom2Valve, R.id.btnBallroom2Heating,
            R.id.btnKpzOpuValve, R.id.btnKpzOpuHeating,
            R.id.btnKpz2Valve, R.id.btnKpz2Heating,
            R.id.btnFirePumpValve, R.id.btnFirePumpHeating,
            R.id.btnWorkshopHeating,
            R.id.btnArtWellHeating,
            R.id.btnArtesianWellHeating,
            R.id.btnRoomAbHeating,
            R.id.btnBasementHeating
        )

        // Настраиваем обработчик для каждой кнопки
        toggleButtonIds.forEach { buttonId ->
            val button = view.findViewById<Button>(buttonId)
            button.setOnClickListener { toggleButtonState(button) }
        }
    }

    private fun toggleButtonState(button: Button) {
        when (button.text.toString()) {
            "○" -> { // Не осмотрено -> Плюс
                button.text = "+"
                button.setTextColor(Color.GREEN)
            }
            "+" -> { // Плюс -> Минус
                button.text = "−"
                button.setTextColor(Color.RED)
            }
            "−" -> { // Минус -> Не осмотрено
                button.text = "○"
                button.setTextColor(Color.GRAY)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = InspectionBuildings()
    }
}