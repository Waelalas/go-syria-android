package com.gosyria.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.gosyria.app.data.model.User
import com.gosyria.app.data.model.UserRole
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    
    override fun getCurrentUser(): User? = auth.currentUser?.toUser()

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    override suspend fun signInWithFacebook(accessToken: String): Result<User> {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        return signInWithCredential(credential)
    }

    private suspend fun signInWithCredential(credential: com.google.firebase.auth.AuthCredential): Result<User> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user?.toUser()
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to get user data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendOtp(phone: String): Result<Unit> = Result.failure(Exception("Not implemented"))
    override suspend fun login(phone: String, otp: String): Result<User> = Result.failure(Exception("Not implemented"))
    
    override fun logout() {
        auth.signOut()
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            id = uid,
            name = displayName ?: "",
            phone = phoneNumber ?: "",
            role = UserRole.RIDER,
            email = email,
            profilePictureUrl = photoUrl?.toString()
        )
    }
}
