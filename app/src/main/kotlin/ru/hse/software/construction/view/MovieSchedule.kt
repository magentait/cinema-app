package ru.hse.software.construction.view

import ru.hse.software.construction.model.Movie
import java.time.format.DateTimeFormatter

class MovieSchedule (
    private val moviesInStock: List<Movie>
) {
    // Функция для создания афиши в кинотеатре
    fun displayMovieSchedule() {
        println("Афиша в кинотеатре:\n")
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm") // Форматирование даты и времени
        moviesInStock.forEach { movie ->
            println("Фильм: ${movie.title}")
            movie.sessions.forEach { session ->
                val formattedDateTime = session.startDateTime.format(formatter) // Применение форматирования
                println("- Сеанс ID: ${session.sessionId}")
                println("  Дата и время начала: $formattedDateTime")
                val availableSeats = session.availableSeatsInHall.count { it.value }
                val totalSeats = session.availableSeatsInHall.size
                val status = if (availableSeats > 0) {
                    "Доступно билетов - $availableSeats/$totalSeats"
                } else {
                    "Билеты проданы"
                }
                println("  Статус: $status")
            }
            println()
        }
    }
}