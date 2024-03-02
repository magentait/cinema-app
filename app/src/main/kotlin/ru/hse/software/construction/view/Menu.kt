package ru.hse.software.construction.view

import ru.hse.software.construction.model.Movie
import ru.hse.software.construction.model.Session
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

class Menu {
    fun displayAuthorizationMenu() {
        println("Меню авторизации:")
        println("1. Регистрация")
        println("2. Авторизация")
        println("0. Выход")

        print("Выберите действие: ")
    }

    fun displayChangePasswordMenu() {
        println("Вы действительно желаете изменить пароль?")
        println("1. Да")
        println("0. Нет")

        print("Выберите действие: ")
    }

    fun displayMainFunctionsMenu() {
        println("Меню:")
        println("1. Посмотреть афишу")
        println("2. Продать билеты")
        println("3. Сделать возврат билетов")
        println("4. Посмотреть визуализацию зала сеанса")
        println("5. Редактировать данные фильма")
        println("6. Добавить фильм в прокат")
        println("7. Снять фильм с проката")
        println("8. Посмотреть кассовые сборы фильм")
        println("9. Дополнительный функционал: Поменять пароль от аккаунта")
        println("0. Выход")
        print("Выберите действие: ")
    }

    fun displayEditingChoicesMenu() {
        println("Выберите, что вы хотите отредактировать:")
        println("1. Редактировать название фильма")
        println("2. Редактировать описание фильма")
        println("3. Редактировать расписание конкретного сеанса")
        println("0. Вернуться в предыдущее меню")

        print("Выберите действие: ")
    }

    fun displayHallLayout(session: Session) {

        val ratio = sqrt(session.seatsInHall / 0.5).toInt() // соотношение длины зала к ширине 1 к 2
        val rows = session.seatsInHall / ratio + if (session.seatsInHall % ratio == 0) 0 else 1
        val hallLayout = Array(rows) { CharArray(ratio) { '_' } }

        session.availableSeatsInHall.forEach { (seat, isAvailable) ->
            val row = (seat - 1) / ratio
            val col = (seat - 1) % ratio
            hallLayout[row][col] = if (isAvailable) '_' else 'x'
        }

        val formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")
        val formattedDateTime = session.startDateTime.format(formatter)
        println("${session.movieTitle}: Время сеанса - $formattedDateTime")

        for ((index, row) in hallLayout.withIndex()) {
            print("${index + 1} ")
            for (seat in row) {
                print("$seat ")
            }
            println()
        }
        println()
    }

    fun displayBoxOffice(selectedMovie: Movie) {
        val totalBoxOffice = selectedMovie.boxOffice
        println("Кассовые сборы фильма '${selectedMovie.title}': $totalBoxOffice")
    }
}