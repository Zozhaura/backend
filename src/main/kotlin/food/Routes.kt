package food

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
//Routes.kt
fun Application.configureRouting() {
    routing {
        get("/hi") {
            val recipe = getFullRecipeByName("Яблочный суп со сметаной")
            if (recipe != null) {
                call.respond(recipe)
            } else {
                call.respond(mapOf("error" to "Recipe not found"))
            }
        }
        get("/recipes/search") {
            val query = call.request.queryParameters["name"] ?: ""
            val recipes = RecipeService.searchRecipesByName(query)
            call.respond(recipes)
        }

    }
}