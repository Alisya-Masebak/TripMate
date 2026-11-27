package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

        searchInput.addTextChangedListener(object :
            android.text.TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                searchText = s?.toString()?.trim() ?: ""
                updateTripVisibility(view)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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
                    if (isSelected) R.color.white else R.color.tripmate_teal
                )
            )
        }
    }

    private fun updateTripVisibility(view: View) {
        val tripCard = view.findViewById<LinearLayout>(R.id.japanTripCard)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyTripsState)

        val matchesSearch = "japan adventure japan".contains(
            searchText.lowercase()
        )

        val shouldShowTrip =
            selectedFilter != "Completed" && matchesSearch

        tripCard.visibility = if (shouldShowTrip) View.VISIBLE else View.GONE
        emptyState.visibility = if (shouldShowTrip) View.GONE else View.VISIBLE
    }
}
