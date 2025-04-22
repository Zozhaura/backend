package logs

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logs.LogEntry


val logClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
}

suspend fun logToCentralService(log: LogEntry) {
    try {
        logClient.post("http://localhost:8083/log") {
            contentType(ContentType.Application.Json)
            setBody(log)
        }
    } catch (e: Exception) {
        println("Ошибка отправки лога: ${e.message}")
    }
}

