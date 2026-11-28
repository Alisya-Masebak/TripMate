package com.example.tripmate.data

import com.example.tripmate.models.DestinationRecord
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

data class DestinationDashboardSummary(
    val totalDestinations: Int,
    val activeDestinations: Int,
    val recentUpdates: List<DestinationDashboardUpdate>
)

data class DestinationDashboardUpdate(
    val name: String,
    val updatedAt: String
)

data class UploadedDestinationImage(
    val path: String,
    val publicUrl: String
)

@Serializable
private data class NewDestinationRecord(
    val name: String,
    val country: String,
    val description: String,
    val category: String,

    @SerialName("image_url")
    val imageUrl: String,

    val latitude: Double,
    val longitude: Double,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("created_by")
    val createdBy: String,

    @SerialName("updated_by")
    val updatedBy: String
)

class DestinationRepository {

    private val supabase = SupabaseClient.client

    private val imageBucket =
        supabase.storage["destination-images"]

    // --------------------------------------------------
    // GET ALL DESTINATIONS
    // --------------------------------------------------

    suspend fun getAdminDestinations():
            List<DestinationRecord> {

        return supabase
            .from("destinations")
            .select(
                columns = Columns.raw(
                    """
                    id,
                    name,
                    country,
                    description,
                    category,
                    image_url,
                    latitude,
                    longitude,
                    is_active,
                    created_at,
                    updated_at
                    """.trimIndent()
                )
            ) {
                order(
                    column = "updated_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<DestinationRecord>()
    }

    // --------------------------------------------------
    // GET ONE DESTINATION
    // --------------------------------------------------

    suspend fun getDestinationById(
        destinationId: String
    ): DestinationRecord {

        require(destinationId.isNotBlank()) {
            "Destination ID is required."
        }

        return supabase
            .from("destinations")
            .select(
                columns = Columns.raw(
                    """
                    id,
                    name,
                    country,
                    description,
                    category,
                    image_url,
                    latitude,
                    longitude,
                    is_active,
                    created_at,
                    updated_at
                    """.trimIndent()
                )
            ) {
                filter {
                    eq("id", destinationId)
                }
            }
            .decodeSingle<DestinationRecord>()
    }

    // --------------------------------------------------
    // UPLOAD DESTINATION IMAGE
    // --------------------------------------------------

    suspend fun uploadDestinationImage(
        imageBytes: ByteArray,
        mimeType: String
    ): UploadedDestinationImage {

        require(imageBytes.isNotEmpty()) {
            "The selected image is empty."
        }

        require(imageBytes.size <= 5 * 1024 * 1024) {
            "Image must not exceed 5 MB."
        }

        require(
            mimeType == "image/jpeg" ||
                    mimeType == "image/png"
        ) {
            "Only JPG and PNG images are supported."
        }

        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> throw IllegalArgumentException(
                "Unsupported image format."
            )
        }

        val imagePath =
            "${UUID.randomUUID()}.$extension"

        imageBucket.upload(
            path = imagePath,
            data = imageBytes
        ) {
            contentType = ContentType.parse(mimeType)
            upsert = false
        }

        val publicUrl =
            imageBucket.publicUrl(imagePath)

        return UploadedDestinationImage(
            path = imagePath,
            publicUrl = publicUrl
        )
    }

    // --------------------------------------------------
    // DELETE UPLOADED IMAGE BY STORAGE PATH
    // --------------------------------------------------

    suspend fun deleteUploadedDestinationImage(
        imagePath: String
    ) {

        if (imagePath.isNotBlank()) {
            imageBucket.delete(imagePath)
        }
    }

    // --------------------------------------------------
    // DELETE IMAGE USING ITS PUBLIC URL
    // --------------------------------------------------

    suspend fun deleteDestinationImage(
        imageUrl: String?
    ): Boolean {

        val marker =
            "/storage/v1/object/public/destination-images/"

        val url = imageUrl ?: return true

        val markerPosition = url.indexOf(marker)

        if (markerPosition == -1) {
            // Do not attempt to delete an external image.
            return true
        }

        val imagePath = url
            .substring(markerPosition + marker.length)
            .substringBefore("?")
            .takeIf { it.isNotBlank() }
            ?: return true

        return try {

            imageBucket.delete(imagePath)

            true

        } catch (_: Exception) {

            false
        }
    }

    // --------------------------------------------------
    // CREATE DESTINATION
    // --------------------------------------------------

    suspend fun createDestination(
        name: String,
        country: String,
        description: String,
        category: String,
        imageUrl: String,
        latitude: Double,
        longitude: Double,
        isActive: Boolean,
        adminUserId: String
    ) {

        require(adminUserId.isNotBlank()) {
            "Administrator account is required."
        }

        val destination = NewDestinationRecord(

            name = name.trim(),

            country = country.trim(),

            description = description.trim(),

            category = category,

            imageUrl = imageUrl,

            latitude = latitude,

            longitude = longitude,

            isActive = isActive,

            createdBy = adminUserId,

            updatedBy = adminUserId
        )

        supabase
            .from("destinations")
            .insert(destination)
    }

    // --------------------------------------------------
    // UPDATE DESTINATION
    // --------------------------------------------------

    suspend fun updateDestination(
        destinationId: String,
        name: String,
        country: String,
        description: String,
        category: String,
        imageUrl: String,
        latitude: Double,
        longitude: Double,
        isActive: Boolean,
        adminUserId: String
    ) {

        require(destinationId.isNotBlank()) {
            "Destination ID is required."
        }

        require(adminUserId.isNotBlank()) {
            "Administrator account is required."
        }

        supabase
            .from("destinations")
            .update({

                set("name", name.trim())

                set("country", country.trim())

                set("description", description.trim())

                set("category", category)

                set("image_url", imageUrl)

                set("latitude", latitude)

                set("longitude", longitude)

                set("is_active", isActive)

                set("updated_by", adminUserId)

            }) {
                filter {
                    eq("id", destinationId)
                }
            }
    }

    // --------------------------------------------------
    // DELETE DESTINATION
    // --------------------------------------------------

    // The database record is deleted first.
    // Then its Storage image is removed.
    // A failed image cleanup does not hide the successful
    // database deletion from the user.

    suspend fun deleteDestination(
        destinationId: String,
        imageUrl: String?
    ): Boolean {

        require(destinationId.isNotBlank()) {
            "Destination ID is required."
        }

        supabase
            .from("destinations")
            .delete {
                filter {
                    eq("id", destinationId)
                }
            }

        return deleteDestinationImage(imageUrl)
    }

    // --------------------------------------------------
    // DASHBOARD SUMMARY
    // --------------------------------------------------

    suspend fun getAdminDashboardSummary():
            DestinationDashboardSummary {

        val destinations = getAdminDestinations()

        return DestinationDashboardSummary(

            totalDestinations =
                destinations.size,

            activeDestinations =
                destinations.count { it.isActive },

            recentUpdates =
                destinations.take(2).map {

                    DestinationDashboardUpdate(
                        name = it.name,
                        updatedAt = it.updatedAt
                    )
                }
        )
    }
}