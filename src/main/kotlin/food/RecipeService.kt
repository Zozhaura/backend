package food

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object RecipeService {
    fun searchRecipesByName(query: String?, categories: List<String>?): List<RecipeShortDTO> {
        return transaction {
            // Если нет query и категорий, возвращаем первые 30 рецептов
            if (query.isNullOrBlank() && categories.isNullOrEmpty()) {
                return@transaction Recipe
                    .leftJoin(Nutrition, { Recipe.id }, { Nutrition.recipeId })
                    .slice(Recipe.id, Recipe.name, Nutrition.calories, Nutrition.proteins, Nutrition.fats, Nutrition.carbohydrates)
                    .selectAll()
                    .limit(30)
                    .map {
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

            // Базовый запрос
            val baseQuery = Recipe
                .leftJoin(Nutrition, { Recipe.id }, { Nutrition.recipeId })
                .leftJoin(Category, { Recipe.categoryId }, { Category.id })
                .slice(Recipe.id, Recipe.name, Nutrition.calories, Nutrition.proteins, Nutrition.fats, Nutrition.carbohydrates)
                .selectAll()

            // Фильтр по названию
            if (!query.isNullOrBlank()) {
                baseQuery.andWhere { Recipe.name.castTo<String>(TextColumnType()).lowerCase() like "%${query.lowercase()}%" }
            }

            // Фильтр по категориям
            if (!categories.isNullOrEmpty()) {
                baseQuery.andWhere { Category.name inList categories }
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
}

