package com.habitrpg.wearos.habitica.ui.activities

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.common.habitica.models.auth.UserAuthResponse
import com.habitrpg.wearos.habitica.R
import com.habitrpg.wearos.habitica.databinding.ActivityLoginBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity: BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    override val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.loginButton.setOnClickListener { loginLocal() }
        binding.googleLoginButton.setOnClickListener { loginGoogle() }
        binding.registerButton.setOnClickListener { openRegisterOnPhone() }
    }

    private fun openRegisterOnPhone() {

    }

    private fun loginLocal() {
        val username: String = binding.usernameEditText.text.toString().trim { it <= ' ' }
        val password: String = binding.passwordEditText.text.toString()
        if (username.isEmpty() || password.isEmpty()) {
            showValidationError(getString(R.string.login_validation_error_fieldsmissing))
            return
        }
        lifecycleScope.launch {
            val result = viewModel.login(username, password)
            if (result != null) {
                handleAuthResponse(result)
            }
        }
    }

    private fun loginGoogle() {
        viewModel.handleGoogleLogin(this, pickAccountResult)
    }

    private fun handleAuthResponse(response: UserAuthResponse) {
        viewModel.handleAuthResponse(response)
        lifecycleScope.launch {
            viewModel.retrieveUser()
            startMainActivity()
        }
    }

    private fun showValidationError(message: String) {
        val alert = AlertDialog.Builder(this).create()
        alert.setTitle(R.string.login_validation_error_title)
        alert.setMessage(message)
        alert.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok)) { alert, _ ->
            alert.dismiss()
        }
        alert.show()
    }

    private val pickAccountResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.googleEmail = it?.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            viewModel.handleGoogleLoginResult(this, recoverFromPlayServicesErrorResult) { response ->
                if (response != null) {
                    handleAuthResponse(response)
                }
            }
        }
    }

    private val recoverFromPlayServicesErrorResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            viewModel.handleGoogleLoginResult(this, null) { response ->
                if (response != null) {
                    handleAuthResponse(response)
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}