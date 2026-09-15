package mx.equilibrio.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import mx.equilibrio.app.R

data class GoogleIdentity(
    val googleId: String,
    val email: String?,
    val displayName: String?,
    val givenName: String?,
    val familyName: String?,
    val photoUrl: String?,
)

/**
 * Envuelve Credential Manager + Google Identity Services (RF01, opción A de la
 * comparativa). Requiere una Activity como [context] — Credential Manager no
 * acepta un Application context — y el Web Client ID real de Google Cloud
 * Console en `R.string.default_web_client_id` (ver TODO en strings.xml).
 */
class GoogleAuthClient(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): Result<GoogleIdentity> {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val response = credentialManager.getCredential(context, request)
            val credential = response.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(IllegalStateException("Credencial inesperada: ${credential.type}"))
            }

            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            Result.success(
                GoogleIdentity(
                    googleId = googleCredential.uniqueId,
                    email = googleCredential.email,
                    displayName = googleCredential.displayName,
                    givenName = googleCredential.givenName,
                    familyName = googleCredential.familyName,
                    photoUrl = googleCredential.profilePictureUri?.toString(),
                ),
            )
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(e)
        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
            Result.failure(e)
        }
    }
}
