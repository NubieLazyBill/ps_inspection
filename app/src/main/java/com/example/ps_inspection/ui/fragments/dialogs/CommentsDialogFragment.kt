package com.example.ps_inspection.ui.fragments.dialogs

import android.R
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.ps_inspection.ui.fragments.inspections.InspectionATG
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU220
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU35
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentsDialogFragment : DialogFragment() {

    private var equipmentName: String = ""
    private var equipmentType: String = ""  // "ATG" или "ORU35"
    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val commentsList = mutableListOf<String>()

    companion object {
        fun newInstance(equipmentName: String, equipmentType: String = "ATG"): CommentsDialogFragment {
            val fragment = CommentsDialogFragment()
            val args = Bundle()
            args.putString("equipment_name", equipmentName)
            args.putString("equipment_type", equipmentType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        equipmentName = arguments?.getString("equipment_name") ?: ""
        equipmentType = arguments?.getString("equipment_type") ?: "ATG"
        setStyle(STYLE_NORMAL, R.style.Theme_DeviceDefault_Light_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Заголовок
        root.addView(TextView(requireContext()).apply {
            text = "💬 Комментарии: $equipmentName"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
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

    private fun getCurrentComments(): List<String> {
        return if (equipmentType == "ATG") {
            sharedViewModel.atgComments.value[equipmentName] ?: emptyList()
        } else if (equipmentType == "ORU35") {
            sharedViewModel.oru35Comments.value[equipmentName] ?: emptyList()
        } else if (equipmentType == "ORU220") {
            sharedViewModel.oru220Comments.value[equipmentName] ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun saveComment(comment: String) {
        when (equipmentType) {
            "ATG" -> sharedViewModel.addATGComment(equipmentName, comment)
            "ORU35" -> sharedViewModel.addORU35Comment(equipmentName, comment)
            "ORU220" -> sharedViewModel.addORU220Comment(equipmentName, comment)
        }
    }

    private fun deleteComment() {
        when (equipmentType) {
            "ATG" -> {
                val comments = sharedViewModel.atgComments.value[equipmentName] ?: emptyList()
                if (comments.isNotEmpty()) {
                    sharedViewModel.removeATGComment(equipmentName, comments.size - 1)
                }
            }
            "ORU35" -> {
                val comments = sharedViewModel.oru35Comments.value[equipmentName] ?: emptyList()
                if (comments.isNotEmpty()) {
                    sharedViewModel.removeORU35Comment(equipmentName, comments.size - 1)
                }
            }
            "ORU220" -> {
                val comments = sharedViewModel.oru220Comments.value[equipmentName] ?: emptyList()
                if (comments.isNotEmpty()) {
                    sharedViewModel.removeORU220Comment(equipmentName, comments.size - 1)
                }
            }
        }
    }

    private fun updateComment(newComment: String) {
        when (equipmentType) {
            "ATG" -> {
                val comments = sharedViewModel.atgComments.value[equipmentName] ?: emptyList()
                if (comments.isNotEmpty()) {
                    sharedViewModel.updateATGComment(equipmentName, comments.size - 1, newComment)
                } else {
                    sharedViewModel.addATGComment(equipmentName, newComment)
                }
            }
            "ORU35" -> {
                val comments = sharedViewModel.oru35Comments.value[equipmentName] ?: emptyList()
                if (comments.isNotEmpty()) {
                    sharedViewModel.updateORU35Comment(equipmentName, comments.size - 1, newComment)
                } else {
                    sharedViewModel.addORU35Comment(equipmentName, newComment)
                }
            }
            "ORU220" -> {
                val comments = sharedViewModel.oru220Comments.value[equipmentName] ?: emptyList()
                if (comments.isNotEmpty()) {
                    sharedViewModel.updateORU220Comment(equipmentName, comments.size - 1, newComment)
                } else {
                    sharedViewModel.addORU220Comment(equipmentName, newComment)
                }
            }
        }
    }

    private fun loadComments() {
        commentsList.clear()

        val currentComments = getCurrentComments()

        if (currentComments.isNotEmpty()) {
            commentsList.addAll(currentComments)
            adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, commentsList)
            Toast.makeText(requireContext(), "Загружено ${currentComments.size} комментариев", Toast.LENGTH_SHORT).show()
        } else {
            val emptyList = listOf("📝 Нет комментариев\n\nНажмите + чтобы добавить")
            adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, emptyList)
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
            gravity = Gravity.TOP
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✏️ Новый комментарий")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ ->
                val text = input.text.toString()
                if (text.isNotBlank()) {
                    saveComment(text)
                    loadComments()
                    updateParentButtonState()
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
            gravity = Gravity.TOP
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✏️ Редактировать комментарий")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newText = input.text.toString()
                if (newText.isNotBlank()) {
                    updateComment(newText)
                    loadComments()
                    updateParentButtonState()
                    Toast.makeText(requireContext(), "Комментарий обновлён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Удалить") { _, _ ->
                deleteComment()
                loadComments()
                updateParentButtonState()
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
                deleteComment()
                loadComments()
                updateParentButtonState()
                Toast.makeText(requireContext(), "Комментарий удалён", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateParentButtonState() {
        when (equipmentType) {
            "ATG" -> (parentFragment as? InspectionATG)?.updateCommentButtonsState(sharedViewModel.atgComments.value)
            "ORU35" -> (parentFragment as? InspectionORU35)?.updateCommentButtonsState(sharedViewModel.oru35Comments.value)
            "ORU220" -> (parentFragment as? InspectionORU220)?.updateCommentButtonsState(sharedViewModel.oru220Comments.value)
        }
    }
}