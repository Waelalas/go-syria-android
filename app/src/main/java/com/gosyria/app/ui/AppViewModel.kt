package com.gosyria.app.ui

import androidx.lifecycle.ViewModel
import com.gosyria.app.data.model.UserRole
import com.gosyria.app.data.repository.AuthRepository
import com.gosyria.app.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {
    val startRoute: String = when (auth.getCurrentUser()?.role) {
        UserRole.RIDER  -> Screen.RiderHome.route
        UserRole.DRIVER -> Screen.DriverHome.route
        null            -> Screen.Login.route
    }

    fun logout() = auth.logout()
}
