package devices

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 8081, module = Application::myDevices).start(wait = true)
}

fun Application.myDevices() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
    }

    routing {
        webSocket("/steps") {
            var steps = 0
            while (true) {
                delay(3000)
                steps += (1..5).random()
                send("$steps")
            }
        }
        webSocket("/pulse") {
            var pulse: Int
            while (true) {
                delay(2000)
                pulse = (55..75).random()
                send("$pulse")
            }
        }
    }
}
