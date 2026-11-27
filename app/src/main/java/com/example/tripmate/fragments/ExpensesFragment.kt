package com.example.tripmate.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tripmate.R
import java.text.NumberFormat
import java.util.Locale

class ExpensesFragment : Fragment(R.layout.fragment_expenses) {

    private val totalBudget = 1250.00
    private var totalSpent = 680.00

    private lateinit var tvTotalSpent: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var expenseList: LinearLayout

    private val categoryTotals = mutableMapOf(
        "Accommodation" to 250.00,
        "Transport" to 180.00,
        "Food" to 150.00,
        "Activities" to 100.00
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTotalSpent = view.findViewById(R.id.tvTotalSpent)
        tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget)
        expenseList = view.findViewById(R.id.expenseList)

        updateBudgetDisplay()

        view.findViewById<Button>(R.id.btnAddExpense)
            .setOnClickListener {
                showAddExpenseDialog()
            }
    }

    private fun showAddExpenseDialog() {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val nameInput = EditText(requireContext()).apply {
            hint = "Expense name"
            textSize = 14f
        }

        val amountInput = EditText(requireContext()).apply {
            hint = "Amount (RM)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            textSize = 14f
        }

        val categorySpinner = Spinner(requireContext())
        val categories = listOf(
            "Accommodation",
            "Transport",
            "Food",
            "Activities"
        )

        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        dialogLayout.addView(nameInput)
        dialogLayout.addView(amountInput)
        dialogLayout.addView(categorySpinner)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Expense")
            .setView(dialogLayout)
            .setPositiveButton("Add") { _, _ ->

                val name = nameInput.text.toString().trim()
                val amount = amountInput.text.toString().toDoubleOrNull()
                val category = categorySpinner.selectedItem.toString()

                if (name.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter an expense name.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (amount == null || amount <= 0) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid amount.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (totalSpent + amount > totalBudget) {
                    Toast.makeText(
                        requireContext(),
                        "This expense exceeds your remaining budget.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                addExpense(name, amount, category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addExpense(
        name: String,
        amount: Double,
        category: String
    ) {
        totalSpent += amount

        categoryTotals[category] =
            (categoryTotals[category] ?: 0.0) + amount

        val expenseRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(
                resources.getColor(R.color.surface, null)
            )
        }

        val detailsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val nameText = TextView(requireContext()).apply {
            text = name
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val categoryText = TextView(requireContext()).apply {
            text = "$category • Today"
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            setPadding(0, 6, 0, 0)
        }

        val amountText = TextView(requireContext()).apply {
            text = "- ${formatCurrency(amount)}"
            textSize = 14f
            setTextColor(resources.getColor(R.color.primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        detailsLayout.addView(nameText)
        detailsLayout.addView(categoryText)

        expenseRow.addView(detailsLayout)
        expenseRow.addView(amountText)

        val divider = View(requireContext()).apply {
            setBackgroundColor(
                resources.getColor(R.color.background, null)
            )
        }

        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )

        expenseList.addView(expenseRow, 0)
        expenseList.addView(divider, 1, dividerParams)

        updateBudgetDisplay()
        updateCategoryDisplay()
    }

    private fun updateBudgetDisplay() {
        val remaining = totalBudget - totalSpent

        view?.findViewById<TextView>(R.id.tvTotalBudget)?.text =
            formatCurrency(totalBudget)

        tvTotalSpent.text = formatCurrency(totalSpent)
        tvRemainingBudget.text = formatCurrency(remaining)
    }

    private fun updateCategoryDisplay() {
        view?.findViewById<TextView>(R.id.tvAccommodation)?.text =
            formatCurrency(categoryTotals["Accommodation"] ?: 0.0)

        view?.findViewById<TextView>(R.id.tvTransport)?.text =
            formatCurrency(categoryTotals["Transport"] ?: 0.0)

        view?.findViewById<TextView>(R.id.tvFood)?.text =
            formatCurrency(categoryTotals["Food"] ?: 0.0)

        view?.findViewById<TextView>(R.id.tvActivities)?.text =
            formatCurrency(categoryTotals["Activities"] ?: 0.0)
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(
            Locale("en", "MY")
        )
        return formatter.format(amount)
    }
}
