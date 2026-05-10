package com.example.postmark.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postmark.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel(
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }

    fun login() = submit { authRepo.login(_state.value.email.trim(), _state.value.password) }
    fun register() = submit { authRepo.register(_state.value.email.trim(), _state.value.password) }

    private fun submit(block: suspend () -> Result<*>) {
        val s = _state.value
        if (s.email.isBlank() || s.password.length < 6) {
            _state.update { it.copy(error = "Enter an email and a password of 6+ characters") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = block()
            _state.update {
                if (result.isSuccess) it.copy(loading = false, success = true)
                else it.copy(loading = false, error = result.exceptionOrNull()?.message ?: "Something went wrong")
            }
        }
    }
}
