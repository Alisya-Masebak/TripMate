package com.example.tripmate.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
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

    private var totalBudget = 1250.00
    private var totalSpent = 680.00

    private lateinit var tvTotalBudget: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvRemainingBudget: TextView
    private lateinit var expenseList: LinearLayout

    // Initial demonstration expense amounts.
    private val categoryTotals = mutableMapOf(
        "Flights" to 0.00,
        "Accommodation" to 250.00,
        "Private Transport" to 180.00,
        "Bullet Train" to 0.00,
        "Attractions" to 100.00,
        "Meals" to 150.00,
        "Personal Expenses" to 0.00
    )

    // Each category is connected to its corresponding XML TextView.
    private val categoryViewIds = mapOf(
        "Flights" to R.id.tvFlights,
        "Accommodation" to R.id.tvAccommodation,
        "Private Transport" to R.id.tvPrivateTransport,
        "Bullet Train" to R.id.tvBulletTrain,
        "Attractions" to R.id.tvAttractions,
        "Meals" to R.id.tvMeals,
        "Personal Expenses" to R.id.tvPersonalExpenses
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        tvTotalBudget = view.findViewById(R.id.tvTotalBudget)
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent)
        tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget)
        expenseList = view.findViewById(R.id.expenseList)

        // Important: Remove the static sample rows from the XML.
        // Expense rows will be generated dynamically by Kotlin.
        expenseList.removeAllViews()

        updateBudgetDisplay()
        updateCategoryDisplay()

        view.findViewById<Button>(R.id.btnAddExpense)
            .setOnClickListener {
                showAddExpenseDialog()
            }
    }

    private fun showAddExpenseDialog() {

        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val etExpenseName = EditText(requireContext()).apply {
            hint = "Expense name"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val etExpenseAmount = EditText(requireContext()).apply {
            hint = "Amount (RM)"
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val spinnerCategory = Spinner(requireContext())

        val categories = categoryTotals.keys.toList()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

        spinnerCategory.adapter = adapter

        dialogLayout.addView(etExpenseName)
        dialogLayout.addView(etExpenseAmount)
        dialogLayout.addView(spinnerCategory)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add New Expense")
            .setView(dialogLayout)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val expenseName =
                        etExpenseName.text.toString().trim()

                    val amountText =
                        etExpenseAmount.text.toString().trim()

                    val category =
                        spinnerCategory.selectedItem.toString()

                    if (expenseName.isEmpty()) {
                        etExpenseName.error =
                            "Please enter an expense name"
                        return@setOnClickListener
                    }

                    val amount = amountText.toDoubleOrNull()

                    if (amount == null || amount <= 0) {
                        etExpenseAmount.error =
                            "Enter a valid amount"
                        return@setOnClickListener
                    }

                    val remainingBudget = totalBudget - totalSpent

                    if (amount > remainingBudget) {
                        etExpenseAmount.error =
                            "Amount exceeds remaining budget"
                        Toast.makeText(
                            requireContext(),
                            "Insufficient remaining budget",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    addExpense(
                        expenseName,
                        amount,
                        category
                    )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    private fun addExpense(
        name: String,
        amount: Double,
        category: String
    ) {

        // Update total spending and category spending.
        totalSpent += amount

        categoryTotals[category] =
            (categoryTotals[category] ?: 0.0) + amount

        // Create a new expense row.
        val expenseRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 20, 16, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Expense name and category.
        val detailsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val tvExpenseName = TextView(requireContext()).apply {
            text = name
            textSize = 14f
            setTextColor(
                resources.getColor(
                    R.color.text_primary,
                    null
                )
            )
        }

        val tvExpenseCategory = TextView(requireContext()).apply {
            text = category
            textSize = 12f
            setTextColor(
                resources.getColor(
                    R.color.text_secondary,
                    null
                )
            )
        }

        detailsLayout.addView(tvExpenseName)
        detailsLayout.addView(tvExpenseCategory)

        // Expense amount.
        val tvExpenseAmount = TextView(requireContext()).apply {
            text = formatCurrency(amount)
            textSize = 14f
            gravity = Gravity.END
            setTextColor(
                resources.getColor(
                    R.color.text_primary,
                    null
                )
            )
        }

        expenseRow.addView(detailsLayout)
        expenseRow.addView(tvExpenseAmount)

        // Insert the latest expense at the top.
        expenseList.addView(expenseRow, 0)

        // Add a divider below the new expense.
        val divider = View(requireContext()).apply {
            setBackgroundColor(
                resources.getColor(
                    R.color.background,
                    null
                )
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
        }

        expenseList.addView(divider, 1)

        updateBudgetDisplay()
        updateCategoryDisplay()

        Toast.makeText(
            requireContext(),
            "Expense added successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateBudgetDisplay() {

        val remaining = totalBudget - totalSpent

        tvTotalBudget.text = formatCurrency(totalBudget)
        tvTotalSpent.text = formatCurrency(totalSpent)
        tvRemainingBudget.text = formatCurrency(remaining)
    }

    private fun updateCategoryDisplay() {

        categoryViewIds.forEach { (category, viewId) ->

            val categoryTextView =
                view?.findViewById<TextView>(viewId)

            categoryTextView?.text = formatCurrency(
                categoryTotals[category] ?: 0.0
            )
        }
    }

    private fun formatCurrency(amount: Double): String {

        return NumberFormat.getCurrencyInstance(
            Locale("en", "MY")
        ).format(amount)
    }
}
