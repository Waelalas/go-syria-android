package com.gosyria.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.model.User
import com.gosyria.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _userState.value = repository.getCurrentUser()
    }

    fun onSignInResult(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                _userState.value = result.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        repository.signOut()
        _userState.value = null
    }
}
