package com.example.johnathan2

import androidx.activity.ComponentActivity

import android.os.Bundle
import android.widget.Button
import android.content.res.ColorStateList
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.text.TextWatcher
import android.text.Editable

import android.util.Log
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.math.round

class MainActivity : ComponentActivity(), ItemAdapter.OnItemClickListener {

    private lateinit var titleTextView: TextView
    private lateinit var priceEditText: EditText
    private lateinit var woodEditText: EditText
    private lateinit var itemNameEditText: EditText
    private lateinit var totalSqMetersEditText: EditText

    private lateinit var item: Item
    private var itemList: MutableList<Item> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    //private lateinit var appBarConfiguration: AppBarConfiguration
    //private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Get itemList from storage
        sharedPreferencesManager = SharedPreferencesManager(this)
        itemList = sharedPreferencesManager.loadItemList()

        titleTextView = findViewById(R.id.textViewTitle)
        priceEditText = findViewById(R.id.editTextPrice)
        woodEditText = findViewById(R.id.editTextWood)
        itemNameEditText = findViewById(R.id.editItemName)
        totalSqMetersEditText = findViewById(R.id.editTextTotalSq)

        val calculateButton: Button = findViewById(R.id.buttonCalculate)
        // Check form validity and set button state
        calculateButton.isEnabled = isFormValid()

        val changeButton: Button = findViewById(R.id.buttonChange)

        val resultTextView: TextView = findViewById(R.id.textViewResult)
        val totalTextView: TextView = findViewById(R.id.textViewTotal)

        var totaltotal: String = calculateTotalTotal(itemList)
        totalTextView.text = totaltotal

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.isVerticalScrollBarEnabled = true

        // Define the OnItemClickListener
        val onItemClickListener = object : ItemAdapter.OnItemClickListener {
            override fun onItemClick(item: Item) {
                priceEditText.setText(item.priceInMeters.toString())
                woodEditText.setText(item.width.toString())
                itemNameEditText.setText(item.itemName)
                totalSqMetersEditText.setText(item.totalSqMeters.toString())
                // Set the result in the TextView
                resultTextView.text =
                    "Result: ${"%.2f".format(item.priceInMeters / (item.width / 100))} DKK\nTotal: ${"%.2f".format(item.totalPrice)} DKK"
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No action needed here
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // No action needed here
            }

            override fun afterTextChanged(s: Editable) {
                calculateButton.isEnabled = isFormValid()
                if (isFormValid()) {
                    calculateButton.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                } else {
                    calculateButton.backgroundTintList = null
                }
            }
        }

        priceEditText.addTextChangedListener(textWatcher)
        woodEditText.addTextChangedListener(textWatcher)
        itemNameEditText.addTextChangedListener(textWatcher)
        totalSqMetersEditText.addTextChangedListener(textWatcher)

        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter = ItemAdapter(itemList, onItemClickListener)

        // Add the ItemTouchHelper code here
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // we are not implementing move functionality here
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                itemList.removeAt(position)
                recyclerView.adapter?.notifyItemRemoved(position)

                // Save the updated list
                sharedPreferencesManager.saveItemList(itemList)

                totaltotal = calculateTotalTotal(itemList)
                totalTextView.text = totaltotal
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    if (dX < 0) { // swipe left
                        val paint = Paint()
                        paint.color = Color.RED
                        c.drawRect(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat(),
                            paint
                        )

                        val textPaint = Paint()
                        textPaint.color = Color.WHITE
                        textPaint.textSize = 55f
                        val text = "Delete"
                        val textX = itemView.right.toFloat() - textPaint.measureText(text) - 30

                        // Calculate the vertical position to center the text
                        val textY = itemView.top.toFloat() + (itemView.height + textPaint.textSize) / 2 - 8

                        resultTextView.gravity = Gravity.CENTER_VERTICAL
                        c.drawText(text, textX, textY, textPaint)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        Log.d("MainActivity", "Initial adapter item count: ${itemList.size}")

        calculateButton.setOnClickListener {
            // Get the values entered by the user
            val priceText: String = priceEditText.text.toString()
            val woodText: String = woodEditText.text.toString()
            val itemNameText: String = itemNameEditText.text.toString()
            var totalSqMetersText: String = totalSqMetersEditText.text.toString()

            // Convert the input to numbers
            val price: Double = priceText.toDouble()
            val wood: Double = woodText.toDouble()
            var totalSqMeters: Double = totalSqMetersText.toDouble()

            // Perform the calculation
            val result: Double = price / (wood / 100)
            val total: Double = result * totalSqMeters

            // Set the result in the TextView
            resultTextView.text =
                "Result: ${"%.2f".format(result)} DKK\nTotal: ${"%.2f".format(total)} DKK"

            // Add the new item to the list
            item = Item(itemNameText, price, wood, totalSqMeters, round(total * 100) / 100)
            itemList.add(0, item)

            // Log the size of the itemList
            Log.d("MainActivity", "Item list size: ${itemList.size}")

            // Notify the adapter that the data set has changed
            (recyclerView.adapter as ItemAdapter).notifyDataSetChanged()

            // Save the updated list
            sharedPreferencesManager.saveItemList(itemList)

            totaltotal = calculateTotalTotal(itemList)
            totalTextView.text = totaltotal
        }

    }

    fun calculateTotalTotal(itemList: MutableList<Item>): String {
        var totaltotal = 0.0
        for (item in itemList) {
            totaltotal += item.totalPrice
        }
        return "Total: ${"%.2f".format(totaltotal)} DKK"
    }

    override fun onItemClick(item: Item) {
        priceEditText.setText(item.priceInMeters.toString())
        woodEditText.setText(item.width.toString())
        itemNameEditText.setText(item.itemName)
        totalSqMetersEditText.setText(item.totalPrice.toString())
    }

    fun changeButtonIsVisible(button: Button) {
        button.isVisible
    }

    private fun isFormValid(): Boolean {
        return priceEditText.text.isNotEmpty() &&
                woodEditText.text.isNotEmpty() &&
                itemNameEditText.text.isNotEmpty() &&
                totalSqMetersEditText.text.isNotEmpty()
    }
}