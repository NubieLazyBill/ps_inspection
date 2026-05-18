package com.example.ps_inspection.data.repositories

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class User(
    val name: String,
    val position: String
) {
    fun getPassword(): String {
        val parts = name.split(" ")
        if (parts.size < 2) return "default123"

        val surname = parts[0]
        val initials = parts[1]

        val surnameLetter = if (surname.isNotEmpty()) surname.first().uppercase() else ""

        val initialLetters = initials.split(".")
            .filter { it.isNotEmpty() }
            .joinToString("") { it.first().uppercase() }

        val password = "$surnameLetter$initialLetters" + "123"
        return password.lowercase()
    }
}

class UserManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USERS = "users_list"
        private const val KEY_CURRENT_USER = "current_user"
    }

    private val defaultUsers = listOf(
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
        User("Журавлёв В.А.", "ДИП Сибирская")
    )

    fun getUsers(): List<User> {
        val json = prefs.getString(KEY_USERS, null)
        if (json != null) {
            val type = object : TypeToken<List<User>>() {}.type
            return gson.fromJson(json, type)
        }
        saveUsers(defaultUsers)
        return defaultUsers
    }

    private fun saveUsers(users: List<User>) {
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_USERS, json).apply()
    }

    // Проверяем, выбран ли пользователь (есть ли сохранённый)
    fun hasSelectedUser(): Boolean {
        return prefs.contains(KEY_CURRENT_USER)
    }

    fun getCurrentUser(): User {
        val json = prefs.getString(KEY_CURRENT_USER, null)
        if (json != null) {
            val type = object : TypeToken<User>() {}.type
            return gson.fromJson(json, type)
        }
        // Если нет сохранённого пользователя, возвращаем первого (но не сохраняем его!)
        return getUsers().first()
    }

    fun saveCurrentUser(user: User) {
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    fun checkPassword(user: User, inputPassword: String): Boolean {
        val expectedPassword = user.getPassword()

        // 🔧 СПЕЦИАЛЬНЫЕ ПАРОЛИ ДЛЯ ЭМУЛЯТОРА
        val isEmulator = Build.PRODUCT.contains("sdk") || Build.FINGERPRINT.contains("vbox")
        if (isEmulator) {
            // Давыдова И.В. -> div123
            if (user.name.contains("Давыдова") && inputPassword.lowercase() == "div123") {
                return true
            }
            // Универсальный пароль для всех на эмуляторе (на случай проблем с русской раскладкой)
            if (inputPassword.lowercase() == "admin123") {
                return true
            }
        }

        return expectedPassword.equals(inputPassword.lowercase(), ignoreCase = false)
    }

    // Удалить эту функцию или оставить, но не использовать
    @Deprecated("Use hasSelectedUser() instead")
    fun isFirstLaunch(): Boolean {
        return !hasSelectedUser()
    }
}