package food

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object RecipeService {
    fun searchRecipesByName(
        query: String?,
        categories: List<String>?,
        includeIngredients: List<String>? = null,
        excludeIngredients: List<String>? = null
    ): List<RecipeShortDTO> {
        return transaction {
            val baseQuery = Recipe
                .leftJoin(Nutrition, { Recipe.id }, { Nutrition.recipeId })
                .leftJoin(Category, { Recipe.categoryId }, { Category.id })
                .slice(
                    Recipe.id,
                    Recipe.name,
                    Nutrition.calories,
                    Nutrition.proteins,
                    Nutrition.fats,
                    Nutrition.carbohydrates
                )
                .selectAll()

            // Фильтр по названию
            if (!query.isNullOrBlank()) {
                baseQuery.andWhere {
                    Recipe.name.castTo<String>(TextColumnType()).lowerCase() like "%${query.lowercase()}%"
                }
            }

            // Фильтр по категориям
            if (!categories.isNullOrEmpty()) {
                baseQuery.andWhere { Category.name inList categories }
            }

            // Фильтр по ингредиентам, которые должны присутствовать
            if (!includeIngredients.isNullOrEmpty()) {
                val subQuery = RecipeIngredient
                    .join(Ingredient, JoinType.INNER, RecipeIngredient.ingredientId, Ingredient.id)
                    .slice(RecipeIngredient.recipeId)
                    .select { Ingredient.name inList includeIngredients }
                    .groupBy(RecipeIngredient.recipeId)
                    .having { RecipeIngredient.recipeId.count() eq includeIngredients.size.toLong() }


                baseQuery.andWhere { Recipe.id inSubQuery subQuery }
            }

            // Фильтр по ингредиентам, которые должны отсутствовать
            if (!excludeIngredients.isNullOrEmpty()) {
                val subQuery = RecipeIngredient
                    .join(Ingredient, JoinType.INNER, RecipeIngredient.ingredientId, Ingredient.id)
                    .slice(RecipeIngredient.recipeId)
                    .select { Ingredient.name inList excludeIngredients }

                baseQuery.andWhere { Recipe.id notInSubQuery subQuery }
            }

            // Получаем результаты
            baseQuery.map {
                RecipeShortDTO(
                    id = it[Recipe.id],
                    name = it[Recipe.name],
                    nutrition = NutritionDTO(
                        calories = it[Nutrition.calories] ?: 0.0,
                        proteins = it[Nutrition.proteins] ?: 0.0,
                        fats = it[Nutrition.fats] ?: 0.0,
                        carbohydrates = it[Nutrition.carbohydrates] ?: 0.0,
                        dietaryFiber = null,
                        water = null
                    )
                )
            }
        }
    }
    fun getRecipeById(recipeId: Int): FullRecipeDTO? {
        return transaction {
            // Ищем рецепт по ID
            val recipeRow = Recipe.select { Recipe.id eq recipeId }.singleOrNull() ?: return@transaction null

            // Ищем категорию (если есть)
            val category = recipeRow[Recipe.categoryId]?.let { categoryId ->
                Category.select { Category.id eq categoryId }.singleOrNull()?.get(Category.name)
            }

            // Ищем КБЖУ (если есть)
            val nutrition = Nutrition.select { Nutrition.recipeId eq recipeId }.singleOrNull()

            // Получаем список ингредиентов
            val ingredients = RecipeIngredient
                .join(Ingredient, JoinType.INNER, RecipeIngredient.ingredientId, Ingredient.id)
                .select { RecipeIngredient.recipeId eq recipeId }
                .map { IngredientDTO(it[Ingredient.name], it[RecipeIngredient.quantity]) }

            // Собираем объект FullRecipeDTO
            FullRecipeDTO(
                id = recipeRow[Recipe.id],
                name = recipeRow[Recipe.name],
                preparationMethod = recipeRow[Recipe.preparationMethod],
                category = category,
                nutrition = nutrition?.let {
                    NutritionDTO(
                        calories = it[Nutrition.calories],
                        proteins = it[Nutrition.proteins],
                        fats = it[Nutrition.fats],
                        carbohydrates = it[Nutrition.carbohydrates],
                        dietaryFiber = it[Nutrition.dietaryFiber],
                        water = it[Nutrition.water]
                    )
                },
                ingredients = ingredients
            )
        }
    }

    fun getRecommendedRecipes(excludeIngredients: List<String>? = null): List<RecipeShortDTO> {
        // Получаем текущее время на сервере
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Определяем категории в зависимости от времени
        val categories = when (currentHour) {
            in 4 until 12 -> listOf("Каши", "Выпечка")
            in 12 until 17 -> listOf("Первые блюда", "Вторые блюда")
            in 17 until 22 -> listOf("Салаты")
            else -> listOf("Закуски")
        }

        // Вызываем searchRecipesByName с определенными категориями и excludeIngredients
        return searchRecipesByName(
            query = null,
            categories = categories,
            includeIngredients = null,
            excludeIngredients = excludeIngredients
        )
    }
}





