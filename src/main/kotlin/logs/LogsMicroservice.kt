package logs

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import java.sql.DriverManager

fun main() {
    embeddedServer(Netty, port = 8083, module = Application::myLogs).start(wait = true)
}

fun Application.myLogs() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    val connection = DriverManager.getConnection(
        "jdbc:clickhouse://localhost:8123/logs",
        "default",
        ""
    )

    routing {
        get("/hi") {
            call.respond(mapOf("service" to "logs", "message" to "Hi from myLogs"))
        }

        post("/log") {
            val logEntry = call.receive<LogEntry>()
            try {
                val statement = connection.prepareStatement("""
        INSERT INTO logs.log_data (level, message, service_name, status, execution_time)
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent())
                statement.setString(1, logEntry.level)
                statement.setString(2, logEntry.message)
                statement.setString(3, logEntry.serviceName)
                statement.setInt(4, logEntry.status)
                statement.setLong(5, logEntry.executionTime)
                statement.executeUpdate()
            } catch (e: Exception) {
                call.respond(mapOf("status" to "error", "message" to "Failed to log data"))
                return@post
            }

        }
    }
}

data class LogEntry(
    val level: String,
    val message: String,
    val serviceName: String,
    val status: Int,
    val executionTime: Long
)
