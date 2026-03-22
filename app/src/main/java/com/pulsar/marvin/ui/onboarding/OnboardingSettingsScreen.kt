package com.pulsar.marvin.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSettingsScreen(
  viewModel: OnboardingSettingsViewModel,
  onBack: () -> Unit,
) {
  val state by viewModel.state.collectAsState()

  // Local state to prevent updating shared viewmodel/preferences before Save
  var useFeetAndInches by remember(state.useFeetAndInches) { mutableStateOf(state.useFeetAndInches) }
  var reductionObese by remember(state.reductionObese) { mutableStateOf(state.reductionObese) }
  var reductionOverweight by remember(state.reductionOverweight) { mutableStateOf(state.reductionOverweight) }
  var reductionNormal by remember(state.reductionNormal) { mutableStateOf(state.reductionNormal) }

  val scrollState = rememberScrollState()

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
          containerColor = Step1IconColor,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(scrollState)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Use ft/inches for height", fontSize = 16.sp)
        Switch(
          checked = useFeetAndInches,
          onCheckedChange = { useFeetAndInches = it },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Step1IconColor,
            checkedTrackColor = Step1IconBg
          )
        )
      }

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
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      )

      OutlinedTextField(
        value = reductionOverweight,
        onValueChange = { reductionOverweight = it },
        label = { Text("Overweight (25 <= BMI < 30)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      )

      OutlinedTextField(
        value = reductionNormal,
        onValueChange = { reductionNormal = it },
        label = { Text("Normal (BMI < 25)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp)
      )

      Button(
        onClick = {
          viewModel.saveSettings(
            reductionObese = reductionObese,
            reductionOverweight = reductionOverweight,
            reductionNormal = reductionNormal,
            useFeetAndInches = useFeetAndInches,
            onComplete = onBack
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Step1IconColor)
      ) {
        Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
