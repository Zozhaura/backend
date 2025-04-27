import org.junit.jupiter.api.Test
import food.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class RecipeServiceTest {

    @BeforeTest
    fun initDatabase() {
        food.initDatabase()
        transaction {
            SchemaUtils.create(Product)

            Product.insert {
                it[name] = "Блины со сгущеным молоком"
                it[calories] = 319.4
                it[proteins] = 8.1
                it[fats] = 12.4
                it[carbohydrates] = 41.7
            }
            Product.insert {
                it[name] = "Кофе с молоком и сахаром"
                it[calories] = 58.1
                it[proteins] = 0.7
                it[fats] = 1.0
                it[carbohydrates] = 11.2
            }
            Product.insert {
                it[name] = "Яблоко"
                it[calories] = 52.0
                it[proteins] = 0.3
                it[fats] = 0.2
                it[carbohydrates] = 14.0
            }
        }
    }

    @AfterTest
    fun teardown() {
        transaction {
            SchemaUtils.drop(Product)
        }
    }

    @Test
    fun `searchRecipesByName should return recipes filtered by ingredients`() {
        transaction {
            val results = RecipeService.searchRecipesByName("мимоза", null)
            assertEquals(1, results.size)
            assertTrue(results.first().name.toLowerCasePreservingASCIIRules().contains("салат"))
        }
    }

//    @Test //not
//    fun `searchRecipesByName should return recipes filtered by name, category and ingredients`() {
//        transaction {
//            val results = RecipeService.searchRecipesByName(
//                query = "варенье",
//                categories = listOf("Варенье и джемы"),
//                includeIngredients = listOf("лимонная кислота", "вода"),
//                excludeIngredients = listOf("груша")
//            )
//
//            assertEquals(3, results.size)
//
//            val names = results.map { it.name }
//            assertTrue(names.contains("Варенье из айвы"))
//            assertTrue(names.contains("Варенье из арбузных корок"))
//            assertTrue(names.contains("Варенье из жимолости"))
//
//            results.forEach { recipe ->
//                assertNotNull(recipe.id)
//                assertNotNull(recipe.name)
//                assertNotNull(recipe.nutrition)
//                assertNotNull(recipe.nutrition.calories)
//                assertNotNull(recipe.nutrition.proteins)
//                assertNotNull(recipe.nutrition.fats)
//                assertNotNull(recipe.nutrition.carbohydrates)
//            }
//        }
//    }


    @Test
    fun `getRecommendedRecipes should return recommended recipes`() {
        transaction {
            val results = RecipeService.getRecommendedRecipes(
                excludeIngredients = listOf("орехи") // например, исключаем орехи
            )

            assertTrue(results.isNotEmpty())

            val firstRecipe = results.first()
            assertNotNull(firstRecipe.id)
            assertNotNull(firstRecipe.name)
            assertNotNull(firstRecipe.nutrition)
            assertNotNull(firstRecipe.nutrition.calories)
            assertNotNull(firstRecipe.nutrition.proteins)
            assertNotNull(firstRecipe.nutrition.fats)
            assertNotNull(firstRecipe.nutrition.carbohydrates)
        }
    }

    @Test
    fun `getRecipeById should return full recipe info`() {
        transaction {
            val recipeId = 1018 // ID из примера
            val recipe = RecipeService.getRecipeById(recipeId)

            assertNotNull(recipe)
            assertEquals(recipeId, recipe!!.id)
            assertEquals("Мусс лимонный", recipe.name)

            assertEquals("Десерты", recipe.category)
            assertNotNull(recipe.preparationMethod)

            // Проверяем нутриенты
            recipe.nutrition?.let { assertEquals(115.1, it.calories, 0.1) }
            recipe.nutrition?.let { assertEquals(2.4, it.proteins, 0.1) }
            recipe.nutrition?.let { assertEquals(0.0, it.fats, 0.1) }
            recipe.nutrition?.let { assertEquals(28.1, it.carbohydrates, 0.1) }
            recipe.nutrition?.dietaryFiber?.let { assertEquals(0.5, it, 0.1) }
            recipe.nutrition?.water?.let { assertEquals(83.7, it, 0.1) }

            // Проверяем ингредиенты
            assertEquals(4, recipe.ingredients.size)
            assertTrue(recipe.ingredients.any { it.name == "сахар" && it.quantity.contains("300.0") })
            assertTrue(recipe.ingredients.any { it.name == "вода" && it.quantity.contains("700.0") })
            assertTrue(recipe.ingredients.any { it.name == "лимон" && it.quantity.contains("238.0") })
            assertTrue(recipe.ingredients.any { it.name == "желатин пищевой" && it.quantity.contains("27.0") })
        }
    }


    @Test
    fun `getRecommendedRecipes should return recipes based on time of day (evening)`() {
        // Arrange
        // Имитация времени: вечер
        val currentHour = 19
        val expectedResult =
            RecipeShortDTO(
                id = 19,
                name = "Винегрет",
                nutrition = NutritionDTO(
                    calories = 130.1,
                    proteins = 1.7,
                    fats = 10.3,
                    carbohydrates = 8.2,
                    dietaryFiber = null,
                    water = null
                )

            )

        // Act
        val result = RecipeService.getRecommendedRecipes()

        // Assert
        assertContains(result, expectedResult)
    }


    @Test
    fun `searchProductsByName should return products filtered by query`() {
        transaction {
            val results = ProductService.searchProductsByName("молоко")

            // Проверка количества найденных продуктов
            assertEquals(2, results.size)

            // Проверка содержимого
            val productNames = results.map { it.name }
            assertTrue(productNames.contains("Блины со сгущеным молоком"))
            assertTrue(productNames.contains("Кофе с молоком и сахаром"))
        }
    }

    @Test
    fun `searchProductsByName should return all products when query is null`() {
        transaction {
            val results = ProductService.searchProductsByName(null)

            // Все 3 продукта должны быть возвращены
            assertEquals(3, results.size)
        }
    }

    @Test
    fun `searchProductsByName should return empty list when no products match`() {
        transaction {
            val results = ProductService.searchProductsByName("фастумгель")

            assertTrue(results.isEmpty())
        }
    }
}
