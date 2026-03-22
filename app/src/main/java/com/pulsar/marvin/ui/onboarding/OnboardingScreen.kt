package com.pulsar.marvin.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

val Step1ButtonColor = Color(0xFF8AD09D)
val Step1IconBg = Color(0xFFD1F4DE)
val Step1IconColor = Color(0xFF0A8537)

val Step2ButtonColor = Color(0xFF8BAEFF)
val Step2IconBg = Color(0xFFD6E4FF)
val Step2IconColor = Color(0xFF0C56D0)

val Step3ButtonColor = Color(0xFFC48BFF)
val Step3IconBg = Color(0xFFF3E5FF)
val Step3IconColor = Color(0xFF8B00DD)

val InfoBoxBg = Color(0xFFE8F8EE)
val InfoBoxBorder = Color(0xFFD1E8DA)
val InfoBoxText = Color(0xFF0A8537)
val ActiveDotColor = Color(0xFF0A8537)
val InactiveDotColor = Color(0xFFD9D9D9)
val CardBgColor = Color.White
val ScreenBgColor = Color(0xFFF4FAF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
  viewModel: OnboardingViewModel,
  onComplete: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  val pagerState = rememberPagerState(pageCount = { 3 })
  val coroutineScope = rememberCoroutineScope()

  val heightText by viewModel.heightText.collectAsState()
  val weightText by viewModel.weightText.collectAsState()
  val targetWeightText by viewModel.targetWeightText.collectAsState()

  val healthyRange = viewModel.getHealthyRange(heightText)

  Scaffold(
    containerColor = ScreenBgColor,
    contentWindowInsets = WindowInsets.safeDrawing
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CardBgColor),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(24.dp),
            userScrollEnabled = false
          ) { page ->
            Box(modifier = Modifier) {
              when (page) {

                0 -> Step1Height(
                  heightText = heightText,
                  useFeetAndInches = viewModel.useFeetAndInches.collectAsState().value,
                  onHeightChange = viewModel::updateHeight,
                  onContinue = {
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                  },
                )

                1 -> Step2Weight(
                  weightText = weightText,
                  onWeightChange = viewModel::updateWeight,
                  onContinue = {
                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                  }
                )

                2 -> Step3Target(
                  targetWeightText = targetWeightText,
                  onTargetWeightChange = viewModel::updateTargetWeight,
                  minWeight = healthyRange.first,
                  maxWeight = healthyRange.second,
                  onStartJourney = {
                    viewModel.completeOnboarding(onComplete)
                  },
                )
              }

              IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.TopEnd)
              ) {
                Icon(
                  imageVector = Icons.Rounded.Settings,
                  contentDescription = "Settings",
                  tint = Color.Gray
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(3) { index ->
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (index == pagerState.currentPage) ActiveDotColor else InactiveDotColor)
            )
          }
        }
      }
    }
  }
}

