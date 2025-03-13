package food

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
//Routes.kt
fun Application.configureRouting() {
    routing {
        get("/recipes_search") {
            val query = call.request.queryParameters["name"]
            val categories = call.request.queryParameters.getAll("category")
            val includeIngredients = call.request.queryParameters.getAll("includeIngredients")
            val excludeIngredients = call.request.queryParameters.getAll("excludeIngredients")

            val recipes = RecipeService.searchRecipesByName(
                query = query,
                categories = categories,
                includeIngredients = includeIngredients,
                excludeIngredients = excludeIngredients
            )
            call.respond(recipes)
        }
        get("/recipes/recommendation") {
            val excludeIngredients = call.request.queryParameters.getAll("excludeIngredients")
            val recipes = RecipeService.getRecommendedRecipes(excludeIngredients)
            call.respond(recipes)
        }

        get("/recipes/{id}") {
            val recipeId = call.parameters["id"]?.toIntOrNull()
            if (recipeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID рецепта")
                return@get
            }
            val recipe = RecipeService.getRecipeById(recipeId)
            if (recipe == null) {
                call.respond(HttpStatusCode.NotFound, "Рецепт с ID $recipeId не найден")
            } else {
                call.respond(recipe)
            }
        }


        get("/products/search") {
            val query = call.request.queryParameters["name"]
            val products = ProductService.searchProductsByName(query)
            call.respond(products)
        }

    }
}