package com.gosyria.app.data.mock

import com.gosyria.app.data.model.User
import com.gosyria.app.data.model.UserRole
import com.gosyria.app.data.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {

    private var currentUser: User? = null

    override suspend fun sendOtp(phone: String): Result<Unit> {
        delay(800)
        return Result.success(Unit)
    }

    override suspend fun login(phone: String, otp: String): Result<User> {
        delay(1000)
        if (otp.isBlank()) return Result.failure(Exception("رمز التحقق غير صحيح"))
        val user = User(
            id = "user_001",
            name = "المستخدم",
            phone = phone,
            role = UserRole.RIDER,
        )
        currentUser = user
        return Result.success(user)
    }

    override fun getCurrentUser(): User? = currentUser

    override suspend fun signInWithGoogle(idToken: String): Result<User> = Result.failure(Exception("Not supported in mock"))
    override suspend fun signInWithFacebook(accessToken: String): Result<User> = Result.failure(Exception("Not supported in mock"))

    override fun logout() { currentUser = null }
    override fun signOut() { logout() }
}
