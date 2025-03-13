package food

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
//Routes.kt
fun Application.configureRouting() {
    routing {
        /**
         * Поиск рецептов.
         *
         * **HTTP Метод:** GET
         * **URL:** `/recipes_recommendation`
         *
         * @param name (query параметр) Название рецепта, необязателен.
         * @param category (query параметр) категория рецептов, необязателен.
         * @param includeIngredients (query параметр) Список ингредиентов, которые должны быть в рецепте, необязателен.
         * @param excludeIngredients (query параметр) Список ингредиентов, которые не должны быть в рецепте, необязателен.
         * @return JSON-массив объектов `RecipeShortDTO`, содержащий рекомендованные блюда.
         *
         * **Пример запроса:**
         * ```
         * GET GET /recipes_search?name=salad&category=vegetarian&category=healthy&includeIngredients=tomato&includeIngredients=cucumber&excludeIngredients=onion&excludeIngredients=garlic
         *
         * ```
         *
         * **Пример ответа (200 OK):**
         * ```json
         * [
         *     {
         *         "id": 1423,
         *         "name": "Салат из сырой моркови м яблоками",
         *         "nutrition": {
         *             "calories": 82.2,
         *             "proteins": 1.2,
         *             "fats": 4.8,
         *             "carbohydrates": 9.1,
         *             "dietaryFiber": null,
         *             "water": null
         *         }
         *     },
         *     {
         *         "id": 1424,
         *         "name": "Салат из сырой свеклы",
         *         "nutrition": {
         *             "calories": 76.7,
         *             "proteins": 1.2,
         *             "fats": 4.7,
         *             "carbohydrates": 8.0,
         *             "dietaryFiber": null,
         *             "water": null
         *         }
         *     },
         *     .
         *     .
         *     .
         * ]
         * ```
         *
         * **Возможные ошибки:**
         * - `500 Internal Server Error` – Ошибка сервера при обработке запроса.
         */
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

        /**
         * Запрос на получениt списка рекомендованных блюдна основе времени на сервере и исключающий ингредиенты, которые указал пользователь.
         *
         * **HTTP Метод:** GET
         * **URL:** `/recipes_recommendation`
         *
         * @return JSON-массив объектов `RecipeShortDTO`, содержащий рекомендованные блюда.
         *
         * **Пример запроса:**
         * ```
         * GET /recipes_recommendation
         * ```
         *
         * **Пример ответа (200 OK):**
         * ```json
         * [
         *     {
         *         "id": 1423,
         *         "name": "Салат из сырой моркови м яблоками",
         *         "nutrition": {
         *             "calories": 82.2,
         *             "proteins": 1.2,
         *             "fats": 4.8,
         *             "carbohydrates": 9.1,
         *             "dietaryFiber": null,
         *             "water": null
         *         }
         *     },
         *     {
         *         "id": 1424,
         *         "name": "Салат из сырой свеклы",
         *         "nutrition": {
         *             "calories": 76.7,
         *             "proteins": 1.2,
         *             "fats": 4.7,
         *             "carbohydrates": 8.0,
         *             "dietaryFiber": null,
         *             "water": null
         *         }
         *     },
         *     .
         *     .
         *     .
         * ]
         * ```
         *
         * **Возможные ошибки:**
         * - `500 Internal Server Error` – Ошибка сервера при обработке запроса.
         */
        get("/recipes_recommendation") {
            val excludeIngredients = call.request.queryParameters.getAll("excludeIngredients")
            val recipes = RecipeService.getRecommendedRecipes(excludeIngredients)
            call.respond(recipes)
        }

        /**
         * Поиск рецепта по id.
         *
         * **HTTP Метод:** GET
         * **URL:** `/recipe`
         *
         * @param id (query параметр) Индекс рецепта, обязателен.
         * @return JSON объект `FullRecipeDTO`, содержащий найденный рецепт.
         *
         * **Пример запроса:**
         * ```
         * GET /recipe?id=777
         * ```
         *
         * **Пример ответа (200 OK):**
         * ```json
         * {
         *     "id": 777,
         *     "name": "Мусс лимонный",
         *     "preparationMethod": "С лимонов срезают цедру, разрезают пополам и отжимают сок. Цедру заливают горячей водой, варят 5-6 мин, процеживают, в отвар добавляют сахар, вводят подготовленный желатин (с. 337), соединяют его с лимонным соком, охлаждают и взбивают. При отпуске мусс поливают сиропом сахарным, или сиропом плодовым, или ягодным натуральным (20 г на порцию).",
         *     "category": "Десерты",
         *     "nutrition": {
         *         "calories": 115.1,
         *         "proteins": 2.4,
         *         "fats": 0.0,
         *         "carbohydrates": 28.1,
         *         "dietaryFiber": 0.5,
         *         "water": 83.7
         *     },
         *     "ingredients": [
         *         {
         *             "name": "сахар",
         *             "quantity": "300.0 (грамм)"
         *         },
         *         {
         *             "name": "вода",
         *             "quantity": "700.0 (грамм)"
         *         },
         *         {
         *             "name": "лимон",
         *             "quantity": "238.0 (грамм)"
         *         },
         *         {
         *             "name": "желатин пищевой",
         *             "quantity": "27.0 (грамм)"
         *         }
         *     ]
         * }
         * ```
         *
         * **Возможные ошибки:**
         * - `400 Bad Request` – Некорректный ID рецепта.
         * - `404 Mot Found` – Рецепт с ID 777 не найден.
         * - `500 Internal Server Error` – Ошибка сервера при обработке запроса.
         */
        get("/recipe") {
            val recipeId = call.request.queryParameters["id"]?.toIntOrNull()
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

        /**
         * Поиск продуктов по подстроке в названии без учета регистра.
         *
         * **HTTP Метод:** GET
         * **URL:** `/products_search`
         *
         * @param name (query параметр) Подстрока для поиска в названии продукта, необязателен. Если не указана, возвращает все продукты из базы данных.
         * @return JSON-массив объектов `ProductDTO`, содержащий найденные продукты.
         *
         * **Пример запроса:**
         * ```
         * GET /products_search?name=молоко
         * ```
         *
         * **Пример ответа (200 OK):**
         * ```json
         * [
         *     {
         *         "id": 301,
         *         "name": "Блины со сгущеным молоком",
         *         "calories": 319.4,
         *         "proteins": 8.1,
         *         "fats": 12.4,
         *         "carbohydrates": 41.7
         *     },
         *     {
         *         "id": 1385,
         *         "name": "Кофе с молоком и сахаром",
         *         "calories": 58.1,
         *         "proteins": 0.7,
         *         "fats": 1.0,
         *         "carbohydrates": 11.2
         *     }
         * ]
         * ```
         *
         * **Возможные ошибки:**
         * - `500 Internal Server Error` – Ошибка сервера при обработке запроса.
         */
        get("/products_search") {
            val query = call.request.queryParameters["name"]
            val products = ProductService.searchProductsByName(query)
            call.respond(products)
        }


    }
}