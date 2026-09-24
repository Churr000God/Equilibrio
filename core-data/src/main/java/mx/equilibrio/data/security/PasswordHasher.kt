package mx.equilibrio.data.security

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/** RF01 — hash de contraseñas con Argon2id. Nunca se guarda ni se compara en claro. */
@Singleton
class PasswordHasher @Inject constructor() {

    private val argon2Kt = Argon2Kt()

    fun hash(password: String): String {
        // Salt aleatorio por password (nunca reutilizado): sin esto, dos usuarios con la
        // misma contraseña tendrían el mismo hash, lo que permitiría un ataque de diccionario
        // precomputado (rainbow table) contra toda la base a la vez.
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val result = argon2Kt.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(Charsets.UTF_8),
            salt = salt,
            tCostInIterations = T_COST_ITERATIONS,
            mCostInKibibyte = M_COST_KIBIBYTE,
            parallelism = PARALLELISM,
        )
        return result.encodedOutputAsString()
    }

    // Ante cualquier excepción (hash corrupto, formato inesperado, etc.) falla cerrado:
    // se trata como contraseña incorrecta en vez de propagar el error, para no filtrar
    // detalles internos ni abrir una vía de bypass si el verify interno se rompe.
    fun verify(password: String, hash: String): Boolean = try {
        argon2Kt.verify(
            mode = Argon2Mode.ARGON2_ID,
            encoded = hash,
            password = password.toByteArray(Charsets.UTF_8),
        )
    } catch (e: Exception) {
        false
    }

    /**
     * Igual que [verify], pero si no hay hash real (cuenta o password inexistente) corre el
     * verify contra un hash señuelo de costo idéntico para que el tiempo de respuesta no delate
     * si el correo existe (mitiga enumeración por canal lateral de tiempo).
     */
    fun verifyOrDummy(password: String, hash: String?): Boolean {
        if (hash == null) {
            verify(password, dummyHash)
            return false
        }
        return verify(password, hash)
    }

    // Se calcula una sola vez (mismo costo que un hash real) y se reutiliza en todos los
    // login fallidos por cuenta inexistente, para que ese caso también tarde lo mismo
    // que un intento contra una cuenta real.
    private val dummyHash: String by lazy { hash(DUMMY_PASSWORD) }

    private companion object {
        const val SALT_LENGTH_BYTES = 16
        // Costo de tiempo/memoria/paralelismo siguiendo el mínimo recomendado por OWASP para
        // Argon2id (m=64 MiB, t=3, p entre 2 y 4): suficiente para encarecer fuerza bruta sin
        // volver el login perceptiblemente lento en un dispositivo móvil de gama media.
        const val T_COST_ITERATIONS = 3
        const val M_COST_KIBIBYTE = 65536
        const val PARALLELISM = 2
        const val DUMMY_PASSWORD = "equilibrio-timing-mitigation-constant"
    }
}
