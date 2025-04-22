package proxy

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class ProxyModuleTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun `proxy forwards POST to auth login and returns response`() = testApplication {
        val mockEngine = MockEngine { request ->
            assertEquals("http://localhost:8085/login", request.url.toString())

            respond(
                content = """{"token":"mocked-token"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        application {
            myProxy(mockClient)
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"user", "password":"pass"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        println("Proxy response: $responseBody")
        assertTrue(responseBody.contains("mocked-token"))
    }

    @Test
    fun `proxy forwards GET to food service with query params`() = testApplication {
        val mockEngine = MockEngine { request ->
            assertEquals("http://localhost:8082/recipes?name=apple", request.url.toString())

            respond(
                content = """[{"id":1,"name":"Apple Pie"}]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        application {
            myProxy(mockClient)
        }

        val response = client.get("/food/recipes?name=apple")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseText = response.bodyAsText()
        println("Proxy response: $responseText")
        assertTrue(responseText.contains("Apple Pie"))
    }


}
