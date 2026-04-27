package com.example.ps_inspection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class InspectionMediaManager(private val context: Context) {

    fun getMediaDir(inspectionId: String, equipment: String): File {
        val dir = File(context.filesDir, "inspection_media/$inspectionId/$equipment")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getPhotoFiles(inspectionId: String, equipment: String): List<String> {
        val dir = getMediaDir(inspectionId, equipment)
        return dir.listFiles()?.map { it.name }?.filter { it.endsWith(".jpg") } ?: emptyList()
    }

    fun deletePhoto(inspectionId: String, equipment: String, fileName: String): Boolean {
        return try {
            File(getMediaDir(inspectionId, equipment), fileName).delete()
        } catch (e: Exception) { false }
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
}