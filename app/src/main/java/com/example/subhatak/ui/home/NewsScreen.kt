package com.example.subhatak.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.subhatak.MainActivity
import com.example.subhatak.R
import com.example.subhatak.databinding.ActivityNewsScreenBinding
import com.example.subhatak.ui.auth.AuthViewModel
import com.example.subhatak.ui.home.viewmodel.NewsViewModel
import com.example.subhatak.utils.ResourceState
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewsScreen : AppCompatActivity() {
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var binding: ActivityNewsScreenBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsAdapter = NewsPageAdapter()
        binding.newsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(this@NewsScreen)
        }

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        observeCurrentUser()
        setupNavigationDrawer(binding.toolbar)

        authViewModel.fetchCurrentUser()
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.news.collect { state ->
                    when (state) {
                        is ResourceState.Loading -> {
                            binding.shimmerContainer.visibility = android.view.View.VISIBLE
                            binding.newsRecyclerView.visibility = android.view.View.GONE
                            Log.d("NewsLandingPage", "Loading news ... ")
                        }

                        is ResourceState.Success -> {
                            binding.shimmerContainer.visibility = android.view.View.GONE
                            binding.newsRecyclerView.visibility = android.view.View.VISIBLE
                            Log.d(
                                "NewsLandingPage", "Received ${state.data.articles.size} articles"
                            )
                            newsAdapter.setArticles(state.data.articles)
                        }

                        is ResourceState.Error -> {
                            binding.shimmerContainer.visibility = android.view.View.GONE
                            binding.newsRecyclerView.visibility = android.view.View.VISIBLE
                            Log.e("NewsLandingPage", "Error: ${state.error}")
                        }
                    }
                }
            }
        }
    }

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.currentUser.collect { user ->
                    user?.let { updateNavHeader(it) }
                }
            }
        }
    }

    private fun updateNavHeader(user: com.example.subhatak.data.model.User) {
        val headerView = if (binding.navView.headerCount > 0) {
            binding.navView.getHeaderView(0)
        } else {
            null
        }

        if (headerView != null) {
            val profileImage = headerView.findViewById<CircleImageView>(R.id.nav_header_image)
            val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
            val tvUserEmail = headerView.findViewById<TextView>(R.id.userEmail)

            tvUserName?.text = user.name
            tvUserEmail?.text = user.email

            if (!user.profilePictureUrl.isNullOrEmpty()) {
                Glide.with(this@NewsScreen)
                    .load(user.profilePictureUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(profileImage)
            } else {
                profileImage?.setImageResource(R.drawable.baseline_person_24)
            }
        } else {
            binding.navView.post { updateNavHeader(user) }
        }
    }

    private fun showThemeDialog() {
        val options = arrayOf("Light Theme", "Dark Theme")
        AlertDialog.Builder(this)
            .setTitle("Select Theme")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
            .show()
    }

    private fun setupNavigationDrawer(toolBar: MaterialToolbar) {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.theme -> {
                    showThemeDialog()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.Aboutus -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.feedback -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.rules -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.conact -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:9512330525")
                    }

                    startActivity(intent)
                    true
                }

                R.id.share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Check this out!")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Hey! Check out this awesome news app "
                        )
                    }

                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Share via"
                        )
                    )
                    true
                }

                R.id.Logout -> {
                    authViewModel.signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                else -> false
            }
        }

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    fun onBackPressedDispatcher() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION") super.onBackPressed()
        }
    }
}
