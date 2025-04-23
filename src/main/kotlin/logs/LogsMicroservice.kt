package logs

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import java.sql.DriverManager
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.io.FileInputStream
import java.util.Properties

fun main() {
    embeddedServer(Netty, port = 8083, module = Application::myLogs).start(wait = true)
}

fun Application.myLogs() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
    }

    val props = Properties().apply {
        load(FileInputStream("src/main/resources/clickhouse.properties"))
    }

    val connection = DriverManager.getConnection(
        props.getProperty("clickhouse.url"),
        props.getProperty("clickhouse.user"),
        props.getProperty("clickhouse.password")
    )

    routing {
        get("/hi") {
            call.respond(mapOf("service" to "logs", "message" to "Hi from myLogs"))
        }

        post("/log") {
            val logEntry = call.receive<LogEntry>()
            try {
                connection.prepareStatement("""
                    INSERT INTO logs.log_data 
                    (level, message, service_name, status, execution_time, timestamp)
                    VALUES (?, ?, ?, ?, ?, now())
                """.trimIndent()).use { stmt ->
                    stmt.setString(1, logEntry.level)
                    stmt.setString(2, logEntry.message)
                    stmt.setString(3, logEntry.serviceName)
                    stmt.setInt(4, logEntry.status)
                    stmt.setLong(5, logEntry.executionTime)
                    stmt.executeUpdate()
                }
                call.respond(mapOf("status" to "success"))
            } catch (e: Exception) {
                println("Logging error: ${e.message}")
                call.respond(mapOf("status" to "error", "message" to e.message))
            }
        }
    }
}


@Serializable
data class LogEntry(
    val level: String,
    val message: String,
    val serviceName: String,
    val status: Int,
    val executionTime: Long
)