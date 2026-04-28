package com.example.ps_inspection.ui.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.data.utils.ExcelExportService
import com.example.ps_inspection.R
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.example.ps_inspection.databinding.FragmentHomeScreenBinding

class HomeScreen : Fragment() {
    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Навигация по карточкам (Сетка)
        binding.cardOru35.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU35)
        }
        binding.cardOru220.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU2202)
        }
        binding.cardOru500.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU500)
        }
        binding.cardAtg.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionATG)
        }
        binding.cardBuildings.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionBuildings)
        }

        // 2. Кнопки нижней панели
        binding.btnBottomArchive.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_archiveFragment)
        }

        binding.btnBottomSave.setOnClickListener {
            exportToExcel()
        }

        binding.btnBottomClear.setOnClickListener {
            clearAllData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportToExcel() {
        try {
            val oru35Data = sharedViewModel.oru35Data.value
            val oru220Data = sharedViewModel.oru220Data.value
            val atgData = sharedViewModel.atgData.value
            val oru500Data = sharedViewModel.oru500Data.value
            val buildingsData = sharedViewModel.buildingsData.value

            val exportService = ExcelExportService(requireContext())
            val fileUri = exportService.exportToExcel(oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            if (fileUri != null) {
                Toast.makeText(requireContext(), "✅ Осмотр сохранён в Excel", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "❌ Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "❌ Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearAllData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Очистка данных")
            .setMessage("Вы уверены, что хотите очистить все данные осмотра?")
            .setPositiveButton("Очистить") { _, _ ->
                sharedViewModel.clearAllData()
                Toast.makeText(requireContext(), "Все данные очищены", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}