package com.example.ui.data

data class CategoryItem(val id: String, var name: String)

data class InventoryItem(val id: String, var name: String, var categoryId: String, var stock: Int)

data class StockHistory(val id: String, val type: String, val itemName: String, val quantity: Int, val adminName: String, val timestamp: Long)
