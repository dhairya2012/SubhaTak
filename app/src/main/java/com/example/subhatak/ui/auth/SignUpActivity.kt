package com.example.subhatak.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.subhatak.R
import com.example.subhatak.databinding.ActivitySignUpBinding
import com.example.subhatak.ui.home.NewsScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        GoogleSignIn.getClient(this, gso)
    }
    private val signInLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    viewModel.signInWithGoogle(account)
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
            else {
                Snackbar
                    .make(binding.root, "Google Sign In failed", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.scrollView.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                ime.bottom
            )
            insets
        }

        setupClickListeners()
        observeAuthState()

    }
    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.etPhone.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInputs(name, email, phone, password, confirmPassword)) {
                viewModel.signUp(

                    email = email,
                    password = password,
                    name = name,
                    phoneNumber = phone,
                    profilePictureUrl = null
                )
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
        binding.btnContinueWithGoogle.setOnClickListener {
            startGoogleSignIn()
        }
    }

    private fun validateInputs(
        name: String, email: String, phone: String, password: String, confirmPassword: String
    ): Boolean {
        if (name.isBlank()) {
            binding.etFullName.error = "Full name is required"
            return false
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            return false
        }

        if (phone.isBlank()) {
            binding.etPhone.error = "Phone number is required"
            return false
        }

        if (password.length < 8) {
            binding.etPassword.error = "Password must be at least 8 characters"
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnSignUp.isEnabled = false
                    }

                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        startActivity(Intent(this@SignUpActivity, NewsScreen::class.java))
                        finish()
                    }

                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignUp.isEnabled = true
                        Toast.makeText(this@SignUpActivity, state.message, Toast.LENGTH_LONG).show()
                    }

                    AuthState.Initial -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignUp.isEnabled = true
                    }
                }
            }
        }
    }
}
