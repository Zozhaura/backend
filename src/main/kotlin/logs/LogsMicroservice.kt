package logs

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8083, module = Application::myLogs).start(wait = true)
}

fun Application.myLogs() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    routing {
        get("/hi") {
            call.respond(mapOf("service" to "logs", "message" to "Hi from myLogs"))
        }
    }
}