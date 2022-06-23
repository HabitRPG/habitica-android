package com.habitrpg.wearos.habitica.ui.activities

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityLoginBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity: BaseActivity<ActivityLoginBinding, LoginViewModel>() {
    enum class State {
        INITIAL,
        OTHER,
        INPUT
    }
    override val viewModel: LoginViewModel by viewModels()
    var currentState: State = State.INITIAL
    set(value) {
        field = value
        when(value) {
            State.INITIAL -> {
                binding.descriptionView.isVisible = true
                binding.signInOnPhoneButton.isVisible = true
                binding.otherButton.isVisible = true
                binding.googleLoginButton.isVisible = false
                binding.registerButton.isVisible = false
                binding.usernamePasswordButton.isVisible = false
                binding.usernameEditText.isVisible = false
                binding.passwordEditText.isVisible = false
                binding.loginButton.isVisible = false
            }
            State.OTHER -> {
                binding.descriptionView.isVisible = false
                binding.signInOnPhoneButton.isVisible = false
                binding.otherButton.isVisible = false
                binding.googleLoginButton.isVisible = true
                binding.registerButton.isVisible = true
                binding.usernamePasswordButton.isVisible = true
                binding.usernameEditText.isVisible = false
                binding.passwordEditText.isVisible = false
                binding.loginButton.isVisible = false
            }
            State.INPUT -> {
                binding.descriptionView.isVisible = false
                binding.signInOnPhoneButton.isVisible = false
                binding.otherButton.isVisible = false
                binding.googleLoginButton.isVisible = false
                binding.registerButton.isVisible = false
                binding.usernamePasswordButton.isVisible = false
                binding.usernameEditText.isVisible = true
                binding.passwordEditText.isVisible = true
                binding.loginButton.isVisible = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        viewModel.onLoginCompleted = {
            startMainActivity()
        }

        binding.signInOnPhoneButton.setOnClickListener {  }
        binding.otherButton.setOnClickListener { currentState = State.OTHER }
        binding.usernamePasswordButton.setOnClickListener { currentState = State.INPUT }

        binding.loginButton.setOnClickListener { loginLocal() }
        binding.googleLoginButton.setOnClickListener { loginGoogle() }
        binding.registerButton.setOnClickListener { openRegisterOnPhone() }

        currentState = State.INITIAL
    }

    private fun openRegisterOnPhone() {
        openRemoteActivity("/show/register")
    }

    private fun loginLocal() {
        val username: String = binding.usernameEditText.text.toString().trim { it <= ' ' }
        val password: String = binding.passwordEditText.text.toString()
        if (username.isEmpty() || password.isEmpty()) {
            showValidationError(getString(R.string.login_validation_error_fieldsmissing))
            return
        }
        viewModel.login(username, password) {
            stopAnimatingProgress()
        }
        startAnimatingProgress()
    }

    private fun loginGoogle() {
        viewModel.handleGoogleLogin(this, pickAccountResult)
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
            viewModel.handleGoogleLoginResult(this, recoverFromPlayServicesErrorResult)
        }
    }

    private val recoverFromPlayServicesErrorResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            viewModel.handleGoogleLoginResult(this, null)
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}