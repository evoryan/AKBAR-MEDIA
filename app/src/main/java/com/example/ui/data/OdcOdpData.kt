package com.example.ui.data

data class OdcItem(val id: String, var name: String, var location: String, var portCount: Int = 0, var portInput: String = "", @com.squareup.moshi.Json(name="redaman_in") var redamanIn: String = "", @com.squareup.moshi.Json(name="redaman_out") var redamanOut: String = "")

data class OdpItem(val id: String, var name: String, var odcId: String, var portCount: Int = 0, var portInput: String = "", @com.squareup.moshi.Json(name="redaman_in") var redamanIn: String = "", @com.squareup.moshi.Json(name="redaman_out") var redamanOut: String = "")
