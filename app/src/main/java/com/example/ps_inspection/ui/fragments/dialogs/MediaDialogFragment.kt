package com.example.ps_inspection.ui.fragments.dialogs

import android.R
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.ps_inspection.data.models.Photo
import com.example.ps_inspection.data.repositories.InspectionMediaManager
import com.example.ps_inspection.ui.fragments.inspections.InspectionATG
import com.example.ps_inspection.ui.fragments.inspections.InspectionBuildings
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU220
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU35
import com.example.ps_inspection.ui.fragments.inspections.InspectionORU500
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaDialogFragment : DialogFragment() {
    private var inspectionId: String = ""
    private var equipmentName: String = ""
    private lateinit var mediaManager: InspectionMediaManager
    private lateinit var gridLayout: GridLayout
    private var currentPhotos = listOf<Photo>()

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
        setStyle(STYLE_NORMAL, R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
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
            text = "📷 $equipmentName"
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

        // Кнопка добавления фото
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

        loadPhotosToGrid()
        btnAdd.setOnClickListener { showAddPhotoOptions() }

        return root
    }

    private fun showAddPhotoOptions() {
        val options = arrayOf("📸 Сделать фото", "🖼️ Выбрать из галереи")
        AlertDialog.Builder(requireContext())
            .setTitle("Добавить фото")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> pickMedia.launch("image/*")
                }
            }.show()
    }

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

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun savePhotoFromUri(uri: Uri) {
        try {
            val newPhoto = mediaManager.savePhoto(inspectionId, equipmentName, uri)

            if (newPhoto != null) {
                loadPhotosToGrid()

                // Обновляем состояние кнопок фото
                when (parentFragment) {
                    is InspectionATG -> (parentFragment as InspectionATG).refreshPhotoButtonsState()
                    is InspectionORU35 -> (parentFragment as InspectionORU35).refreshPhotoButtonsState()
                    is InspectionORU220 -> (parentFragment as InspectionORU220).refreshPhotoButtonsState()
                    is InspectionORU500 -> (parentFragment as InspectionORU500).refreshAllStates()
                    is InspectionBuildings -> (parentFragment as InspectionBuildings).refreshAllStates()
                }

                Toast.makeText(requireContext(), "Фото добавлено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Ошибка сохранения фото", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPhotosToGrid() {
        gridLayout.removeAllViews()
        currentPhotos = mediaManager.getPhotos(inspectionId, equipmentName)
            .sortedByDescending { it.timestamp } // Сначала новые

        if (currentPhotos.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "📭 Нет фото"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            gridLayout.addView(emptyText)
            return
        }

        currentPhotos.forEach { photo ->
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 200
                    height = 260
                    setMargins(8, 8, 8, 8)
                }
            }

            val imageView = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP

                val fullPath = mediaManager.getFullPhotoPath(inspectionId, equipmentName, photo.fileName)
                val bitmap = BitmapFactory.decodeFile(fullPath)
                if (bitmap != null) {
                    setImageBitmap(bitmap)
                }

                setOnClickListener {
                    showFullscreenPhoto(fullPath)
                }
            }
            container.addView(imageView)

            val dateLabel = TextView(requireContext()).apply {
                text = photo.getFormattedTimeShort()
                textSize = 10f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 4, 0, 0)
                setTextColor(android.graphics.Color.GRAY)
            }
            container.addView(dateLabel)

            // Долгое нажатие → удаление
            container.setOnLongClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Удалить фото?")
                    .setMessage("${photo.getFormattedTime()}\n\nУдалить это фото?")
                    .setPositiveButton("Удалить") { _, _ ->
                        mediaManager.deletePhoto(inspectionId, equipmentName, photo.fileName)
                        loadPhotosToGrid()

                        when (parentFragment) {
                            is InspectionATG -> (parentFragment as InspectionATG).refreshPhotoButtonsState()
                            is InspectionORU35 -> (parentFragment as InspectionORU35).refreshPhotoButtonsState()
                            is InspectionORU220 -> (parentFragment as InspectionORU220).refreshPhotoButtonsState()
                            is InspectionORU500 -> (parentFragment as InspectionORU500).refreshAllStates()
                            is InspectionBuildings -> (parentFragment as InspectionBuildings).refreshAllStates()
                        }

                        Toast.makeText(requireContext(), "Фото удалено", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
                true
            }

            gridLayout.addView(container)
        }
    }

    private fun showFullscreenPhoto(photoPath: String) {
        val dialog = FullscreenPhotoDialog.Companion.newInstance(photoPath)
        dialog.show(childFragmentManager, "fullscreen_photo")
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}