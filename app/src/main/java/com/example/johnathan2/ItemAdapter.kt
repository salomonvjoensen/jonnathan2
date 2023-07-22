package com.example.johnathan2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        Log.d("ItemAdapter", "Creating view holder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val itemNameTextView = holder.view.findViewById<TextView>(R.id.item_name)
        val itemTotalPriceTextView = holder.view.findViewById<TextView>(R.id.item_total_price)

        val item = items[position]
        itemNameTextView.text = item.itemName
        itemTotalPriceTextView.text = item.totalPrice.toString()
    }


    override fun getItemCount(): Int {
        Log.d("ItemAdapter", "Item count requested, count is ${items.size}")
        return items.size
    }
}

