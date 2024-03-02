package ru.hse.software.construction.service

import ru.hse.software.construction.model.Movie
import ru.hse.software.construction.model.Session
import ru.hse.software.construction.repository.CinemaRepository
import ru.hse.software.construction.repository.JsonCinemaApp

class TicketService(
    private val cinema: JsonCinemaApp,
    private val cinemaRepository: CinemaRepository
) {

    fun sellTickets(movie: Movie, session: Session, numberOfTickets: Int, seatsToPurchase: List<Int>) {
        if (session.availableSeatsInHall.count { it.value } < numberOfTickets) {
            throw IllegalArgumentException("Недостаточно свободных мест для покупки $numberOfTickets tickets")
        }

        for (seat in seatsToPurchase) {
            if (!session.availableSeatsInHall[seat]!!) {
                throw IllegalArgumentException("Место $seat уже занято")
            }
        }

        val availableSeats = session.availableSeatsInHall.filter { it.value }.keys.toList()

        val seatsToSell = if (seatsToPurchase.size > numberOfTickets) {
            seatsToPurchase.subList(0, numberOfTickets)
        } else {
            seatsToPurchase.takeIf { it.size <= numberOfTickets } ?: availableSeats.take(numberOfTickets)
        }

        for (seat in seatsToSell) {
            session.availableSeatsInHall[seat] = false
        }

        movie.boxOffice += seatsToSell.count() * session.ticketPrice

        cinemaRepository.saveMovies(cinema.getAllMovies())
    }

    fun returnTickets(movie: Movie, session: Session, numberOfTickets: Int, seatsToReturn: List<Int>) {
        if (movie.boxOffice < numberOfTickets * session.ticketPrice) {
            throw IllegalArgumentException("Недостаточно прибыли для возврата $numberOfTickets билетов")
        }

        if (seatsToReturn.any { session.availableSeatsInHall[it]!! }) {
            throw IllegalArgumentException("Нельзя вернуть незанятое место")
        }

        val seatsToRefund = seatsToReturn.takeIf { it.size <= numberOfTickets }
            ?: session.availableSeatsInHall.filter { !it.value }.keys.toList().take(numberOfTickets)

        for (seat in seatsToRefund) {
            session.availableSeatsInHall[seat] = true
        }

        movie.boxOffice -= seatsToRefund.count() * session.ticketPrice

        cinemaRepository.saveMovies(cinema.getAllMovies())
    }
}
