package ru.hse.software.construction.service

import ru.hse.software.construction.model.User
import ru.hse.software.construction.repository.UserRepository


// Пример системы аутентификации пользователей
interface Authenticator {
    fun authenticate(username: String, password: String): Boolean
    fun updateUserPassword(username: String, newPassword: String)
}

class SimpleAuthenticator(
    private val repository: UserRepository
) : Authenticator {

    override fun authenticate(username: String, password: String): Boolean {
        val user = repository.getUserByUsername(username)

        val hashedPassword = repository.encryptPassword(password)
        return user?.hashedPassword == hashedPassword
    }

    override fun updateUserPassword(username: String, newPassword: String) {
        val user = repository.getUserByUsername(username)

        if (user != null) {
            val hashedPassword = repository.encryptPassword(newPassword)
            user.hashedPassword = hashedPassword
            repository.updateUser(user)
        } else {
            throw IllegalArgumentException("Пользователь с логином $username не найден.")
        }
    }
}

interface Registrar {
    fun isUsernameAvailable(username: String): Boolean
    fun isPasswordStrong(password: String): Boolean
    fun register(username: String, password: String)
}

class SimpleRegistrar(
    private val repository: UserRepository
) : Registrar {

    override fun isUsernameAvailable(username: String): Boolean {
        return repository.getUserByUsername(username) == null
    }

    override fun isPasswordStrong(password: String) : Boolean {
        return password.length >= 8
    }

    override fun register(username: String, password: String) {
        val hashedPassword = repository.encryptPassword(password)
        val newAccount = User(username, hashedPassword)
        repository.saveUser(newAccount)
    }

}