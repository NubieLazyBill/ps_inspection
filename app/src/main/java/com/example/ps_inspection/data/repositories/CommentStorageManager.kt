package com.example.ps_inspection.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.ps_inspection.data.models.Comment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CommentStorageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("comments_prefs_v2", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_COMMENTS = "equipment_comments"
        private const val OLD_PREFS_NAME = "comments_prefs"
        private const val OLD_KEY_COMMENTS = "equipment_comments"
    }

    init {
        // Миграция старых комментариев при первом запуске
        migrateOldComments(context)
    }

    private fun migrateOldComments(context: Context) {
        // Проверяем, есть ли уже данные в новом хранилище
        if (prefs.contains(KEY_COMMENTS)) return

        // Пытаемся загрузить старые комментарии
        val oldPrefs = context.getSharedPreferences(OLD_PREFS_NAME, Context.MODE_PRIVATE)
        val oldJson = oldPrefs.getString(OLD_KEY_COMMENTS, null) ?: return

        try {
            // Пробуем распарсить как старый формат (Map<String, List<String>>)
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            val oldMap: Map<String, List<String>> = gson.fromJson(oldJson, type)
            val newMap = mutableMapOf<String, List<Comment>>()

            oldMap.forEach { (key, strings) ->
                newMap[key] = strings.map { Comment(text = it) }
            }

            if (newMap.isNotEmpty()) {
                saveAllComments(newMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveAllComments(comments: Map<String, List<Comment>>) {
        val json = gson.toJson(comments)
        prefs.edit().putString(KEY_COMMENTS, json).apply()
    }

    fun loadAllComments(): Map<String, List<Comment>> {
        val json = prefs.getString(KEY_COMMENTS, null) ?: return emptyMap()
        try {
            val type = object : TypeToken<Map<String, List<Comment>>>() {}.type
            return gson.fromJson(json, type)
        } catch (e: Exception) {
            return emptyMap()
        }
    }

    fun getComments(equipmentName: String): List<Comment> {
        return loadAllComments()[equipmentName] ?: emptyList()
    }

    fun addComment(equipmentName: String, commentText: String) {
        val all = loadAllComments().toMutableMap()
        val current = all[equipmentName]?.toMutableList() ?: mutableListOf()
        current.add(Comment(text = commentText))
        all[equipmentName] = current
        saveAllComments(all)
    }

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

    fun updateComment(equipmentName: String, index: Int, newText: String) {
        val all = loadAllComments().toMutableMap()
        val current = all[equipmentName]?.toMutableList() ?: return
        if (index in current.indices) {
            val oldComment = current[index]
            current[index] = oldComment.copy(text = newText)
            all[equipmentName] = current
            saveAllComments(all)
        }
    }

    fun clearAllComments() {
        prefs.edit().remove(KEY_COMMENTS).apply()
        // Также очищаем старые префы для чистоты
        val oldPrefs = prefs
    }
}