package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tripmate.MainActivity
import com.example.tripmate.R

class TripDetailsFragment : Fragment(R.layout.fragment_trip_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back to My Trips
        view.findViewById<TextView>(R.id.btnBackToTrips)
            .setOnClickListener {
                (activity as? MainActivity)?.openTrips()
            }

        view.findViewById<TextView>(R.id.btnBackToTripsBottom)
            .setOnClickListener {
                (activity as? MainActivity)?.openTrips()
            }
        // Open Japan Adventure details
        view.findViewById<TextView>(R.id.btnTripDetails)
            .setOnClickListener {
                (activity as? MainActivity)?.openTripDetails()
            }
    }
}
