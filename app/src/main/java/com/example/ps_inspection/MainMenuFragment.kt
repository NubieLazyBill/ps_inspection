package com.example.ps_inspection

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var lastInspectionManager: LastInspectionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lastInspectionManager = LastInspectionManager(requireContext())

        setupButtons()
        checkForSavedInspection()
    }

    private fun setupButtons() {
        binding.cardStartInspection.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_homeScreen)
        }

        binding.cardArchive.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_archiveFragment)
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Настройки (в разработке)", Toast.LENGTH_SHORT).show()
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun checkForSavedInspection() {
        val hasSavedInspection = lastInspectionManager.hasSavedInspection()
        if (hasSavedInspection) {
            showRestoreDialog()
        }
    }

    private fun showRestoreDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("💾 Восстановление осмотра")
            .setMessage("Обнаружен сохранённый осмотр от ${lastInspectionManager.getLastInspectionDate() ?: "неизвестной даты"}\n\nВосстановить его?")
            .setPositiveButton("Восстановить") { _, _ ->
                restoreLastInspection()
            }
            .setNegativeButton("Удалить сохранение") { _, _ ->
                lastInspectionManager.clearLastInspection()
                Toast.makeText(requireContext(), "Сохранённый осмотр удалён", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Позже", null)
            .show()
    }

    private fun restoreLastInspection() {
        val oru35Data = lastInspectionManager.getLastOru35Data()
        val oru220Data = lastInspectionManager.getLastOru220Data()
        val oru500Data = lastInspectionManager.getLastOru500Data()
        val atgData = lastInspectionManager.getLastAtgData()
        val buildingsData = lastInspectionManager.getLastBuildingsData()
        val date = lastInspectionManager.getLastInspectionDate()

        // Восстанавливаем данные
        oru35Data?.let {
            sharedViewModel.updateORU35Data {
                tsn2 = it.tsn2; tsn3 = it.tsn3; tsn4 = it.tsn4
                tt352tsnA = it.tt352tsnA; tt352tsnB = it.tt352tsnB; tt352tsnC = it.tt352tsnC
                tt353tsnA = it.tt353tsnA; tt353tsnB = it.tt353tsnB; tt353tsnC = it.tt353tsnC
                v352tsnA = it.v352tsnA; v352tsnB = it.v352tsnB; v352tsnC = it.v352tsnC
                v353tsnA = it.v353tsnA; v353tsnB = it.v353tsnB; v353tsnC = it.v353tsnC
                tn352atg = it.tn352atg; tn353atg = it.tn353atg
            }
        }

        oru220Data?.let { sharedViewModel.updateORU220Data { /* копирование полей */ } }
        oru500Data?.let { sharedViewModel.updateORU500Data { /* копирование полей */ } }
        atgData?.let { sharedViewModel.updateATGData { /* копирование полей */ } }
        buildingsData?.let { sharedViewModel.updateBuildingsData { /* копирование полей */ } }

        Toast.makeText(requireContext(), "Осмотр от $date восстановлен", Toast.LENGTH_LONG).show()

        // Не удаляем сохранённый осмотр, чтобы можно было восстановить несколько раз
        // lastInspectionManager.clearLastInspection() - НЕ вызываем!

        // Переход на экран выбора объекта
        findNavController().navigate(R.id.homeScreen)
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("О программе")
            .setMessage("""
                ПС 500кВ «Кустовая»
                Система осмотра оборудования
                
                Версия 2.0
                © 2026
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}