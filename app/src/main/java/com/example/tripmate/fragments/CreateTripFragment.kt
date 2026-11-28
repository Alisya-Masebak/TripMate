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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class CreateTripFragment : Fragment(R.layout.fragment_create_trip) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private var selectedStartDate: Calendar? = null
    private var selectedEndDate: Calendar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripNameInput = view.findViewById<EditText>(R.id.etTripName)
        val destinationInput = view.findViewById<EditText>(R.id.etDestination)
        val budgetInput = view.findViewById<EditText>(R.id.etBudget)

        val startDateText = view.findViewById<TextView>(R.id.tvStartDate)
        val endDateText = view.findViewById<TextView>(R.id.tvEndDate)

        val saveButton = view.findViewById<Button>(R.id.btnSaveTrip)
        val cancelButton = view.findViewById<Button>(R.id.btnCancelCreateTrip)

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

        // Save trip
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
                budgetInput.error = "Please enter a valid budget greater than RM0."
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

            // Create trip object
            val newTrip = TripDraft(
                id = UUID.randomUUID().toString(),
                name = tripName,
                destination = destination,
                startDate = dateFormat.format(selectedStartDate!!.time),
                endDate = dateFormat.format(selectedEndDate!!.time),
                budget = budget
            )

            // Save to shared in-memory store
            TripDraftStore.addTrip(newTrip)

            Toast.makeText(
                requireContext(),
                "Trip created successfully!",
                Toast.LENGTH_SHORT
            ).show()

            // Return to My Trips
            (activity as? MainActivity)?.openTrips()
        }

        // Cancel and return to My Trips
        cancelButton.setOnClickListener {
            (activity as? MainActivity)?.openTrips()
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
                startDateText.text = dateFormat.format(selectedDate.time)

                // Clear an end date that is now earlier than the new start date
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
                endDateText.text = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Prevent selecting an end date earlier than the start date
        dialog.datePicker.minDate = startDate.timeInMillis

        dialog.show()
    }
}
