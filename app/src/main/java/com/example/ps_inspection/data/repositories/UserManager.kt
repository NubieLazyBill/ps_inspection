package com.example.ps_inspection.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class User(
    val name: String,
    val position: String
)

class UserManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_USERS_LIST = "users_list"
    }

    // Список по умолчанию
    fun getDefaultUsers(): List<User> {
        return listOf(
            User("Арютина Е.А.", "ДИП"),
            User("Давыдова И.В.", "ДИП"),
            User("Костенюк К.С.", "ДИП"),
            User("Ложников И.Г.", "ДИП"),
            User("Пашедко Е.И.", "ДИП"),
            User("Плотников А.П.", "ДИП"),
            User("Богданов Д.А.", "ДЭМ"),
            User("Шристолюбский А.В.", "ДЭМ"),
            User("Таранухин А.С.", "ДЭМ"),
            User("Светличный И.А.", "ДЭМ"),
            User("Журавлёв В.А.", "ДЭМ"),
        )
    }

    fun getUsers(): List<User> {
        val json = prefs.getString(KEY_USERS_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(json, type)
        } else {
            val default = getDefaultUsers()
            saveUsers(default)
            default
        }
    }

    fun saveUsers(users: List<User>) {
        prefs.edit().putString(KEY_USERS_LIST, gson.toJson(users)).apply()
    }

    fun addUser(user: User) {
        val users = getUsers().toMutableList()
        users.add(user)
        saveUsers(users)
    }

    fun removeUser(name: String) {
        val users = getUsers().toMutableList()
        users.removeAll { it.name == name }
        saveUsers(users)
    }

    fun getCurrentUser(): User {
        val json = prefs.getString(KEY_CURRENT_USER, null)
        return if (json != null) {
            gson.fromJson(json, User::class.java)
        } else {
            val default = getUsers().first()
            saveCurrentUser(default)
            default
        }
    }

    fun saveCurrentUser(user: User) {
        prefs.edit().putString(KEY_CURRENT_USER, gson.toJson(user)).apply()
    }

    fun hasSelectedUser(): Boolean {
        return prefs.getString(KEY_CURRENT_USER, null) != null
    }
}