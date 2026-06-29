package com.example.subhatak.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subhatak.data.model.User
import com.example.subhatak.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(

    private val repository: AuthRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun fetchCurrentUser() {
        viewModelScope.launch {
            try {
                val user = repository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
            }
        }
    }

    fun signUp(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        profilePictureUrl: String?
    ) {
        viewModelScope.launch {

            _authState.value = AuthState.Loading
            try {
                val result = repository.signUpWithEmail(
                    email = email,
                    password = password,
                    name = name,
                    phoneNumber = phoneNumber,
                    profilePictureUrl = profilePictureUrl
                )

                when (result) {
                    is Result.Success -> _authState.value =
                        AuthState.Success(result.data)

                    is Result.Error -> _authState.value =
                        AuthState.Error(result.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }


    fun signOut(){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.signOut()
                _authState.value = AuthState.Initial
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.signInWithEmail(
                    email = email, password = password
                )
                _authState.value =
                    when (result) {
                        is Result.Success ->
                            AuthState.Success(result.data)

                        is Result.Error ->
                            AuthState.Error(result.message)
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.signInWithGoogle(account)
                _authState.value =
                    when (result) {
                        is Result.Success ->
                            AuthState.Success(result.data)

                        is Result.Error ->
                            AuthState.Error(result.message)
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

