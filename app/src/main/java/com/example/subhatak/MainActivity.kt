package com.example.subhatak

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subhatak.data.model.User
import com.example.subhatak.data.repository.AuthRepository
import com.example.subhatak.databinding.ActivityMainBinding
import com.example.subhatak.ui.auth.AuthActivity
import com.example.subhatak.ui.home.NewsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        // Configure initial state: 95% scale and 0% opacity
        binding.imageView.apply {
            alpha = 0f
            scaleX = 0.95f
            scaleY = 0.95f

            // Start cinematic zoom and fade-in
            animate()
                .alpha(1f)
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(2200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Fade out the entire root layout before navigating
                    binding.root.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            lifecycleScope.launch {
                                checkAuthAndNavigate()
                            }
                        }
                        .start()
                }
                .start()
        }
    }

    private suspend fun checkAuthAndNavigate() {
        val currentUser: User? = authRepository.getCurrentUser()
        val intent = if (currentUser != null) {
            Intent(this, NewsScreen::class.java)
        } else
            Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
