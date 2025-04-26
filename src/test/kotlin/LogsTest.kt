package logs

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.*

class LogsTest {

    @Test
    fun `test POST log saves entry and returns response`() = testApplication {
        application { myLogs() }

        val logEntry = LogEntry(
            level = "INFO",
            message = "Test log entry",
            serviceName = "testService",
            status = 200,
            executionTime = 123
        )

        val response = client.post("/log") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(logEntry))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("status"), "Response should contain 'status'")
    }

    @Test
    fun `test POST log with incorrect data returns error`() = testApplication {
        application { myLogs() }

        val response = client.post("/log") {
            contentType(ContentType.Application.Json)
            setBody("{invalid_json}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
