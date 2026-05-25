package com.gosyria.app.data.repository

import com.gosyria.app.data.model.User
import com.gosyria.app.data.model.UserRole

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signInWithGoogle(idToken: String, role: String = "RIDER"): Result<User>
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun login(phone: String, otp: String, role: UserRole): Result<User>
    fun logout()
    fun signOut()
}
