package br.com.pedrotlf.barcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.pedrotlf.barcalc.presentation.orders.OrderListScreen
import br.com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BarCalcTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "orders_screen"
                ) {
                    composable("orders_screen") {
                        OrderListScreen(navController = navController)
                    }
                    composable("people_screen") {

                    }
                }
            }
        }
    }
}