package com.example.tripmate.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tripmate.MainActivity
import com.example.tripmate.R
import com.example.tripmate.data.TripDraft
import com.example.tripmate.data.TripDraftStore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripsFragment : Fragment(R.layout.fragment_trips) {

    private var selectedFilter = "All trips"
    private var searchText = ""

    private data class TripCardItem(
        val trip: TripDraft,
        val card: LinearLayout
    )

    private val dynamicTripCards = mutableListOf<TripCardItem>()

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.etSearchTrips)

        val allTrips = view.findViewById<TextView>(R.id.filterAll)
        val upcoming = view.findViewById<TextView>(R.id.filterUpcoming)
        val completed = view.findViewById<TextView>(R.id.filterCompleted)

        // Existing Japan Trip Details navigation
        view.findViewById<View>(R.id.btnTripDetails).setOnClickListener {
            (activity as? MainActivity)?.openTripDetails()
        }

        // Open Create Trip form
        view.findViewById<View>(R.id.btnCreateTrip).setOnClickListener {
            (activity as? MainActivity)?.openCreateTrip()
        }

        // Display saved trips
        displaySavedTrips(view)

        // Search trips
        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchText = s?.toString()?.trim()?.lowercase() ?: ""
                updateTripVisibility(view)
            }

            override fun afterTextChanged(s: Editable?) {
            }
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

    private fun displaySavedTrips(view: View) {

        val japanTripCard =
            view.findViewById<LinearLayout>(R.id.japanTripCard)

        val cardParent = japanTripCard.parent as? ViewGroup

        if (cardParent == null) {
            Toast.makeText(
                requireContext(),
                "Unable to display new trips. Please check the trip card layout.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        dynamicTripCards.clear()

        // Insert new trips immediately after the Japan card
        var insertPosition = cardParent.indexOfChild(japanTripCard) + 1

        TripDraftStore.trips.forEach { trip ->

            val card = createTripCard(trip)

            val layoutParams = if (cardParent is LinearLayout) {
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(14)
                }
            } else {
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            cardParent.addView(card, insertPosition, layoutParams)

            dynamicTripCards.add(
                TripCardItem(trip, card)
            )

            insertPosition++
        }
    }

    private fun createTripCard(trip: TripDraft): LinearLayout {

        val context = requireContext()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
            )

            setBackgroundResource(R.drawable.bg_card_lavender)

            // Make the complete card clickable
            isClickable = true
            isFocusable = true

            setOnClickListener {
                (activity as? MainActivity)?.openTripDetails(trip.id)
            }
        }

        val status = if (isCompleted(trip)) {
            "COMPLETED"
        } else {
            "UPCOMING"
        }

        val statusText = TextView(context).apply {
            text = status
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)

            setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.tripmate_teal
                )
            )

            setBackgroundResource(R.drawable.bg_icon_circle)

            setPadding(
                dpToPx(12),
                dpToPx(5),
                dpToPx(12),
                dpToPx(5)
            )
        }

        val tripName = TextView(context).apply {
            text = trip.name
            textSize = 21f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF245B55.toInt())
            setPadding(0, dpToPx(14), 0, 0)
        }

        val destination = TextView(context).apply {
            text = trip.destination
            textSize = 14f
            setTextColor(0xFF858292.toInt())
            setPadding(0, dpToPx(7), 0, 0)
        }

        val dates = TextView(context).apply {
            text = "${trip.startDate} - ${trip.endDate}"
            textSize = 12f
            setTextColor(0xFF858292.toInt())
            setPadding(0, dpToPx(7), 0, 0)
        }

        val budget = TextView(context).apply {
            text = "Budget: ${
                NumberFormat.getCurrencyInstance(
                    Locale("en", "MY")
                ).format(trip.budget)
            }"

            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF245B55.toInt())
            setPadding(0, dpToPx(10), 0, 0)
        }

        card.addView(statusText)
        card.addView(tripName)
        card.addView(destination)
        card.addView(dates)
        card.addView(budget)

        return card
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

        val japanTripCard =
            view.findViewById<LinearLayout>(R.id.japanTripCard)

        val emptyState =
            view.findViewById<LinearLayout>(R.id.emptyTripsState)

        val japanSearchContent = """
        japan adventure tokyo osaka mount fuji kyoto nara
        8 days 7 nights 11 18 december dec 2026
        11–18 dec 2026
    """.lowercase()

        val japanMatchesSearch =
            japanSearchContent.contains(searchText)

        // Determine Japan trip status using its actual end date
        val japanCompleted = isJapanTripCompleted()

        val japanMatchesFilter = when (selectedFilter) {
            "Upcoming" -> !japanCompleted
            "Completed" -> japanCompleted
            else -> true
        }

        val showJapanTrip =
            japanMatchesSearch && japanMatchesFilter
        // Update Japan card status label
        view.findViewById<TextView>(R.id.tvJapanTripStatus).text =
            if (japanCompleted) "COMPLETED" else "UPCOMING"

        japanTripCard.visibility = if (showJapanTrip) {
            View.VISIBLE
        } else {
            View.GONE
        }

        var visibleTripCount = if (showJapanTrip) 1 else 0

        // Update newly created trip cards
        dynamicTripCards.forEach { item ->

            val trip = item.trip

            val searchableText = """
                ${trip.name}
                ${trip.destination}
                ${trip.startDate}
                ${trip.endDate}
            """.lowercase()

            val matchesSearch = searchableText.contains(searchText)

            val completed = isCompleted(trip)

            val matchesFilter = when (selectedFilter) {
                "Upcoming" -> !completed
                "Completed" -> completed
                else -> true
            }

            val shouldShow = matchesSearch && matchesFilter

            item.card.visibility = if (shouldShow) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (shouldShow) {
                visibleTripCount++
            }
        }

        // Show empty state only when no trips match
        emptyState.visibility = if (visibleTripCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun isCompleted(trip: TripDraft): Boolean {

        return try {
            val endDate = dateFormat.parse(trip.endDate) ?: return false

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
    private fun isJapanTripCompleted(): Boolean {

        return try {
            val japanEndDate =
                dateFormat.parse("18 Dec 2026") ?: return false

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            japanEndDate.before(today.time)

        } catch (e: Exception) {
            false
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
