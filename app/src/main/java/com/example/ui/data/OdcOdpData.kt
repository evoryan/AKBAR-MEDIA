package com.example.ui.data

data class OdcItem(val id: String, var name: String, var location: String)

data class OdpItem(val id: String, var name: String, var odcId: String, var portCount: Int)
