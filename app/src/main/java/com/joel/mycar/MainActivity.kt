@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.joel.mycar

import android.os.Bundle
import android.net.Uri
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joel.mycar.core.di.ServiceLocator
import com.joel.mycar.feature.fuel.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object FuelAdd  : Screen("fuel_add")
    data object FuelEdit : Screen("fuel_edit/{id}")   // ← con parámetro
    data object FuelList : Screen("fuel_list")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = ServiceLocator.database(this).refuelDao()

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val nav = rememberNavController()
                val db = ServiceLocator.database(this)
                val vm: RefuelViewModel = viewModel(
                    factory = RefuelVMFactory(db.refuelDao(), db.monthlyStatDao())
                )
                // Seed desde assets (solo 1 vez)
                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences("mycar_prefs", MODE_PRIVATE)
                    val done = prefs.getBoolean("seed_done", false)
                    if (!done) {
                        val lines: List<String> = withContext(Dispatchers.IO) {
                            context.assets.open("seed/fuel_seed.csv")
                                .bufferedReader().use { it.readLines() }
                        }
                        vm.importCsv(lines)
                        prefs.edit().putBoolean("seed_done", true).apply()
                    }
                }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("MyCar") }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = nav,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding) // ← evita que la app bar tape el contenido
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                fuelVM = vm,
                                toAddFuel = { nav.navigate(Screen.FuelAdd.route) },
                                toListFuel = { nav.navigate(Screen.FuelList.route) }
                            )
                        }
                        composable(Screen.FuelAdd.route) {
                            AddRefuelScreen(vm) { nav.popBackStack() }
                        }
                        // Lista con "Editar"
                        composable(Screen.FuelList.route) {
                            RefuelListScreen(
                                viewModel = vm,
                                onEdit = { id -> nav.navigate("fuel_edit/$id") }
                            )
                        }
                        // Ruta que recibe el id (Long)
                        composable(
                            route = Screen.FuelEdit.route,
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStack ->
                            val id = backStack.arguments?.getLong("id")
                            EditRefuelScreen(viewModel = vm, id = id) {
                                nav.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    // (opcional) util si algún día lees de un URI externo
    private fun readAllLines(uri: Uri): List<String> {
        return contentResolver.openInputStream(uri).use { input ->
            if (input == null) return emptyList()
            BufferedReader(InputStreamReader(input)).use { br -> br.readLines() }
        }
    }
}
