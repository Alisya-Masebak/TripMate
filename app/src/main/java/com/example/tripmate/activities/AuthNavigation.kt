package com.example.tripmate.activities

import android.app.Activity
import android.content.Intent
import com.example.tripmate.MainActivity
import com.example.tripmate.data.UserProfile

object AuthNavigation {

    fun openDashboard(
        activity: Activity,
        profile: UserProfile
    ) {

        val destination = when (profile.role) {

            "traveller" -> MainActivity::class.java

            "admin" -> AdminDashboardActivity::class.java

            else -> throw IllegalStateException(
                "Unknown account role."
            )
        }

        val intent = Intent(
            activity,
            destination
        ).apply {

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        activity.startActivity(intent)
        activity.finish()
    }
}
