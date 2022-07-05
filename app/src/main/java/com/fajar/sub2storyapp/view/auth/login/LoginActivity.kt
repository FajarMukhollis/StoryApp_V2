package com.fajar.sub2storyapp.view.auth.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.databinding.ActivityLoginBinding
import com.fajar.sub2storyapp.model.UserLoginModel
import com.fajar.sub2storyapp.model.UserViewModel
import com.fajar.sub2storyapp.model.ViewModelFactory
import com.fajar.sub2storyapp.utils.helper.showToast
import com.fajar.sub2storyapp.view.home.main.MainActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: UserViewModel by viewModels { ViewModelFactory.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel.loginResponse.observe(this) { data ->
            binding.loadingProgress.isVisible = data.message.isEmpty()

            if (!data.error && data.message.isNotEmpty()) {
                moveToMainActivity()
            }

            if (data.error) {
                showToast(this, data.message)
                return@observe
            }
        }

        binding.buttonLogin.setOnClickListener {
            if (binding.emailEditText.text.toString().isEmpty()) {
                binding.emailEditText.error = getString(R.string.input_error)
                return@setOnClickListener
            }

            if (binding.passwordEditText.text.toString().isEmpty()) {
                binding.passwordEditText.error = getString(R.string.input_error)
                return@setOnClickListener
            }

            if (isInputReady()) {
                sendRequest()
            }
        }

        playAnimation()
        setupView()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30F, 30F).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            startDelay = 500
        }.start()

        val email = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1F).setDuration(500)
        val emailInput =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1F).setDuration(500)
        val passwordInput =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.buttonLogin, View.ALPHA, 1F).setDuration(500)

        AnimatorSet().apply {
            playSequentially(email, emailInput, password, passwordInput, button)
            start()
        }
    }

    private fun isInputReady(): Boolean {
        val isDataInputReady =
            binding.emailEditText.text.toString()
                .isNotEmpty() && binding.passwordEditText.text.toString().isNotEmpty()
        val isPassValidation =
            binding.emailEditText.error.isNullOrBlank() && binding.passwordEditText.error.isNullOrEmpty()
        return isDataInputReady && isPassValidation
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun sendRequest() {
        val user = UserLoginModel(
            email = binding.emailEditText.text.toString().trim(),
            password = binding.passwordEditText.text.toString()
        )
        val pref = UserPreference.getInstance(dataStore)
        loginViewModel.login(user, pref)
    }


    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}
