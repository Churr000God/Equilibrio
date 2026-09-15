package mx.equilibrio.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import mx.equilibrio.domain.model.User
import mx.equilibrio.domain.usecase.ObserveCurrentUser
import javax.inject.Inject

/** Solo alimenta el avatar del HUD (RF01) — el resto de la lógica vive en [ProfileViewModel]. */
@HiltViewModel
class AuthViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    val user: StateFlow<User?> = observeCurrentUser().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )
}
