package com.example.tripmate.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DestinationRecord(

    val id: String,

    val name: String,

    val country: String,

    val description: String? = null,

    val category: String,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)