package mx.equilibrio.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import mx.equilibrio.data.local.dao.UserDao
import mx.equilibrio.data.local.entity.UserEntity
import mx.equilibrio.data.mapper.toDomain
import mx.equilibrio.data.prefs.LocalSession
import mx.equilibrio.data.security.PasswordHasher
import mx.equilibrio.domain.model.PASSWORD_MAX_LENGTH
import mx.equilibrio.domain.model.PASSWORD_MIN_LENGTH
import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.model.isValidEmailFormat
import mx.equilibrio.domain.model.isValidPasswordLength
import mx.equilibrio.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val session: LocalSession,
    private val passwordHasher: PasswordHasher,
) : UserRepository {

    override suspend fun getCurrentUser(): User? = dao.getById(session.currentUserId())?.toDomain()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentUser(): Flow<User?> =
        session.observeCurrentUserId().flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeById(id).map { it?.toDomain() }
        }

    override suspend fun signInWithGoogle(
        googleId: String,
        email: String?,
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User {
        val now = System.currentTimeMillis()

        // Ya existe un usuario ligado a esta cuenta de Google (re-login) → reusa su id.
        // Si no, reclama el usuario local anónimo actual para no perder sus cuentas/movimientos.
        val existing = dao.getByGoogleId(googleId)
        val targetId = existing?.id ?: session.currentUserId()
        val base = existing ?: dao.getById(targetId) ?: UserEntity(
            id = targetId,
            displayName = null,
            createdAt = now,
            updatedAt = now,
            syncState = "PENDING",
        )

        val updated = base.copy(
            googleId = googleId,
            email = email ?: base.email,
            // El nombre de usuario es una elección del usuario (Perfil) — Google nunca lo
            // sobreescribe en re-logins, solo lo siembra la primera vez si aún no existe.
            displayName = base.displayName ?: displayName,
            givenName = givenName ?: base.givenName,
            familyName = familyName ?: base.familyName,
            photoUrl = photoUrl ?: base.photoUrl,
            updatedAt = now,
            syncState = "PENDING",
        )

        dao.upsert(updated)
        session.setCurrentUserId(targetId)
        return updated.toDomain()
    }

    override suspend fun signOut() {
        session.clear()
    }

    override suspend fun updateProfile(
        displayName: String?,
        givenName: String?,
        familyName: String?,
        photoUrl: String?,
    ): User {
        val id = session.currentUserId()
        val current = dao.getById(id) ?: error("Usuario local no encontrado: $id")
        val updated = current.copy(
            displayName = displayName,
            givenName = givenName,
            familyName = familyName,
            photoUrl = photoUrl,
            updatedAt = System.currentTimeMillis(),
            syncState = "PENDING",
        )
        dao.upsert(updated)
        return updated.toDomain()
    }

    override suspend fun registerWithPassword(displayName: String?, email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()

        if (displayName.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío."))
        }
        if (!isValidEmailFormat(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("Ingresa un correo válido."))
        }
        if (!isValidPasswordLength(password)) {
            return Result.failure(
                IllegalArgumentException(
                    "La contraseña debe tener entre $PASSWORD_MIN_LENGTH y $PASSWORD_MAX_LENGTH caracteres.",
                ),
            )
        }

        if (dao.getByEmail(normalizedEmail) != null) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo."))
        }

        return try {
            val now = System.currentTimeMillis()
            val targetId = session.currentUserId()
            val base = dao.getById(targetId) ?: UserEntity(
                id = targetId,
                displayName = null,
                createdAt = now,
                updatedAt = now,
                syncState = "PENDING",
            )

            val updated = base.copy(
                email = normalizedEmail,
                displayName = displayName ?: base.displayName,
                passwordHash = passwordHasher.hash(password),
                updatedAt = now,
                syncState = "PENDING",
            )

            dao.upsert(updated)
            session.setCurrentUserId(targetId)
            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithPassword(email: String, password: String): Result<User> {
        val invalidCredentials = IllegalArgumentException("Correo o contraseña incorrectos.")
        val normalizedEmail = email.trim().lowercase()

        // Formato inválido o largo fuera de rango nunca puede matchear una cuenta real
        // (el registro ya exige formato/longitud), así que cortar acá no delata nada
        // por canal lateral de tiempo: es uniforme para toda entrada de esa forma, no
        // depende de si el correo enviado corresponde a una cuenta existente.
        if (!isValidEmailFormat(normalizedEmail) || password.length > PASSWORD_MAX_LENGTH) {
            return Result.failure(invalidCredentials)
        }

        return try {
            val entity = dao.getByEmail(normalizedEmail)
            // Siempre corre un verify (real o señuelo) antes de responder, para que el tiempo
            // no delate si el correo existe (mitiga enumeración por canal lateral de tiempo).
            if (!passwordHasher.verifyOrDummy(password, entity?.passwordHash)) {
                return Result.failure(invalidCredentials)
            }

            session.setCurrentUserId(entity!!.id)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
