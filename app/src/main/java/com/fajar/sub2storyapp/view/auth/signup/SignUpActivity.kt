package com.fajar.sub2storyapp.view.auth.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.databinding.ActivitySignUpBinding
import com.fajar.sub2storyapp.model.UserSignUpModel
import com.fajar.sub2storyapp.model.UserViewModel
import com.fajar.sub2storyapp.model.ViewModelFactory
import com.fajar.sub2storyapp.utils.helper.showToast
import com.fajar.sub2storyapp.view.auth.login.LoginActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val signUpViewModel: UserViewModel by viewModels { ViewModelFactory.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)
        signUpViewModel.signUpResponse.observe(this) { data ->
            binding.loadingProgress.isVisible = data.message.isEmpty()

            if (!data.error && data.message.isNotEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            if (data.error) {
                showToast(this, data.message)
                return@observe
            }
        }

        setupView()
        setupAction()
        playAnimation()
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1F).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1F).setDuration(500)
        val nameInput =
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1F).setDuration(500)
        val emailInput =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val password =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1F).setDuration(500)
        val passwordInput =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1F).setDuration(500)
        val button = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1F).setDuration(500)

        AnimatorSet().apply {
            playSequentially(
                title,
                name,
                nameInput,
                email,
                emailInput,
                password,
                passwordInput,
                button
            )
            start()
        }
    }

    private fun isInputReady(): Boolean {
        val isDataInputReady =
            binding.emailEditText.text.toString().trim()
                .isNotEmpty() && binding.nameEditText.text.toString()
                .isNotEmpty() && binding.passwordEditText.text.toString().isNotEmpty()
        val isPassValidation =
            binding.emailEditText.error.isNullOrBlank() && binding.passwordEditText.error.isNullOrEmpty()
        return isDataInputReady && isPassValidation
    }

    private fun sendRequest() {
        val newUser = UserSignUpModel(
            binding.nameEditText.text.toString().trim(),
            binding.emailEditText.text.toString().trim(),
            binding.passwordEditText.text.toString().trim()
        )
        signUpViewModel.signUp(newUser)
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            if (binding.nameEditText.text.toString().isEmpty()) {
                binding.nameEditText.error = getString(R.string.input_error)
            }

            if (binding.emailEditText.text.toString().isEmpty()) {
                binding.emailEditText.error = getString(R.string.input_error)
            }

            if (binding.passwordEditText.text.toString().isEmpty()) {
                binding.passwordEditText.error = getString(R.string.input_error)
            }

            if (isInputReady()) {
                sendRequest()
            }
        }
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