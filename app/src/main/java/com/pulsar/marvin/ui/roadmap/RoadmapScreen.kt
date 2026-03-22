package com.pulsar.marvin.ui.roadmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pulsar.marvin.data.model.DailyLog
import com.pulsar.marvin.data.model.WeeklyPlan
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val GreenPrimary = Color(0xFF0A8537)
val GreenSecondary = Color(0xFFD1F4DE)
val DarkGrey = Color(0xFF333333)
val LightGrey = Color(0xFFF5F5F5)
val DividerColor = Color(0xFFE0E0E0)
val CurrentWeekBg = Color(0xFFE8F8EE)
val CardBg = Color.White
val ScreenBg = Color(0xFFF9F9F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
  viewModel: RoadmapViewModel,
  onNavigateToProgress: () -> Unit,
  onPivotRequested: (weekNumber: Int) -> Unit,
) {
  val state by viewModel.state.collectAsState()

  if (state.isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = GreenPrimary)
    }
    return
  }

  Scaffold(
    containerColor = ScreenBg,
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.setShowCheckInModal(true) },
        containerColor = GreenPrimary,
        contentColor = Color.White,
        shape = CircleShape
      ) {
        Icon(Icons.Rounded.Add, contentDescription = "Add Daily Log")
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        val daysToSubtract = today.dayOfWeek.value % 7
        val currentWeekStartMillis = today.minusDays(daysToSubtract.toLong()).toInstant()
          .toEpochMilli() // start of week: Sunday
        val currentWeekIndex =
          state.weeklyPlans.map { it.startOfWeekMillis }.indexOf(currentWeekStartMillis)

        val lastWeekStart = Instant.ofEpochMilli(state.weeklyPlans.last().startOfWeekMillis)
          .atZone(ZoneId.systemDefault()).toLocalDate()
        val etaDate = lastWeekStart.plusDays(6)
        RoadmapHeader(
          targetWeight = state.userPreferences?.targetWeight ?: 0f,
          totalWeeks = state.weeklyPlans.size,
          currentWeek = currentWeekIndex,
          onStatsClick = onNavigateToProgress,
          etaDate = etaDate,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Weekly Breakdown",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
          )
          Text(
            text = "${state.weeklyPlans.size} weeks total",
            fontSize = 14.sp,
            color = Color.Gray
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(state.weeklyPlans) { plan ->
            val dailyLogs = state.dailyLogs
            WeeklyPlanCard(
              plan = plan,
              dailyLogs = dailyLogs,
              isCurrentWeek = plan.startOfWeekMillis == state.currentWeekStartMillis,
              onPivotClick = { 
                onPivotRequested(plan.weekNumber) 
              },
              onDeleteLog = { log -> viewModel.deleteDailyLog(log) }
            )
          }
          item {
            Spacer(modifier = Modifier.height(80.dp)) // Fab clearance
          }
        }
      }

      if (state.showCheckInModal) {
        DailyCheckInDialog(
          onDismiss = { viewModel.setShowCheckInModal(false) },
          onSave = { weight, calories, dateMillis ->
            viewModel.saveDailyLog(weight, calories, dateMillis)
          }
        )
      }
    }
  }
}

@Composable
fun RoadmapHeader(
  targetWeight: Float,
  totalWeeks: Int,
  currentWeek: Int,
  onStatsClick: () -> Unit,
  etaDate: LocalDate,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        color = GreenPrimary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
      )
      .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Your Roadmap",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          IconButton(onClick = onStatsClick, modifier = Modifier.size(24.dp)) {
            Icon(
              Icons.AutoMirrored.Rounded.ShowChart,
              contentDescription = "Stats",
              tint = Color.White
            )
          }
          // Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Goal Weight Card
        Box(
          modifier = Modifier
            .weight(1f)
            .background(Color(0xFF269E4E), RoundedCornerShape(12.dp))
            .padding(16.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Rounded.MonitorWeight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Goal Weight", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "$targetWeight kg",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // ETA Card
        Box(
          modifier = Modifier
            .weight(1f)
            .background(Color(0xFF269E4E), RoundedCornerShape(12.dp))
            .padding(16.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Rounded.CalendarToday,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("ETA", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Calculate ETA based on the end of the last week in the plan

            val formattedDate =
              etaDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()))
            Text(formattedDate, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Progress Bar Area
      val progressPercent =
        if (totalWeeks > 0) ((currentWeek.toFloat() / totalWeeks) * 100).toInt() else 0
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Week ${currentWeek + 1} of $totalWeeks", color = Color.White, fontSize = 14.sp)
        Text("$progressPercent%", color = Color.White, fontSize = 14.sp)
      }
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { if (totalWeeks > 0) currentWeek.toFloat() / totalWeeks else 0f },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = GreenSecondary,
        trackColor = Color(0xFF269E4E)
      )
    }
  }
}

