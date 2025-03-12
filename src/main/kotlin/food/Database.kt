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


