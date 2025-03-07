package food

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/hi") {
            val recipe = getFullRecipeByName("Яично-масляный соус")
            if (recipe != null) {
                call.respond(recipe)
            } else {
                call.respond(mapOf("error" to "Recipe not found"))
            }
        }
    }
}