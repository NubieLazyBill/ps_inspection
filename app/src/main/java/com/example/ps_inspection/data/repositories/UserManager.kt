package com.example.ps_inspection.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class User(
    val name: String,
    val position: String
) {
    // Генерация пароля: первые буквы ФАМИЛИИ, ИМЕНИ, ОТЧЕСТВА + 123
    fun getPassword(): String {
        // Разбираем "Фамилия И.О."
        val parts = name.split(" ")
        if (parts.size < 2) return "default123"

        val surname = parts[0]  // Фамилия
        val initials = parts[1]  // "Е.А." или "И.В." и т.д.

        // Первая буква фамилии
        val surnameLetter = if (surname.isNotEmpty()) surname.first().uppercase() else ""

        // Буквы из инициалов (например "Е.А." -> Е и А)
        val initialLetters = initials.split(".")
            .filter { it.isNotEmpty() }
            .joinToString("") { it.first().uppercase() }

        // Пароль: буквы + 123 (в нижнем регистре)
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

    // Дефолтные пользователи (реальные люди)
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

    fun getCurrentUser(): User {
        val json = prefs.getString(KEY_CURRENT_USER, null)
        if (json != null) {
            val type = object : TypeToken<User>() {}.type
            return gson.fromJson(json, type)
        }
        return getUsers().first()
    }

    fun saveCurrentUser(user: User) {
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    fun checkPassword(user: User, inputPassword: String): Boolean {
        val expectedPassword = user.getPassword()
        return expectedPassword.equals(inputPassword.lowercase(), ignoreCase = false)
    }

    fun isFirstLaunch(): Boolean {
        val isFirst = prefs.getBoolean("is_first_launch", true)
        if (isFirst) {
            prefs.edit().putBoolean("is_first_launch", false).apply()
        }
        return isFirst
    }
}