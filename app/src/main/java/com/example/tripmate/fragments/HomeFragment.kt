package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.tripmate.R
import com.example.tripmate.MainActivity

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Quick action: My Trips
        view.findViewById<View>(R.id.btnMyTrips).setOnClickListener {
            activity?.findViewById<View>(R.id.navTrips)?.performClick()
        }

        // Quick action: Explore
        view.findViewById<View>(R.id.btnExplore).setOnClickListener {
            activity?.findViewById<View>(R.id.navExplore)?.performClick()
        }

        // Quick action: Expenses
        view.findViewById<View>(R.id.btnExpenses).setOnClickListener {
            activity?.findViewById<View>(R.id.navExpenses)?.performClick()
        }

        // View the trip through the Trips page
        view.findViewById<View>(R.id.btnViewTrip).setOnClickListener {
            (activity as? MainActivity)?.openTripDetails()
        }

        // Create a trip through the Trips page
        view.findViewById<View>(R.id.btnCreateTrip).setOnClickListener {
            activity?.findViewById<View>(R.id.navTrips)?.performClick()
        }
    }
}
