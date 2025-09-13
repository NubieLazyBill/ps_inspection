package com.example.ps_inspection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

class HomeScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим кнопки и назначаем обработчики
        view.findViewById<View>(R.id.inspect_oru_35).setOnClickListener {
            // Переход к InspectionORU35
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU35)
        }

        view.findViewById<View>(R.id.inspect_oru220).setOnClickListener {
            // Переход к InspectionORU220
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU2202)
        }

        view.findViewById<View>(R.id.inspect_oru500).setOnClickListener {
            // Переход к InspectionORU500
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU500)
        }

        view.findViewById<View>(R.id.inspect_buildings).setOnClickListener {
            // Переход к InspectionBuildings
            findNavController().navigate(R.id.action_homeScreen_to_inspectionBuildings)
        }
    }

    companion object {
        /**
         * Упрощенная фабрика для создания экземпляра фрагмента
         */
        @JvmStatic
        fun newInstance() = HomeScreen()
    }
}