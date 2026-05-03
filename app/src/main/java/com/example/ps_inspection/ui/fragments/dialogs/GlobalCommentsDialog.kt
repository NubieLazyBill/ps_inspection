package com.example.ps_inspection.ui.fragments.dialogs

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.ps_inspection.data.models.Comment
import com.example.ps_inspection.data.utils.ExcelExportService
import com.example.ps_inspection.viewmodel.SharedInspectionViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.content.Intent

class GlobalCommentsDialog : DialogFragment() {

    private val sharedViewModel: SharedInspectionViewModel by activityViewModels()
    private var totalCommentsCount = 0
    private lateinit var contentLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var resultCounter: TextView
    private lateinit var exportService: ExcelExportService

    private var selectedSection = "Все"
    private var searchQuery = ""

    // Данные для фильтрации
    private data class CommentItem(
        val section: String,
        val equipment: String,
        val comment: Comment
    )

    private val allComments = mutableListOf<CommentItem>()
    private var filteredComments = mutableListOf<CommentItem>()

    companion object {
        fun newInstance(): GlobalCommentsDialog = GlobalCommentsDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog)
        exportService = ExcelExportService(requireContext())
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

        // Панель фильтрации
        val filterCard = MaterialCardView(requireContext()).apply {
            radius = 12f
            cardElevation = 2f
            setCardBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        val filterLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
        }

        // Выпадающий список секций
        val sectionLabel = TextView(requireContext()).apply {
            text = "Фильтр по секции:"
            textSize = 12f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 0, 0, 4)
        }
        filterLayout.addView(sectionLabel)

        val sectionSpinner = Spinner(requireContext()).apply {
            val sections = listOf("Все", "ОРУ-35", "ОРУ-220", "ОРУ-500", "АТГ", "Здания")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sections)
            this.adapter = adapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedSection = sections[position]
                    applyFilters()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
        filterLayout.addView(sectionSpinner)

        // Поле поиска
        val searchLabel = TextView(requireContext()).apply {
            text = "Поиск по оборудованию:"
            textSize = 12f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 12, 0, 4)
        }
        filterLayout.addView(searchLabel)

        val searchInput = EditText(requireContext()).apply {
            hint = "Введите название оборудования..."
            setPadding(16, 12, 16, 12)
            background = androidx.appcompat.widget.AppCompatEditText(requireContext()).background
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchQuery = s?.toString()?.lowercase() ?: ""
                    applyFilters()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        filterLayout.addView(searchInput)

        // Кнопка экспорта
        val btnExport = MaterialButton(requireContext()).apply {
            text = "📎 Экспортировать в Excel"
            setPadding(12, 8, 12, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2196F3"))
        }
        filterLayout.addView(btnExport)

        filterCard.addView(filterLayout)
        root.addView(filterCard)

        // Счётчик результатов
        resultCounter = TextView(requireContext()).apply {
            id = View.generateViewId()
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
            setPadding(0, 8, 0, 8)
        }
        root.addView(resultCounter)

        // ScrollView с комментариями
        scrollView = ScrollView(requireContext())
        contentLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }
        root.addView(btnClose)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        loadAllCommentsData()
        applyFilters()

        btnExport.setOnClickListener {
            exportComments()
        }

        btnClose.setOnClickListener { dismiss() }

        return root
    }

    private fun exportComments() {
        if (filteredComments.isEmpty()) {
            Toast.makeText(requireContext(), "Нет комментариев для экспорта", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Подготавливаем данные для экспорта
            val exportData = filteredComments.map { commentItem ->
                "${commentItem.section}: ${commentItem.equipment}" to commentItem.comment
            }

            val fileUri = exportService.exportCommentsToExcel(exportData, selectedSection)

            if (fileUri != null) {
                Toast.makeText(requireContext(), "Файл сохранён в кэш приложения", Toast.LENGTH_LONG).show()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Поделиться файлом комментариев"))
            } else {
                Toast.makeText(requireContext(), "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadAllCommentsData() {
        allComments.clear()
        totalCommentsCount = 0

        // ОРУ-35
        sharedViewModel.oru35Comments.value.forEach { (equipment, comments) ->
            comments.forEach { comment ->
                allComments.add(CommentItem("ОРУ-35", equipment, comment))
                totalCommentsCount++
            }
        }

        // ОРУ-220
        sharedViewModel.oru220Comments.value.forEach { (equipment, comments) ->
            comments.forEach { comment ->
                allComments.add(CommentItem("ОРУ-220", equipment, comment))
                totalCommentsCount++
            }
        }

        // ОРУ-500
        sharedViewModel.oru500Comments.value.forEach { (equipment, comments) ->
            comments.forEach { comment ->
                allComments.add(CommentItem("ОРУ-500", equipment, comment))
                totalCommentsCount++
            }
        }

        // АТГ
        sharedViewModel.atgComments.value.forEach { (equipment, comments) ->
            comments.forEach { comment ->
                allComments.add(CommentItem("АТГ", equipment, comment))
                totalCommentsCount++
            }
        }

        // Здания
        sharedViewModel.buildingsComments.value.forEach { (equipment, comments) ->
            comments.forEach { comment ->
                allComments.add(CommentItem("Здания", equipment, comment))
                totalCommentsCount++
            }
        }

        // Сортируем по времени (сначала новые)
        allComments.sortByDescending { it.comment.timestamp }
    }

    private fun applyFilters() {
        filteredComments.clear()

        filteredComments.addAll(allComments.filter { item ->
            // Фильтр по секции
            val sectionMatch = selectedSection == "Все" || item.section == selectedSection

            // Фильтр по поиску
            val searchMatch = searchQuery.isEmpty() ||
                    item.equipment.lowercase().contains(searchQuery) ||
                    item.comment.text.lowercase().contains(searchQuery) ||
                    item.section.lowercase().contains(searchQuery)

            sectionMatch && searchMatch
        })

        updateUI()
    }

    private fun updateUI() {
        contentLayout.removeAllViews()

        val resultCount = filteredComments.size

        // Обновляем счётчик
        resultCounter.text = "Найдено комментариев: $resultCount (всего: $totalCommentsCount)"

        if (filteredComments.isEmpty()) {
            contentLayout.addView(TextView(requireContext()).apply {
                text = "📭 Нет комментариев по вашему запросу"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            })
            return
        }

        // Группируем по секции и оборудованию для отображения
        val grouped = filteredComments.groupBy { "${it.section}: ${it.equipment}" }

        grouped.toSortedMap().forEach { (title, items) ->
            // Заголовок секции
            val titleView = TextView(requireContext()).apply {
                text = "📌 $title (${items.size})"
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 8)
                setTextColor(Color.parseColor("#4CAF50"))
            }
            contentLayout.addView(titleView)

            // Комментарии в этой группе
            items.sortedByDescending { it.comment.timestamp }.forEach { item ->
                val commentView = TextView(requireContext()).apply {
                    text = "• ${item.comment.getFormattedTime()}\n  ${item.comment.text}"
                    textSize = 14f
                    setPadding(16, 8, 16, 8)
                }
                contentLayout.addView(commentView)
            }
        }
    }
}