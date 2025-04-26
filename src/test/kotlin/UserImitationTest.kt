package userImitation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class UserImitationTest {

    private val client = HttpClient(CIO)
    private val baseUrl = "http://localhost:8084"

    @Test
    fun testStartImitation() = runBlocking {
        val response: String = client.get("$baseUrl/start").body()
        println("Start response: $response")
        assertTrue(response.contains("started"), "Response should indicate 'started'")
    }

    @Test
    fun testStopImitation() = runBlocking {
        val response: String = client.get("$baseUrl/stop").body()
        println("Stop response: $response")
        assertTrue(response.contains("stopped"), "Response should indicate 'stopped'")
    }

    @Test
    fun testStartThenStop() = runBlocking {
        val startResponse: String = client.get("$baseUrl/start").body()
        assertTrue(startResponse.contains("started"), "Start response should indicate 'started'")

        val stopResponse: String = client.get("$baseUrl/stop").body()
        assertTrue(stopResponse.contains("stopped"), "Stop response should indicate 'stopped'")
    }
}