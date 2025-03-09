package proxy

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.request.*
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

        route("/auth/{action}") {
            handle {
                val action = call.parameters["action"]
                val requestBody = call.receiveOrNull<String>()
                val response: String = client.request("http://localhost:8085/$action") {
                    method = call.request.httpMethod
                    contentType(ContentType.Application.Json)
                    requestBody?.let { setBody(it) }
                    headers.appendAll(call.request.headers)
                }.body()
                call.respond(response)
            }
        }
    }
}
