package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripmate.MainActivity
import com.example.tripmate.R
import com.example.tripmate.data.TripDraft
import com.example.tripmate.data.TripDraftStore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreatedTripDetailsFragment :
    Fragment(R.layout.fragment_create_trip_details) {

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button at the top
        view.findViewById<View>(R.id.btnBackToTripsCreated)
            .setOnClickListener {
                (activity as? MainActivity)?.openTrips()
            }

        // Back button at the bottom
        view.findViewById<View>(R.id.btnBackToTripsCreatedBottom)
            .setOnClickListener {
                (activity as? MainActivity)?.openTrips()
            }

        // Retrieve the selected trip ID
        val tripId = arguments?.getString(ARG_TRIP_ID)

        // Find the corresponding trip in the temporary store
        val trip = tripId?.let { id ->
            TripDraftStore.trips.find { it.id == id }
        }

        // Handle missing or invalid trip information
        if (trip == null) {
            Toast.makeText(
                requireContext(),
                "Trip information could not be found.",
                Toast.LENGTH_LONG
            ).show()

            (activity as? MainActivity)?.openTrips()
            return
        }
        // Open the Create Trip form in edit mode
        view.findViewById<View>(R.id.btnEditCreatedTrip)
            .setOnClickListener {
                (activity as? MainActivity)?.openCreateTrip(trip.id)
            }
        // Display the selected trip
        displayTripDetails(view, trip)
    }

    private fun displayTripDetails(
        view: View,
        trip: TripDraft
    ) {

        // Format the budget
        val formattedBudget =
            NumberFormat.getCurrencyInstance(
                Locale("en", "MY")
            ).format(trip.budget)

        // Calculate trip duration
        val duration = calculateDuration(
            trip.startDate,
            trip.endDate
        )

        // Calculate trip status
        val status = if (isCompleted(trip.endDate)) {
            "COMPLETED"
        } else {
            "UPCOMING"
        }

        // Main trip overview
        view.findViewById<TextView>(
            R.id.tvCreatedTripStatus
        ).text = status

        view.findViewById<TextView>(
            R.id.tvCreatedTripName
        ).text = trip.name

        view.findViewById<TextView>(
            R.id.tvCreatedTripDestination
        ).text = trip.destination

        view.findViewById<TextView>(
            R.id.tvCreatedTripDates
        ).text = "${trip.startDate} - ${trip.endDate}"

        view.findViewById<TextView>(
            R.id.tvCreatedTripDuration
        ).text = duration

        view.findViewById<TextView>(
            R.id.tvCreatedTripBudget
        ).text = formattedBudget

        // Detailed trip information
        view.findViewById<TextView>(
            R.id.tvCreatedTripNameInfo
        ).text = trip.name

        view.findViewById<TextView>(
            R.id.tvCreatedTripDestinationInfo
        ).text = trip.destination

        view.findViewById<TextView>(
            R.id.tvCreatedTripStartDate
        ).text = trip.startDate

        view.findViewById<TextView>(
            R.id.tvCreatedTripEndDate
        ).text = trip.endDate

        view.findViewById<TextView>(
            R.id.tvCreatedTripBudgetInfo
        ).text = formattedBudget
    }

    private fun calculateDuration(
        startDateText: String,
        endDateText: String
    ): String {

        return try {

            val startDate = dateFormat.parse(startDateText)
                ?: return "Unavailable"

            val endDate = dateFormat.parse(endDateText)
                ?: return "Unavailable"

            val startCalendar = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val endCalendar = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (endCalendar.before(startCalendar)) {
                return "Unavailable"
            }

            // Count both the departure and return dates
            var days = 1

            while (startCalendar.before(endCalendar)) {
                startCalendar.add(Calendar.DAY_OF_YEAR, 1)
                days++
            }

            val nights = days - 1

            "$days days · $nights nights"

        } catch (e: Exception) {
            "Unavailable"
        }
    }

    private fun isCompleted(endDateText: String): Boolean {

        return try {

            val endDate = dateFormat.parse(endDateText)
                ?: return false

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            endDate.before(today.time)

        } catch (e: Exception) {
            false
        }
    }

    companion object {

        private const val ARG_TRIP_ID = "trip_id"

        fun newInstance(tripId: String): CreatedTripDetailsFragment {

            return CreatedTripDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRIP_ID, tripId)

                }

            }
        }
    }
}
