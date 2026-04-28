package com.example.ps_inspection

import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentsDialogFragment : DialogFragment() {

    private var equipmentName: String = ""
    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val commentsList = mutableListOf<String>()

    companion object {
        fun newInstance(equipmentName: String): CommentsDialogFragment {
            val fragment = CommentsDialogFragment()
            val args = Bundle()
            args.putString("equipment_name", equipmentName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        equipmentName = arguments?.getString("equipment_name") ?: ""
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Заголовок
        root.addView(TextView(requireContext()).apply {
            text = "💬 Комментарии: $equipmentName"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        })

        // Список комментариев
        listView = ListView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(listView)

        // Кнопка добавления
        val btnAdd = Button(requireContext()).apply {
            text = "➕ Добавить комментарий"
            setPadding(0, 16, 0, 16)
        }
        root.addView(btnAdd)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        loadComments()

        btnAdd.setOnClickListener {
            showAddCommentDialog()
        }

        return root
    }

    private fun loadComments() {
        commentsList.clear()

        val allComments = sharedViewModel.atgComments.value
        val currentComments = allComments[equipmentName] ?: emptyList()

        if (currentComments.isNotEmpty()) {
            commentsList.addAll(currentComments)
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, commentsList)
            Toast.makeText(requireContext(), "Загружено ${currentComments.size} комментариев", Toast.LENGTH_SHORT).show()
        } else {
            val emptyList = listOf("📝 Нет комментариев\n\nНажмите + чтобы добавить")
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, emptyList)
            Toast.makeText(requireContext(), "Нет комментариев для $equipmentName", Toast.LENGTH_SHORT).show()
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            if (commentsList.isNotEmpty() && position < commentsList.size) {
                val comment = commentsList[position]
                showEditCommentDialog(position, comment)
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (commentsList.isNotEmpty() && position < commentsList.size) {
                showDeleteCommentDialog(position)
                true
            } else {
                false
            }
        }
    }

    private fun showAddCommentDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Введите комментарий для $equipmentName..."
            setPadding(24, 16, 24, 16)
            setMinimumHeight(120)
            gravity = android.view.Gravity.TOP
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✏️ Новый комментарий")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val text = input.text.toString()
                if (text.isNotBlank()) {
                    sharedViewModel.addATGComment(equipmentName, text)
                    loadComments()
                    (parentFragment as? InspectionATG)?.updateCommentButtonsState(sharedViewModel.atgComments.value)
                    Toast.makeText(requireContext(), "Комментарий добавлен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditCommentDialog(position: Int, currentComment: String) {
        val input = EditText(requireContext()).apply {
            setText(currentComment)
            hint = "Редактировать комментарий..."
            setPadding(24, 16, 24, 16)
            setMinimumHeight(120)
            gravity = android.view.Gravity.TOP
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✏️ Редактировать комментарий")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newText = input.text.toString()
                if (newText.isNotBlank()) {
                    sharedViewModel.updateATGComment(equipmentName, position, newText)
                    loadComments()
                    Toast.makeText(requireContext(), "Комментарий обновлён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Удалить") { _, _ ->
                sharedViewModel.removeATGComment(equipmentName, position)
                loadComments()
                Toast.makeText(requireContext(), "Комментарий удалён", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    private fun showDeleteCommentDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить комментарий?")
            .setMessage("Удалить этот комментарий?")
            .setPositiveButton("Удалить") { _, _ ->
                sharedViewModel.removeATGComment(equipmentName, position)
                loadComments()
                Toast.makeText(requireContext(), "Комментарий удалён", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}