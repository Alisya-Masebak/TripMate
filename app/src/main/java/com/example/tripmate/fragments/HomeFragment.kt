package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.tripmate.MainActivity
import com.example.tripmate.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to My Trips
        view.findViewById<View>(R.id.btnMyTrips).setOnClickListener {
            activity?.findViewById<View>(R.id.navTrips)?.performClick()
        }

        // Navigate to Explore
        view.findViewById<View>(R.id.btnExplore).setOnClickListener {
            activity?.findViewById<View>(R.id.navExplore)?.performClick()
        }

        // Navigate to Expenses
        view.findViewById<View>(R.id.btnExpenses).setOnClickListener {
            activity?.findViewById<View>(R.id.navExpenses)?.performClick()
        }

        // Open the existing Japan Trip Details
        view.findViewById<View>(R.id.btnViewTrip).setOnClickListener {
            (activity as? MainActivity)?.openTripDetails()
        }

        // Open Create Trip form directly
        view.findViewById<View>(R.id.btnCreateTrip).setOnClickListener {
            (activity as? MainActivity)?.openCreateTrip()
        }
    }
}
