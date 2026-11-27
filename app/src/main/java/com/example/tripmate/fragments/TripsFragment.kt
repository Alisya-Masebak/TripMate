package com.example.tripmate.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tripmate.MainActivity
import com.example.tripmate.R

class TripsFragment : Fragment(R.layout.fragment_trips) {

    private var selectedFilter = "All trips"
    private var searchText = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.etSearchTrips)

        val allTrips = view.findViewById<TextView>(R.id.filterAll)
        val upcoming = view.findViewById<TextView>(R.id.filterUpcoming)
        val completed = view.findViewById<TextView>(R.id.filterCompleted)

        // Open the existing Trip Details screen
        view.findViewById<TextView>(R.id.btnTripDetails)
            .setOnClickListener {
                (activity as? MainActivity)?.openTripDetails()
            }

        // Search trips
        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                searchText = s?.toString()?.trim()?.lowercase() ?: ""
                updateTripVisibility(view)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter buttons
        allTrips.setOnClickListener {
            selectFilter("All trips", view)
        }

        upcoming.setOnClickListener {
            selectFilter("Upcoming", view)
        }

        completed.setOnClickListener {
            selectFilter("Completed", view)
        }

        updateFilterAppearance(view)
        updateTripVisibility(view)
    }

    private fun selectFilter(filter: String, view: View) {
        selectedFilter = filter
        updateFilterAppearance(view)
        updateTripVisibility(view)
    }

    private fun updateFilterAppearance(view: View) {

        val filters = listOf(
            R.id.filterAll to "All trips",
            R.id.filterUpcoming to "Upcoming",
            R.id.filterCompleted to "Completed"
        )

        filters.forEach { (id, label) ->

            val filterView = view.findViewById<TextView>(id)
            val isSelected = selectedFilter == label

            filterView.setBackgroundResource(
                if (isSelected) {
                    R.drawable.bg_filter_selected
                } else {
                    R.drawable.bg_filter_unselected
                }
            )

            filterView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.white
                    else R.color.tripmate_teal
                )
            )
        }
    }

    private fun updateTripVisibility(view: View) {

        val tripCard = view.findViewById<LinearLayout>(R.id.japanTripCard)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyTripsState)

        // Searchable information for the existing demo trip
        val tripSearchContent = """
            tokyo osaka adventure japan mount fuji kyoto nara
            8 days 7 nights october 10 17
        """.lowercase()

        val matchesSearch = tripSearchContent.contains(searchText)

        // This demo trip is currently an upcoming trip
        val matchesFilter = selectedFilter != "Completed"

        val shouldShowTrip = matchesSearch && matchesFilter

        tripCard.visibility =
            if (shouldShowTrip) View.VISIBLE else View.GONE

        emptyState.visibility =
            if (shouldShowTrip) View.GONE else View.VISIBLE
    }
}
