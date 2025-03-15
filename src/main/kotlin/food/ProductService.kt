package food

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object ProductService {
    fun searchProductsByName(query: String?): List<ProductDTO> {
        return transaction {
            if (query.isNullOrBlank()) {
                return@transaction Product
                    .selectAll()
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
