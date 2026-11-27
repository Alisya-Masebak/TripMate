package com.example.tripmate

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

import com.example.tripmate.fragments.HomeFragment
import com.example.tripmate.fragments.TripsFragment
import com.example.tripmate.fragments.ExploreFragment
import com.example.tripmate.fragments.ExpensesFragment
import com.example.tripmate.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

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

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            loadFragment(HomeFragment())
        }

        findViewById<LinearLayout>(R.id.navTrips).setOnClickListener {
            loadFragment(TripsFragment())
        }

        findViewById<LinearLayout>(R.id.navExplore).setOnClickListener {
            loadFragment(ExploreFragment())
        }

        findViewById<LinearLayout>(R.id.navExpenses).setOnClickListener {
            loadFragment(ExpensesFragment())
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            loadFragment(ProfileFragment())
        }
    }

    fun openExpenses() {
        loadFragment(ExpensesFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
