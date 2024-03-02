package ru.hse.software.construction.model

import kotlinx.serialization.Serializable
import ru.hse.software.construction.repository.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Session(
    val movieTitle: String,
    val sessionId: Int,
    var ticketPrice: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    var startDateTime: LocalDateTime,
    val duration: Int,
    val seatsInHall: Int,
    // Свободные Места в Зале: номер места -> доступность (свободно/занято)
    var availableSeatsInHall: MutableMap<Int, Boolean> = mutableMapOf(),
    var availableSeatsInHallJson: String = ""
) {

    init {
        availableSeatsInHall = if (availableSeatsInHall.isEmpty()) {
            generateInitialSeatsMap(seatsInHall)
        } else {
            availableSeatsInHall
        }
    }

    companion object {
        private fun generateInitialSeatsMap(seatsInHall: Int): MutableMap<Int, Boolean> {
            val seatsMap = mutableMapOf<Int, Boolean>()
            // Заполняем мапу мест: инициализируем все места как свободные (true)
            for (seatNumber in 1..seatsInHall) {
                seatsMap[seatNumber] = true // true - место свободно
            }
            return seatsMap
        }
    }
}
