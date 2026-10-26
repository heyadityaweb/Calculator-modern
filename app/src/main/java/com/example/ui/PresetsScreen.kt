package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MathPreset
import com.example.data.MathPresetsRepository
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.setScreen(AppScreen.GRAPH_STUDIO)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "High School", "University", "Algebra", "Trigonometry", "Calculus & ODEs")

    val filteredPresets = remember(searchQuery, selectedFilter) {
        MathPresetsRepository.PRESETS.filter { preset ->
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "High School" -> preset.level == "High School"
                "University" -> preset.level == "University"
                else -> preset.category.contains(selectedFilter, ignoreCase = true)
            }
            val matchesSearch = preset.title.contains(searchQuery, ignoreCase = true) ||
                    preset.formula.contains(searchQuery, ignoreCase = true) ||
                    preset.description.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setScreen(AppScreen.GRAPH_STUDIO) },
                modifier = Modifier.testTag("back_from_presets_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Graph",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Formula Library",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Secondary & University Level Mathematics",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search curves (e.g. sine, damped, normal, taylor)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("presets_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { option ->
                val isSelected = selectedFilter == option
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = option },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("preset_chip_$option")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Presets List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredPresets) { preset ->
                PresetCard(
                    preset = preset,
                    onPlot = { viewModel.applyPreset(preset) }
                )
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: MathPreset,
    onPlot: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth().testTag("preset_card_${preset.title}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (preset.level == "University") Color(0xFF312E81) else Color(0xFF064E3B)
                ) {
                    Text(
                        text = preset.level,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (preset.level == "University") Color(0xFFC7D2FE) else Color(0xFFA7F3D0),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = preset.category,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = preset.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Formula Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "f(x) = ${preset.formula}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = preset.accentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = onPlot,
                colors = ButtonDefaults.buttonColors(containerColor = preset.accentColor),
                modifier = Modifier.fillMaxWidth().testTag("plot_preset_${preset.title}")
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plot in Graph Studio", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
