package com.example.ps_inspection.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.BuildConfig
import com.example.ps_inspection.R
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
        // Инфлейтим кастомный layout для диалога
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null)

        val tvVersion = dialogView.findViewById<TextView>(R.id.tvVersion)
        val tvCopyright = dialogView.findViewById<TextView>(R.id.tvCopyright)
        val tvAuthor = dialogView.findViewById<TextView>(R.id.tvAuthor)
        val tvLicense = dialogView.findViewById<TextView>(R.id.tvLicense)
        val tvNote = dialogView.findViewById<TextView>(R.id.tvNote)

        // Получаем версию из build.gradle
        val versionName = BuildConfig.VERSION_NAME
        tvVersion.text = "Версия $versionName"

        tvCopyright.text = "© 2026"
        tvAuthor.text = "Разработано: Плотников Алексей Петрович\n"
        tvLicense.text = "Все права защищены.\n"
        tvNote.text = "Программа предназначена исключительно для использования\nна ПС 500кВ «Кустовая».\nКоммерческое использование и тиражирование только с разрешения автора."

        AlertDialog.Builder(requireContext())
            .setTitle("ℹ️ О программе")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}