@Composable
fun Step1Height(
  heightText: String,
  useFeetAndInches: Boolean,
  onHeightChange: (String) -> Unit,
  onContinue: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(Step1IconBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Rounded.Straighten,
        contentDescription = "Height",
        tint = Step1IconColor,
        modifier = Modifier.size(32.dp)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Welcome to Marvin",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Let's start with your height to calculate\nyour healthy weight range",
      fontSize = 16.sp,
      color = Color.Gray,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
      if (!useFeetAndInches) {
        Text(
          text = "Height (cm)",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = heightText,
          onValueChange = onHeightChange,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          isError = !heightText.isEmpty() && heightText.replace(",", ".").toFloatOrNull() == null,
          singleLine = true,
          placeholder = { Text("170", color = Color.Gray) },
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF3F3F3),
            focusedContainerColor = Color(0xFFF3F3F3),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Step1IconColor
          )
        )
      } else {
        Text(
          text = "Height (ft' in'')",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          var ft by remember { mutableIntStateOf(0) }
          var inch by remember { mutableIntStateOf(0) }

          OutlinedTextField(
            value = if (ft == 0) "" else ft.toString(),
            onValueChange = {
              ft = it.toIntOrNull() ?: 0
              onHeightChange((ft * 30.48 + inch * 2.54).toString())
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = !heightText.isEmpty() && heightText.replace(",", ".").toFloatOrNull() == null,
            singleLine = true,
            placeholder = { Text("ft", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color(0xFFF3F3F3),
              focusedContainerColor = Color(0xFFF3F3F3),
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = Step1IconColor
            )
          )
          OutlinedTextField(
            value = if (inch == 0) "" else inch.toString(),
            onValueChange = {
              inch = it.toIntOrNull() ?: 0
              onHeightChange((ft * 30.48 + inch * 2.54).toString())
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = !heightText.isEmpty() && heightText.replace(",", ".").toFloatOrNull() == null,
            singleLine = true,
            placeholder = { Text("inch", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color(0xFFF3F3F3),
              focusedContainerColor = Color(0xFFF3F3F3),
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = Step1IconColor
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onContinue,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Step1IconColor),
      enabled = heightText.replace(",", ".").toFloatOrNull() != null
    ) {
      Text(text = "Continue", fontSize = 16.sp, color = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
        contentDescription = null,
        tint = Color.White
      )
    }
  }
}

@Composable
fun Step2Weight(
  weightText: String,
  onWeightChange: (String) -> Unit,
  onContinue: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(Step2IconBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Rounded.MonitorWeight,
        contentDescription = "Weight",
        tint = Step2IconColor,
        modifier = Modifier.size(32.dp)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Current Weight",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "What's your current weight?",
      fontSize = 16.sp,
      color = Color.Gray,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "Current Weight (kg)",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = weightText,
        onValueChange = onWeightChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = !weightText.isEmpty() && weightText.replace(",", ".").toFloatOrNull() == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        placeholder = { Text("80", color = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedContainerColor = Color(0xFFF3F3F3),
          focusedContainerColor = Color(0xFFF3F3F3),
          unfocusedBorderColor = Color.Transparent,
          focusedBorderColor = Step2IconColor
        )
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onContinue,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Step2IconColor),
      enabled = weightText.replace(",", ".").toFloatOrNull() != null
    ) {
      Text(text = "Continue", fontSize = 16.sp, color = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
        contentDescription = null,
        tint = Color.White
      )
    }
  }
}

@Composable
fun Step3Target(
  targetWeightText: String,
  onTargetWeightChange: (String) -> Unit,
  minWeight: Float,
  maxWeight: Float,
  onStartJourney: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(Step3IconBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Rounded.AdsClick,
        contentDescription = "Target",
        tint = Step3IconColor,
        modifier = Modifier.size(32.dp)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Choose Your Target",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Select a target weight within your healthy range",
      fontSize = 16.sp,
      color = Color.Gray,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Info Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(InfoBoxBg, RoundedCornerShape(12.dp))
        .border(1.dp, InfoBoxBorder, RoundedCornerShape(12.dp))
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "Healthy Weight Range (BMI 18.5-24.9)",
          fontSize = 14.sp,
          color = InfoBoxText,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = String.format(Locale.getDefault(), "%.1f kg - %.1f kg", minWeight, maxWeight),
          fontSize = 18.sp,
          color = InfoBoxText,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "Target Weight (kg)",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
      )
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
        value = targetWeightText,
        onValueChange = onTargetWeightChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = !targetWeightText.isEmpty() && targetWeightText.replace(",", ".")
          .toFloatOrNull() == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        placeholder = { Text("72.0", color = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedContainerColor = Color(0xFFF3F3F3),
          focusedContainerColor = Color(0xFFF3F3F3),
          unfocusedBorderColor = Color.Transparent,
          focusedBorderColor = Step3IconColor
        )
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onStartJourney,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Step3IconColor),
      enabled = targetWeightText.replace(",", ".").toFloatOrNull() != null
    ) {
      Text(text = "Start My Journey", fontSize = 16.sp, color = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
        contentDescription = null,
        tint = Color.White
      )
    }
  }
}
