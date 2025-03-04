package auth

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8085, module = Application::myAuth).start(wait = true)
}

fun Application.myAuth() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    routing {
        get("/hi") {
            call.respond(mapOf("service" to "auth", "message" to "Hi from myAuth"))
        }
    }
}
