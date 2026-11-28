package com.example.tripmate.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripmate.R
import com.example.tripmate.data.AuthenticationRepository
import com.example.tripmate.data.DestinationDashboardSummary
import com.example.tripmate.data.DestinationDashboardUpdate
import com.example.tripmate.data.DestinationRepository
import com.example.tripmate.data.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private val authRepository = AuthenticationRepository()
    private val destinationRepository = DestinationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var adminContent: View
    private lateinit var progressAdmin: ProgressBar

    private lateinit var tvAdminWelcome: TextView
    private lateinit var tvAdminName: TextView
    private lateinit var tvAdminEmail: TextView
    private lateinit var tvTotalDestinations: TextView
    private lateinit var tvActiveDestinations: TextView

    private lateinit var recentUpdateOne: View
    private lateinit var recentUpdateTwo: View
    private lateinit var recentUpdateDivider: View

    private lateinit var tvRecentUpdateOneName: TextView
    private lateinit var tvRecentUpdateOneDetail: TextView
    private lateinit var tvRecentUpdateTwoName: TextView
    private lateinit var tvRecentUpdateTwoDetail: TextView
    private lateinit var tvNoRecentUpdates: TextView

    private var adminProfile: UserProfile? = null
    private var adminEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.adminRoot)
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

        bindViews()
        setupClickListeners()
        verifyAdministrator()
    }

    private fun bindViews() {

        adminContent = findViewById(R.id.adminContent)
        progressAdmin = findViewById(R.id.progressAdmin)

        tvAdminWelcome = findViewById(R.id.tvAdminWelcome)
        tvAdminName = findViewById(R.id.tvAdminName)
        tvAdminEmail = findViewById(R.id.tvAdminEmail)

        tvTotalDestinations =
            findViewById(R.id.tvTotalDestinations)

        tvActiveDestinations =
            findViewById(R.id.tvActiveDestinations)

        recentUpdateOne = findViewById(R.id.recentUpdateOne)
        recentUpdateTwo = findViewById(R.id.recentUpdateTwo)
        recentUpdateDivider =
            findViewById(R.id.recentUpdateDivider)

        tvRecentUpdateOneName =
            findViewById(R.id.tvRecentUpdateOneName)

        tvRecentUpdateOneDetail =
            findViewById(R.id.tvRecentUpdateOneDetail)

        tvRecentUpdateTwoName =
            findViewById(R.id.tvRecentUpdateTwoName)

        tvRecentUpdateTwoDetail =
            findViewById(R.id.tvRecentUpdateTwoDetail)

        tvNoRecentUpdates =
            findViewById(R.id.tvNoRecentUpdates)
    }

    private fun setupClickListeners() {

        // Add Destination will be connected in the next step.
        findViewById<Button>(R.id.btnAddDestination)
            .setOnClickListener {

                val intent = Intent(
                    this,
                    AddDestinationActivity::class.java
                )

                startActivity(intent)
            }

        // Open Manage Destinations.
        findViewById<Button>(R.id.btnManageDestinations)
            .setOnClickListener {
                openManageDestinations()
            }

        // Dashboard navigation.
        findViewById<LinearLayout>(R.id.navAdminDashboard)
            .setOnClickListener {
                // Already on Dashboard.
            }

        // Destinations navigation.
        findViewById<LinearLayout>(R.id.navAdminDestinations)
            .setOnClickListener {
                openManageDestinations()
            }

        // Account navigation.
        findViewById<LinearLayout>(R.id.navAdminAccount)
            .setOnClickListener {
                showAdminAccountOptions()
            }
    }
    private fun openManageDestinations() {

        val intent = Intent(
            this,
            ManageDestinationsActivity::class.java
        )

        startActivity(intent)
    }

    private fun verifyAdministrator() {

        activityScope.launch {

            try {

                val profile =
                    authRepository.getCurrentProfile()

                if (profile?.role != "admin") {

                    Toast.makeText(
                        this@AdminDashboardActivity,
                        R.string.admin_access_required,
                        Toast.LENGTH_LONG
                    ).show()

                    openLogin()
                    return@launch
                }

                adminProfile = profile
                adminEmail =
                    authRepository.getCurrentUserEmail()

                bindAdministratorDetails(
                    profile,
                    adminEmail
                )

                adminContent.visibility = View.VISIBLE
                progressAdmin.visibility = View.GONE

                loadDestinationOverview()

            } catch (e: Exception) {

                Toast.makeText(
                    this@AdminDashboardActivity,
                    e.message
                        ?: getString(
                            R.string.admin_verification_failed
                        ),
                    Toast.LENGTH_LONG
                ).show()

                openLogin()
            }
        }
    }

    private fun bindAdministratorDetails(
        profile: UserProfile,
        email: String?
    ) {

        val fullName = profile.fullName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.admin_fallback_name)

        val firstName = fullName
            .split(Regex("\\s+"))
            .firstOrNull()
            .orEmpty()
            .ifBlank {
                getString(R.string.admin_fallback_name)
            }

        tvAdminWelcome.text =
            getString(R.string.admin_welcome_back, firstName)

        tvAdminName.text = fullName

        tvAdminEmail.text =
            email?.takeIf { it.isNotBlank() }
                ?: getString(R.string.admin_email_unavailable)
    }

    private suspend fun loadDestinationOverview() {

        try {

            val summary =
                destinationRepository.getAdminDashboardSummary()

            bindDestinationSummary(summary)

        } catch (e: Exception) {

            tvTotalDestinations.text = "—"
            tvActiveDestinations.text = "—"

            showRecentUpdatesError()

            Toast.makeText(
                this,
                e.message
                    ?: getString(
                        R.string.admin_destination_load_error
                    ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun bindDestinationSummary(
        summary: DestinationDashboardSummary
    ) {

        tvTotalDestinations.text =
            summary.totalDestinations.toString()

        tvActiveDestinations.text =
            summary.activeDestinations.toString()

        bindRecentUpdates(summary.recentUpdates)
    }

    private fun bindRecentUpdates(
        updates: List<DestinationDashboardUpdate>
    ) {

        recentUpdateOne.visibility = View.GONE
        recentUpdateTwo.visibility = View.GONE
        recentUpdateDivider.visibility = View.GONE
        tvNoRecentUpdates.visibility = View.GONE

        if (updates.isEmpty()) {

            tvNoRecentUpdates.text =
                getString(R.string.admin_no_recent_updates)

            tvNoRecentUpdates.visibility = View.VISIBLE
            return
        }

        val first = updates[0]

        tvRecentUpdateOneName.text = first.name

        tvRecentUpdateOneDetail.text = getString(
            R.string.admin_recent_last_changed,
            formatTimestamp(first.updatedAt)
        )

        recentUpdateOne.visibility = View.VISIBLE

        if (updates.size > 1) {

            val second = updates[1]

            tvRecentUpdateTwoName.text = second.name

            tvRecentUpdateTwoDetail.text = getString(
                R.string.admin_recent_last_changed,
                formatTimestamp(second.updatedAt)
            )

            recentUpdateDivider.visibility = View.VISIBLE
            recentUpdateTwo.visibility = View.VISIBLE
        }
    }

    private fun showRecentUpdatesError() {

        recentUpdateOne.visibility = View.GONE
        recentUpdateTwo.visibility = View.GONE
        recentUpdateDivider.visibility = View.GONE

        tvNoRecentUpdates.text =
            getString(R.string.admin_destination_load_error)

        tvNoRecentUpdates.visibility = View.VISIBLE
    }

    private fun formatTimestamp(value: String): String {

        return try {

            val instant =
                OffsetDateTime.parse(value).toInstant()

            DateTimeFormatter
                .ofPattern(
                    "dd MMM yyyy, HH:mm",
                    Locale.getDefault()
                )
                .withZone(ZoneId.systemDefault())
                .format(instant)

        } catch (_: Exception) {

            value.replace("T", " ").take(16)
        }
    }

    private fun showDestinationScreensComingSoon() {

        Toast.makeText(
            this,
            R.string.admin_destination_screens_next,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showAdminAccountOptions() {

        val profile = adminProfile ?: return

        val name = profile.fullName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.admin_fallback_name)

        val email = adminEmail
            ?.takeIf { it.isNotBlank() }
            ?: getString(R.string.admin_email_unavailable)

        AlertDialog.Builder(this)
            .setTitle(R.string.admin_account_title)
            .setMessage(
                "$name\n$email\n\n" +
                        getString(R.string.admin_role_label)
            )
            .setPositiveButton(R.string.sign_out) { _, _ ->
                signOut()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun signOut() {

        activityScope.launch {

            try {

                authRepository.signOut()
                openLogin()

            } catch (e: Exception) {

                Toast.makeText(
                    this@AdminDashboardActivity,
                    e.message
                        ?: getString(
                            R.string.admin_sign_out_failed
                        ),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openLogin() {

        val intent = Intent(
            this,
            LoginActivity::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
    override fun onResume() {

        super.onResume()

        if (adminProfile != null) {

            activityScope.launch {
                loadDestinationOverview()
            }
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}