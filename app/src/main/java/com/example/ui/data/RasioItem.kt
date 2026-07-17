package com.example.ui.data

data class RasioItem(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val size: String = "",
    @com.squareup.moshi.Json(name = "redaman_in") val redamanIn: String = "",
    @com.squareup.moshi.Json(name = "redaman_out_a") val redamanOutA: String = "",
    @com.squareup.moshi.Json(name = "redaman_out_b") val redamanOutB: String = ""
)
