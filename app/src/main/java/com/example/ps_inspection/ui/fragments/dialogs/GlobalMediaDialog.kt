package com.example.ps_inspection.ui.fragments.dialogs

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import java.io.File

class GlobalMediaDialog : DialogFragment() {

    private lateinit var mediaManager: InspectionMediaManager
    private lateinit var gridLayout: GridLayout
    private val allPhotos = mutableListOf<Pair<String, String>>() // (путь, название оборудования)

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

        // Сетка фото
        gridLayout = GridLayout(requireContext()).apply {
            columnCount = 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(gridLayout)

        // Кнопка закрытия
        val btnClose = Button(requireContext()).apply {
            text = "Закрыть"
        }
        root.addView(btnClose, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        collectAllGlobalPhotos()
        loadPhotosToGrid()
        btnClose.setOnClickListener { dismiss() }

        return root
    }

    private fun collectAllGlobalPhotos() {
        allPhotos.clear()

        val baseDir = File(requireContext().filesDir, "inspection_media")
        if (!baseDir.exists()) {
            Log.d("GlobalMediaDialog", "Папка inspection_media не существует")
            return
        }

        // Ищем ВСЕ jpg/jpeg/png файлы рекурсивно
        baseDir.walkTopDown().forEach { file ->
            if (file.extension in listOf("jpg", "jpeg", "png")) {
                val equipment = file.parentFile?.name ?: "Неизвестно"
                allPhotos.add(file.absolutePath to equipment)
                Log.d("GlobalMediaDialog", "Найдено фото: ${file.absolutePath} (оборудование: $equipment)")
            }
        }

        Log.d("GlobalMediaDialog", "Всего найдено фото: ${allPhotos.size}")
    }

    private fun loadPhotosToGrid() {
        gridLayout.removeAllViews()

        if (allPhotos.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "📭 Нет фото в приложении"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            gridLayout.addView(emptyText)
            return
        }

        allPhotos.forEach { (photoPath, equipment) ->
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 200
                    height = 240
                    setMargins(8, 8, 8, 8)
                }
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP

                val bitmap = BitmapFactory.decodeFile(photoPath)
                if (bitmap != null) {
                    setImageBitmap(bitmap)
                }

                setOnClickListener {
                    showFullscreenPhoto(photoPath)
                }
            }
            container.addView(imageView)

            val label = TextView(requireContext()).apply {
                text = equipment
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 4, 0, 0)
            }
            container.addView(label)

            gridLayout.addView(container)
        }
    }

    private fun showFullscreenPhoto(photoPath: String) {
        val dialog = FullscreenPhotoDialog.newInstance(photoPath)
        dialog.show(childFragmentManager, "fullscreen_photo")
    }
}