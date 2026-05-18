package com.example.ps_inspection.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ps_inspection.R
import com.example.ps_inspection.databinding.FragmentMainMenuBinding
import com.example.ps_inspection.data.repositories.AutoSaveManager
import com.example.ps_inspection.data.repositories.LastInspectionManager
import com.example.ps_inspection.data.repositories.UserManager
import android.content.DialogInterface
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel


class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager
    private lateinit var autoSaveManager: AutoSaveManager
    private lateinit var lastInspectionManager: LastInspectionManager
    private lateinit var sharedViewModel: SharedInspectionViewModel

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
        userManager.addNewUsersIfNeeded()  // ← ДОБАВИТЬ ЭТУ СТРОКУ
        autoSaveManager = AutoSaveManager(requireContext())
        lastInspectionManager = LastInspectionManager(requireContext())
        sharedViewModel = ViewModelProvider(requireActivity())[SharedInspectionViewModel::class.java]

        updateUserDisplay()

        // ✅ ИСПРАВЛЕНО: проверяем, есть ли выбранный пользователь
        if (!userManager.hasSelectedUser()) {
            showUserSelectionWithPasswordDialog(isFirstLaunch = true)
        }

        binding.cardUserSelect.setOnClickListener {
            showUserSelectionWithPasswordDialog(isFirstLaunch = false)
        }

        // КНОПКА "ПРОДОЛЖИТЬ ОСМОТР"
        binding.cardStartInspection.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenu_to_homeScreen)
        }

        // НОВАЯ КНОПКА "НОВЫЙ ОСМОТР"
        binding.cardNewInspection.setOnClickListener {
            showNewInspectionConfirmDialog()
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
            findNavController().navigate(R.id.action_mainMenu_to_graphsFragment)
        }
    }

    private fun showUserSelectionWithPasswordDialog(isFirstLaunch: Boolean = false) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_selection_with_password, null)
        val spinnerUser = dialogView.findViewById<Spinner>(R.id.spinnerUser)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val tvError = dialogView.findViewById<TextView>(R.id.tvError)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val users = userManager.getUsers()
        val userNames = users.map { "${it.name} (${it.position})" }.toTypedArray()
        var selectedUser = users.first()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUser.adapter = adapter

        spinnerUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUser = users[position]
                tvError.visibility = View.GONE
                etPassword.text?.clear()
                // Динамическая подсказка
                etPassword.hint = "Пароль (например: ${selectedUser.getPassword()})"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Устанавливаем подсказку для первого пользователя
        etPassword.hint = "Пароль (например: ${users.first().getPassword()})"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(!isFirstLaunch)
            .create()

        // Обработчики кнопок ДО показа диалога
        btnConfirm.setOnClickListener {
            val rawPassword = etPassword.text.toString()
            // Очищаем от пробелов, переносов строк и других скрытых символов
            val password = rawPassword
                .trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "")

            // Отладочный тост — показывает, что реально введено
            Toast.makeText(requireContext(), "Введено: '$password'", Toast.LENGTH_SHORT).show()

            if (password.isEmpty()) {
                tvError.text = "Введите пароль"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (userManager.checkPassword(selectedUser, password)) {
                userManager.saveCurrentUser(selectedUser)
                updateUserDisplay()
                dialog.dismiss()
                if (isFirstLaunch) {
                    Toast.makeText(requireContext(), "Добро пожаловать, ${selectedUser.name}!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Выбран: ${selectedUser.name}", Toast.LENGTH_SHORT).show()
                }
            } else {
                tvError.text = "Неверный пароль! Попробуйте снова."
                tvError.visibility = View.VISIBLE
                etPassword.text?.clear()
            }
        }

        btnCancel.setOnClickListener {
            if (!isFirstLaunch) {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showNewInspectionConfirmDialog() {
        val hasAutoSave = autoSaveManager.hasAutoSave()
        val hasLastInspection = lastInspectionManager.hasSavedInspection()

        val message = if (hasAutoSave) {
            "⚠️ ВНИМАНИЕ!\n\nОбнаружены несохранённые данные текущего осмотра.\n\nПри начале нового осмотра все несохранённые данные будут УТЕРЯНЫ безвозвратно!\n\nВы уверены, что хотите начать новый осмотр?"
        } else {
            "⚠️ Вы уверены, что хотите начать новый осмотр?\n\nТекущие данные будут сброшены."
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("🔄 Новый осмотр")
            .setMessage(message)
            .setPositiveButton("✅ Да, начать новый") { _, _ ->
                autoSaveManager.clearAutoSave()
                lastInspectionManager.clearLastInspection()
                sharedViewModel.clearAllData()
                sharedViewModel.initCommentStorage(requireContext())

                Toast.makeText(requireContext(), "Начат новый осмотр", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_mainMenu_to_homeScreen)
            }
            .setNegativeButton("❌ Отмена", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(requireContext().getColor(R.color.teal_700))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext().getColor(R.color.gray_600))
    }

    private fun showAboutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null)
        val tvVersion = dialogView.findViewById<TextView>(R.id.tvVersion)

        // Получаем версию через PackageManager
        val versionName = try {
            context?.packageManager?.getPackageInfo(context?.packageName ?: "", 0)?.versionName ?: "2.5.4"
        } catch (e: Exception) {
            "2.5.4"
        }

        tvVersion.text = "Версия $versionName"

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}