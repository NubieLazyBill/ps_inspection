package com.example.ps_inspection

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inspectOru35.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU35)
        }

        binding.inspectOru220.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU2202)
        }

        binding.inspectOru500.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionORU500)
        }

        binding.inspectBuildings.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionBuildings)
        }

        binding.inspectATG.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_inspectionATG)
        }

        binding.btnExportExcel.setOnClickListener {
            exportToExcel()
        }

        binding.btnClearData.setOnClickListener {
            clearAllData()
        }
    }

    private fun exportToExcel() {
        try {
            // Проверяем версию API перед вызовом
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val oru35Data = sharedViewModel.oru35Data.value
                val oru220Data = sharedViewModel.oru220Data.value
                val atgData = sharedViewModel.atgData.value
                val oru500Data = sharedViewModel.oru500Data.value
                val buildingsData = sharedViewModel.buildingsData.value

                val exportService = ExcelExportService(requireContext())
                val fileUri = exportService.exportToExcel(oru35Data, oru220Data, atgData, oru500Data, buildingsData)

                if (fileUri != null) {
                    Toast.makeText(requireContext(), "Файл успешно сохранен!", Toast.LENGTH_LONG).show()
                    shareExcelFile(fileUri)
                } else {
                    Toast.makeText(requireContext(), "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Альтернативная реализация для старых версий
                exportToExcelLegacy()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportToExcelLegacy() {
        try {
            val oru35Data = sharedViewModel.oru35Data.value
            val oru220Data = sharedViewModel.oru220Data.value
            val atgData = sharedViewModel.atgData.value
            val oru500Data = sharedViewModel.oru500Data.value
            val buildingsData = sharedViewModel.buildingsData.value

            val exportService = ExcelExportService(requireContext())
            // ВАЖНО: вызываем новый метод для старых устройств
            val fileUri = exportService.exportToExcelLegacy(oru35Data, oru220Data, atgData, oru500Data, buildingsData)

            if (fileUri != null) {
                Toast.makeText(requireContext(), "Файл успешно сохранен в Downloads!", Toast.LENGTH_LONG).show()
                shareExcelFile(fileUri)
            } else {
                Toast.makeText(requireContext(), "Ошибка при сохранении файла", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareExcelFile(fileUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться файлом осмотра"))
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