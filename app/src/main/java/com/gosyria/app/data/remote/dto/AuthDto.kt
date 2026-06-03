package com.gosyria.app.data.remote.dto

data class GoogleSignInRequest(
    val id_token: String,
    val role: String,
)

data class TokenResponse(
    val token: String,
    val user_id: String,
    val name: String,
    val role: String,
)
