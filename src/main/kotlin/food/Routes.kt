package food

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
//Routes.kt
fun Application.configureRouting() {
    routing {
        get("/recipes/search") {
            val query = call.request.queryParameters["name"]
            val categories = call.request.queryParameters.getAll("category") // Получаем список категорий
            val recipes = RecipeService.searchRecipesByName(query, categories)
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