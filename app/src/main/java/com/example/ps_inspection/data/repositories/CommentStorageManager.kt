package com.example.ps_inspection.data.repositories

import android.content.Context
import com.example.ps_inspection.data.models.Comment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CommentStorageManager(context: Context) {

    private val gson = Gson()
    private val commentsFile: File = File(context.filesDir, "comments_data.json")

    private val OLD_PREFS_NAME = "comments_prefs"
    private val OLD_KEY_COMMENTS = "equipment_comments"
    private val NEW_PREFS_NAME = "comments_prefs_v2"
    private val NEW_KEY_COMMENTS = "equipment_comments"

    init {
        // Миграция старых комментариев из SharedPreferences
        migrateFromPreferences(context)
    }

    private fun migrateFromPreferences(context: Context) {
        // Если файл уже существует, миграция не нужна
        if (commentsFile.exists()) return

        var oldComments: Map<String, List<Comment>>? = null

        // Пробуем из новой версии SharedPreferences (v2)
        val newPrefs = context.getSharedPreferences(NEW_PREFS_NAME, Context.MODE_PRIVATE)
        val newJson = newPrefs.getString(NEW_KEY_COMMENTS, null)

        if (!newJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<Map<String, List<Comment>>>() {}.type
                oldComments = gson.fromJson(newJson, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Если нет, пробуем из старой версии SharedPreferences
        if (oldComments == null || oldComments.isEmpty()) {
            val oldPrefs = context.getSharedPreferences(OLD_PREFS_NAME, Context.MODE_PRIVATE)
            val oldJson = oldPrefs.getString(OLD_KEY_COMMENTS, null)

            if (!oldJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<Map<String, List<String>>>() {}.type
                    val oldMap: Map<String, List<String>> = gson.fromJson(oldJson, type)
                    oldComments = oldMap.mapValues { (_, strings) ->
                        strings.map { Comment(text = it) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Сохраняем в файл, если есть данные
        if (!oldComments.isNullOrEmpty()) {
            saveAllComments(oldComments)
        }
    }

    fun saveAllComments(comments: Map<String, List<Comment>>) {
        try {
            val json = gson.toJson(comments)
            commentsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAllComments(): Map<String, List<Comment>> {
        return try {
            if (!commentsFile.exists()) return emptyMap()
            val json = commentsFile.readText()
            val type = object : TypeToken<Map<String, List<Comment>>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
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
        commentsFile.delete()
    }
}