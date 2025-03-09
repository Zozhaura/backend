package userImitation

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.random.Random

fun main() {
    embeddedServer(Netty, port = 8084, module = Application::myUserImitation).start(wait = true)
}

fun Application.myUserImitation() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    val client = HttpClient(CIO)

    environment.monitor.subscribe(ApplicationStopped) {
        client.close()
    }

    routing {
        get("/hi") {
            call.respond(mapOf("service" to "imitation", "message" to "Hi from myUserImitation"))
        }

        get("/start") {
            launch { simulateUsers(client) }
        }
    }
}

suspend fun simulateUsers(client: HttpClient) {
    repeat(10_000) { userId ->
//        launch {
//            while (true) {
//                delay(Random.nextLong(500, 5000))
//                val action = listOf("search", "recommendation").random()
//                val query = listOf("apple", "banana", "salad", "chicken", "pasta").random()
//                try {
//                    val response: String = client.get("http://localhost:8082/$action?query=$query").body()
//                    logAction(client, userId, action, query, response)
//                } catch (e: Exception) {
//                    logAction(client, userId, action, query, "error: ${e.message}")
//                }
//            }
//        }
    }
}

suspend fun logAction(client: HttpClient, userId: Int, action: String, query: String, result: String) {
    val logData = mapOf(
        "level" to "INFO",
        "message" to "User $userId performed $action with query: $query. Result: $result",
        "serviceName" to "userImitation",
        "status" to 200,
        "executionTime" to Random.nextLong(10, 100)
    )
    client.post("http://localhost:8083/log") { setBody(logData) }
}
