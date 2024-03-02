package ru.hse.software.construction.repository

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import ru.hse.software.construction.model.Movie
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FileCinemaException(
    filePath: String,
    override val message: String? = null,
    cause: Throwable? = null
) : RuntimeException("Проблема с файлом $filePath\n$message", cause)


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDate::class)
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm")

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val formattedString = value.format(formatter)
        encoder.encodeString(formattedString)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val string = decoder.decodeString()
        return LocalDateTime.parse(string, formatter)
    }
}

interface CinemaApp {
    fun addMovieToStock(movie: Movie)
    fun removeMovieFromStock(movie: Movie)
    fun getAllMovies(): List<Movie>
    fun findMovieByTitle(title: String): Movie?
}

class CinemaRepository (
    path: String,
    fileName: String
) {

    private val filePath = "$path/$fileName"
    private val file = File(filePath)

    fun loadMovies() : MutableList<Movie> {
        try {
            return if (file.exists()) {
                val jsonString = file.readText()
                val movies = Json.decodeFromString<MutableList<Movie>>(jsonString)
                movies.forEach { movie ->
                    movie.sessions.forEach { session ->
                        // Десериализация свободных мест в зале
                        val availableSeats = Json.decodeFromString<MutableMap<Int, Boolean>>(session.availableSeatsInHallJson)
                        session.availableSeatsInHall = availableSeats
                    }
                }
                movies
            } else {
                mutableListOf()
            }
        } catch (ex: Throwable) {
            throw FileCinemaException(filePath, "Нельзя загрузить данные кинотеатра.", ex)
        }
    }

    fun saveMovies(movies : List<Movie>) {
        try {
            movies.forEach { movie ->
                movie.sessions.forEach { session ->
                    // Сериализация свободных мест в зале
                    session.availableSeatsInHallJson = Json.encodeToString(session.availableSeatsInHall)
                }
            }
            val jsonString = Json.encodeToString(movies)
            file.writeText(jsonString)
        } catch (ex: Throwable) {
            throw FileCinemaException(filePath, "Нельзя сохранить данные кинотеатра.", ex)
        }
    }
}

class JsonCinemaApp(
    private val repository: CinemaRepository,
    private var moviesInStock: MutableList<Movie>
) : CinemaApp {

    init {
        moviesInStock = repository.loadMovies()
    }

    override fun addMovieToStock(movie: Movie) {
        moviesInStock.add(movie)
        repository.saveMovies(moviesInStock)
    }

    override fun removeMovieFromStock(movie: Movie) {
        moviesInStock.remove(movie)
        repository.saveMovies(moviesInStock)
    }

    override fun getAllMovies(): List<Movie> {
        return moviesInStock.toList()
    }

    override fun findMovieByTitle(title: String): Movie? {
        return moviesInStock.find { it.title == title }
    }
}