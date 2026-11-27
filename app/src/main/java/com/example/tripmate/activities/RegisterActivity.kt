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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Handle system bar spacing
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
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

        // Connect UI components
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)

        // Create Account button
        btnCreateAccount.setOnClickListener {

            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            when {
                fullName.isEmpty() -> {
                    etFullName.error = "Please enter your full name"
                }

                email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Please enter a valid email address"
                }

                password.length < 8 -> {
                    etPassword.error = "Password must be at least 8 characters"
                }

                confirmPassword != password -> {
                    etConfirmPassword.error = "Passwords do not match"
                }

                !cbTerms.isChecked -> {
                    Toast.makeText(
                        this,
                        "Please agree to the Terms of Service and Privacy Policy",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Your details are valid. Account creation will be enabled after Supabase integration.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Navigate back to Login
        findViewById<TextView>(R.id.tvSignIn).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Google registration placeholder
        findViewById<LinearLayout>(R.id.btnGoogleRegister).setOnClickListener {
            Toast.makeText(
                this,
                "Google sign-up will be available after authentication setup.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Facebook registration placeholder
        findViewById<LinearLayout>(R.id.btnFacebookRegister).setOnClickListener {
            Toast.makeText(
                this,
                "Facebook sign-up will be available after authentication setup.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
