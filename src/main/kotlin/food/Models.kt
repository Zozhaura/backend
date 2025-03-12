package food

import kotlinx.serialization.Serializable
//Models.kt
@Serializable
data class FullRecipeDTO(
    val id: Int,
    val name: String,
    val preparationMethod: String,
    val category: String?,
    val nutrition: NutritionDTO?,
    val ingredients: List<IngredientDTO>
)

@Serializable
data class NutritionDTO(
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrates: Double,
    val dietaryFiber: Double?,
    val water: Double?
)

@Serializable
data class IngredientDTO(val name: String, val quantity: String)

@Serializable
data class RecipeShortDTO(
    val id: Int,
    val name: String,
    val nutrition: NutritionDTO
)

