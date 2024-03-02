package ru.hse.software.construction.controller

import ru.hse.software.construction.model.Movie
import ru.hse.software.construction.model.Session
import ru.hse.software.construction.repository.CinemaRepository
import ru.hse.software.construction.repository.JsonCinemaApp
import ru.hse.software.construction.service.TicketService

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CinemaController {

    fun isFilePathValid (
        path: String,
        fileName: String
    ) : Boolean {

        val filePath = "$path/$fileName"
        val file = File(filePath)

        try {
            if (!file.exists()) {
                // Пытаемся создать файл, если он не существует
                val isCreated = file.createNewFile()
                if (!isCreated) {
                    println("Ошибка при создании файла.")
                    return false
                }
                file.writeText("[]")
            }
        } catch (ex: Throwable) {
            println("Ошибка при создании файла: ${ex.message}")
            return false
        }

        return true
    }


    // Функция для добавления нового фильма в прокат
    fun addNewMovieToStock(cinema: JsonCinemaApp, cinemaRepository: CinemaRepository) {
        println("Введите название нового фильма: ")
        val title = readlnOrNull() ?: return

        println("Введите описание нового фильма: ")
        val description = readlnOrNull() ?: return

        val sessions = mutableListOf<Session>()
        // Добавление сеансов к фильму
        addSessionsToMovie(title, sessions, cinema, cinemaRepository)

        val newMovie = Movie(title, description, sessions)
        cinema.addMovieToStock(newMovie)
        println("Фильм успешно добавлен в прокат.")
    }

    // Функция для добавления сеансов к фильму
    private fun addSessionsToMovie(movieTitle: String, sessions: MutableList<Session>, cinema: JsonCinemaApp, cinemaRepository: CinemaRepository) {
        println("Хотите добавить сеансы к фильму? (Да/Нет): ")
        val wantToAddSessions = readlnOrNull()?.toLowerCase()

        if (wantToAddSessions == "да") {
            while (true) {
                println("Введите ID сеанса для фильма '$movieTitle': ")
                val sessionId = readlnOrNull()?.toIntOrNull() ?: continue

                println("Введите стоимость билета на сеанс ${sessionId}: ")
                val ticketPrice = readlnOrNull()?.toIntOrNull() ?: continue

                println("Введите дату и время начала сеанса в формате 'yyyy-MM-dd HH:mm': ")
                val startDateTimeInput = readlnOrNull() ?: continue
                val startDateTime = try {
                    LocalDateTime.parse(startDateTimeInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (e: DateTimeParseException) {
                    println("Ошибка при вводе даты и времени. Пожалуйста, введите в правильном формате.")
                    continue
                }

                println("Введите длительность сеанса в минутах: ")
                val duration = readlnOrNull()?.toIntOrNull() ?: continue

                println("Введите количество мест в зале для сеанса '$sessionId': ")
                val seatsInHall = readlnOrNull()?.toIntOrNull() ?: continue

                val newSession = Session(movieTitle, sessionId, ticketPrice, startDateTime, duration, seatsInHall)
                sessions.add(newSession)

                println("Сеанс успешно добавлен к фильму '$movieTitle'.")
                println("Хотите добавить еще один сеанс для этого фильма? (Да/Нет): ")
                val wantToAddAnotherSession = readlnOrNull()?.toLowerCase()
                if (wantToAddAnotherSession != "да") {
                    cinemaRepository.saveMovies(cinema.getAllMovies())
                    break
                }
            }
        }
    }

    // Функция для удаления фильма из проката
    fun removeMovieFromStock(cinema: JsonCinemaApp) {
        println("Введите название фильма, который нужно убрать с проката: ")
        val movieTitleToRemove = readlnOrNull() ?: return

        val movieToRemove = cinema.findMovieByTitle(movieTitleToRemove)
        if (movieToRemove != null) {
            cinema.removeMovieFromStock(movieToRemove)
            println("Фильм '$movieTitleToRemove' успешно убран с проката.")
        } else {
            println("Фильм '$movieTitleToRemove' не найден в прокате.")
        }
    }

    fun selectMovieAndSession(cinema: JsonCinemaApp): Pair<Movie, Session>? {
        val movies = cinema.getAllMovies()
        println("Введите название фильма: ")
        val selectedMovieTitle = readlnOrNull() ?: return null
        val selectedMovie = movies.firstOrNull { it.title == selectedMovieTitle }

        if (selectedMovie == null) {
            println("Фильм с названием $selectedMovieTitle не найден.")
            return null
        }

        val sessions = selectedMovie.sessions
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        sessions.forEachIndexed { index, session ->
            val formattedDateTime = session.startDateTime.format(formatter) // Применение форматирования
            println("${index + 1}. Сеанс: $formattedDateTime")
        }

        println("Выберите сеанс (введите номер): ")
        val selectedSessionIndex = readlnOrNull()?.toIntOrNull() ?: return null

        if (selectedSessionIndex <= 0 || selectedSessionIndex > sessions.size) {
            println("Некорректный номер сеанса")
            return null
        }

        val selectedSession = sessions[selectedSessionIndex - 1]
        return Pair(selectedMovie, selectedSession)
    }

    fun sellOrReturnTickets(ticketService: TicketService, cinema: JsonCinemaApp, action: String) {
        val movieAndSession = selectMovieAndSession(cinema) ?: return

        val (selectedMovie, selectedSession) = movieAndSession

        println("Введите количество билетов для $action: ")
        val numberOfTickets = readlnOrNull()?.toIntOrNull() ?: return

        println("Введите номера мест для $action: (через пробел)")
        val seatsInput = readlnOrNull() ?: return
        val seats = seatsInput.split(" ").mapNotNull { it.toIntOrNull() }.filter { it in 1..selectedSession.seatsInHall }

        // Проверка существования выбранных мест в зале
        val unavailableSeats = seats.filter { it !in selectedSession.availableSeatsInHall.keys }
        if (unavailableSeats.isNotEmpty()) {
            println("Места $unavailableSeats не существуют в зале.")
            return
        }

        try {
            when (action) {
                "продажи" -> {
                    ticketService.sellTickets(selectedMovie, selectedSession, numberOfTickets, seats)
                    println("Билеты успешно проданы")
                }
                "возврата" -> {
                    ticketService.returnTickets(selectedMovie, selectedSession, numberOfTickets, seats)
                    println("Билеты успешно возвращены")
                }
                else -> println("Неверное действие")
            }
        } catch (e: IllegalArgumentException) {
            println("Ошибка при $action билетов: ${e.message}")
        }
    }

    fun selectOnlyMovie(cinema: JsonCinemaApp) : Movie? {
        val movies = cinema.getAllMovies()
        println("Введите название фильма: ")
        val selectedMovieTitle = readlnOrNull() ?: return null
        val selectedMovie = movies.firstOrNull { it.title == selectedMovieTitle }

        if (selectedMovie == null) {
            println("Фильм с названием $selectedMovieTitle не найден.")
            return null
        }

        return selectedMovie
    }
}