package com.jonathancalculator.johnathan2

import android.app.AlertDialog
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
import android.text.Html
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.jonathancalculator.johnathan2.R
import java.io.IOException
import java.util.Collections
import kotlin.math.round

class MainActivity : ComponentActivity(), ItemAdapter.OnItemClickListener {

    // Title
    private lateinit var titleTextView: TextView

    // EditText
    private lateinit var priceEditText: EditText
    private lateinit var woodEditText: EditText
    private lateinit var itemNameEditText: EditText
    private lateinit var totalSqMetersEditText: EditText

    // ItemList and Storage
    private lateinit var item: Item
    private var itemList: MutableList<Item> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private var itemAdapter: ItemAdapter? = null
    private var selectedItemIndex: Int? = null

    override fun onItemMove(item: Item, fromPosition: Int, toPosition: Int) {
        // Handle item move
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Get itemList from storage
        sharedPreferencesManager = SharedPreferencesManager(this)
        itemList = sharedPreferencesManager.loadItemList()

        // Title and EditText
        titleTextView = findViewById(R.id.textViewTitle)
        priceEditText = findViewById(R.id.editTextPrice)
        woodEditText = findViewById(R.id.editTextWood)
        itemNameEditText = findViewById(R.id.editItemName)
        totalSqMetersEditText = findViewById(R.id.editTextTotalSq)

        // Clear buttons
        setupClearButtonWithAction(priceEditText)
        setupClearButtonWithAction(woodEditText)
        setupClearButtonWithAction(itemNameEditText)
        setupClearButtonWithAction(totalSqMetersEditText)

        // Buttons
        val calculateButton: Button = findViewById(R.id.buttonCalculate)
        // Check form validity and set button state
        calculateButton.isEnabled = isFormValid()
        val changeButton: Button = findViewById(R.id.buttonChange)

        val emptyListButton: Button = findViewById(R.id.buttonEmptyList)

        // Result and total
        val resultTextView: TextView = findViewById(R.id.textViewResult)
        val totalTextView: TextView = findViewById(R.id.textViewTotal)

        // TotalTotal
        var totaltotal: String = calculateTotalTotal(itemList)
        totalTextView.text = totaltotal

        itemAdapter = ItemAdapter(itemList, object : ItemAdapter.OnItemClickListener {
            override fun onItemClick(item: Item) {
                priceEditText.setText(item.priceInMeters.toString())
                woodEditText.setText(item.width.toString())
                itemNameEditText.setText(item.itemName)
                totalSqMetersEditText.setText(item.totalSqMeters.toString())

                changeButtonIsVisible(changeButton)

                // Find the index of the clicked item
                selectedItemIndex = itemList.indexOf(item)

                // Set the result in the TextView
                resultTextView.text =
                    "Result: ${"%.2f".format(item.priceInMeters / (item.width / 100))}" +
                            " DKK/m²\nTotal: ${"%.2f".format(item.totalPrice)} DKK, " +
                            "${item.totalSqMeters} m²"
            }

            override fun onItemMove(item: Item, fromPosition: Int, toPosition: Int) {
                // Handle item move
            }

        })

        // RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.isVerticalScrollBarEnabled = true
        recyclerView.adapter = itemAdapter

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
        recyclerView.adapter = itemAdapter

        // Add the ItemTouchHelper code here
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                Collections.swap(itemList, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                // Save the updated list after reordering
                sharedPreferencesManager.saveItemList(itemList)

                changeButton.visibility = View.INVISIBLE

                priceEditText.setText("")
                woodEditText.setText("")
                totalSqMetersEditText.setText("")
                itemNameEditText.setText("")
                resultTextView.text = "Result: \nTotal:"

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
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
            val totalSqMetersText: String = totalSqMetersEditText.text.toString()

            // Convert the input to numbers
            val price: Double = priceText.toDouble()
            val wood: Double = woodText.toDouble()
            val totalSqMeters: Double = totalSqMetersText.toDouble()

            // Perform the calculation
            val result: Double = price / (wood / 100)
            val total: Double = result * totalSqMeters

            // Set the result in the TextView
            resultTextView.text =
                "Result: ${"%.2f".format(result)} DKK/m²\nTotal: ${"%.2f".format(total)} DKK, ${totalSqMeters} m²"
                //"Result: ${"%.2f".format(result)} DKK\nTotal: ${"%.2f".format(total)} DKK"

            // Add the new item to the list
            item = Item(itemNameText, price, wood, totalSqMeters, round(total * 100) / 100)
            itemList.add(0, item)

            // Log the size of the itemList
            Log.d("MainActivity", "Item list size: ${itemList.size}")

            // Notify the adapter that the data set has changed
            (recyclerView.adapter as ItemAdapter).notifyItemInserted(0)

            // Save the updated list
            sharedPreferencesManager.saveItemList(itemList)

            totaltotal = calculateTotalTotal(itemList)
            totalTextView.text = totaltotal

            priceEditText.setText("")
            woodEditText.setText("")
            totalSqMetersEditText.setText("")
            itemNameEditText.setText("")
            resultTextView.text = "Result: \nTotal:"
        }

        changeButton.setOnClickListener {
            // Check if an item is selected
            if (selectedItemIndex != null) {
                // Get the selected item
                val selectedItem = itemList[selectedItemIndex!!]

                // Update the selected item
                selectedItem.priceInMeters = priceEditText.text.toString().toDouble()
                selectedItem.width = woodEditText.text.toString().toDouble()
                selectedItem.totalSqMeters = totalSqMetersEditText.text.toString().toDouble()
                selectedItem.itemName = itemNameEditText.text.toString()

                // Perform the calculation
                val result: Double = selectedItem.priceInMeters / (selectedItem.width / 100)
                val total: Double = result * selectedItem.totalSqMeters

                selectedItem.totalPrice = round(total * 100) / 100

                // Notify the adapter that the data set has changed
                (recyclerView.adapter as ItemAdapter).notifyItemChanged(selectedItemIndex!!)

                // Save the updated list
                sharedPreferencesManager.saveItemList(itemList)

                changeButton.visibility = View.INVISIBLE

                priceEditText.setText("")
                woodEditText.setText("")
                totalSqMetersEditText.setText("")
                itemNameEditText.setText("")
                resultTextView.text = "Result: \nTotal:"

                totaltotal = calculateTotalTotal(itemList)
                totalTextView.text = totaltotal
            }
        }

        emptyListButton.setOnClickListener {
            itemList.clear()
            (recyclerView.adapter as ItemAdapter).notifyDataSetChanged()
            sharedPreferencesManager.saveItemList(itemList)
            totaltotal = calculateTotalTotal(itemList)
            totalTextView.text = totaltotal

            changeButton.visibility = View.INVISIBLE

            priceEditText.setText("")
            woodEditText.setText("")
            totalSqMetersEditText.setText("")
            itemNameEditText.setText("")
            resultTextView.text = "Result: \nTotal:"
        }

        val buttonExport: Button = findViewById(R.id.buttonExport)
        val buttonImport: Button = findViewById(R.id.buttonImport)

        buttonExport.setOnClickListener {
            // Launch the create file activity
            createFileLauncher.launch("export")
        }

        val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                try {
                    contentResolver.openInputStream(it)?.use { inputStream ->
                        inputStream.bufferedReader().use { reader ->
                            // Clear the current list
                            itemList.clear()

                            val header = reader.readLine()
                            if (header != "Item Name,Price,Width,Total Sq Meters,Total Price") {
                                throw IOException("Invalid file format")
                            }
                            reader.forEachLine { line ->
                                val tokens = line.split(",")
                                if (tokens.size == 5) {
                                    val item = Item(tokens[0], tokens[1].toDouble(), tokens[2].toDouble(), tokens[3].toDouble(), tokens[4].toDouble())
                                    itemList.add(item)
                                }
                            }
                            // Notify the adapter that the data set has changed
                            itemAdapter?.notifyDataSetChanged()

                            // Update the total TextView
                            val totalTextView: TextView = findViewById(R.id.textViewTotal)
                            val totaltotal = calculateTotalTotal(itemList)
                            totalTextView.text = totaltotal
                        }
                    }

                    // Save the the list into the sharedPreference memory after loading file.
                    sharedPreferencesManager.saveItemList(itemList)
                } catch (e: IOException) {
                    // Handle the exception
                    Log.e("MainActivity", "Error reading CSV file", e)
                    // Show an error message to the user
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Invalid file format. Please select a valid CSV file.")
                        .setPositiveButton("OK", null)
                        .show()
                }

            }
        }

        buttonImport.setOnClickListener {
            openDocument.launch(arrayOf("*/*"))
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
        button.visibility = View.VISIBLE
    }

    private fun isFormValid(): Boolean {
        return priceEditText.text.isNotEmpty() &&
                woodEditText.text.isNotEmpty() &&
                itemNameEditText.text.isNotEmpty() &&
                totalSqMetersEditText.text.isNotEmpty()
    }

    private fun setupClearButtonWithAction(editText: EditText) {
        // Hide the 'X' initially
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        editText.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2

            if (event.action == MotionEvent.ACTION_UP) {
                // Check if the drawable exists
                if (editText.compoundDrawables[DRAWABLE_RIGHT] != null) {
                    if (event.rawX >= (editText.right - editText.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                        // Clear the text and hide the 'X'
                        editText.setText("")
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

                        // Perform a click event
                        editText.performClick()

                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener false
        }


        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    // Get the custom 'X' drawable
                    val drawable = resources.getDrawable(R.drawable.ic_clear, null)
                    if (drawable != null) {
                        // Show the 'X'
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                    }
                } else {
                    // Hide the 'X'
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    // Define the result launcher for creating a file
    private val createFileLauncher = registerForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            // Write your data to the uri
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                // Write the header
                writer.append("Item Name,Price,Width,Total Sq Meters,Total Price\n")
                // Write each item
                itemList.forEach { item ->
                    writer.append("${item.itemName},${item.priceInMeters},${item.width},${item.totalSqMeters},${item.totalPrice}\n")
                }
            }
        }
    }

}