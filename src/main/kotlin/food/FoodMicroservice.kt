package food

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
//FoodMicroservice.kt
fun main() {
    initDatabase()
    embeddedServer(Netty, port = 8082, module = Application::myFood).start(wait = true)
}

fun Application.myFood() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }
    configureRouting()
}