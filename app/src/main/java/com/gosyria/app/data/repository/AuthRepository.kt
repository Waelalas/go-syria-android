package com.gosyria.app.data.repository

import com.gosyria.app.data.model.User
import com.gosyria.app.data.model.UserRole

interface AuthRepository {
    suspend fun login(phone: String, otp: String): Result<User>
    suspend fun sendOtp(phone: String): Result<Unit>
    fun getCurrentUser(): User?
    fun logout()
}
