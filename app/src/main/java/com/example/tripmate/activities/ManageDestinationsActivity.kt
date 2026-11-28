package com.example.tripmate.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tripmate.R
import com.example.tripmate.data.AuthenticationRepository
import com.example.tripmate.data.DestinationRepository
import com.example.tripmate.data.UserProfile
import com.example.tripmate.models.DestinationRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class ManageDestinationsActivity : AppCompatActivity() {

    private val authRepository =
        AuthenticationRepository()

    private val destinationRepository =
        DestinationRepository()

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var etDestinationSearch: EditText

    private lateinit var tvDestinationCount: TextView

    private lateinit var tvDestinationEmptyState: TextView

    private lateinit var tvAllDestinationsShown: TextView

    private lateinit var destinationContainer: LinearLayout

    private lateinit var progressDestinations: ProgressBar

    private lateinit var filterAll: TextView
    private lateinit var filterNature: TextView
    private lateinit var filterCulture: TextView
    private lateinit var filterCity: TextView

    private var allDestinations =
        emptyList<DestinationRecord>()

    private var selectedCategory = "All"

    private var currentSearchQuery = ""

    private var adminProfile: UserProfile? = null

    private var adminEmail: String? = null

    private var hasVerifiedAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_manage_destinations
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.manageDestinationsRoot)
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

        setupSearch()

        setupCategoryFilters()

        setupNavigation()

        setupButtons()

        verifyAdministrator()
    }

    private fun bindViews() {

        etDestinationSearch =
            findViewById(R.id.etDestinationSearch)

        tvDestinationCount =
            findViewById(R.id.tvDestinationCount)

        tvDestinationEmptyState =
            findViewById(R.id.tvDestinationEmptyState)

        tvAllDestinationsShown =
            findViewById(R.id.tvAllDestinationsShown)

        destinationContainer =
            findViewById(R.id.destinationContainer)

        progressDestinations =
            findViewById(R.id.progressDestinations)

        filterAll = findViewById(R.id.filterAll)

        filterNature = findViewById(R.id.filterNature)

        filterCulture = findViewById(R.id.filterCulture)

        filterCity = findViewById(R.id.filterCity)
    }

    // --------------------------------------------------
    // ADMIN VERIFICATION
    // --------------------------------------------------

    private fun verifyAdministrator() {

        activityScope.launch {

            try {

                val profile =
                    authRepository.getCurrentProfile()

                if (profile?.role != "admin") {

                    Toast.makeText(
                        this@ManageDestinationsActivity,
                        R.string.admin_access_required,
                        Toast.LENGTH_LONG
                    ).show()

                    openLogin()

                    return@launch
                }

                adminProfile = profile

                adminEmail =
                    authRepository.getCurrentUserEmail()

                hasVerifiedAdmin = true

                loadDestinations()

            } catch (e: Exception) {

                Toast.makeText(
                    this@ManageDestinationsActivity,
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

    // Refresh destination records when returning
    // to this activity.
    override fun onResume() {

        super.onResume()

        if (hasVerifiedAdmin) {

            activityScope.launch {
                loadDestinations()
            }
        }
    }

    // --------------------------------------------------
    // LOAD DESTINATIONS
    // --------------------------------------------------

    private suspend fun loadDestinations() {

        progressDestinations.visibility = View.VISIBLE

        try {

            allDestinations =
                destinationRepository.getAdminDestinations()

            renderDestinations()

        } catch (e: Exception) {

            allDestinations = emptyList()

            destinationContainer.removeAllViews()

            tvDestinationCount.text =
                getString(R.string.destination_count_unavailable)

            tvDestinationEmptyState.text =
                getString(R.string.destination_list_load_error)

            tvDestinationEmptyState.visibility =
                View.VISIBLE

            tvAllDestinationsShown.visibility =
                View.GONE

            Toast.makeText(
                this,
                e.message
                    ?: getString(
                        R.string.destination_list_load_error
                    ),
                Toast.LENGTH_LONG
            ).show()

        } finally {

            progressDestinations.visibility = View.GONE
        }
    }

    // --------------------------------------------------
    // SEARCH
    // --------------------------------------------------

    private fun setupSearch() {

        etDestinationSearch.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Not required.
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    currentSearchQuery =
                        s?.toString()?.trim().orEmpty()

                    renderDestinations()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                    // Not required.
                }
            }
        )
    }

    // --------------------------------------------------
    // CATEGORY FILTERS
    // --------------------------------------------------

    private fun setupCategoryFilters() {

        filterAll.setOnClickListener {
            selectCategory("All")
        }

        filterNature.setOnClickListener {
            selectCategory("Nature")
        }

        filterCulture.setOnClickListener {
            selectCategory("Culture")
        }

        filterCity.setOnClickListener {
            selectCategory("City")
        }

        updateCategoryFilterAppearance()
    }

    private fun selectCategory(category: String) {

        selectedCategory = category

        updateCategoryFilterAppearance()

        renderDestinations()
    }

    private fun updateCategoryFilterAppearance() {

        val filters = listOf(

            "All" to filterAll,

            "Nature" to filterNature,

            "Culture" to filterCulture,

            "City" to filterCity
        )

        filters.forEach { (category, view) ->

            val selected =
                selectedCategory == category

            if (selected) {

                view.setBackgroundResource(
                    R.drawable.bg_admin_filter_selected
                )

                view.setTextColor(
                    getColor(R.color.primary)
                )

            } else {

                view.setBackgroundResource(
                    R.drawable.bg_admin_filter_unselected
                )

                view.setTextColor(
                    getColor(R.color.text_primary)
                )
            }
        }
    }

    // --------------------------------------------------
    // RENDER DESTINATION LIST
    // --------------------------------------------------

    private fun renderDestinations() {

        destinationContainer.removeAllViews()

        val total = allDestinations.size

        val active = allDestinations.count {
            it.isActive
        }

        tvDestinationCount.text = getString(
            R.string.destination_count_format,
            total,
            active
        )

        val filteredDestinations =
            allDestinations.filter { destination ->

                val categoryMatches =
                    selectedCategory == "All" ||
                            destination.category.equals(
                                selectedCategory,
                                ignoreCase = true
                            )

                val searchMatches =
                    currentSearchQuery.isBlank() ||
                            destination.name.contains(
                                currentSearchQuery,
                                ignoreCase = true
                            ) ||
                            destination.country.contains(
                                currentSearchQuery,
                                ignoreCase = true
                            )

                categoryMatches && searchMatches
            }

        if (filteredDestinations.isEmpty()) {

            tvAllDestinationsShown.visibility =
                View.GONE

            tvDestinationEmptyState.text =
                if (allDestinations.isEmpty()) {

                    getString(
                        R.string.destination_empty_message
                    )

                } else {

                    getString(
                        R.string.destination_search_empty
                    )
                }

            tvDestinationEmptyState.visibility =
                View.VISIBLE

            return
        }

        tvDestinationEmptyState.visibility =
            View.GONE

        tvAllDestinationsShown.visibility =
            View.VISIBLE

        filteredDestinations.forEach { destination ->

            addDestinationCard(destination)
        }
    }

    private fun addDestinationCard(
        destination: DestinationRecord
    ) {

        val itemView = LayoutInflater.from(this)
            .inflate(
                R.layout.item_destination,
                destinationContainer,
                false
            )

        val imageView =
            itemView.findViewById<ImageView>(
                R.id.ivDestinationImage
            )

        val tvName =
            itemView.findViewById<TextView>(
                R.id.tvDestinationName
            )

        val tvLocation =
            itemView.findViewById<TextView>(
                R.id.tvDestinationLocation
            )

        val tvStatus =
            itemView.findViewById<TextView>(
                R.id.tvDestinationStatus
            )

        val btnEdit =
            itemView.findViewById<Button>(
                R.id.btnEditDestination
            )

        val btnDelete =
            itemView.findViewById<Button>(
                R.id.btnDeleteDestination
            )

        tvName.text = destination.name

        tvLocation.text = getString(
            R.string.destination_country_category,
            destination.country,
            destination.category
        )

        if (destination.isActive) {

            tvStatus.text =
                getString(R.string.destination_status_active)

            tvStatus.setTextColor(
                getColor(R.color.primary)
            )

        } else {

            tvStatus.text =
                getString(R.string.destination_status_inactive)

            tvStatus.setTextColor(
                getColor(R.color.text_secondary)
            )
        }

        loadDestinationImage(
            imageView,
            destination
        )

        // Open destination details.
        itemView.findViewById<View>(
            R.id.destinationPreviewCard
        ).setOnClickListener {

            showDestinationDetails(destination)
        }

        // Edit will be connected to the Edit screen
        // in the next development increment.
        btnEdit.setOnClickListener {

            Toast.makeText(
                this,
                R.string.destination_edit_next,
                Toast.LENGTH_SHORT
            ).show()
        }

        // Delete destination.
        btnDelete.setOnClickListener {

            confirmDeleteDestination(destination)
        }

        destinationContainer.addView(itemView)
    }

    // --------------------------------------------------
    // DESTINATION IMAGE
    // --------------------------------------------------

    private fun loadDestinationImage(
        imageView: ImageView,
        destination: DestinationRecord
    ) {

        imageView.setImageResource(
            getFallbackDestinationImage(destination.name)
        )

        val imageUrl = destination.imageUrl
            ?.trim()
            ?.takeIf {
                it.startsWith("https://", ignoreCase = true)
            }
            ?: return

        imageView.tag = imageUrl

        activityScope.launch {

            val bitmap = withContext(Dispatchers.IO) {

                downloadImage(imageUrl)
            }

            if (
                bitmap != null &&
                imageView.tag == imageUrl
            ) {

                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun downloadImage(url: String): Bitmap? {

        var connection: HttpURLConnection? = null

        return try {

            connection =
                URL(url).openConnection() as HttpURLConnection

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

    private fun getFallbackDestinationImage(
        name: String
    ): Int {

        val value = name.lowercase(Locale.ROOT)

        return when {

            "fuji" in value ->
                R.drawable.destination_mount_fuji

            "kyoto" in value ->
                R.drawable.destination_kyoto

            "tokyo" in value ->
                R.drawable.destination_tokyo

            "nara" in value ->
                R.drawable.destination_nara

            "osaka" in value ->
                R.drawable.destination_osaka

            "fushimi" in value ->
                R.drawable.destination_fushimi_inari

            else ->
                R.drawable.bg_image_placeholder
        }
    }

    // --------------------------------------------------
    // DESTINATION DETAILS
    // --------------------------------------------------

    private fun showDestinationDetails(
        destination: DestinationRecord
    ) {

        val status = if (destination.isActive) {
            getString(R.string.destination_status_active)
        } else {
            getString(R.string.destination_status_inactive)
        }

        val description =
            destination.description
                ?.takeIf { it.isNotBlank() }
                ?: getString(
                    R.string.destination_description_unavailable
                )

        val message = buildString {

            append(destination.country)

            append(" · ")

            append(destination.category)

            append("\n\n")

            append(description)

            append("\n\n")

            append(status)
        }

        AlertDialog.Builder(this)
            .setTitle(destination.name)
            .setMessage(message)
            .setPositiveButton(
                R.string.close,
                null
            )
            .show()
    }

    // --------------------------------------------------
    // DELETE DESTINATION
    // --------------------------------------------------

    private fun confirmDeleteDestination(
        destination: DestinationRecord
    ) {

        AlertDialog.Builder(this)
            .setTitle(R.string.destination_delete_title)
            .setMessage(
                getString(
                    R.string.destination_delete_message,
                    destination.name
                )
            )
            .setNegativeButton(
                R.string.cancel,
                null
            )
            .setPositiveButton(
                R.string.destination_action_delete
            ) { _, _ ->

                deleteDestination(destination)
            }
            .show()
    }

    private fun deleteDestination(
        destination: DestinationRecord
    ) {

        activityScope.launch {

            try {

                destinationRepository.deleteDestination(
                    destination.id
                )

                Toast.makeText(
                    this@ManageDestinationsActivity,
                    R.string.destination_delete_success,
                    Toast.LENGTH_SHORT
                ).show()

                loadDestinations()

            } catch (e: Exception) {

                val message = e.message.orEmpty()

                val displayMessage = if (
                    message.contains(
                        "foreign key",
                        ignoreCase = true
                    ) ||
                    message.contains(
                        "violates",
                        ignoreCase = true
                    )
                ) {

                    getString(
                        R.string.destination_delete_linked_error
                    )

                } else {

                    e.message
                        ?: getString(
                            R.string.destination_delete_error
                        )
                }

                Toast.makeText(
                    this@ManageDestinationsActivity,
                    displayMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // --------------------------------------------------
    // NAVIGATION
    // --------------------------------------------------

    private fun setupNavigation() {

        findViewById<ImageButton>(R.id.btnManageBack)
            .setOnClickListener {
                finish()
            }

        findViewById<LinearLayout>(R.id.navManageDashboard)
            .setOnClickListener {
                finish()
            }

        findViewById<LinearLayout>(R.id.navManageDestinations)
            .setOnClickListener {
                // Already on this screen.
            }

        findViewById<LinearLayout>(R.id.navManageAccount)
            .setOnClickListener {
                showAccountOptions()
            }
    }

    private fun setupButtons() {

        findViewById<Button>(R.id.btnAddNewDestination)
            .setOnClickListener {

                val intent = Intent(
                    this,
                    AddDestinationActivity::class.java
                )

                startActivity(intent)
            }
    }

    private fun showAccountOptions() {

        val profile = adminProfile ?: return

        val name = profile.fullName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.admin_fallback_name)

        val email = adminEmail
            ?.takeIf { it.isNotBlank() }
            ?: getString(R.string.admin_email_unavailable)

        AlertDialog.Builder(this)
            .setTitle(R.string.admin_account_title)
            .setMessage(
                "$name\n$email\n\n" +
                        getString(R.string.admin_role_label)
            )
            .setPositiveButton(
                R.string.sign_out
            ) { _, _ ->

                signOut()
            }
            .setNegativeButton(
                R.string.cancel,
                null
            )
            .show()
    }

    private fun signOut() {

        activityScope.launch {

            try {

                authRepository.signOut()

                openLogin()

            } catch (e: Exception) {

                Toast.makeText(
                    this@ManageDestinationsActivity,
                    e.message
                        ?: getString(
                            R.string.admin_sign_out_failed
                        ),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

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
}