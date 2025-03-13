package food

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object ProductService {
    fun searchProductsByName(query: String?): List<ProductDTO> {
        return transaction {
            // Если запрос пустой, возвращаем первые 30 записей
            if (query.isNullOrBlank()) {
                return@transaction Product
                    .selectAll()
                    .limit(30)
                    .map {
                        ProductDTO(
                            id = it[Product.id],
                            name = it[Product.name],
                            calories = it[Product.calories],
                            proteins = it[Product.proteins],
                            fats = it[Product.fats],
                            carbohydrates = it[Product.carbohydrates]
                        )
                    }
            }

            // Выполняем поиск по подстроке (без учета регистра)
            Product
                .select { Product.name.lowerCase() like "%${query.lowercase()}%" }
                .map {
                    ProductDTO(
                        id = it[Product.id],
                        name = it[Product.name],
                        calories = it[Product.calories],
                        proteins = it[Product.proteins],
                        fats = it[Product.fats],
                        carbohydrates = it[Product.carbohydrates]
                    )
                }
        }
    }
}
