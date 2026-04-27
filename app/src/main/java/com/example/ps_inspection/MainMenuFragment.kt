package com.example.ps_inspection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

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