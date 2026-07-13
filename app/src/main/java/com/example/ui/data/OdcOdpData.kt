package com.example.ui.data

data class OdcItem(val id: String, var name: String, var location: String, var portCount: Int = 0, var portInput: String = "")

data class OdpItem(val id: String, var name: String, var odcId: String, var portCount: Int = 0, var portInput: String = "")
