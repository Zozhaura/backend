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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.decodeFromString
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::myProxy).start(wait = true)
}

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
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
        route("/food/{action}") {
            handle {
                val action = call.parameters["action"] ?: return@handle call.respond(
                    HttpStatusCode.BadRequest, "Missing action"
                )

                val encodedParams = call.request.queryParameters.entries()
                    .flatMap { (key, values) ->
                        values.map { value ->
                            "${URLEncoder.encode(key, StandardCharsets.UTF_8)}=${
                                URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
                            }"
                        }
                    }.joinToString("&")


                val fullUrl = if (encodedParams.isNotEmpty()) {
                    "$action?$encodedParams"
                } else {
                    action
                }

                val requestBody = call.receiveOrNull<String>()

                try {
                    val foodResponse: String = client.request("http://localhost:8082/$fullUrl") {
                        method = call.request.httpMethod
                        contentType(ContentType.Application.Json)
                        requestBody?.let { setBody(it) }
                        headers.appendAll(call.request.headers)
                    }.body()

                    println(">>> Ответ от food-сервиса на /food/$fullUrl:")
                    println(foodResponse)

                    val parsedJson = json.decodeFromString<JsonElement>(foodResponse)

                    call.respond(
                        JsonObject(
                            mapOf(
                                "message" to JsonPrimitive("Ответ от сервиса еды получен"),
                                "proxyPath" to JsonPrimitive("/food/$fullUrl"),
                                "foodResponse" to parsedJson
                            )
                        )
                    )



                } catch (e: Exception) {
                    println(">>> Ошибка при обращении к food-сервису на /food/$fullUrl: ${e.localizedMessage}")

                    call.respond(
                        HttpStatusCode.BadGateway,
                        mapOf(
                            "error" to "Не удалось получить ответ от food-сервиса",
                            "details" to e.localizedMessage
                        )
                    )
                }
            }
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

                val httpResponse = client.request("http://localhost:8085/$action") {
                    method = call.request.httpMethod
                    contentType(ContentType.Application.Json)
                    requestBody?.let { setBody(it) }
                    headers.appendAll(call.request.headers)
                }

                val responseBytes = httpResponse.body<ByteArray>()
                val responseContentType = httpResponse.headers[HttpHeaders.ContentType]?.let { ContentType.parse(it) }
                val status = httpResponse.status

                call.respondBytes(
                    bytes = responseBytes,
                    contentType = responseContentType ?: ContentType.Application.Json,
                    status = status
                )
            }
        }
    }
}
