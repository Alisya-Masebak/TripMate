package com.example.tripmate.data

data class TripDraft(
    val id: String,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budget: Double
)

object TripDraftStore {

    val trips = mutableListOf<TripDraft>()

    // Add a newly created trip
    fun addTrip(trip: TripDraft) {
        trips.add(trip)
    }

    // Retrieve a trip using its unique ID
    fun getTripById(tripId: String): TripDraft? {
        return trips.find { it.id == tripId }
    }

    // Update an existing trip without creating a duplicate
    fun updateTrip(updatedTrip: TripDraft): Boolean {

        val index = trips.indexOfFirst {
            it.id == updatedTrip.id
        }

        if (index == -1) {
            return false
        }

        trips[index] = updatedTrip

        return true
    }
}
