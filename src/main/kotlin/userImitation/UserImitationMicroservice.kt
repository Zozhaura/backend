package userImitation

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8084, module = Application::myUserImitation).start(wait = true)
}

fun Application.myUserImitation() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    routing {
        get("/hi") {
            call.respond(mapOf("service" to "imitation", "message" to "Hi from myUserImitation"))
        }
    }
}