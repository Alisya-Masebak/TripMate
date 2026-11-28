package com.example.tripmate.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,

    @SerialName("full_name")
    val fullName: String? = null,

    val role: String
)
