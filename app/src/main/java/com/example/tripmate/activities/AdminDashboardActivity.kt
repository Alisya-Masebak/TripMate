package com.example.tripmate.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripmate.R
import com.example.tripmate.data.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private val authRepository = AuthenticationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_dashboard)

        // Verify the administrator role before displaying this screen.
        activityScope.launch {
            try {
                val profile = authRepository.getCurrentProfile()

                if (profile?.role != "admin") {
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Administrator access is required.",
                        Toast.LENGTH_LONG
                    ).show()

                    openLogin()
                }
            } catch (e: Exception) {
                openLogin()
            }
        }

        findViewById<Button>(R.id.btnAdminSignOut)
            .setOnClickListener {

                activityScope.launch {

                    try {
                        authRepository.signOut()
                        openLogin()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            e.message ?: "Unable to sign out.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun openLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}
