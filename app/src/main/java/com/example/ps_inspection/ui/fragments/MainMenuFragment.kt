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
import com.example.ps_inspection.data.repositories.UserManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.widget.Button

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager  // ← Добавить

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

        userManager = UserManager(requireContext())
        updateUserDisplay()

        binding.cardUserSelect.setOnClickListener {
            showUserSelectionDialog()
        }

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


        binding.cardGraphs.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("1716443098"))
            startActivity(intent)
        }
    }

    private fun showAboutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null)

        val tvVersion = dialogView.findViewById<TextView>(R.id.tvVersion)
        tvVersion.text = "Версия ${BuildConfig.VERSION_NAME} (сборка ${BuildConfig.VERSION_CODE})"

        val dialog = AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()

        val btnClose = dialogView.findViewById<Button>(R.id.btnClose)
        btnClose?.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun updateUserDisplay() {
        val user = userManager.getCurrentUser()
        val initials = user.name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvUserInitials.text = initials
        binding.tvUserName.text = user.name.split(" ").first()
    }

    private fun showUserSelectionDialog() {
        val users = userManager.getUsers()
        val currentUser = userManager.getCurrentUser()
        val names = users.map { "${it.name} (${it.position})" }.toTypedArray()
        val checkedIndex = users.indexOfFirst { it.name == currentUser.name }

        AlertDialog.Builder(requireContext())
            .setTitle("👤 Выберите дежурного")
            .setSingleChoiceItems(names, checkedIndex) { dialog: DialogInterface, which: Int ->
                userManager.saveCurrentUser(users[which])
                updateUserDisplay()
                dialog.dismiss()
                Toast.makeText(requireContext(), "Выбран: ${users[which].name}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}