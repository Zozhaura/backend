package auth

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.util.*
import java.sql.Connection

object Database {
    private val properties = Properties().apply {
        val inputStream = ClassLoader.getSystemResourceAsStream("users.properties")
            ?: throw IllegalArgumentException("database.properties not found")
        load(inputStream)
    }

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = properties.getProperty("database.url")
        driverClassName = "org.postgresql.Driver"
        username = properties.getProperty("database.user")
        password = properties.getProperty("database.password")
        maximumPoolSize = properties.getProperty("database.maxPoolSize").toInt()
    }

    private val dataSource = HikariDataSource(hikariConfig)

    fun getConnection(): Connection = dataSource.connection
}
