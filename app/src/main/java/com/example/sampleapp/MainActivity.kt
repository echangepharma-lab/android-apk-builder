package com.mediapp.interactions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mediapp.interactions.ui.screens.AjoutMedicamentScreen
import com.mediapp.interactions.ui.screens.HomeScreen
import com.mediapp.interactions.ui.screens.ScannerScreen
import com.mediapp.interactions.ui.theme.MediAppTheme
import com.mediapp.interactions.ui.viewmodel.MedicamentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediAppTheme {
                MaterialTheme {
                    val navController = rememberNavController()
                    val viewModel: MedicamentViewModel = hiltViewModel()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAjout = { navController.navigate("ajout") }
                            )
                        }
                        composable("ajout") { entry ->
                            AjoutMedicamentScreen(
                                backStackEntry = entry,
                                onNavigateToScanner = { navController.navigate("scanner") },
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = viewModel
                            )
                        }
                        composable("scanner") {
                            ScannerScreen(
                                onBarcodeScanned = { code ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("scanned_barcode", code)
                                    navController.popBackStack()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
