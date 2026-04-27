package com.example.ps_inspection

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaDialogFragment : DialogFragment() {
    private var inspectionId: String = ""
    private var equipmentName: String = ""
    private lateinit var mediaManager: InspectionMediaManager

    // 📸 Лаунчер для камеры
    private var currentPhotoUri: Uri? = null
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                savePhotoFromUri(uri)
            }
        }
    }

    // 🖼️ Лаунчер для галереи
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { savePhotoFromUri(it) }
    }

    companion object {
        fun newInstance(inspectionId: String, equipment: String) = MediaDialogFragment().apply {
            arguments = Bundle().apply {
                putString("INSPECTION_ID", inspectionId)
                putString("EQUIPMENT", equipment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inspectionId = arguments?.getString("INSPECTION_ID") ?: return
        equipmentName = arguments?.getString("EQUIPMENT") ?: return
        mediaManager = InspectionMediaManager(requireContext())
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
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
        root.addView(android.widget.TextView(requireContext()).apply {
            text = "📷 $equipmentName"
            textSize = 18f
            setPadding(0, 0, 0, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })

        // Сетка фото
        val grid = GridLayout(requireContext()).apply {
            columnCount = 3
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(grid)

        // 🔹 Кнопка ТОЛЬКО для добавления фото (кнопку комментария убрали!)
        val btnAdd = Button(requireContext()).apply {
            text = "📸 Добавить фото"
        }
        root.addView(btnAdd, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Логика
        loadPhotosToGrid(grid)
        btnAdd.setOnClickListener { showAddPhotoOptions() }

        return root
    }

    // 🎯 Меню выбора: Камера или Галерея
    private fun showAddPhotoOptions() {
        val options = arrayOf("📸 Сделать фото", "🖼️ Выбрать из галереи")
        AlertDialog.Builder(requireContext())
            .setTitle("Добавить фото")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()  // Камера — работает как было
                    1 -> pickMedia.launch("image/*")  // ✅ Галерея — исправлено
                }
            }.show()
    }

    // 📸 Запуск камеры
    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takePicture.launch(currentPhotoUri)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка камеры: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Создаём временный файл для фото
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    // Сохраняем фото из Uri в папку оборудования
    private fun savePhotoFromUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val dir = mediaManager.getMediaDir(inspectionId, equipmentName)
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val target = File(dir, fileName)

            inputStream?.use { it.copyTo(target.outputStream()) }

            // Обновляем сетку
            updateGridPhotos()
            Toast.makeText(requireContext(), "Фото добавлено", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Поиск GridLayout через цикл
    private fun updateGridPhotos() {
        val root = view as? LinearLayout ?: return
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is GridLayout) {
                loadPhotosToGrid(child)
                break
            }
        }
    }

    private fun loadPhotosToGrid(grid: GridLayout) {
        grid.removeAllViews()
        mediaManager.getPhotoFiles(inspectionId, equipmentName).forEach { fileName ->
            val iv = ImageView(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 200
                    height = 200
                    setMargins(8, 8, 8, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(mediaManager.loadThumbnail(inspectionId, equipmentName, fileName))

                // Клик → полный просмотр с зумом
                setOnClickListener { showFullImage(fileName) }

                // Долгое нажатие → удаление
                setOnLongClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Удалить фото?")
                        .setMessage("Это действие нельзя отменить")
                        .setPositiveButton("Удалить") { _, _ ->
                            mediaManager.deletePhoto(inspectionId, equipmentName, fileName)
                            loadPhotosToGrid(grid)
                        }
                        .setNegativeButton("Отмена", null).show()
                    true
                }
            }
            grid.addView(iv)
        }
    }

    private fun showFullImage(fileName: String) {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true

            val path = mediaManager.getFullPhotoPath(inspectionId, equipmentName, fileName)
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                setImageBitmap(bitmap)
            }
        }
        dialogView.addView(imageView)

        // Простой pinch-to-zoom
        val scaleDetector = android.view.ScaleGestureDetector(
            requireContext(),
            object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                private var mScaleFactor = 1f
                override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                    mScaleFactor *= detector.scaleFactor
                    mScaleFactor = mScaleFactor.coerceIn(0.5f, 3.0f)
                    imageView.scaleX = mScaleFactor
                    imageView.scaleY = mScaleFactor
                    return true
                }
            }
        )
        imageView.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            true
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Закрыть", null)
            .show()
    }
}