package com.example.tripmate.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import java.util.UUID

class CreateTripFragment : Fragment(R.layout.fragment_create_trip) {

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private var selectedStartDate: Calendar? = null
    private var selectedEndDate: Calendar? = null

    private var editingTrip: TripDraft? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripNameInput = view.findViewById<EditText>(R.id.etTripName)
        val destinationInput = view.findViewById<EditText>(R.id.etDestination)
        val budgetInput = view.findViewById<EditText>(R.id.etBudget)

        val startDateText = view.findViewById<TextView>(R.id.tvStartDate)
        val endDateText = view.findViewById<TextView>(R.id.tvEndDate)

        val title = view.findViewById<TextView>(R.id.tvCreateTripTitle)
        val saveButton = view.findViewById<Button>(R.id.btnSaveTrip)
        val cancelButton = view.findViewById<Button>(R.id.btnCancelCreateTrip)

        // Retrieve the trip ID passed from Trip Details
        val tripId = arguments?.getString(ARG_TRIP_ID)

        // Enter edit mode when a trip ID is provided
        if (tripId != null) {

            editingTrip = TripDraftStore.getTripById(tripId)

            // Handle missing trip
            if (editingTrip == null) {

                Toast.makeText(
                    requireContext(),
                    "Trip information could not be found.",
                    Toast.LENGTH_LONG
                ).show()

                (activity as? MainActivity)?.openTrips()
                return
            }

            // Load the existing trip information
            val trip = editingTrip!!

            tripNameInput.setText(trip.name)
            destinationInput.setText(trip.destination)
            budgetInput.setText(trip.budget.toString())

            selectedStartDate = parseDateToCalendar(trip.startDate)
            selectedEndDate = parseDateToCalendar(trip.endDate)

            startDateText.text = trip.startDate
            endDateText.text = trip.endDate

            // Change the form heading and button
            title.text = "Edit Trip"
            saveButton.text = "Update Trip"

        } else {

            // Normal create mode
            title.text = "Create New Trip"
            saveButton.text = "Save Trip"
        }

        // Open start date picker
        startDateText.setOnClickListener {
            showStartDatePicker(startDateText, endDateText)
        }

        // Open end date picker
        endDateText.setOnClickListener {

            if (selectedStartDate == null) {

                Toast.makeText(
                    requireContext(),
                    "Please select the start date first.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                showEndDatePicker(endDateText)
            }
        }

        // Save or update trip
        saveButton.setOnClickListener {

            val tripName = tripNameInput.text.toString().trim()
            val destination = destinationInput.text.toString().trim()
            val budgetText = budgetInput.text.toString().trim()
            val budget = budgetText.toDoubleOrNull()

            // Validate trip name
            if (tripName.isEmpty()) {
                tripNameInput.error = "Please enter a trip name."
                tripNameInput.requestFocus()
                return@setOnClickListener
            }

            // Validate destination
            if (destination.isEmpty()) {
                destinationInput.error = "Please enter a destination."
                destinationInput.requestFocus()
                return@setOnClickListener
            }

            // Validate start date
            if (selectedStartDate == null) {

                Toast.makeText(
                    requireContext(),
                    "Please select a start date.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Validate end date
            if (selectedEndDate == null) {

                Toast.makeText(
                    requireContext(),
                    "Please select an end date.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Validate budget
            if (budget == null || !budget.isFinite() || budget <= 0.0) {

                budgetInput.error =
                    "Please enter a valid budget greater than RM0."

                budgetInput.requestFocus()
                return@setOnClickListener
            }

            // Ensure end date is not before start date
            if (selectedEndDate!!.before(selectedStartDate)) {

                Toast.makeText(
                    requireContext(),
                    "End date cannot be before the start date.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Preserve the original ID when editing
            val updatedTrip = TripDraft(
                id = editingTrip?.id ?: UUID.randomUUID().toString(),
                name = tripName,
                destination = destination,
                startDate = dateFormat.format(selectedStartDate!!.time),
                endDate = dateFormat.format(selectedEndDate!!.time),
                budget = budget
            )

            if (editingTrip != null) {

                // Update the existing trip
                val success = TripDraftStore.updateTrip(updatedTrip)

                if (!success) {

                    Toast.makeText(
                        requireContext(),
                        "Unable to update trip. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                Toast.makeText(
                    requireContext(),
                    "Trip updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Return to the updated trip details
                (activity as? MainActivity)
                    ?.openTripDetails(updatedTrip.id)

            } else {

                // Create a new trip
                TripDraftStore.addTrip(updatedTrip)

                Toast.makeText(
                    requireContext(),
                    "Trip created successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Return to My Trips
                (activity as? MainActivity)?.openTrips()
            }
        }

        // Cancel editing or creation
        cancelButton.setOnClickListener {

            if (editingTrip != null) {

                // Discard changes and return to original details
                (activity as? MainActivity)
                    ?.openTripDetails(editingTrip!!.id)

            } else {

                (activity as? MainActivity)?.openTrips()
            }
        }
    }

    // Convert saved date text into Calendar
    private fun parseDateToCalendar(dateText: String): Calendar? {

        return try {

            val date = dateFormat.parse(dateText) ?: return null

            Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        } catch (e: Exception) {
            null
        }
    }

    private fun showStartDatePicker(
        startDateText: TextView,
        endDateText: TextView
    ) {

        val calendar = selectedStartDate?.clone() as? Calendar
            ?: Calendar.getInstance()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                selectedStartDate = selectedDate

                startDateText.text =
                    dateFormat.format(selectedDate.time)

                // Clear the end date only if it is now invalid
                if (selectedEndDate != null &&
                    selectedEndDate!!.before(selectedDate)
                ) {

                    selectedEndDate = null
                    endDateText.text = ""
                    endDateText.hint = "Select end date"
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun showEndDatePicker(endDateText: TextView) {

        val startDate = selectedStartDate ?: return

        val calendar = selectedEndDate?.clone() as? Calendar
            ?: (startDate.clone() as Calendar)

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (selectedDate.before(startDate)) {

                    Toast.makeText(
                        requireContext(),
                        "End date cannot be before the start date.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@DatePickerDialog
                }

                selectedEndDate = selectedDate

                endDateText.text =
                    dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Prevent choosing an end date before the start date
        dialog.datePicker.minDate = startDate.timeInMillis

        dialog.show()
    }

    companion object {

        private const val ARG_TRIP_ID = "trip_id"

        fun newInstance(tripId: String? = null): CreateTripFragment {

            return CreateTripFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRIP_ID, tripId)
                }
            }
        }
    }
}
