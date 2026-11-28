package com.example.tripmate.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.tripmate.R
import com.example.tripmate.activities.LoginActivity
import com.example.tripmate.data.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val authRepository = AuthenticationRepository()

    private var screenScope: CoroutineScope? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        screenScope = CoroutineScope(
            SupervisorJob() + Dispatchers.Main
        )

        setupProfileActions(view)
        loadProfile(view)
    }

    // Retrieve the signed-in user's information from Supabase.
    private fun loadProfile(view: View) {

        screenScope?.launch {

            try {

                val profile = authRepository.getCurrentProfile()

                if (profile == null) {
                    navigateToLogin()
                    return@launch
                }

                val email = authRepository.getCurrentUserEmail()

                val fullName = profile.fullName
                    ?.takeIf { it.isNotBlank() }
                    ?: "TripMate Traveller"

                view.findViewById<TextView>(
                    R.id.tvProfileName
                ).text = fullName

                view.findViewById<TextView>(
                    R.id.tvProfileEmail
                ).text = email ?: "Email unavailable"

                val initials = fullName
                    .trim()
                    .split(Regex("\\s+"))
                    .take(2)
                    .mapNotNull { it.firstOrNull() }
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "T" }

                view.findViewById<TextView>(
                    R.id.tvProfileInitial
                ).text = initials

            } catch (e: Exception) {

                if (isAdded) {

                    Toast.makeText(
                        requireContext(),
                        "Unable to load profile: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Set up Profile screen interactions.
    private fun setupProfileActions(view: View) {

        view.findViewById<LinearLayout>(
            R.id.rowPersonalInformation
        ).setOnClickListener {
            showComingSoon("Personal information")
        }

        view.findViewById<LinearLayout>(
            R.id.rowTravelPreferences
        ).setOnClickListener {
            showComingSoon("Travel preferences")
        }

        view.findViewById<LinearLayout>(
            R.id.rowNotifications
        ).setOnClickListener {
            showComingSoon("Notifications")
        }

        view.findViewById<LinearLayout>(
            R.id.rowPrivacySecurity
        ).setOnClickListener {
            showComingSoon("Privacy and security")
        }

        view.findViewById<LinearLayout>(
            R.id.rowHelpSupport
        ).setOnClickListener {
            showComingSoon("Help and support")
        }

        view.findViewById<TextView>(
            R.id.btnLogout
        ).setOnClickListener {
            showLogoutConfirmation()
        }
    }

    // Show a confirmation before logging out.
    private fun showLogoutConfirmation() {

        AlertDialog.Builder(requireContext())
            .setTitle("Log out?")
            .setMessage(
                "Are you sure you want to log out of TripMate?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Log out") { _, _ ->

                performLogout()
            }
            .show()
    }

    // Sign out from Supabase and return to Login.
    private fun performLogout() {

        val logoutButton =
            view?.findViewById<TextView>(R.id.btnLogout)

        logoutButton?.isEnabled = false

        screenScope?.launch {

            try {

                authRepository.signOut()

                navigateToLogin()

            } catch (e: Exception) {

                logoutButton?.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Logout failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Clear the authenticated screen from the activity stack.
    private fun navigateToLogin() {

        val intent = Intent(
            requireContext(),
            LoginActivity::class.java
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        requireActivity().finish()
    }

    // Placeholder until the other Profile modules are implemented.
    private fun showComingSoon(feature: String) {

        Toast.makeText(
            requireContext(),
            "$feature will be available soon.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {

        screenScope?.cancel()
        screenScope = null

        super.onDestroyView()
    }
}