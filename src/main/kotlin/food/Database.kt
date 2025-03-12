package food

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.util.*
//Database.kt
object Recipe : Table("recipe") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val preparationMethod = text("preparation_method")
    val categoryId = integer("category_id").nullable()
    override val primaryKey = PrimaryKey(id)
}

object Category : Table("category") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    override val primaryKey = PrimaryKey(id)
}

object Nutrition : Table("nutrition") {
    val recipeId = integer("recipe_id").references(Recipe.id, onDelete = ReferenceOption.CASCADE)
    val calories = double("calories")
    val proteins = double("proteins")
    val fats = double("fats")
    val carbohydrates = double("carbohydrates")
    val dietaryFiber = double("dietary_fiber").nullable()
    val water = double("water").nullable()
    override val primaryKey = PrimaryKey(recipeId)
}

object Ingredient : Table("ingredient") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    override val primaryKey = PrimaryKey(id)
}

object RecipeIngredient : Table("recipe_ingredient") {
    val recipeId = integer("recipe_id").references(Recipe.id, onDelete = ReferenceOption.CASCADE)
    val ingredientId = integer("ingredient_id").references(Ingredient.id, onDelete = ReferenceOption.CASCADE)
    val quantity = text("quantity")
    override val primaryKey = PrimaryKey(recipeId, ingredientId)
}


fun initDatabase() {
    val properties = Properties()
    properties.load(ClassLoader.getSystemResourceAsStream("database.properties"))

    val url = properties.getProperty("db.url")
    val driver = properties.getProperty("db.driver")
    val user = properties.getProperty("db.user")
    val password = properties.getProperty("db.password")

    Database.connect(url, driver = driver, user = user, password = password)
}

fun getFullRecipeByName(recipeName: String): FullRecipeDTO? {
    return transaction {
        val recipeRow = Recipe.select { Recipe.name eq recipeName }.singleOrNull() ?: return@transaction null
        val category = recipeRow[Recipe.categoryId]?.let { categoryId ->
            Category.select { Category.id eq categoryId }.singleOrNull()?.get(Category.name)
        }
        val nutrition = Nutrition.select { Nutrition.recipeId eq recipeRow[Recipe.id] }.singleOrNull()
        val ingredients = RecipeIngredient
            .join(Ingredient, JoinType.INNER, RecipeIngredient.ingredientId, Ingredient.id)
            .select { RecipeIngredient.recipeId eq recipeRow[Recipe.id] }
            .map { IngredientDTO(it[Ingredient.name], it[RecipeIngredient.quantity]) }

        FullRecipeDTO(
            id = recipeRow[Recipe.id],
            name = recipeRow[Recipe.name],
            preparationMethod = recipeRow[Recipe.preparationMethod],
            category = category,
            nutrition = nutrition?.let {
                NutritionDTO(
                    it[Nutrition.calories],
                    it[Nutrition.proteins],
                    it[Nutrition.fats],
                    it[Nutrition.carbohydrates],
                    it[Nutrition.dietaryFiber],
                    it[Nutrition.water]
                )
            },
            ingredients = ingredients
        )
    }
}

