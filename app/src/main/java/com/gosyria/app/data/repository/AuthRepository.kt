package com.gosyria.app.data.repository

import com.gosyria.app.data.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signInWithGoogle(idToken: String, role: String = "RIDER"): Result<User>
    fun logout()
    fun signOut()
}
