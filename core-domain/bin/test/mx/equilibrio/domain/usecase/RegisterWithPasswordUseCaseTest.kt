package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import mx.equilibrio.domain.model.PlanTier
import mx.equilibrio.domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegisterWithPasswordUseCaseTest {

    private val user = User(
        id = "u1",
        googleId = null,
        email = "nueva@equilibrio.mx",
        displayName = "Nueva",
        givenName = null,
        familyName = null,
        photoUrl = null,
        plan = PlanTier.FREE,
        hasPassword = true,
    )

    @Test
    fun `registro exitoso retorna el usuario y reenvia los datos tal cual al repositorio`() = runTest {
        val repository = FakeUserRepository(registerResult = Result.success(user))
        val useCase = RegisterWithPasswordUseCase(repository)

        val result = useCase("Nueva", "nueva@equilibrio.mx", "password123")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        assertEquals(Triple("Nueva", "nueva@equilibrio.mx", "password123"), repository.lastRegisterArgs)
    }

    @Test
    fun `correo ya en uso propaga el fallo del repositorio sin transformarlo`() = runTest {
        val failure = IllegalStateException("el correo ya está en uso")
        val repository = FakeUserRepository(registerResult = Result.failure(failure))
        val useCase = RegisterWithPasswordUseCase(repository)

        val result = useCase(null, "repetido@equilibrio.mx", "password123")

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }

    @Test
    fun `displayName nulo se reenvia sin forzar un valor`() = runTest {
        val repository = FakeUserRepository(registerResult = Result.success(user))
        val useCase = RegisterWithPasswordUseCase(repository)

        useCase(null, "nueva@equilibrio.mx", "password123")

        assertEquals(null, repository.lastRegisterArgs?.first)
    }
}
