package ru.hse.software.construction.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Movie(
    var title: String,
    var description: String,
    val sessions: MutableList<Session>,
    var boxOffice: Int = 0
) {

    fun changeTitle(newTitle: String) {
        title = newTitle
    }

    fun changeDescription(newDescription: String) {
        description = newDescription
    }

    fun changeSessionSchedule(sessionIndex: Int, newStartDateTime: LocalDateTime) {
        if (sessionIndex >= 0 && sessionIndex < sessions.size) {
            val session = sessions[sessionIndex]
            session.startDateTime = newStartDateTime
        } else {
            throw IllegalArgumentException("Некорректный индекс сеанса")
        }
    }
}
