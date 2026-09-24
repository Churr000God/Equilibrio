package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import mx.equilibrio.domain.model.PlanTier
import mx.equilibrio.domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SignInWithPasswordUseCaseTest {

    private val user = User(
        id = "u1",
        googleId = null,
        email = "diego@equilibrio.mx",
        displayName = "Diego",
        givenName = null,
        familyName = null,
        photoUrl = null,
        plan = PlanTier.FREE,
        hasPassword = true,
    )

    @Test
    fun `credenciales validas retorna el usuario del repositorio`() = runTest {
        val repository = FakeUserRepository(signInResult = Result.success(user))
        val useCase = SignInWithPasswordUseCase(repository)

        val result = useCase("diego@equilibrio.mx", "correcta123")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals("diego@equilibrio.mx" to "correcta123", repository.lastSignInArgs)
    }

    @Test
    fun `credenciales invalidas propaga el fallo del repositorio sin transformarlo`() = runTest {
        val failure = IllegalArgumentException("credenciales inválidas")
        val repository = FakeUserRepository(signInResult = Result.failure(failure))
        val useCase = SignInWithPasswordUseCase(repository)

        val result = useCase("diego@equilibrio.mx", "incorrecta")

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
        assertNull(result.getOrNull())
    }
}
