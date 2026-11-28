package com.example.tripmate

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tripmate.fragments.CreateTripFragment
import com.example.tripmate.fragments.ExploreFragment
import com.example.tripmate.fragments.ExpensesFragment
import com.example.tripmate.fragments.HomeFragment
import com.example.tripmate.fragments.ProfileFragment
import com.example.tripmate.fragments.TripDetailsFragment
import com.example.tripmate.fragments.TripsFragment

class MainActivity : AppCompatActivity() {

    private val selectedColor = 0xFF245B55.toInt()
    private val unselectedColor = 0xFF8A8492.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            navigateTo(HomeFragment(), R.id.navHome)
        }

        findViewById<LinearLayout>(R.id.navTrips).setOnClickListener {
            navigateTo(TripsFragment(), R.id.navTrips)
        }

        findViewById<LinearLayout>(R.id.navExplore).setOnClickListener {
            navigateTo(ExploreFragment(), R.id.navExplore)
        }

        findViewById<LinearLayout>(R.id.navExpenses).setOnClickListener {
            navigateTo(ExpensesFragment(), R.id.navExpenses)
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            navigateTo(ProfileFragment(), R.id.navProfile)
        }

        if (savedInstanceState == null) {
            navigateTo(HomeFragment(), R.id.navHome)
        }
    }

    fun openExpenses() {
        navigateTo(ExpensesFragment(), R.id.navExpenses)
    }

    fun openTripDetails() {
        navigateTo(TripDetailsFragment(), R.id.navTrips)
    }

    fun openTrips() {
        navigateTo(TripsFragment(), R.id.navTrips)
    }

    // Open the Create Trip form
    fun openCreateTrip() {
        navigateTo(CreateTripFragment(), R.id.navTrips)
    }

    fun openTripDetails(tripId: String? = null) {
        navigateTo(
            TripDetailsFragment.newInstance(tripId),
            R.id.navTrips
        )
    }


    private fun navigateTo(fragment: Fragment, selectedNavId: Int) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        updateNavigationSelection(selectedNavId)
    }

    private fun updateNavigationSelection(selectedNavId: Int) {

        val navigationIds = listOf(
            R.id.navHome,
            R.id.navTrips,
            R.id.navExplore,
            R.id.navExpenses,
            R.id.navProfile
        )

        navigationIds.forEach { navId ->

            val navigationItem = findViewById<LinearLayout>(navId)
            val isSelected = navId == selectedNavId

            val color = if (isSelected) {
                selectedColor
            } else {
                unselectedColor
            }

            val icon = navigationItem.getChildAt(0) as ImageView
            val label = navigationItem.getChildAt(1) as TextView

            icon.setColorFilter(color)
            label.setTextColor(color)

            label.setTypeface(
                null,
                if (isSelected) android.graphics.Typeface.BOLD
                else android.graphics.Typeface.NORMAL
            )
        }
    }
}
