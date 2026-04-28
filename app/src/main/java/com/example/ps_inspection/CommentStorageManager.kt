package com.example.ps_inspection

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CommentStorageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("comments_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_COMMENTS = "equipment_comments"
    }

    // Сохранить все комментарии
    fun saveAllComments(comments: Map<String, List<String>>) {
        val json = gson.toJson(comments)
        prefs.edit().putString(KEY_COMMENTS, json).apply()
    }

    // Загрузить все комментарии
    fun loadAllComments(): Map<String, List<String>> {
        val json = prefs.getString(KEY_COMMENTS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        return gson.fromJson(json, type)
    }

    // Получить комментарии для оборудования
    fun getComments(equipmentName: String): List<String> {
        return loadAllComments()[equipmentName] ?: emptyList()
    }

    // Добавить комментарий
    fun addComment(equipmentName: String, comment: String) {
        val all = loadAllComments().toMutableMap()
        val current = all[equipmentName]?.toMutableList() ?: mutableListOf()
        current.add(comment)
        all[equipmentName] = current
        saveAllComments(all)
    }

    // Удалить комментарий
    fun removeComment(equipmentName: String, index: Int) {
        val all = loadAllComments().toMutableMap()
        val current = all[equipmentName]?.toMutableList() ?: return
        if (index in current.indices) {
            current.removeAt(index)
            if (current.isEmpty()) {
                all.remove(equipmentName)
            } else {
                all[equipmentName] = current
            }
            saveAllComments(all)
        }
    }

    // Обновить комментарий
    fun updateComment(equipmentName: String, index: Int, newComment: String) {
        val all = loadAllComments().toMutableMap()
        val current = all[equipmentName]?.toMutableList() ?: return
        if (index in current.indices) {
            current[index] = newComment
            all[equipmentName] = current
            saveAllComments(all)
        }
    }

    // Очистить все комментарии (если нужно)
    fun clearAllComments() {
        prefs.edit().remove(KEY_COMMENTS).apply()
    }
}