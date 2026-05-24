package com.gosyria.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs    = context.getSharedPreferences("gosyria_prefs", Context.MODE_PRIVATE)
    private val fcmPrefs = context.getSharedPreferences("gosyria_fcm",   Context.MODE_PRIVATE)

    // @Volatile ensures cross-thread visibility (coroutine write → OkHttp thread read)
    @Volatile private var _token:    String? = prefs.getString("token",     null)
    @Volatile private var _userId:   String? = prefs.getString("user_id",   null)
    @Volatile private var _userName: String? = prefs.getString("user_name", null)
    @Volatile private var _userRole: String? = prefs.getString("user_role", null)
    @Volatile private var _fcmToken: String? = fcmPrefs.getString("fcm_token", null)

    var token: String?
        get() = _token
        set(v) {
            _token = v
            if (v != null) prefs.edit().putString("token", v).apply()
            else prefs.edit().remove("token").apply()
        }

    var userId: String?
        get() = _userId
        set(v) {
            _userId = v
            if (v != null) prefs.edit().putString("user_id", v).apply()
            else prefs.edit().remove("user_id").apply()
        }

    var userName: String?
        get() = _userName
        set(v) {
            _userName = v
            if (v != null) prefs.edit().putString("user_name", v).apply()
            else prefs.edit().remove("user_name").apply()
        }

    var userRole: String?
        get() = _userRole
        set(v) {
            _userRole = v
            if (v != null) prefs.edit().putString("user_role", v).apply()
            else prefs.edit().remove("user_role").apply()
        }

    var fcmToken: String?
        get() = _fcmToken
        set(v) {
            _fcmToken = v
            if (v != null) fcmPrefs.edit().putString("fcm_token", v).apply()
            else fcmPrefs.edit().remove("fcm_token").apply()
        }

    val isLoggedIn get() = !_token.isNullOrBlank()

    val authHeader get() = _token?.let { "Bearer $it" } ?: ""

    fun clear() {
        _token = null; _userId = null; _userName = null; _userRole = null
        prefs.edit().clear().apply()
    }
}
