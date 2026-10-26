package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.math.MathExpressionEvaluator
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: CalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val evaluator = remember { MathExpressionEvaluator() }

    BackHandler(enabled = state.currentScreen != AppScreen.GRAPH_STUDIO) {
        viewModel.setScreen(AppScreen.GRAPH_STUDIO)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090D16),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.GRAPH_STUDIO,
                    onClick = { viewModel.setScreen(AppScreen.GRAPH_STUDIO) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.GRAPH_STUDIO)
                                Icons.Filled.ShowChart else Icons.Outlined.ShowChart,
                            contentDescription = "Graph Studio"
                        )
                    },
                    label = {
                        Text(
                            "Graph",
                            fontWeight = if (state.currentScreen == AppScreen.GRAPH_STUDIO) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF042F2E),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_graph_studio")
                )

                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.SCIENTIFIC_CALC,
                    onClick = { viewModel.setScreen(AppScreen.SCIENTIFIC_CALC) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.SCIENTIFIC_CALC)
                                Icons.Filled.Calculate else Icons.Outlined.Calculate,
                            contentDescription = "Scientific Calculator"
                        )
                    },
                    label = {
                        Text(
                            "Calculator",
                            fontWeight = if (state.currentScreen == AppScreen.SCIENTIFIC_CALC) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF042F2E),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_scientific_calc")
                )

                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.PRESETS_LIBRARY,
                    onClick = { viewModel.setScreen(AppScreen.PRESETS_LIBRARY) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.PRESETS_LIBRARY)
                                Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = "Formulas & Presets"
                        )
                    },
                    label = {
                        Text(
                            "Library",
                            fontWeight = if (state.currentScreen == AppScreen.PRESETS_LIBRARY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF042F2E),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_presets_library")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentScreen) {
                AppScreen.GRAPH_STUDIO -> GraphScreen(viewModel = viewModel, evaluator = evaluator)
                AppScreen.SCIENTIFIC_CALC -> CalculatorScreen(viewModel = viewModel)
                AppScreen.PRESETS_LIBRARY -> PresetsScreen(viewModel = viewModel)
            }
        }
    }
}
