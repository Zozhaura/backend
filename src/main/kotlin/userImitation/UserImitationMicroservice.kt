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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = 8084, module = Application::myUserImitation).start(wait = true)
}

fun Application.myUserImitation() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    val client = HttpClient(CIO)

    val imitationScope = CoroutineScope(Dispatchers.Default)

    environment.monitor.subscribe(ApplicationStopped) {
        imitationScope.cancel()
        client.close()
    }

    val semaphore = Semaphore(5)
    val log = LoggerFactory.getLogger("Имитация")
    var isImitationActive = true

    routing {
        get("/start") {
            repeat(10000) {
                imitationScope.launch {
                    semaphore.withPermit {
                        try {
                            simulateUsers(client)
                        } catch (e: Exception) {
                            log.error("simulateUsers error", e)
                        }
                    }
                }
            }
            call.respond(mapOf("status" to "started"))
        }
        get("/stop") {
            isImitationActive = false
            call.respond(mapOf("status" to "stopped"))
        }
    }
}

suspend fun simulateUsers(client: HttpClient) {
    while (true) {
        val action = when (Random.nextInt(4)) {
            0 -> "recipes_search"
            1 -> "recipes_recommendation"
            2 -> "recipe"
            3 -> "products_search"
            else -> "recipes_search"
        }

        val url = when (action) {
            "recipes_search" -> {
                val query = listOf("name", "category", "includeIngredients", "excludeIngredients").random()
                val value = listOf("варенье", "лимон", "сахар", "вода").random()
                "/food/recipes_search?$query=$value"
            }
            "recipes_recommendation" -> "/food/recipes_recommendation"
            "recipe" -> {
                val id = Random.nextInt(1, 1000)
                "/food/recipe?id=$id"
            }
            "products_search" -> {
                val query = listOf("молоко", "сахар", "лимон", "вода").random()
                "/food/products_search?name=$query"
            }
            else -> "/food/recipes_search"
        }

        try {
            val response: String = client.get("http://localhost:8080$url").body()
            println("Request sent to $url")

        } catch (e: Exception) {
            println("Failed to send request to $url: ${e.message}")
        }

        delay(4000)
    }
}
