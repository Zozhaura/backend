package proxy

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::myProxy).start(wait = true)
}

fun Application.myProxy() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    val client = HttpClient(CIO)

    environment.monitor.subscribe(ApplicationStopped) {
        client.close()
    }

    routing {
        // Здесь в целом все написано, мы просто передаем запрос нужному микросервису
        // Единственное что здесь потребуется менять - это добавлять различные варианты запросов
        // по типу post, delete и тд
        // Также очевидно может потребоваться отредактировать существующий метод
        // Например action может быть не нужен вообще или будет Path-переменные или Query-параметры
        get("/devices/{action}") {
            val action = call.parameters["action"]
            val response: String = client.get("http://localhost:8081/$action").body()
            call.respond(response)
        }
        get("/food/{action}") {
            val action = call.parameters["action"]
            val response: String = client.get("http://localhost:8082/$action").body()
            call.respond(response)
        }
        get("/logs/{action}") {
            val action = call.parameters["action"]
            val response: String = client.get("http://localhost:8083/$action").body()
            call.respond(response)
        }
        get("/imitation/{action}") {
            val action = call.parameters["action"]
            val response: String = client.get("http://localhost:8084/$action").body()
            call.respond(response)
        }
        get("/auth/{action}") {
            val action = call.parameters["action"]
            val response: String = client.get("http://localhost:8085/$action").body()
            call.respond(response)
        }
    }
}
