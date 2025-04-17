import auth.UserRepository
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest

val secret = "supersecretkey"
val algorithm = Algorithm.HMAC256(secret)
val verifier: JWTVerifier = JWT.require(algorithm).withSubject("Authentication").build()

@Serializable
data class UserInfo(
    val username: String,
    val passwordHash: String,
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goalWeight: Double
)

@Serializable
data class AuthRequest(
    val username: String,
    val password: String,
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goalWeight: Double
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class UserResponse(
    val username: String,
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goalWeight: Double
)
@Serializable
data class UpdateNameRequest(val name: String)

@Serializable
data class UpdateHeightRequest(val height: Double)

@Serializable
data class UpdateWeightRequest(val weight: Double)

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

fun generateToken(username: String): String = JWT.create()
    .withSubject("Authentication")
    .withClaim("username", username)
    .sign(algorithm)

fun main() {
    embeddedServer(Netty, port = 8085, module = Application::authModule).start(wait = true)
}

fun Application.authModule() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true })
    }

    routing {
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = UserRepository.findUserByUsername(request.username)
            if (user != null && user.passwordHash == hashPassword(request.password)) {
                call.respond(AuthResponse(generateToken(request.username)))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        post("/register") {
            val request = call.receive<AuthRequest>()
            if (UserRepository.findUserByUsername(request.username) != null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
            } else {
                val newUser = UserInfo(
                    username = request.username,
                    passwordHash = hashPassword(request.password),
                    name = request.name,
                    height = request.height,
                    weight = request.weight,
                    gender = request.gender,
                    goalWeight = request.goalWeight
                )
                UserRepository.createUser(newUser)
                call.respond(HttpStatusCode.Created, AuthResponse(generateToken(request.username)))
            }
        }

        post("/updatename") {
            val token = call.request.header("Authorization")?.removePrefix("Bearer ")
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Missing token")
                return@post
            }
            try {
                val decodedJWT = verifier.verify(token)
                val username = decodedJWT.getClaim("username").asString()
                val request = call.receive<UpdateNameRequest>()
                UserRepository.updateName(username, request.name)
                call.respond(HttpStatusCode.OK, "Name updated")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request or token")
            }
        }

        post("/updateheight") {
            val token = call.request.header("Authorization")?.removePrefix("Bearer ")
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Missing token")
                return@post
            }
            try {
                val decodedJWT = verifier.verify(token)
                val username = decodedJWT.getClaim("username").asString()
                val request = call.receive<UpdateHeightRequest>()
                UserRepository.updateHeight(username, request.height)
                call.respond(HttpStatusCode.OK, "Height updated")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request or token")
            }
        }

        post("/updateweight") {
            val token = call.request.header("Authorization")?.removePrefix("Bearer ")
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Missing token")
                return@post
            }
            try {
                val decodedJWT = verifier.verify(token)
                val username = decodedJWT.getClaim("username").asString()
                val request = call.receive<UpdateWeightRequest>()
                UserRepository.updateWeight(username, request.weight)
                call.respond(HttpStatusCode.OK, "Weight updated")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid request or token")
            }
        }


        get("/userinfo") {
            val token = call.request.header("Authorization")?.removePrefix("Bearer ")
            if (token == null) {
                call.respond(HttpStatusCode.Unauthorized, "Missing token")
                return@get
            }
            try {
                val decodedJWT = verifier.verify(token)
                val username = decodedJWT.getClaim("username").asString()
                val user = UserRepository.findUserByUsername(username)
                if (user != null) {
                    call.respond(UserResponse(
                        username = user.username,
                        name = user.name,
                        height = user.height,
                        weight = user.weight,
                        gender = user.gender,
                        goalWeight = user.goalWeight
                    ))
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid token")
            }
        }
    }
}