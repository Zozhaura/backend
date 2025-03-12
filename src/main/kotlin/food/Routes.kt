package food

import io.ktor.http.*
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
            val query = call.request.queryParameters["name"]
            val category = call.request.queryParameters["category"]
            val recipes = RecipeService.searchRecipesByName(query, category)
            call.respond(recipes)
        }
        get("/recipes/{id}") {
            val recipeId = call.parameters["id"]?.toIntOrNull()

            // Проверяем корректность ID
            if (recipeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID рецепта")
                return@get
            }

            // Получаем рецепт
            val recipe = RecipeService.getRecipeById(recipeId)

            // Если не найден — отправляем 404
            if (recipe == null) {
                call.respond(HttpStatusCode.NotFound, "Рецепт с ID $recipeId не найден")
            } else {
                call.respond(recipe)
            }
        }

    }
}