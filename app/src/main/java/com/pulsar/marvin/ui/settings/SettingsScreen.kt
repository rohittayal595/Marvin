package com.pulsar.marvin.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape

val GreenPrimary = Color(0xFF0A8537)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var height by remember(state.height) { mutableStateOf(state.height) }
    var targetWeight by remember(state.targetWeight) { mutableStateOf(state.targetWeight) }
    var reductionObese by remember(state.reductionObese) { mutableStateOf(state.reductionObese) }
    var reductionOverweight by remember(state.reductionOverweight) { mutableStateOf(state.reductionOverweight) }
    var reductionNormal by remember(state.reductionNormal) { mutableStateOf(state.reductionNormal) }
    var useFeetAndInches by remember(state.useFeetAndInches) { mutableStateOf(state.useFeetAndInches) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Personal Info",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use ft/inches for height", fontSize = 16.sp)
                Switch(
                    checked = useFeetAndInches,
                    onCheckedChange = { useFeetAndInches = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary, checkedTrackColor = Color(0xFFD1F4DE))
                )
            }

            if (useFeetAndInches) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    val currentCm = height.toFloatOrNull() ?: 0f
                    val totalInches = (currentCm / 2.54f)
                    var ft by remember(height) { mutableIntStateOf((totalInches / 12).toInt()) }
                    var inch by remember(height) { mutableIntStateOf((totalInches % 12).toInt()) }

                    OutlinedTextField(
                        value = if (ft == 0) "" else ft.toString(),
                        onValueChange = {
                            ft = it.toIntOrNull() ?: 0
                            height = (ft * 30.48 + inch * 2.54).toString()
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = { Text("Height (ft)") }
                    )
                    OutlinedTextField(
                        value = if (inch == 0) "" else inch.toString(),
                        onValueChange = {
                            inch = it.toIntOrNull() ?: 0
                            height = (ft * 30.48 + inch * 2.54).toString()
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = { Text("Height (in)") }
                    )
                }
            } else {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = targetWeight,
                onValueChange = { targetWeight = it },
                label = { Text("Target Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

            Text(
                text = "Target Weight Reduction Rates",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = reductionObese,
                onValueChange = { reductionObese = it },
                label = { Text("Obese (BMI >= 30)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = reductionOverweight,
                onValueChange = { reductionOverweight = it },
                label = { Text("Overweight (25 <= BMI < 30)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = reductionNormal,
                onValueChange = { reductionNormal = it },
                label = { Text("Normal (BMI < 25)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    viewModel.saveSettings(
                        height = height,
                        targetWeight = targetWeight,
                        reductionObese = reductionObese,
                        reductionOverweight = reductionOverweight,
                        reductionNormal = reductionNormal,
                        useFeetAndInches = useFeetAndInches,
                        onComplete = onBack
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}