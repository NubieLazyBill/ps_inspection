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
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.ui.fragments.inspections.InspectionATG
import com.example.ps_inspection.ui.fragments.inspections.InspectionBuildings
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU220
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU35
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU500
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommentsDialogFragment : DialogFragment() {

    private var equipmentName: String = ""
    private var equipmentType: String = ""
    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val commentsList = mutableListOf<String>()
    private var currentComments = listOf<Comment>()

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

        root.addView(TextView(requireContext()).apply {
            text = "💬 Комментарии: $equipmentName"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        })

        listView = ListView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(listView)

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

    private fun getCurrentComments(): List<Comment> {
        return when (equipmentType) {
            "ATG" -> sharedViewModel.atgComments.value[equipmentName] ?: emptyList()
            "ORU35" -> sharedViewModel.oru35Comments.value[equipmentName] ?: emptyList()
            "ORU220" -> sharedViewModel.oru220Comments.value[equipmentName] ?: emptyList()
            "ORU500" -> sharedViewModel.oru500Comments.value[equipmentName] ?: emptyList()
            "BUILDINGS" -> sharedViewModel.buildingsComments.value[equipmentName] ?: emptyList()
            else -> emptyList()
        }
    }

    private fun saveComment(commentText: String) {
        when (equipmentType) {
            "ATG" -> sharedViewModel.addATGComment(equipmentName, commentText)
            "ORU35" -> sharedViewModel.addORU35Comment(equipmentName, commentText)
            "ORU220" -> sharedViewModel.addORU220Comment(equipmentName, commentText)
            "ORU500" -> sharedViewModel.addORU500Comment(equipmentName, commentText)
            "BUILDINGS" -> sharedViewModel.addBuildingsComment(equipmentName, commentText)
        }
    }

    private fun deleteComment(index: Int) {
        when (equipmentType) {
            "ATG" -> sharedViewModel.removeATGComment(equipmentName, index)
            "ORU35" -> sharedViewModel.removeORU35Comment(equipmentName, index)
            "ORU220" -> sharedViewModel.removeORU220Comment(equipmentName, index)
            "ORU500" -> sharedViewModel.removeORU500Comment(equipmentName, index)
            "BUILDINGS" -> sharedViewModel.removeBuildingsComment(equipmentName, index)
        }
    }

    private fun updateComment(index: Int, newText: String) {
        when (equipmentType) {
            "ATG" -> sharedViewModel.updateATGComment(equipmentName, index, newText)
            "ORU35" -> sharedViewModel.updateORU35Comment(equipmentName, index, newText)
            "ORU220" -> sharedViewModel.updateORU220Comment(equipmentName, index, newText)
            "ORU500" -> sharedViewModel.updateORU500Comment(equipmentName, index, newText)
            "BUILDINGS" -> sharedViewModel.updateBuildingsComment(equipmentName, index, newText)
        }
    }

    private fun loadComments() {
        commentsList.clear()
        currentComments = getCurrentComments()

        if (currentComments.isNotEmpty()) {
            // Форматируем с датой
            val formatted = currentComments.map { comment ->
                "${comment.getFormattedTimeShort()}\n${comment.text}"
            }
            commentsList.addAll(formatted)
            adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, commentsList)
            Toast.makeText(requireContext(), "Загружено ${currentComments.size} комментариев", Toast.LENGTH_SHORT).show()
        } else {
            val emptyList = listOf("📝 Нет комментариев\n\nНажмите + чтобы добавить")
            adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, emptyList)
            Toast.makeText(requireContext(), "Нет комментариев для $equipmentName", Toast.LENGTH_SHORT).show()
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            if (currentComments.isNotEmpty() && position < currentComments.size) {
                showEditCommentDialog(position, currentComments[position])
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (currentComments.isNotEmpty() && position < currentComments.size) {
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

    private fun showEditCommentDialog(position: Int, comment: Comment) {
        val input = EditText(requireContext()).apply {
            setText(comment.text)
            hint = "Редактировать комментарий..."
            setPadding(24, 16, 24, 16)
            setMinimumHeight(120)
            gravity = Gravity.TOP
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✏️ Редактировать комментарий (${comment.getFormattedTimeShort()})")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newText = input.text.toString()
                if (newText.isNotBlank()) {
                    updateComment(position, newText)
                    loadComments()
                    updateParentButtonState()
                    Toast.makeText(requireContext(), "Комментарий обновлён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Удалить") { _, _ ->
                deleteComment(position)
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
                deleteComment(position)
                loadComments()
                updateParentButtonState()
                Toast.makeText(requireContext(), "Комментарий удалён", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateParentButtonState() {
        when (equipmentType) {
            "ATG" -> (parentFragment as? InspectionATG)?.updateCommentButtonsState(
                sharedViewModel.atgComments.value
            )
            "ORU35" -> (parentFragment as? InspectionORU35)?.updateCommentButtonsState(
                sharedViewModel.oru35Comments.value
            )
            "ORU220" -> (parentFragment as? InspectionORU220)?.updateCommentButtonsState(
                sharedViewModel.oru220Comments.value
            )
            "ORU500" -> (parentFragment as? InspectionORU500)?.refreshAllStates()
            "BUILDINGS" -> (parentFragment as? InspectionBuildings)?.refreshAllStates()
        }
    }
}