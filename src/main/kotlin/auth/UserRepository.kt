package auth

import UserInfo
import java.sql.Connection
import java.sql.ResultSet

object UserRepository {
    fun findUserByUsername(username: String): UserInfo? {
        val query = "SELECT * FROM users WHERE username = ?"
        Database.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, username)
                val rs = stmt.executeQuery()
                return if (rs.next()) resultSetToUser(rs) else null
            }
        }
    }

    fun createUser(user: UserInfo) {
        val query = """
            INSERT INTO users (username, password_hash, name, height, weight, gender, goal_weight) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        Database.getConnection().use { conn ->
            conn.prepareStatement(query).use { stmt ->
                stmt.setString(1, user.username)
                stmt.setString(2, user.passwordHash)
                stmt.setString(3, user.name)
                stmt.setDouble(4, user.height)
                stmt.setDouble(5, user.weight)
                stmt.setString(6, user.gender)
                stmt.setDouble(7, user.goalWeight)
                stmt.executeUpdate()
            }
        }
    }

    private fun resultSetToUser(rs: ResultSet): UserInfo {
        return UserInfo(
            username = rs.getString("username"),
            passwordHash = rs.getString("password_hash"),
            name = rs.getString("name"),
            height = rs.getDouble("height"),
            weight = rs.getDouble("weight"),
            gender = rs.getString("gender"),
            goalWeight = rs.getDouble("goal_weight")
        )
    }
}
