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

    fun verify(password: String, hash: String): Boolean = try {
        argon2Kt.verify(
            mode = Argon2Mode.ARGON2_ID,
            encoded = hash,
            password = password.toByteArray(Charsets.UTF_8),
        )
    } catch (e: Exception) {
        false
    }

    private companion object {
        const val SALT_LENGTH_BYTES = 16
        const val T_COST_ITERATIONS = 3
        const val M_COST_KIBIBYTE = 65536
        const val PARALLELISM = 2
    }
}
