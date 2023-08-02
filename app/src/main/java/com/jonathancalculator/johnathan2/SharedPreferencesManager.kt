package com.jonathancalculator.johnathan2

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveItemList(itemList: MutableList<Item>) {
        val json = gson.toJson(itemList)
        sharedPreferences.edit().putString("itemList", json).apply()
    }

    fun loadItemList(): MutableList<Item> {
        val json = sharedPreferences.getString("itemList", null)
        val type = object : TypeToken<MutableList<Item>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
}