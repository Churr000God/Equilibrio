package mx.equilibrio.app.auth

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

/**
 * El picker de fotos solo garantiza el permiso de lectura sobre [sourceUri] de
 * forma transitoria — copiamos el archivo al storage privado de la app para
 * que la foto siga siendo legible después de reiniciar el dispositivo.
 */
fun persistAvatarLocally(context: Context, userId: String, sourceUri: Uri): String {
    val avatarsDir = File(context.filesDir, "avatars").apply { mkdirs() }
    val destFile = File(avatarsDir, "$userId.jpg")
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        destFile.outputStream().use { output -> input.copyTo(output) }
    }
    return destFile.toUri().toString()
}
