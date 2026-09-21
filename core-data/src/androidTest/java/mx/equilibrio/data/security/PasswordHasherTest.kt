package mx.equilibrio.data.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Argon2Kt (com.lambdapioneer.argon2kt) es un binding Android-only con .so nativos
 * por ABI: no hay implementación desktop-JVM, así que no puede correr en JVM/Robolectric.
 * Requiere instrumentación real (dispositivo/emulador), que no está disponible en el
 * entorno actual de CI local — no se pudo ejecutar este archivo en esta sesión.
 */
@RunWith(AndroidJUnit4::class)
class PasswordHasherTest {

    private val hasher = PasswordHasher()

    @Test
    fun verifyAceptaLaMismaPasswordConLaQueSeHasheo() {
        val hash = hasher.hash("correcta123")

        assertTrue(hasher.verify("correcta123", hash))
    }

    @Test
    fun verifyRechazaUnaPasswordIncorrecta() {
        val hash = hasher.hash("correcta123")

        assertFalse(hasher.verify("otra-password", hash))
    }

    @Test
    fun dosHashesDeLaMismaPasswordSonDistintosPorSaltAleatorio() {
        val hashA = hasher.hash("correcta123")
        val hashB = hasher.hash("correcta123")

        assertNotEquals(hashA, hashB)
        assertTrue(hasher.verify("correcta123", hashA))
        assertTrue(hasher.verify("correcta123", hashB))
    }

    @Test
    fun verifyOrDummyConHashNuloRetornaFalseSinLanzar() {
        assertFalse(hasher.verifyOrDummy("cualquier-password", null))
    }

    @Test
    fun verifyOrDummyConHashRealDelegaEnVerify() {
        val hash = hasher.hash("correcta123")

        assertTrue(hasher.verifyOrDummy("correcta123", hash))
        assertFalse(hasher.verifyOrDummy("incorrecta", hash))
    }
}
