package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tripmate.MainActivity
import com.example.tripmate.R
import com.example.tripmate.data.TripDraftStore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripDetailsFragment : Fragment(R.layout.fragment_trip_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackToTrips).setOnClickListener {
            (activity as? MainActivity)?.openTrips()
        }

        view.findViewById<View>(R.id.btnBackToTripsBottom).setOnClickListener {
            (activity as? MainActivity)?.openTrips()
        }

        val tripId = arguments?.getString(ARG_TRIP_ID)

        val trip = TripDraftStore.trips.find {
            it.id == tripId
        }

        if (trip != null) {
            displaySelectedTrip(view, trip)
        } else {
            displayDefaultJapanTrip(view)
        }
    }

    private fun displaySelectedTrip(
        view: View,
        trip: com.example.tripmate.data.TripDraft
    ) {
        view.findViewById<TextView>(R.id.tvDetailTripName).text = trip.name

        view.findViewById<TextView>(R.id.tvDetailDestination).text =
            trip.destination

        view.findViewById<TextView>(R.id.tvDetailDates).text =
            "${trip.startDate} - ${trip.endDate}"

        view.findViewById<TextView>(R.id.tvDetailBudget).text =
            NumberFormat.getCurrencyInstance(Locale("en", "MY"))
                .format(trip.budget)

        view.findViewById<TextView>(R.id.tvDetailStatus).text =
            if (isCompleted(trip.endDate)) "COMPLETED" else "UPCOMING"
    }

    private fun displayDefaultJapanTrip(view: View) {
        view.findViewById<TextView>(R.id.tvDetailTripName).text =
            "Japan Adventure"

        view.findViewById<TextView>(R.id.tvDetailDestination).text =
            "Tokyo, Mount Fuji, Osaka, Kyoto and Nara"

        view.findViewById<TextView>(R.id.tvDetailDates).text =
            "10 Oct 2026 - 17 Oct 2026"

        view.findViewById<TextView>(R.id.tvDetailBudget).text =
            "See your Expenses screen"

        view.findViewById<TextView>(R.id.tvDetailStatus).text =
            "JAPAN DEMO TRIP"
    }

    private fun isCompleted(endDateText: String): Boolean {
        return try {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val endDate = format.parse(endDateText) ?: return false

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

        fun newInstance(tripId: String? = null): TripDetailsFragment {
            return TripDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRIP_ID, tripId)
                }
            }
        }
    }
}
