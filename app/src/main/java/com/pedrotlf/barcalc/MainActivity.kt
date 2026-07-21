package com.pedrotlf.barcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pedrotlf.barcalc.ui.BarTabApp
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarCalcTheme {
                BarTabApp()
            }
        }
    }
}
