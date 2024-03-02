package ru.hse.software.construction.repository

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.hse.software.construction.model.User
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class FileUserException(
    filePath: String,
    override val message: String? = null,
    cause: Throwable? = null
) : RuntimeException("Проблема с файлом $filePath\n$message", cause)

class UserRepository(
    path: String,
    fileName: String
) {

    private val filePath = "$path/$fileName"
    private val file = File(filePath)

    fun saveUser(user: User) {
        val users = loadUsers().toMutableList()
        users.add(user)
        saveUsers(users)
    }

    fun updateUser(user: User) {
        val users = loadUsers().toMutableList()
        val existingUser = users.find { it.username == user.username }
        existingUser?.hashedPassword = user.hashedPassword // Обновляем только пароль
        saveUsers(users)
    }

    fun loadUsers() : MutableList<User> {
        try {
            return if (file.exists()) {
                val jsonString = file.readText()
                Json.decodeFromString(jsonString)
            } else {
                mutableListOf()
            }
        } catch (ex: Throwable) {
            throw FileUserException(filePath, "Нельзя загрузить пользователей", ex)
        }
    }
    fun saveUsers(users: List<User>) {
        val encryptedUsers = users.map { user ->
            User(user.username, user.hashedPassword)
        }

        try {
            val jsonString = Json.encodeToString(encryptedUsers)
            file.writeText(jsonString)
        } catch (ex: Throwable) {
            throw FileUserException(filePath, "Нельзя сохранить пользователей", ex)
        }
    }

    fun getUserByUsername(username: String): User? {
        val allUsers = loadUsers()
        return allUsers.find { it.username == username }
    }

    fun encryptPassword(password: String): String {
        // Пример шифрования пароля с использованием SHA-256
        val bytes = password.toByteArray(StandardCharsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}