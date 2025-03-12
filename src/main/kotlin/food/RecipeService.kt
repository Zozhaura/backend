package food

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object RecipeService {

    //TODO: оптимизировать запросы
    /*
    * Добавь индекс CREATE INDEX idx_recipe_name ON recipe (name);
    * Загружай только нужные данные.
    * */

    //выдает полный список рецептов по подстроке - рецепт самый полный
//    fun searchRecipesByName(query: String): List<FullRecipeDTO> {
//        return transaction {
//            Recipe.select { Recipe.name.castTo<String>(TextColumnType()).lowerCase() like "%${query.lowercase()}%" }
//                .map { recipeRow ->
//                    val category = recipeRow[Recipe.categoryId]?.let { categoryId ->
//                        Category.select { Category.id eq categoryId }.singleOrNull()?.get(Category.name)
//                    }
//                    val nutrition = Nutrition.select { Nutrition.recipeId eq recipeRow[Recipe.id] }.singleOrNull()
//                    val ingredients = RecipeIngredient
//                        .join(Ingredient, JoinType.INNER, RecipeIngredient.ingredientId, Ingredient.id)
//                        .select { RecipeIngredient.recipeId eq recipeRow[Recipe.id] }
//                        .map { IngredientDTO(it[Ingredient.name], it[RecipeIngredient.quantity]) }
//
//                    FullRecipeDTO(
//                        id = recipeRow[Recipe.id],
//                        name = recipeRow[Recipe.name],
//                        preparationMethod = recipeRow[Recipe.preparationMethod],
//                        category = category,
//                        nutrition = nutrition?.let {
//                            NutritionDTO(
//                                it[Nutrition.calories],
//                                it[Nutrition.proteins],
//                                it[Nutrition.fats],
//                                it[Nutrition.carbohydrates],
//                                it[Nutrition.dietaryFiber],
//                                it[Nutrition.water]
//                            )
//                        },
//                        ingredients = ingredients
//                    )
//                }
//        }
//    }

    fun searchRecipesByName(query: String): List<RecipeShortDTO> {
        return transaction {
            Recipe
                .leftJoin(Nutrition, { Recipe.id }, { Nutrition.recipeId }) // Присоединяем Nutrition
                .slice(Recipe.id, Recipe.name, Nutrition.calories, Nutrition.proteins, Nutrition.fats, Nutrition.carbohydrates)
                .select { Recipe.name.castTo<String>(TextColumnType()).lowerCase() like "%${query.lowercase()}%" }
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
    }
}

