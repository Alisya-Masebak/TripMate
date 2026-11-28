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

    fun addTrip(trip: TripDraft) {
        trips.add(trip)
    }
}
