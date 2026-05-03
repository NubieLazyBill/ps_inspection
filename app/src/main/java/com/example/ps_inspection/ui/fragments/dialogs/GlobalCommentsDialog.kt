package com.example.ps_inspection.ui.fragments.dialogs

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.google.android.material.button.MaterialButton

class GlobalCommentsDialog : DialogFragment() {

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()

    companion object {
        fun newInstance(): GlobalCommentsDialog = GlobalCommentsDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Заголовок
        root.addView(TextView(requireContext()).apply {
            text = "💬 Все комментарии"
            textSize = 18f
            setPadding(0, 0, 0, 16)
            setTypeface(null, Typeface.BOLD)
        })

        // ScrollView с комментариями
        val scrollView = ScrollView(requireContext())
        val contentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
        }

        // Собираем все комментарии из ViewModel
        fun addCommentSection(title: String, comments: List<String>) {
            if (comments.isEmpty() || comments.all { it.isBlank() }) return

            val titleView = TextView(requireContext()).apply {
                text = "📌 $title"
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 8)
                setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
            contentLayout.addView(titleView)

            comments.forEach { comment ->
                if (comment.isNotBlank()) {
                    val commentView = TextView(requireContext()).apply {
                        text = "• $comment"
                        textSize = 14f
                        setPadding(16, 4, 16, 4)
                    }
                    contentLayout.addView(commentView)
                }
            }
        }

        // ОРУ-35
        sharedViewModel.oru35Comments.value.forEach { (equipment, comments) ->
            addCommentSection("ОРУ-35: $equipment", comments)
        }

        // ОРУ-220
        sharedViewModel.oru220Comments.value.forEach { (equipment, comments) ->
            addCommentSection("ОРУ-220: $equipment", comments)
        }

        // ОРУ-500
        sharedViewModel.oru500Comments.value.forEach { (equipment, comments) ->
            addCommentSection("ОРУ-500: $equipment", comments)
        }

        // АТГ
        sharedViewModel.atgComments.value.forEach { (equipment, comments) ->
            addCommentSection("АТГ: $equipment", comments)
        }

        // Здания
        sharedViewModel.buildingsComments.value.forEach { (equipment, comments) ->
            addCommentSection("Здания: $equipment", comments)
        }

        if (contentLayout.childCount == 0) {
            contentLayout.addView(TextView(requireContext()).apply {
                text = "📭 Нет комментариев"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            })
        }

        scrollView.addView(contentLayout)
        root.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        // Кнопка закрытия
        val btnClose = MaterialButton(requireContext()).apply {
            text = "Закрыть"
        }
        root.addView(btnClose, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        btnClose.setOnClickListener { dismiss() }

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return root
    }
}