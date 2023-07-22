package com.example.johnathan2

import androidx.activity.ComponentActivity
/*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.johnathan2.ui.theme.Johnathan2Theme
*/

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager

//import com.google.android.material.snackbar.Snackbar

/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Johnathan2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}
*/


class MainActivity : ComponentActivity() {

    private lateinit var item: Item
    private var itemList: MutableList<Item> = mutableListOf()

    private lateinit var appBarConfiguration: AppBarConfiguration
    //private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        */
        setContentView(R.layout.home)

        val calculateButton: Button = findViewById(R.id.buttonCalculate)
        val priceEditText: EditText = findViewById(R.id.editTextPrice)
        val woodEditText: EditText = findViewById(R.id.editTextWood)
        val itemNameEditText: EditText = findViewById(R.id.editItemName)
        var totalSqMetersEditText: EditText = findViewById(R.id.editTextTotalSq)
        val resultTextView: TextView = findViewById(R.id.textViewResult)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter = ItemAdapter(itemList)
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
}
/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Johnathan2Theme {
        Greeting("Android")
    }
}
*/
