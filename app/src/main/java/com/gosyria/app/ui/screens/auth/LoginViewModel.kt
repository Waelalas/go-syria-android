package com.gosyria.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.model.UserRole
import com.gosyria.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRole: UserRole = UserRole.RIDER,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onRoleChange(role: UserRole) = _state.update { it.copy(selectedRole = role) }

    fun signInWithGoogle(idToken: String, onSuccess: (UserRole) -> Unit) {
        val roleStr = if (state.value.selectedRole == UserRole.DRIVER) "DRIVER" else "RIDER"
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            auth.signInWithGoogle(idToken, roleStr)
                .onSuccess { user -> _state.update { s -> s.copy(isLoading = false) }; onSuccess(user.role) }
                .onFailure { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
        }
    }
}
