package com.example.johnathan2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
}

class ItemAdapter(
        private val items: MutableList<Item>,
        private val listener: OnItemClickListener
    ) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {


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

        holder.view.setOnClickListener {
            listener.onItemClick(item)
        }
    }


    override fun getItemCount(): Int {
        Log.d("ItemAdapter", "Item count requested, count is ${items.size}")
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
        fun onItemMove(item: Item, fromPosition: Int, toPosition: Int)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        listener.onItemMove(items[toPosition], fromPosition, toPosition)
    }

}

