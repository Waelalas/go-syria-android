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

    override suspend fun signInWithGoogle(idToken: String, role: String): Result<User> {
        delay(1000)
        val userRole = if (role == "DRIVER") UserRole.DRIVER else UserRole.RIDER
        val user = User(
            id = "user_001",
            name = "المستخدم",
            phone = "",
            role = userRole,
        )
        currentUser = user
        return Result.success(user)
    }

    override fun getCurrentUser(): User? = currentUser
    override fun logout() { currentUser = null }
    override fun signOut() { logout() }
}
