package auth

import io.ktor.server.testing.*
import io.ktor.http.*
import kotlin.test.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.mockk.*
import kotlinx.serialization.json.*

class AuthModuleTest {

    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setUp() {
        mockkObject(UserRepository)

        val hashed = hashPassword("password123")
        every { UserRepository.findUserByUsername("testuser") } returns UserInfo(
            username = "testuser",
            passwordHash = hashed,
            name = "Test User",
            height = 180.0,
            weight = 75.0,
            gender = "male",
            goalWeight = 70.0
        )
    }


    @Test
    fun `login with correct credentials returns token`() = testApplication {
        application { authModule() }

        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "username": "testuser",
                    "password": "password123"
                }
            """.trimIndent())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val authResponse = json.decodeFromString<AuthResponse>(body)
        assertTrue(authResponse.token.isNotBlank())
    }

    @Test
    fun `login with incorrect password returns 401`() = testApplication {
        application { authModule() }

        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "username": "testuser",
                    "password": "wrongpassword"
                }
            """.trimIndent())
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `register new user returns 201 Created`() = testApplication {
        application { authModule() }

        every { UserRepository.findUserByUsername("newuser") } returns null
        every { UserRepository.createUser(any()) } just Runs

        val response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("""
            {
                "username": "newuser",
                "password": "pass123",
                "name": "New User",
                "height": 175.0,
                "weight": 70.0,
                "gender": "female",
                "goalWeight": 65.0
            }
        """.trimIndent())
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `register existing user returns 409 Conflict`() = testApplication {
        application { authModule() }

        every { UserRepository.findUserByUsername("newuser") } returns mockk()

        val response = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("""{ "username": "newuser", "password": "pass", "name": "", "height": 0.0, "weight": 0.0, "gender": "", "goalWeight": 0.0 }""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `get userinfo returns user data with valid token`() = testApplication {
        application { authModule() }

        val token = generateToken("testuser")
        every { UserRepository.findUserByUsername("testuser") } returns UserInfo(
            "testuser", "hash", "User", 170.0, 60.0, "female", 55.0
        )

        val response = client.get("/userinfo") {
            header("Authorization", "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("testuser"))
    }

    @Test
    fun `update name returns OK with valid token`() = testApplication {
        application { authModule() }

        val token = generateToken("testuser")
        every { UserRepository.updateName("testuser", "Updated Name") } just Runs

        val response = client.post("/updatename") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "name": "Updated Name" }""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `update height returns OK`() = testApplication {
        application { authModule() }

        val token = generateToken("testuser")
        every { UserRepository.updateHeight("testuser", 180.5) } just Runs

        val response = client.post("/updateheight") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "height": 180.5 }""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `update weight returns OK`() = testApplication {
        application { authModule() }

        val token = generateToken("testuser")
        every { UserRepository.updateWeight("testuser", 75.0) } just Runs

        val response = client.post("/updateweight") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "weight": 75.0 }""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `update goal returns OK`() = testApplication {
        application { authModule() }

        val token = generateToken("testuser")
        every { UserRepository.updateGoal("testuser", 65.0) } just Runs

        val response = client.post("/updategoal") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "goal": 65.0 }""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

}
