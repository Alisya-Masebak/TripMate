package com.example.tripmate.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripmate.R
import com.example.tripmate.data.AuthenticationRepository
import com.example.tripmate.data.DestinationRepository
import com.example.tripmate.data.UploadedDestinationImage
import com.example.tripmate.data.UserProfile
import com.example.tripmate.models.DestinationRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class EditDestinationActivity : AppCompatActivity() {

    private val authRepository =
        AuthenticationRepository()

    private val destinationRepository =
        DestinationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var editContent: View

    private lateinit var progressVerifyAdmin: ProgressBar
    private lateinit var progressSaveChanges: ProgressBar

    private lateinit var etEditDestinationName: EditText
    private lateinit var etEditDestinationCountry: EditText
    private lateinit var etEditDestinationDescription: EditText
    private lateinit var etEditDestinationLatitude: EditText
    private lateinit var etEditDestinationLongitude: EditText

    private lateinit var ivEditDestinationPreview: ImageView
    private lateinit var tvEditSelectedImageStatus: TextView

    private lateinit var tvEditCategoryNature: TextView
    private lateinit var tvEditCategoryCulture: TextView
    private lateinit var tvEditCategoryCity: TextView

    private lateinit var tvEditVisibilityTitle: TextView
    private lateinit var tvEditVisibilityDescription: TextView

    private lateinit var btnEditToggleVisibility: Button
    private lateinit var btnReplaceCoverImage: Button
    private lateinit var btnSaveChanges: Button

    private var adminProfile: UserProfile? = null

    private var currentDestination: DestinationRecord? = null

    private var destinationId: String = ""

    private var selectedCategory = "Nature"

    private var isActive = true

    private var isSaving = false

    private var selectedImageBytes: ByteArray? = null

    private var selectedImageMimeType: String? = null

    private var currentImageUrl: String? = null

    // --------------------------------------------------
    // IMAGE PICKER
    // --------------------------------------------------

    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                handleSelectedImage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_edit_destination
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.editDestinationRoot)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        bindViews()

        setupCategorySelection()

        setupImagePicker()

        setupVisibilityToggle()

        setupButtons()

        destinationId =
            intent.getStringExtra(EXTRA_DESTINATION_ID).orEmpty()

        if (destinationId.isBlank()) {

            Toast.makeText(
                this,
                R.string.destination_edit_load_error,
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        verifyAdministrator()
    }

    // --------------------------------------------------
    // BIND VIEWS
    // --------------------------------------------------

    private fun bindViews() {

        editContent =
            findViewById(R.id.editDestinationContent)

        progressVerifyAdmin =
            findViewById(R.id.progressEditVerifyAdmin)

        progressSaveChanges =
            findViewById(R.id.progressSaveChanges)

        etEditDestinationName =
            findViewById(R.id.etEditDestinationName)

        etEditDestinationCountry =
            findViewById(R.id.etEditDestinationCountry)

        etEditDestinationDescription =
            findViewById(R.id.etEditDestinationDescription)

        etEditDestinationLatitude =
            findViewById(R.id.etEditDestinationLatitude)

        etEditDestinationLongitude =
            findViewById(R.id.etEditDestinationLongitude)

        ivEditDestinationPreview =
            findViewById(R.id.ivEditDestinationPreview)

        tvEditSelectedImageStatus =
            findViewById(R.id.tvEditSelectedImageStatus)

        tvEditCategoryNature =
            findViewById(R.id.tvEditCategoryNature)

        tvEditCategoryCulture =
            findViewById(R.id.tvEditCategoryCulture)

        tvEditCategoryCity =
            findViewById(R.id.tvEditCategoryCity)

        tvEditVisibilityTitle =
            findViewById(R.id.tvEditVisibilityTitle)

        tvEditVisibilityDescription =
            findViewById(R.id.tvEditVisibilityDescription)

        btnEditToggleVisibility =
            findViewById(R.id.btnEditToggleVisibility)

        btnReplaceCoverImage =
            findViewById(R.id.btnReplaceCoverImage)

        btnSaveChanges =
            findViewById(R.id.btnSaveDestinationChanges)
    }

    // --------------------------------------------------
    // ADMIN VERIFICATION AND DESTINATION LOADING
    // --------------------------------------------------

    private fun verifyAdministrator() {

        activityScope.launch {

            try {

                val profile =
                    authRepository.getCurrentProfile()

                if (profile?.role != "admin") {

                    Toast.makeText(
                        this@EditDestinationActivity,
                        R.string.admin_access_required,
                        Toast.LENGTH_LONG
                    ).show()

                    openLogin()
                    return@launch
                }

                adminProfile = profile

                loadDestination()

            } catch (e: Exception) {

                Toast.makeText(
                    this@EditDestinationActivity,
                    e.message
                        ?: getString(
                            R.string.destination_edit_load_error
                        ),
                    Toast.LENGTH_LONG
                ).show()

                openLogin()
            }
        }
    }

    private suspend fun loadDestination() {

        try {

            val destination = withContext(Dispatchers.IO) {

                destinationRepository.getDestinationById(
                    destinationId
                )
            }

            currentDestination = destination

            currentImageUrl = destination.imageUrl

            populateForm(destination)

            progressVerifyAdmin.visibility = View.GONE

            editContent.visibility = View.VISIBLE

            loadExistingImage(destination.imageUrl)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                e.message
                    ?: getString(
                        R.string.destination_edit_load_error
                    ),
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }

    // --------------------------------------------------
    // PREFILL EXISTING INFORMATION
    // --------------------------------------------------

    private fun populateForm(
        destination: DestinationRecord
    ) {

        etEditDestinationName.setText(
            destination.name
        )

        etEditDestinationCountry.setText(
            destination.country
        )

        etEditDestinationDescription.setText(
            destination.description.orEmpty()
        )

        etEditDestinationLatitude.setText(
            destination.latitude?.toString().orEmpty()
        )

        etEditDestinationLongitude.setText(
            destination.longitude?.toString().orEmpty()
        )

        selectedCategory =
            destination.category

        isActive =
            destination.isActive

        updateCategoryAppearance()

        updateVisibilityAppearance()

        if (!destination.imageUrl.isNullOrBlank()) {

            tvEditSelectedImageStatus.text =
                getString(
                    R.string.destination_existing_image_kept
                )

        } else {

            tvEditSelectedImageStatus.text =
                getString(
                    R.string.destination_no_image_selected
                )
        }
    }

    // --------------------------------------------------
    // CATEGORY
    // --------------------------------------------------

    private fun setupCategorySelection() {

        tvEditCategoryNature.setOnClickListener {
            selectCategory("Nature")
        }

        tvEditCategoryCulture.setOnClickListener {
            selectCategory("Culture")
        }

        tvEditCategoryCity.setOnClickListener {
            selectCategory("City")
        }
    }

    private fun selectCategory(category: String) {

        selectedCategory = category

        updateCategoryAppearance()
    }

    private fun updateCategoryAppearance() {

        val categories = listOf(

            "Nature" to tvEditCategoryNature,

            "Culture" to tvEditCategoryCulture,

            "City" to tvEditCategoryCity
        )

        categories.forEach { (category, textView) ->

            val selected =
                selectedCategory == category

            if (selected) {

                textView.setBackgroundResource(
                    R.drawable.bg_admin_filter_selected
                )

                textView.setTextColor(
                    getColor(R.color.primary)
                )

            } else {

                textView.setBackgroundResource(
                    R.drawable.bg_admin_filter_unselected
                )

                textView.setTextColor(
                    getColor(R.color.text_primary)
                )
            }
        }
    }

    // --------------------------------------------------
    // EXISTING IMAGE PREVIEW
    // --------------------------------------------------

    private fun loadExistingImage(
        imageUrl: String?
    ) {

        if (imageUrl.isNullOrBlank()) {
            return
        }

        ivEditDestinationPreview.tag = imageUrl

        activityScope.launch {

            val bitmap = withContext(Dispatchers.IO) {

                downloadImage(imageUrl)
            }

            if (
                bitmap != null &&
                ivEditDestinationPreview.tag == imageUrl
            ) {

                ivEditDestinationPreview.setImageBitmap(
                    bitmap
                )
            }
        }
    }

    private fun downloadImage(
        imageUrl: String
    ): Bitmap? {

        var connection: HttpURLConnection? = null

        return try {

            connection =
                URL(imageUrl).openConnection() as HttpURLConnection

            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.instanceFollowRedirects = true

            connection.connect()

            if (connection.responseCode !in 200..299) {

                null

            } else {

                connection.inputStream.use { input ->

                    BitmapFactory.decodeStream(input)
                }
            }

        } catch (_: Exception) {

            null

        } finally {

            connection?.disconnect()
        }
    }

    // --------------------------------------------------
    // REPLACE IMAGE
    // --------------------------------------------------

    private fun setupImagePicker() {

        btnReplaceCoverImage.setOnClickListener {

            imagePicker.launch(
                arrayOf(
                    "image/jpeg",
                    "image/png"
                )
            )
        }
    }

    private fun handleSelectedImage(uri: Uri) {

        try {

            val mimeType =
                contentResolver.getType(uri)

            if (
                mimeType != "image/jpeg" &&
                mimeType != "image/png"
            ) {

                Toast.makeText(
                    this,
                    R.string.destination_image_format_error,
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            val bytes = readImageBytes(uri)

            if (bytes.isEmpty()) {

                Toast.makeText(
                    this,
                    R.string.destination_image_read_error,
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            val bitmap = decodePreviewImage(bytes)

            if (bitmap == null) {

                Toast.makeText(
                    this,
                    R.string.destination_image_read_error,
                    Toast.LENGTH_LONG
                ).show()

                return
            }

            selectedImageBytes = bytes

            selectedImageMimeType = mimeType

            ivEditDestinationPreview.setImageBitmap(bitmap)

            tvEditSelectedImageStatus.text =
                getString(
                    R.string.destination_new_image_selected,
                    formatFileSize(bytes.size)
                )

            btnReplaceCoverImage.text =
                getString(R.string.destination_change_image)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                e.message
                    ?: getString(
                        R.string.destination_image_read_error
                    ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun readImageBytes(uri: Uri): ByteArray {

        val input = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException(
                "Unable to open the selected image."
            )

        input.use { stream ->

            val output = ByteArrayOutputStream()

            val buffer = ByteArray(8192)

            var totalBytes = 0

            while (true) {

                val count = stream.read(buffer)

                if (count == -1) break

                totalBytes += count

                if (totalBytes > MAX_IMAGE_SIZE) {

                    throw IllegalArgumentException(
                        getString(
                            R.string.destination_image_size_error
                        )
                    )
                }

                output.write(buffer, 0, count)
            }

            return output.toByteArray()
        }
    }

    private fun decodePreviewImage(
        bytes: ByteArray
    ): Bitmap? {

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            bounds
        )

        var sampleSize = 1

        while (
            bounds.outWidth / sampleSize > 1200 ||
            bounds.outHeight / sampleSize > 900
        ) {

            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            options
        )
    }

    private fun formatFileSize(size: Int): String {

        val kb = size / 1024.0

        return String.format(
            java.util.Locale.getDefault(),
            "%.1f KB",
            kb
        )
    }

    // --------------------------------------------------
    // VISIBILITY
    // --------------------------------------------------

    private fun setupVisibilityToggle() {

        btnEditToggleVisibility.setOnClickListener {

            isActive = !isActive

            updateVisibilityAppearance()
        }
    }

    private fun updateVisibilityAppearance() {

        if (isActive) {

            tvEditVisibilityTitle.text =
                getString(R.string.visibility_active_title)

            tvEditVisibilityDescription.text =
                getString(R.string.visibility_active_description)

            btnEditToggleVisibility.text =
                getString(R.string.visibility_set_inactive)

        } else {

            tvEditVisibilityTitle.text =
                getString(R.string.visibility_inactive_title)

            tvEditVisibilityDescription.text =
                getString(R.string.visibility_inactive_description)

            btnEditToggleVisibility.text =
                getString(R.string.visibility_set_active)
        }
    }

    // --------------------------------------------------
    // BUTTONS
    // --------------------------------------------------

    private fun setupButtons() {

        findViewById<ImageButton>(
            R.id.btnEditDestinationBack
        ).setOnClickListener {
            finish()
        }

        findViewById<Button>(
            R.id.btnCancelEditDestination
        ).setOnClickListener {
            finish()
        }

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    // --------------------------------------------------
    // VALIDATION
    // --------------------------------------------------

    private fun validateForm(): Boolean {

        val name =
            etEditDestinationName.text.toString().trim()

        val country =
            etEditDestinationCountry.text.toString().trim()

        val description =
            etEditDestinationDescription.text.toString().trim()

        val latitude =
            etEditDestinationLatitude.text.toString()
                .trim()
                .toDoubleOrNull()

        val longitude =
            etEditDestinationLongitude.text.toString()
                .trim()
                .toDoubleOrNull()

        if (name.isBlank()) {

            etEditDestinationName.error =
                getString(R.string.destination_name_required)

            etEditDestinationName.requestFocus()

            return false
        }

        if (country.isBlank()) {

            etEditDestinationCountry.error =
                getString(R.string.destination_country_required)

            etEditDestinationCountry.requestFocus()

            return false
        }

        if (description.isBlank()) {

            etEditDestinationDescription.error =
                getString(R.string.destination_description_required)

            etEditDestinationDescription.requestFocus()

            return false
        }

        if (latitude == null || latitude !in -90.0..90.0) {

            etEditDestinationLatitude.error =
                getString(R.string.destination_latitude_invalid)

            etEditDestinationLatitude.requestFocus()

            return false
        }

        if (
            longitude == null ||
            longitude !in -180.0..180.0
        ) {

            etEditDestinationLongitude.error =
                getString(R.string.destination_longitude_invalid)

            etEditDestinationLongitude.requestFocus()

            return false
        }

        val hasExistingImage =
            !currentImageUrl.isNullOrBlank()

        val hasNewImage =
            selectedImageBytes != null

        if (!hasExistingImage && !hasNewImage) {

            Toast.makeText(
                this,
                R.string.destination_image_required,
                Toast.LENGTH_LONG
            ).show()

            return false
        }

        return true
    }

    // --------------------------------------------------
    // SAVE CHANGES
    // --------------------------------------------------

    private fun saveChanges() {

        if (isSaving) return

        if (!validateForm()) return

        val profile = adminProfile ?: run {

            Toast.makeText(
                this,
                R.string.admin_access_required,
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val destination = currentDestination ?: return

        val name =
            etEditDestinationName.text.toString().trim()

        val country =
            etEditDestinationCountry.text.toString().trim()

        val description =
            etEditDestinationDescription.text.toString().trim()

        val latitude =
            etEditDestinationLatitude.text.toString()
                .trim()
                .toDouble()

        val longitude =
            etEditDestinationLongitude.text.toString()
                .trim()
                .toDouble()

        val imageBytes = selectedImageBytes

        val imageMime = selectedImageMimeType

        isSaving = true

        btnSaveChanges.isEnabled = false

        btnSaveChanges.text =
            getString(R.string.saving_destination_changes)

        progressSaveChanges.visibility =
            View.VISIBLE

        activityScope.launch {

            var uploadedImage: UploadedDestinationImage? = null

            try {

                // Step 1: Upload a replacement image only
                // when the Admin has selected a new image.
                if (
                    imageBytes != null &&
                    imageMime != null
                ) {

                    uploadedImage = withContext(Dispatchers.IO) {

                        destinationRepository.uploadDestinationImage(
                            imageBytes = imageBytes,
                            mimeType = imageMime
                        )
                    }
                }

                val finalImageUrl =
                    uploadedImage?.publicUrl
                        ?: currentImageUrl.orEmpty()

                // Step 2: Update the destination record.
                withContext(Dispatchers.IO) {

                    destinationRepository.updateDestination(

                        destinationId = destination.id,

                        name = name,

                        country = country,

                        description = description,

                        category = selectedCategory,

                        imageUrl = finalImageUrl,

                        latitude = latitude,

                        longitude = longitude,

                        isActive = isActive,

                        adminUserId = profile.id
                    )
                }

                // Step 3: Delete the old image only after
                // the database successfully stores the new URL.
                var oldImageRemoved = true

                if (uploadedImage != null) {

                    oldImageRemoved = withContext(Dispatchers.IO) {

                        destinationRepository.deleteDestinationImage(
                            destination.imageUrl
                        )
                    }
                }

                Toast.makeText(
                    this@EditDestinationActivity,
                    if (oldImageRemoved) {
                        R.string.destination_edit_success
                    } else {
                        R.string.destination_edit_success_image_warning
                    },
                    Toast.LENGTH_LONG
                ).show()

                setResult(Activity.RESULT_OK)

                finish()

            } catch (e: Exception) {

                // If the record update failed, remove the
                // newly uploaded image to avoid orphaned files.
                uploadedImage?.let { image ->

                    try {

                        withContext(Dispatchers.IO) {

                            destinationRepository
                                .deleteUploadedDestinationImage(
                                    image.path
                                )
                        }

                    } catch (_: Exception) {
                        // Keep the original update error.
                    }
                }

                Toast.makeText(
                    this@EditDestinationActivity,
                    e.message
                        ?: getString(
                            R.string.destination_edit_error
                        ),
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                isSaving = false

                if (!isFinishing) {

                    btnSaveChanges.isEnabled = true

                    btnSaveChanges.text =
                        getString(R.string.save_destination_changes)

                    progressSaveChanges.visibility =
                        View.GONE
                }
            }
        }
    }

    // --------------------------------------------------
    // LOGIN
    // --------------------------------------------------

    private fun openLogin() {

        val intent = Intent(
            this,
            LoginActivity::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)

        finish()
    }

    override fun onDestroy() {

        activityScope.cancel()

        super.onDestroy()
    }

    companion object {

        const val EXTRA_DESTINATION_ID =
            "extra_destination_id"

        private const val MAX_IMAGE_SIZE =
            5 * 1024 * 1024
    }
}