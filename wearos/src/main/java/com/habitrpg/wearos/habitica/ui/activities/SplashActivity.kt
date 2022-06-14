package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.habitrpg.android.habitica.databinding.ActivitySplashBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity: BaseActivity<ActivitySplashBinding, SplashViewModel>() {
    override val viewModel: SplashViewModel by viewModels()
    val messageClient: MessageClient by lazy { Wearable.getMessageClient(this) }
    val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
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

        messageClient.addListener(viewModel)
        lifecycleScope.launch(Dispatchers.IO) {
            val info = Tasks.await(capabilityClient.getCapability("provide_auth", CapabilityClient.FILTER_REACHABLE))
            val nodeID = info.nodes.firstOrNull { it.isNearby }
            if (nodeID != null) {
                showAccountLoader(true)
                Tasks.await(messageClient.sendMessage(nodeID.id, "/request/auth", null))
            } else {
                showAccountLoader(false)
                startLoginActivity()
            }
        }
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
        binding.progressBar.isVisible = show
        binding.textView.isVisible = show
    }

    override fun onPause() {
        messageClient.removeListener(viewModel)
        super.onPause()
    }
}