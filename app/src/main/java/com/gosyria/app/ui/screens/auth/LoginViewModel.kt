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
    val phone: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
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

    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v, error = null) }
    fun onOtpChange(v: String)   = _state.update { it.copy(otp = v, error = null) }
    fun onRoleChange(role: UserRole) = _state.update { it.copy(selectedRole = role) }

    fun sendOtp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            auth.sendOtp(state.value.phone)
                .onSuccess { _state.update { s -> s.copy(otpSent = true, isLoading = false) }; onSuccess() }
                .onFailure { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
        }
    }

    fun login(onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            auth.login(state.value.phone, state.value.otp, state.value.selectedRole)
                .onSuccess { user -> _state.update { s -> s.copy(isLoading = false) }; onSuccess(user.role) }
                .onFailure { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            auth.signInWithGoogle(idToken)
                .onSuccess { user -> _state.update { s -> s.copy(isLoading = false) }; onSuccess(user.role) }
                .onFailure { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
        }
    }

    fun signInWithFacebook(accessToken: String, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            auth.signInWithFacebook(accessToken)
                .onSuccess { user -> _state.update { s -> s.copy(isLoading = false) }; onSuccess(user.role) }
                .onFailure { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
        }
    }
}
