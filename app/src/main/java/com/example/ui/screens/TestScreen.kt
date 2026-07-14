package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column

@Composable
fun TestScreen() {
    val graphicsLayer = rememberGraphicsLayer()
    Column(
        modifier = Modifier.drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }
            drawLayer(graphicsLayer)
        }
    ) {
        Text("Hello")
    }
}
