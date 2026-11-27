package com.example.tripmate.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tripmate.R

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private var selectedCategory = "All"
    private var searchText = ""

    private data class Destination(
        val cardId: Int,
        val name: String,
        val category: String
    )

    private val destinations = listOf(
        Destination(R.id.cardKyoto, "Kyoto", "Culture"),
        Destination(R.id.cardOsaka, "Osaka", "City"),
        Destination(R.id.cardTokyo, "Tokyo", "City"),
        Destination(R.id.cardNara, "Nara", "Nature"),
        Destination(R.id.cardMountFuji, "Mount Fuji", "Nature"),
        Destination(R.id.cardFushimi, "Fushimi Inari", "Culture")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.etSearchDestinations)

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                searchText = s?.toString()?.trim() ?: ""
                updateDestinationVisibility(view)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val categoryIds = listOf(
            R.id.categoryAll to "All",
            R.id.categoryNature to "Nature",
            R.id.categoryCulture to "Culture",
            R.id.categoryCity to "City"
        )

        categoryIds.forEach { (id, category) ->
            view.findViewById<TextView>(id).setOnClickListener {
                selectedCategory = category
                updateCategoryAppearance(view)
                updateDestinationVisibility(view)
            }
        }

        updateCategoryAppearance(view)
        updateDestinationVisibility(view)
    }

    private fun updateCategoryAppearance(view: View) {
        val categories = listOf(
            R.id.categoryAll to "All",
            R.id.categoryNature to "Nature",
            R.id.categoryCulture to "Culture",
            R.id.categoryCity to "City"
        )

        categories.forEach { (id, category) ->
            val textView = view.findViewById<TextView>(id)
            val selected = selectedCategory == category

            textView.setBackgroundResource(
                if (selected) R.drawable.bg_filter_selected
                else R.drawable.bg_filter_unselected
            )

            textView.setTextColor(
                requireContext().getColor(
                    if (selected) R.color.white
                    else R.color.tripmate_teal
                )
            )
        }
    }

    private fun updateDestinationVisibility(view: View) {
        var visibleCount = 0

        destinations.forEach { destination ->
            val card = view.findViewById<LinearLayout>(destination.cardId)

            val matchesSearch = destination.name.contains(
                searchText,
                ignoreCase = true
            )

            val matchesCategory =
                selectedCategory == "All" ||
                        destination.category == selectedCategory

            val visible = matchesSearch && matchesCategory

            card.visibility = if (visible) View.VISIBLE else View.GONE

            if (visible) visibleCount++
        }

        view.findViewById<TextView>(R.id.tvNoDestinations).visibility =
            if (visibleCount == 0) View.VISIBLE else View.GONE
    }
}
