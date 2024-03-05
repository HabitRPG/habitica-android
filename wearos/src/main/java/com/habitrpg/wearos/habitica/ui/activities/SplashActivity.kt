package com.habitrpg.wearos.habitica.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.databinding.ActivitySplashBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    override val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        if (viewModel.hasAuthentication) {
            startMainActivity()
            return
        }

        viewModel.onLoginCompleted = {
            if (it) {
                startMainActivity()
            } else {
                startLoginActivity()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        messageClient.addListener(viewModel)

        if (!viewModel.hasAuthentication) {
            sendMessage("provide_auth", "/request/auth", null) {
                if (it) {
                    showAccountLoader(true)
                } else {
                    showAccountLoader(false)
                    startLoginActivity()
                }
            }
        }
    }

    override fun onStop() {
        messageClient.removeListener(viewModel)
        super.onStop()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAccountLoader(show: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (show) {
                startAnimatingProgress()
            } else {
                stopAnimatingProgress()
            }
            delay(40.toDuration(DurationUnit.SECONDS))
            if (isActive) {
                // the sync attempt has timed out
                startLoginActivity()
            }
        }
    }
}
