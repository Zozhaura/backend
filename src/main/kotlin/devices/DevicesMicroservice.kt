package devices

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.time.Duration
import kotlin.math.roundToInt

fun main() {
    embeddedServer(Netty, port = 8081, module = Application::myDevices).start(wait = true)
}

fun Application.myDevices() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
    }

    routing {
        val stepsChannel = Channel<Int>(Channel.CONFLATED)

        webSocket("/steps") {
            var steps = 0
            while (true) {
                delay(3000)
                val newSteps = (1..5).random()
                steps += newSteps
                stepsChannel.trySend(steps)
                send("$steps")
            }
        }

        webSocket("/pulse") {
            while (true) {
                delay(2000)
                val pulse = (55..75).random()
                send("$pulse")
            }
        }

        webSocket("/calories") {
            val weight = call.request.queryParameters["weight"]?.toDoubleOrNull() ?: 100.0
            val stepLength = 0.75
            val kcalPerKm = 0.65 * weight

            while (true) {
                val steps = stepsChannel.receive()
                val distance = steps * stepLength / 1000
                val calories = (distance * kcalPerKm).roundToInt()
                send("$calories")
            }
        }
    }
}
