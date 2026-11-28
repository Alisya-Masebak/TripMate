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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AddDestinationActivity : AppCompatActivity() {

    private val authRepository =
        AuthenticationRepository()

    private val destinationRepository =
        DestinationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var formContent: View

    private lateinit var progressVerifyAdmin: ProgressBar
    private lateinit var progressSaveDestination: ProgressBar

    private lateinit var etDestinationName: EditText
    private lateinit var etDestinationCountry: EditText
    private lateinit var etDestinationDescription: EditText
    private lateinit var etDestinationLatitude: EditText
    private lateinit var etDestinationLongitude: EditText

    private lateinit var ivDestinationPreview: ImageView
    private lateinit var tvSelectedImageStatus: TextView

    private lateinit var tvCategoryNature: TextView
    private lateinit var tvCategoryCulture: TextView
    private lateinit var tvCategoryCity: TextView

    private lateinit var tvVisibilityTitle: TextView
    private lateinit var tvVisibilityDescription: TextView

    private lateinit var btnToggleVisibility: Button
    private lateinit var btnChooseCoverImage: Button
    private lateinit var btnSaveDestination: Button

    private var adminProfile: UserProfile? = null

    private var selectedCategory = "Nature"

    private var isActive = true

    private var isSaving = false

    private var selectedImageBytes: ByteArray? = null

    private var selectedImageMimeType: String? = null

    private var selectedImageUri: Uri? = null

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
            R.layout.activity_add_destination
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.addDestinationRoot)
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

        updateVisibilityAppearance()

        updateCategoryAppearance()

        verifyAdministrator()
    }

    // --------------------------------------------------
    // BIND VIEWS
    // --------------------------------------------------

    private fun bindViews() {

        formContent =
            findViewById(R.id.addDestinationContent)

        progressVerifyAdmin =
            findViewById(R.id.progressVerifyAdmin)

        progressSaveDestination =
            findViewById(R.id.progressSaveDestination)

        etDestinationName =
            findViewById(R.id.etDestinationName)

        etDestinationCountry =
            findViewById(R.id.etDestinationCountry)

        etDestinationDescription =
            findViewById(R.id.etDestinationDescription)

        etDestinationLatitude =
            findViewById(R.id.etDestinationLatitude)

        etDestinationLongitude =
            findViewById(R.id.etDestinationLongitude)

        ivDestinationPreview =
            findViewById(R.id.ivDestinationPreview)

        tvSelectedImageStatus =
            findViewById(R.id.tvSelectedImageStatus)

        tvCategoryNature =
            findViewById(R.id.tvCategoryNature)

        tvCategoryCulture =
            findViewById(R.id.tvCategoryCulture)

        tvCategoryCity =
            findViewById(R.id.tvCategoryCity)

        tvVisibilityTitle =
            findViewById(R.id.tvVisibilityTitle)

        tvVisibilityDescription =
            findViewById(R.id.tvVisibilityDescription)

        btnToggleVisibility =
            findViewById(R.id.btnToggleVisibility)

        btnChooseCoverImage =
            findViewById(R.id.btnChooseCoverImage)

        btnSaveDestination =
            findViewById(R.id.btnSaveDestination)
    }

    // --------------------------------------------------
    // VERIFY ADMIN
    // --------------------------------------------------

    private fun verifyAdministrator() {

        activityScope.launch {

            try {

                val profile =
                    authRepository.getCurrentProfile()

                if (profile?.role != "admin") {

                    Toast.makeText(
                        this@AddDestinationActivity,
                        R.string.admin_access_required,
                        Toast.LENGTH_LONG
                    ).show()

                    openLogin()

                    return@launch
                }

                adminProfile = profile

                progressVerifyAdmin.visibility =
                    View.GONE

                formContent.visibility =
                    View.VISIBLE

            } catch (e: Exception) {

                Toast.makeText(
                    this@AddDestinationActivity,
                    e.message
                        ?: getString(
                            R.string.admin_verification_failed
                        ),
                    Toast.LENGTH_LONG
                ).show()

                openLogin()
            }
        }
    }

    // --------------------------------------------------
    // CATEGORY SELECTION
    // --------------------------------------------------

    private fun setupCategorySelection() {

        tvCategoryNature.setOnClickListener {
            selectCategory("Nature")
        }

        tvCategoryCulture.setOnClickListener {
            selectCategory("Culture")
        }

        tvCategoryCity.setOnClickListener {
            selectCategory("City")
        }
    }

    private fun selectCategory(category: String) {

        selectedCategory = category

        updateCategoryAppearance()
    }

    private fun updateCategoryAppearance() {

        val categories = listOf(

            "Nature" to tvCategoryNature,

            "Culture" to tvCategoryCulture,

            "City" to tvCategoryCity
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
    // IMAGE PICKER
    // --------------------------------------------------

    private fun setupImagePicker() {

        btnChooseCoverImage.setOnClickListener {

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

            if (bytes.size > MAX_IMAGE_SIZE) {

                Toast.makeText(
                    this,
                    R.string.destination_image_size_error,
                    Toast.LENGTH_LONG
                ).show()

                return
            }

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
            selectedImageUri = uri

            ivDestinationPreview.setImageBitmap(bitmap)

            tvSelectedImageStatus.text =
                getString(
                    R.string.destination_image_selected,
                    formatFileSize(bytes.size)
                )

            btnChooseCoverImage.text =
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

        btnToggleVisibility.setOnClickListener {

            isActive = !isActive

            updateVisibilityAppearance()
        }
    }

    private fun updateVisibilityAppearance() {

        if (isActive) {

            tvVisibilityTitle.text =
                getString(R.string.visibility_active_title)

            tvVisibilityDescription.text =
                getString(R.string.visibility_active_description)

            btnToggleVisibility.text =
                getString(R.string.visibility_set_inactive)

        } else {

            tvVisibilityTitle.text =
                getString(R.string.visibility_inactive_title)

            tvVisibilityDescription.text =
                getString(R.string.visibility_inactive_description)

            btnToggleVisibility.text =
                getString(R.string.visibility_set_active)
        }
    }

    // --------------------------------------------------
    // BUTTONS
    // --------------------------------------------------

    private fun setupButtons() {

        findViewById<ImageButton>(
            R.id.btnAddDestinationBack
        ).setOnClickListener {

            finish()
        }

        findViewById<Button>(
            R.id.btnCancelDestination
        ).setOnClickListener {

            finish()
        }

        btnSaveDestination.setOnClickListener {

            saveDestination()
        }
    }

    // --------------------------------------------------
    // FORM VALIDATION
    // --------------------------------------------------

    private fun validateForm(): Boolean {

        val name =
            etDestinationName.text.toString().trim()

        val country =
            etDestinationCountry.text.toString().trim()

        val description =
            etDestinationDescription.text.toString().trim()

        val latitudeText =
            etDestinationLatitude.text.toString().trim()

        val longitudeText =
            etDestinationLongitude.text.toString().trim()

        if (name.isBlank()) {

            etDestinationName.error =
                getString(R.string.destination_name_required)

            etDestinationName.requestFocus()

            return false
        }

        if (country.isBlank()) {

            etDestinationCountry.error =
                getString(R.string.destination_country_required)

            etDestinationCountry.requestFocus()

            return false
        }

        if (description.isBlank()) {

            etDestinationDescription.error =
                getString(R.string.destination_description_required)

            etDestinationDescription.requestFocus()

            return false
        }

        if (selectedImageBytes == null) {

            Toast.makeText(
                this,
                R.string.destination_image_required,
                Toast.LENGTH_LONG
            ).show()

            return false
        }

        val latitude =
            latitudeText.toDoubleOrNull()

        if (latitude == null || latitude !in -90.0..90.0) {

            etDestinationLatitude.error =
                getString(R.string.destination_latitude_invalid)

            etDestinationLatitude.requestFocus()

            return false
        }

        val longitude =
            longitudeText.toDoubleOrNull()

        if (
            longitude == null ||
            longitude !in -180.0..180.0
        ) {

            etDestinationLongitude.error =
                getString(R.string.destination_longitude_invalid)

            etDestinationLongitude.requestFocus()

            return false
        }

        return true
    }

    // --------------------------------------------------
    // SAVE DESTINATION
    // --------------------------------------------------

    private fun saveDestination() {

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

        val name =
            etDestinationName.text.toString().trim()

        val country =
            etDestinationCountry.text.toString().trim()

        val description =
            etDestinationDescription.text.toString().trim()

        val latitude =
            etDestinationLatitude.text.toString().trim().toDouble()

        val longitude =
            etDestinationLongitude.text.toString().trim().toDouble()

        val imageBytes =
            selectedImageBytes ?: return

        val imageMime =
            selectedImageMimeType ?: return

        isSaving = true

        btnSaveDestination.isEnabled = false

        btnSaveDestination.text =
            getString(R.string.saving_destination)

        progressSaveDestination.visibility =
            View.VISIBLE

        activityScope.launch {

            var uploadedImage: UploadedDestinationImage? = null

            try {

                // Step 1: Upload image to Supabase Storage.
                val uploaded = withContext(Dispatchers.IO) {

                    destinationRepository.uploadDestinationImage(
                        imageBytes = imageBytes,
                        mimeType = imageMime
                    )
                }

                uploadedImage = uploaded

                // Step 2: Insert destination record.
                withContext(Dispatchers.IO) {

                    destinationRepository.createDestination(

                        name = name,

                        country = country,

                        description = description,

                        category = selectedCategory,

                        imageUrl = uploaded.publicUrl,

                        latitude = latitude,

                        longitude = longitude,

                        isActive = isActive,

                        adminUserId = profile.id
                    )
                }

                Toast.makeText(
                    this@AddDestinationActivity,
                    R.string.destination_save_success,
                    Toast.LENGTH_LONG
                ).show()

                setResult(Activity.RESULT_OK)

                finish()

            } catch (e: Exception) {

                // Remove the uploaded file if the database
                // insert fails, to avoid leaving unused images.
                uploadedImage?.let { image ->

                    try {

                        withContext(Dispatchers.IO) {

                            destinationRepository
                                .deleteUploadedDestinationImage(
                                    image.path
                                )
                        }

                    } catch (_: Exception) {
                        // Preserve the original save error.
                    }
                }

                Toast.makeText(
                    this@AddDestinationActivity,
                    e.message
                        ?: getString(
                            R.string.destination_save_error
                        ),
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                isSaving = false

                if (!isFinishing) {

                    btnSaveDestination.isEnabled = true

                    btnSaveDestination.text =
                        getString(R.string.save_destination)

                    progressSaveDestination.visibility =
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

        private const val MAX_IMAGE_SIZE =
            5 * 1024 * 1024
    }
}