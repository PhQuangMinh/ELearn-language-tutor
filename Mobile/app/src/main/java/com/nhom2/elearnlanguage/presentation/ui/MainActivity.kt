package com.nhom2.elearnlanguage.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.data.source.local.TokenManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handleOAuthCallback()
        restoreSessionIfAvailable()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleOAuthCallback()
    }

    private fun handleOAuthCallback() {
        val data = intent?.data ?: return
        if (data.scheme != "elearn" || data.host != "login" || data.path != "/callback") {
            return
        }

        val error = data.getQueryParameter("error")
        if (!error.isNullOrBlank()) {
            Toast.makeText(this, "OAuth failed: $error", Toast.LENGTH_LONG).show()
            return
        }

        val accessToken = data.getQueryParameter("token")
        if (accessToken.isNullOrBlank()) {
            Toast.makeText(this, "Missing access token", Toast.LENGTH_LONG).show()
            return
        }

        val refreshToken = data.getQueryParameter("refreshToken")
        TokenManager.saveAccessToken(this, accessToken)
        TokenManager.saveRefreshToken(this, refreshToken)

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController(R.id.nav_host_fragment).navigate(R.id.homeFragment, null, navOptions)
    }

    private fun restoreSessionIfAvailable() {
        val accessToken = TokenManager.getAccessToken(this)
        if (accessToken.isNullOrBlank()) return

        val navController = findNavController(R.id.nav_host_fragment)
        val currentDestId = navController.currentDestination?.id
        if (currentDestId == R.id.loginFragment) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
            navController.navigate(R.id.homeFragment, null, navOptions)
        }
    }
}