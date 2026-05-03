package com.example.ps_inspection.ui.fragments.dialogs

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.google.android.material.card.MaterialCardView
import java.io.File

class GlobalMediaDialog : DialogFragment() {

    private lateinit var mediaManager: InspectionMediaManager
    private lateinit var gridLayout: GridLayout
    private lateinit var scrollView: ScrollView
    private lateinit var resultCounter: TextView

    private val allPhotos = mutableListOf<PhotoItem>()
    private var filteredPhotos = mutableListOf<PhotoItem>()

    private var selectedSection = "Все"
    private var searchQuery = ""

    private data class PhotoItem(
        val path: String,
        val section: String,
        val equipment: String,
        val timestamp: Long
    )

    companion object {
        fun newInstance(): GlobalMediaDialog = GlobalMediaDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaManager = InspectionMediaManager(requireContext())
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
            text = "📷 Все фото из приложения"
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

        filterCard.addView(filterLayout)
        root.addView(filterCard)

        // Счётчик результатов
        resultCounter = TextView(requireContext()).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
            setPadding(0, 8, 0, 8)
        }
        root.addView(resultCounter)

        // ScrollView с сеткой фото
        scrollView = ScrollView(requireContext())
        gridLayout = GridLayout(requireContext()).apply {
            columnCount = 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(gridLayout)
        root.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        // Кнопка закрытия
        val btnClose = Button(requireContext()).apply {
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

        collectAllGlobalPhotos()
        applyFilters()
        btnClose.setOnClickListener { dismiss() }

        return root
    }

    private fun getSectionByEquipment(equipmentName: String): String {
        return when {
            // ОРУ-35
            equipmentName in listOf("ТСН", "ТТ-35 2ТСН", "ТТ-35 3ТСН", "В-35 2ТСН", "В-35 3ТСН") -> "ОРУ-35"

            // ОРУ-220
            equipmentName in listOf(
                "Мирная", "Мирная ТТ", "Топаз", "Топаз ТТ", "ОВ", "ОВ ТТ",
                "ТН-220 ОСШ", "2АТГ", "2АТГ ТТ", "ШСВ", "ШСВ ТТ", "3АТГ", "3АТГ ТТ",
                "Орбита", "Орбита ТТ", "Факел", "Факел ТТ", "Комета-1", "Комета-1 ТТ",
                "Комета-2", "Комета-2 ТТ", "1ТН-220", "2ТН-220"
            ) -> "ОРУ-220"

            // ОРУ-500
            equipmentName in listOf(
                "В-500 Р-500 2С", "В-500 ВШТ-31", "В-500 ВЛТ-30", "В-500 ВШЛ-32",
                "В-500 ВШЛ-21", "В-500 ВШТ-22", "В-500 ВЛТ-20", "В-500 ВШТ-11", "В-500 ВШЛ-12",
                "ТТ-500 ВШТ-31", "ТТ-500 ВЛТ-30", "ТТ-500 ВШЛ-32", "ТТ-500 ВШЛ-21",
                "ТТ-500 ВШТ-22", "ТТ-500 ВЛТ-20", "ТТ-500 ВШТ-11", "ТТ-500 ВШЛ-12",
                "1ТН-500", "2ТН-500", "ТН-500 СГРЭС-1",
                "Трачуковская ТТ", "Трачуковская 2ТН", "Трачуковская 1ТН", "Белозёрная 2ТН"
            ) -> "ОРУ-500"

            // АТГ
            equipmentName.startsWith("2 АТГ") ||
                    equipmentName.startsWith("3 АТГ") ||
                    equipmentName == "АТГ резервная" ||
                    equipmentName.startsWith("Реактор") ||
                    equipmentName == "ТН-35" -> "АТГ"

            // Здания
            equipmentName.startsWith("Компрессорная") ||
                    equipmentName.startsWith("Баллоная") ||
                    equipmentName in listOf(
                "КПЗ ОПУ", "КПЗ-2", "Насосная пожаротушения",
                "Мастерская по ремонту ВВ", "Артскважина", "Здание артезианской скважины",
                "Помещение 1 (2) АБ", "Помещение п/этажа №1,2,3"
            ) -> "Здания"

            else -> "Другое"
        }
    }

    private fun collectAllGlobalPhotos() {
        allPhotos.clear()

        val baseDir = File(requireContext().filesDir, "inspection_media/current_inspection")
        if (!baseDir.exists()) {
            Log.d("GlobalMediaDialog", "Папка current_inspection не существует")
            return
        }

        Log.d("GlobalMediaDialog", "Начинаем поиск фото в: ${baseDir.absolutePath}")

        baseDir.walkTopDown().forEach { file ->
            if (file.extension in listOf("jpg", "jpeg", "png")) {
                val equipmentName = file.parentFile?.name ?: "Неизвестно"
                val section = getSectionByEquipment(equipmentName)
                val timestamp = extractTimestampFromFileName(file.name) ?: file.lastModified()

                Log.d("GlobalMediaDialog", "📷 Фото: ${file.name} | оборудование: $equipmentName | секция: $section")
                allPhotos.add(PhotoItem(file.absolutePath, section, equipmentName, timestamp))
            }
        }

        // Сортируем по времени (сначала новые)
        allPhotos.sortByDescending { it.timestamp }

        val stats = allPhotos.groupBy { it.section }.mapValues { it.value.size }
        Log.d("GlobalMediaDialog", "Статистика по секциям: $stats")
        Log.d("GlobalMediaDialog", "Всего найдено фото: ${allPhotos.size}")
    }

    private fun extractTimestampFromFileName(fileName: String): Long? {
        val pattern = Regex("IMG_(\\d{8})_(\\d{6})\\.jpg")
        val matchResult = pattern.find(fileName)
        return matchResult?.let {
            val dateStr = it.groupValues[1]
            val timeStr = it.groupValues[2]
            try {
                val sdf = java.text.SimpleDateFormat("yyyyMMdd HHmmss", java.util.Locale.getDefault())
                sdf.parse("$dateStr $timeStr")?.time
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun applyFilters() {
        filteredPhotos.clear()

        filteredPhotos.addAll(allPhotos.filter { item ->
            val sectionMatch = selectedSection == "Все" || item.section == selectedSection
            val searchMatch = searchQuery.isEmpty() ||
                    item.equipment.lowercase().contains(searchQuery) ||
                    item.section.lowercase().contains(searchQuery)

            sectionMatch && searchMatch
        })

        updateGrid()
    }

    private fun updateGrid() {
        gridLayout.removeAllViews()

        resultCounter.text = "Найдено фото: ${filteredPhotos.size} (всего: ${allPhotos.size})"

        if (filteredPhotos.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = if (allPhotos.isEmpty()) {
                    "📭 Нет фото в приложении"
                } else {
                    "📭 Нет фото по вашему запросу"
                }
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            gridLayout.addView(emptyText)
            return
        }

        filteredPhotos.forEach { photo ->
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 200
                    height = 280
                    setMargins(8, 8, 8, 8)
                }
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP

                val bitmap = BitmapFactory.decodeFile(photo.path)
                if (bitmap != null) {
                    setImageBitmap(bitmap)
                }

                // Короткое нажатие - просмотр
                setOnClickListener {
                    showFullscreenPhoto(photo.path)
                }

                // Долгое нажатие - сразу отправка
                setOnLongClickListener {
                    sharePhoto(photo.path)
                    true
                }
            }
            container.addView(imageView)

            val label = TextView(requireContext()).apply {
                text = "${photo.equipment}\n${formatTime(photo.timestamp)}"
                textSize = 11f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 4, 0, 0)
                setTextColor(android.graphics.Color.GRAY)
            }
            container.addView(label)

            gridLayout.addView(container)
        }
    }

    private fun showSharePhotoDialog(photo: PhotoItem) {
        val options = arrayOf("📤 Отправить фото", "❌ Отмена")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Действие с фото")
            .setMessage("Фото: ${photo.equipment}\n${formatTime(photo.timestamp)}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sharePhoto(photo.path)
                }
            }
            .show()
    }

    private fun sharePhoto(photoPath: String) {
        try {
            val photoFile = File(photoPath)
            if (!photoFile.exists()) {
                Toast.makeText(requireContext(), "Файл не найден", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Поделиться фото"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFullscreenPhoto(photoPath: String) {
        val dialog = FullscreenPhotoDialog.newInstance(photoPath)
        dialog.show(childFragmentManager, "fullscreen_photo")
    }
}