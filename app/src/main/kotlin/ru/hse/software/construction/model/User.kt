package ru.hse.software.construction.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    var hashedPassword: String
)