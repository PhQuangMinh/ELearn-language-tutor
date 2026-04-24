package com.nhom2.elearnlanguage.presentation.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.nhom2.elearnlanguage.data.source.local.OnboardingPreferenceManager
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.data.source.local.TokenManager
import com.nhom2.elearnlanguage.domain.usecase.SyncFcmTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val mainContainer = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        ViewCompat.requestApplyInsets(mainContainer)

        requestNotificationPermissionIfNeeded()
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
        syncFcmToken()

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(R.id.homeFragment, null, navOptions)
    }

    private fun restoreSessionIfAvailable() {
        val onboardingCompleted = OnboardingPreferenceManager.isOnboardingCompleted(this)
        if (!onboardingCompleted) return

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val currentDestId = navController.currentDestination?.id

        if (currentDestId == R.id.onboardingFragment) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.onboardingFragment, true)
                .build()
            navController.navigate(R.id.loginFragment, null, navOptions)
        }

        val accessToken = TokenManager.getAccessToken(this)
        if (accessToken.isNullOrBlank()) return

        if (currentDestId == R.id.loginFragment || currentDestId == R.id.onboardingFragment) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.onboardingFragment, true)
                .build()
            navController.navigate(R.id.homeFragment, null, navOptions)
        }
        syncFcmToken()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun syncFcmToken() {
        lifecycleScope.launch {
            runCatching {
                syncFcmTokenUseCase()
            }
        }
    }
}