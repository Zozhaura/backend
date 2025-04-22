package devices

import io.ktor.client.plugins.websocket.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlin.test.*

class DevicesModuleTest {

    @Test
    fun `steps websocket sends steps value`() = testApplication {
        application { myDevices() }

        val client = createClient {
            install(WebSockets)
        }

        val session = client.webSocketSession("/steps")
        val message = withTimeoutOrNull(4000) {
            session.incoming.receive() as? Frame.Text
        }

        assertNotNull(message)
    }

    @Test
    fun `pulse websocket sends pulse value`() = testApplication {
        application { myDevices() }

        val client = createClient {
            install(WebSockets)
        }

        val session = client.webSocketSession("/pulse")
        val message = withTimeoutOrNull(3000) {
            session.incoming.receive() as? Frame.Text
        }

        assertNotNull(message)
        val pulse = message.readText().toInt()
        assertTrue(pulse in 55..75)
    }

    @Test
    fun `maxpulse websocket sends max pulse value`() = testApplication {
        application { myDevices() }

        val client = createClient {
            install(WebSockets)
        }

        val session = client.webSocketSession("/maxpulse")
        val message = withTimeoutOrNull(3000) {
            session.incoming.receive() as? Frame.Text
        }

        assertNotNull(message)
        val max = message.readText().toIntOrNull()
        assertNotNull(max)
    }

    @Test
    fun `minpulse websocket sends min pulse value`() = testApplication {
        application { myDevices() }

        val client = createClient {
            install(WebSockets)
        }

        val session = client.webSocketSession("/minpulse")
        val message = withTimeoutOrNull(3000) {
            session.incoming.receive() as? Frame.Text
        }

        assertNotNull(message)
        val min = message.readText().toIntOrNull()
        assertNotNull(min)
    }


}
