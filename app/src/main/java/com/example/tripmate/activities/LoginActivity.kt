package com.example.tripmate.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripmate.MainActivity
import com.example.tripmate.R
import com.example.tripmate.data.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val authRepository = AuthenticationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Navigate to Register.
        findViewById<TextView>(R.id.tvRegister)
            .setOnClickListener {

                startActivity(
                    Intent(this, RegisterActivity::class.java)
                )
            }

        // Login using Supabase Authentication.
        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            when {

                email.isEmpty() ||
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {

                    etEmail.error = "Please enter a valid email address"
                }

                password.isEmpty() -> {

                    etPassword.error = "Please enter your password"
                }

                else -> {

                    btnLogin.isEnabled = false
                    btnLogin.text = "Signing in..."

                    activityScope.launch {

                        try {

                            val profile = authRepository.signIn(
                                email = email,
                                password = password
                            )

                            Toast.makeText(
                                this@LoginActivity,
                                "Login successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            AuthNavigation.openDashboard(
                                this@LoginActivity,
                                profile
                            )

                        } catch (e: Exception) {

                            Toast.makeText(
                                this@LoginActivity,
                                e.message ?: "Login failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()

                        } finally {

                            if (!isFinishing && !isDestroyed) {
                                btnLogin.isEnabled = true
                                btnLogin.text = "Login"
                            }
                        }
                    }
                }
            }
        }

        // Forgot password will be implemented separately.
        findViewById<TextView>(R.id.tvForgotPassword)
            .setOnClickListener {

                Toast.makeText(
                    this,
                    "Password reset will be available in a later update.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Social login is not configured yet.
        findViewById<android.widget.LinearLayout>(R.id.btnGoogle)
            .setOnClickListener {

                Toast.makeText(
                    this,
                    "Google login is not configured yet.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        findViewById<android.widget.LinearLayout>(R.id.btnFacebook)
            .setOnClickListener {

                Toast.makeText(
                    this,
                    "Facebook login is not configured yet.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Restore an existing session when the app opens.
        checkExistingSession()
    }

    private fun checkExistingSession() {

        activityScope.launch {

            try {

                val profile = authRepository.getCurrentProfile()

                if (
                    profile != null &&
                    !isFinishing &&
                    !isDestroyed
                ) {

                    AuthNavigation.openDashboard(
                        this@LoginActivity,
                        profile
                    )
                }

            } catch (e: Exception) {

                // Keep the login screen available.
                // A temporary network error should not crash the app.
            }
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}