@Composable
fun WeeklyPlanCard(
  plan: WeeklyPlan,
  dailyLogs: List<DailyLog>,
  isCurrentWeek: Boolean,
  onPivotClick: () -> Unit,
  onDeleteLog: (DailyLog) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  val logsThisWeek = dailyLogs.filter { log ->
    log.dateMillis >= plan.startOfWeekMillis &&
      log.dateMillis <= plan.startOfWeekMillis + Duration.ofDays(6).toMillis()
  }
  val avgWeight = logsThisWeek.mapNotNull { it.weight }.average()
  val avgCalories = logsThisWeek.mapNotNull { it.calories }.average()

  val avgWeightText =
    if (avgWeight.isNaN()) "-- kg" else String.format(
      Locale.getDefault(),
      "%.1f kg",
      avgWeight
    )
  val avgCaloriesText =
    if (avgCalories.isNaN()) "-- kcal" else String.format(
      Locale.getDefault(),
      "%.0f kcal",
      avgCalories
    )

  val borderColor = if (isCurrentWeek) GreenPrimary else DividerColor
  val bgColor = if (isCurrentWeek) CurrentWeekBg else CardBg

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { expanded = !expanded },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = bgColor),
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Week Number Circle
          // Box(
          //   modifier = Modifier
          //     .size(40.dp)
          //     .background(if (isCurrentWeek) GreenSecondary else LightGrey, CircleShape),
          //   contentAlignment = Alignment.Center
          // ) {
          //   Text(
          //     text = plan.weekNumber.toString(),
          //     fontWeight = FontWeight.Bold,
          //     color = if (isCurrentWeek) GreenPrimary else DarkGrey
          //   )
          // }

          // Spacer(modifier = Modifier.width(16.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Week ${plan.weekNumber}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrey
              )
              if (isCurrentWeek) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .background(GreenSecondary, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    "Current",
                    color = GreenPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            val startDate =
              Instant.ofEpochMilli(plan.startOfWeekMillis).atZone(ZoneId.systemDefault())
                .toLocalDate()
            val endDate = startDate.plusDays(6)
            val fmt = DateTimeFormatter.ofPattern("MMM dd")
            Text(
              text = "${startDate.format(fmt)} - ${endDate.format(fmt)}",
              fontSize = 12.sp,
              color = Color.Gray
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Projected",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkGrey
            )
            if (!avgWeightText.contains("--")) {
              Text(
                text = "Actual",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF26A69A)
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = String.format(Locale.getDefault(), "%.1f kg", plan.targetWeight),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkGrey
            )
            if (!avgWeightText.contains("--")) {
              Text(
                text = avgWeightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF26A69A)
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = "Expand",
            tint = Color.Gray
          )
        }
      }

      AnimatedVisibility(visible = expanded) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          HorizontalDivider(color = DividerColor)
          Spacer(modifier = Modifier.height(16.dp))

          // Daily logs for the week
          val dayAbbreviations = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
          val startDate =
            Instant.ofEpochMilli(plan.startOfWeekMillis).atZone(ZoneId.systemDefault())
              .toLocalDate()

          for (i in 0..6) {
            val currentDate = startDate.plusDays(i.toLong())
            val currentDayMillis =
              currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val logForDay = dailyLogs.find { it.dateMillis == currentDayMillis }

            val weightText =
              logForDay?.weight?.let { String.format(Locale.getDefault(), "%.1f kg", it) }
                ?: "-- kg"
            val caloriesText = logForDay?.calories?.let { "$it kcal" } ?: "-- kcal"

            Row {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(dayAbbreviations[i], color = Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(weightText, color = DarkGrey, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(caloriesText, color = DarkGrey, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
              }
              Spacer(modifier = Modifier.size(4.dp))
              if (logForDay != null) {
                IconButton(onClick = { onDeleteLog(logForDay) }, modifier = Modifier.size(24.dp)) {
                  Icon(Icons.Rounded.Delete, contentDescription = "Delete Log", tint = Color.Gray)
                }
              } else {
                Spacer(modifier = Modifier.size(24.dp))
              }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (i == 6) {
              Row {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    "Avg", color = Color.Gray, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                  )

                  Text(
                    avgWeightText, color = DarkGrey, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                  )
                  Text(
                    avgCaloriesText, color = DarkGrey, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                  )
                }
                Spacer(modifier = Modifier.width(28.dp))
              }
            }
          }


          val nowMillis =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
          val startDayOfWeek = Instant.ofEpochMilli(plan.startOfWeekMillis)
          val lastDayOfWeek = startDayOfWeek.plus(Duration.ofDays(6)).toEpochMilli()
          // Pivot Option for Task 3
          if (/*nowMillis > lastDayOfWeek &&*/
            dailyLogs.isNotEmpty() &&
            dailyLogs.first().dateMillis >= startDayOfWeek.toEpochMilli() &&
            dailyLogs.first().dateMillis <= lastDayOfWeek
          ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
              onClick = onPivotClick,
              modifier = Modifier.fillMaxWidth(),
              border = BorderStroke(1.dp, Color(0xFFE65100))
            ) {
              Text("Review Week / Pivot", color = Color(0xFFE65100))
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCheckInDialog(
  onDismiss: () -> Unit,
  onSave: (weight: Float?, calories: Int?, dateMillis: Long) -> Unit,
) {
  var weightInput by remember { mutableStateOf("") }
  var caloriesInput by remember { mutableStateOf("") }
  var selectedDateMillis by remember {
    mutableLongStateOf(
      LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
  }
  var showDatePicker by remember { mutableStateOf(false) }

  if (showDatePicker) {
    val initialUtcMillis = remember(selectedDateMillis) {
      val localDate =
        Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
      localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          val utcSelected = datePickerState.selectedDateMillis
          if (utcSelected != null) {
            val localDate = Instant.ofEpochMilli(utcSelected).atZone(ZoneId.of("UTC")).toLocalDate()
            selectedDateMillis =
              localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
          }
          showDatePicker = false
        }) {
          Text("OK")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Spacer(modifier = Modifier.width(24.dp))
          Text(
            text = "Daily Check-in",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }

        val formattedDate = Instant.ofEpochMilli(selectedDateMillis)
          .atZone(ZoneId.systemDefault())
          .toLocalDate()
          .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd", Locale.getDefault()))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { showDatePicker = true }
            .padding(4.dp)
        ) {
          Text(
            text = formattedDate,
            color = GreenPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            Icons.Rounded.ExpandMore,
            contentDescription = "Select Date",
            tint = GreenPrimary,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weight Input
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Rounded.MonitorWeight,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Weight (kg)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = weightInput,
            onValueChange = { weightInput = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = LightGrey,
              focusedContainerColor = LightGrey,
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = GreenPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calories Input
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Rounded.Restaurant,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calories (optional)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = caloriesInput,
            onValueChange = { caloriesInput = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = LightGrey,
              focusedContainerColor = LightGrey,
              unfocusedBorderColor = Color.Transparent,
              focusedBorderColor = GreenPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DividerColor)
          ) {
            Text("Cancel", color = Color.Black)
          }
          Button(
            onClick = {
              val w = weightInput.toFloatOrNull()
              val c = caloriesInput.toIntOrNull()
              onSave(w, c, selectedDateMillis)
            },
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
          ) {
            Text("Save", color = Color.White)
          }
        }
      }
    }
  }
}
