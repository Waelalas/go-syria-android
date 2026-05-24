package com.gosyria.app.data.remote.dto

data class SendOtpRequest(val phone: String)

data class SendOtpResponse(val message: String)

data class VerifyOtpRequest(
    val phone: String,
    val code: String,
    val name: String = "",
    val role: String = "RIDER",
)

data class TokenResponse(
    val token: String,
    val user_id: String,
    val name: String,
    val role: String,
)
