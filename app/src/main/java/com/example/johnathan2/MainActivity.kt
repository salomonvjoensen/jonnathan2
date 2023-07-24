package com.example.johnathan2

import androidx.activity.ComponentActivity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.util.Log
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : ComponentActivity(), ItemAdapter.OnItemClickListener {

    private lateinit var priceEditText: EditText
    private lateinit var woodEditText: EditText
    private lateinit var itemNameEditText: EditText
    private lateinit var totalSqMetersEditText: EditText

    private lateinit var item: Item
    private var itemList: MutableList<Item> = mutableListOf()

    private lateinit var appBarConfiguration: AppBarConfiguration
    //private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        priceEditText = findViewById(R.id.editTextPrice)
        woodEditText = findViewById(R.id.editTextWood)
        itemNameEditText = findViewById(R.id.editItemName)
        totalSqMetersEditText = findViewById(R.id.editTextTotalSq)

        val calculateButton: Button = findViewById(R.id.buttonCalculate)
        val resultTextView: TextView = findViewById(R.id.textViewResult)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 1)

        // Define the OnItemClickListener
        val onItemClickListener = object : ItemAdapter.OnItemClickListener {
            override fun onItemClick(item: Item) {
                priceEditText.setText(item.priceInMeters.toString())
                woodEditText.setText(item.width.toString())
                itemNameEditText.setText(item.itemName)
                totalSqMetersEditText.setText(item.totalPrice.toString())
            }
        }

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
            item = Item(itemNameText, price, wood, totalSqMeters)
            itemList.add(item)

            // Log the size of the itemList
            Log.d("MainActivity", "Item list size: ${itemList.size}")

            // Notify the adapter that the data set has changed
            (recyclerView.adapter as ItemAdapter).notifyDataSetChanged()
        }

    }

    override fun onItemClick(item: Item) {
        priceEditText.setText(item.priceInMeters.toString())
        woodEditText.setText(item.width.toString())
        itemNameEditText.setText(item.itemName)
        totalSqMetersEditText.setText(item.totalPrice.toString())
    }
}