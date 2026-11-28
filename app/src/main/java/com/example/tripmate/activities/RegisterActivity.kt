package com.example.tripmate.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripmate.R
import com.example.tripmate.data.AuthenticationRepository
import com.example.tripmate.data.RegistrationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val authRepository = AuthenticationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

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

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword =
            findViewById<EditText>(R.id.etConfirmPassword)

        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)

        val btnCreateAccount =
            findViewById<Button>(R.id.btnCreateAccount)

        btnCreateAccount.setOnClickListener {

            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword =
                etConfirmPassword.text.toString()

            when {

                fullName.isEmpty() -> {

                    etFullName.error =
                        "Please enter your full name"
                }

                email.isEmpty() ||
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {

                    etEmail.error =
                        "Please enter a valid email address"
                }

                password.length < 8 -> {

                    etPassword.error =
                        "Password must be at least 8 characters"
                }

                confirmPassword != password -> {

                    etConfirmPassword.error =
                        "Passwords do not match"
                }

                !cbTerms.isChecked -> {

                    Toast.makeText(
                        this,
                        "Please agree to the Terms of Service and Privacy Policy",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                    btnCreateAccount.isEnabled = false
                    btnCreateAccount.text = "Creating account..."

                    activityScope.launch {

                        try {

                            when (
                                val result = authRepository.register(
                                    fullName = fullName,
                                    email = email,
                                    password = password
                                )
                            ) {

                                is RegistrationResult.EmailConfirmationRequired -> {

                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Account created. Please check your email to confirm your account, then sign in.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    startActivity(
                                        Intent(
                                            this@RegisterActivity,
                                            LoginActivity::class.java
                                        ).apply {
                                            flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        }
                                    )

                                    finish()
                                }

                                is RegistrationResult.SignedIn -> {

                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Account created successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    AuthNavigation.openDashboard(
                                        this@RegisterActivity,
                                        result.profile
                                    )
                                }
                            }

                        } catch (e: Exception) {

                            Toast.makeText(
                                this@RegisterActivity,
                                e.message ?: "Registration failed. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()

                        } finally {

                            if (!isFinishing && !isDestroyed) {
                                btnCreateAccount.isEnabled = true
                                btnCreateAccount.text = "Create Account"
                            }
                        }
                    }
                }
            }
        }

        // Navigate back to Login.
        findViewById<TextView>(R.id.tvSignIn)
            .setOnClickListener {

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )

                finish()
            }

        // Social registration is not configured yet.
        findViewById<LinearLayout>(R.id.btnGoogleRegister)
            .setOnClickListener {

                Toast.makeText(
                    this,
                    "Google registration is not configured yet.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        findViewById<LinearLayout>(R.id.btnFacebookRegister)
            .setOnClickListener {

                Toast.makeText(
                    this,
                    "Facebook registration is not configured yet.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}
