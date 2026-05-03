package com.example.ps_inspection.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.ps_inspection.data.models.Photo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class InspectionMediaManager(private val context: Context) {

    fun getMediaDir(inspectionId: String, equipment: String): File {
        val dir = File(context.filesDir, "inspection_media/$inspectionId/$equipment")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPhotos(inspectionId: String, equipment: String): List<Photo> {
        val dir = getMediaDir(inspectionId, equipment)
        return dir.listFiles()
            ?.filter { it.isFile && it.extension in listOf("jpg", "jpeg", "png") }
            ?.mapNotNull { file ->
                // Пытаемся извлечь timestamp из имени файла или использовать дату файла
                val timestamp = extractTimestampFromFileName(file.name) ?: file.lastModified()
                Photo(fileName = file.name, timestamp = timestamp)
            }
            ?: emptyList()
    }

    fun getPhotoFiles(inspectionId: String, equipment: String): List<String> {
        return getPhotos(inspectionId, equipment).map { it.fileName }
    }

    private fun extractTimestampFromFileName(fileName: String): Long? {
        // Формат: IMG_20241225_143025.jpg
        val pattern = Regex("IMG_(\\d{8})_(\\d{6})\\.jpg")
        val matchResult = pattern.find(fileName)
        return matchResult?.let {
            val dateStr = it.groupValues[1]
            val timeStr = it.groupValues[2]
            try {
                val sdf = SimpleDateFormat("yyyyMMdd HHmmss", Locale.getDefault())
                sdf.parse("$dateStr $timeStr")?.time
            } catch (e: Exception) {
                null
            }
        }
    }

    fun addPhoto(inspectionId: String, equipment: String, fileName: String) {
        // Метод для совместимости, но лучше использовать savePhoto
    }

    fun savePhoto(inspectionId: String, equipment: String, sourceUri: android.net.Uri): Photo? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val dir = getMediaDir(inspectionId, equipment)
            val timestamp = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            val fileName = "IMG_${sdf.format(java.util.Date(timestamp))}.jpg"
            val target = File(dir, fileName)

            inputStream?.use { it.copyTo(target.outputStream()) }
            Photo(fileName = fileName, timestamp = timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePhoto(inspectionId: String, equipment: String, fileName: String): Boolean {
        return try {
            File(getMediaDir(inspectionId, equipment), fileName).delete()
        } catch (e: Exception) { false }
    }

    fun deletePhotoByFileName(inspectionId: String, equipment: String, fileName: String): Boolean {
        return deletePhoto(inspectionId, equipment, fileName)
    }

    fun loadThumbnail(inspectionId: String, equipment: String, fileName: String): Bitmap? {
        val file = File(getMediaDir(inspectionId, equipment), fileName)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)?.let {
            Bitmap.createScaledBitmap(it, 200, 200, false)
        }
    }

    fun getFullPhotoPath(inspectionId: String, equipment: String, fileName: String): String {
        return File(getMediaDir(inspectionId, equipment), fileName).absolutePath
    }

    fun clearMediaForInspection(inspectionId: String) {
        val dir = File(context.filesDir, "inspection_media/$inspectionId")
        if (dir.exists()) dir.deleteRecursively()
    }

    fun hasPhotos(inspectionId: String, equipmentName: String): Boolean {
        val dir = getMediaDir(inspectionId, equipmentName)
        return dir.exists() && dir.listFiles()?.isNotEmpty() == true
    }

    fun getPhotoCount(inspectionId: String, equipmentName: String): Int {
        val dir = getMediaDir(inspectionId, equipmentName)
        return if (dir.exists()) dir.listFiles()?.size ?: 0 else 0
    }
}