package com.gosyria.app.data.repository

import com.gosyria.app.data.local.TokenStore
import com.gosyria.app.data.model.User
import com.gosyria.app.data.model.UserRole
import com.gosyria.app.data.remote.ApiService
import com.gosyria.app.data.remote.dto.SendOtpRequest
import com.gosyria.app.data.remote.dto.VerifyOtpRequest
import javax.inject.Inject

class HttpAuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : AuthRepository {

    private var currentUser: User? = null

    override suspend fun sendOtp(phone: String): Result<Unit> = runCatching {
        api.sendOtp(SendOtpRequest(phone))
        Unit
    }

    override suspend fun login(phone: String, otp: String, role: UserRole): Result<User> = runCatching {
        val roleStr = if (role == UserRole.DRIVER) "DRIVER" else "RIDER"
        val resp = api.verifyOtp(VerifyOtpRequest(phone = phone, code = otp, role = roleStr))
        tokenStore.token = resp.token
        tokenStore.userId = resp.user_id
        tokenStore.userName = resp.name
        tokenStore.userRole = resp.role
        val userRole = if (resp.role == "DRIVER") UserRole.DRIVER else UserRole.RIDER
        User(id = resp.user_id, name = resp.name, phone = phone, role = userRole).also {
            currentUser = it
        }
    }

    override fun getCurrentUser(): User? {
        if (currentUser == null && tokenStore.userId != null) {
            currentUser = User(
                id = tokenStore.userId!!,
                name = tokenStore.userName ?: "",
                phone = "",
                role = if (tokenStore.userRole == "DRIVER") UserRole.DRIVER else UserRole.RIDER,
            )
        }
        return currentUser
    }

    override fun logout() {
        tokenStore.clear()
        currentUser = null
    }

    override fun signOut() { logout() }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = Result.failure(Exception("Not supported in HTTP repository"))
    override suspend fun signInWithFacebook(accessToken: String): Result<User> = Result.failure(Exception("Not supported in HTTP repository"))
}
