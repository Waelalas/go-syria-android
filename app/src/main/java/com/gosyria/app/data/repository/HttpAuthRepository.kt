package com.gosyria.app.data.repository

import com.gosyria.app.data.local.TokenStore
import com.gosyria.app.data.model.User
import com.gosyria.app.data.model.UserRole
import com.gosyria.app.data.remote.ApiService
import com.gosyria.app.data.remote.dto.GoogleSignInRequest
import javax.inject.Inject

class HttpAuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : AuthRepository {

    private var currentUser: User? = null

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

    override suspend fun signInWithGoogle(idToken: String, role: String): Result<User> = runCatching {
        val resp = api.googleSignIn(GoogleSignInRequest(id_token = idToken, role = role))
        tokenStore.token    = resp.token
        tokenStore.userId   = resp.user_id
        tokenStore.userName = resp.name
        tokenStore.userRole = resp.role
        val userRole = if (resp.role == "DRIVER") UserRole.DRIVER else UserRole.RIDER
        User(id = resp.user_id, name = resp.name, phone = "", role = userRole).also { currentUser = it }
    }
}
