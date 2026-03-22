package com.pulsar.marvin.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsar.marvin.data.model.DailyLog
import com.pulsar.marvin.data.model.WeeklyPlan

val GreenPrimary = Color(0xFF0A8537)
val GreenSecondary = Color(0xFFD1F4DE)
val CardBg = Color.White
val ScreenBg = Color(0xFFF9F9F9)
val DottedLineColor = Color(0xFF80CBC4)
val SolidLineColor = Color(0xFF26A69A)
val GoalPurple = Color(0xFF9C27B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
  weeklyPlans: List<WeeklyPlan>,
  dailyLogs: List<DailyLog>,
  startingWeight: Float,
  targetWeight: Float,
  onBack: () -> Unit,
) {
  Scaffold(
    containerColor = ScreenBg, topBar = {
      TopAppBar(
        title = { Text("Progress Visualization", color = Color.White) }, navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White
            )
          }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
      )
    }) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Total Loss Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(GreenSecondary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.AutoMirrored.Rounded.TrendingDown,
              contentDescription = null,
              tint = GreenPrimary
            )
          }
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text("Total Loss", color = Color.Gray, fontSize = 14.sp)
            val currentWeight = dailyLogs.firstOrNull()?.weight ?: startingWeight
            val totalLoss = startingWeight - currentWeight
            Text(
              String.format(
                java.util.Locale.getDefault(), "%.1f kg", totalLoss.coerceAtLeast(0f)
              ), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black
            )
          }
        }
      }

      // Progress Chart Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.AutoMirrored.Rounded.ShowChart, contentDescription = null, tint = GreenPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "Progress Chart", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text("Your journey to $targetWeight kg", color = Color.Gray, fontSize = 14.sp)

          Spacer(modifier = Modifier.height(24.dp))

          // Chart Canvas
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(250.dp)
          ) {
            ProgressChart(
              weeklyPlans = weeklyPlans,
              dailyLogs = dailyLogs,
              startingWeight = startingWeight,
              targetWeight = targetWeight
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Legend
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.AutoMirrored.Rounded.ShowChart,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Projected", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.AutoMirrored.Rounded.ShowChart,
                contentDescription = null,
                tint = SolidLineColor,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Actual", color = SolidLineColor, fontSize = 14.sp)
            }
          }

          Spacer(modifier = Modifier.height(32.dp))

          // Stats row
          Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Starting", color = Color.Gray, fontSize = 12.sp)
              Text(
                "$startingWeight kg",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Current", color = Color.Gray, fontSize = 12.sp)
              val curr = dailyLogs.firstOrNull()?.weight
              Text(
                if (curr != null) "$curr kg" else "-- kg",
                color = GreenPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Goal", color = Color.Gray, fontSize = 12.sp)
              Text(
                "$targetWeight kg",
                color = GoalPurple,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Your Progress", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Start tracking your daily weight to see your progress on the chart. The dotted line shows your projected path to your goal weight.",
            color = Color.DarkGray,
            fontSize = 14.sp,
            lineHeight = 20.sp
          )
        }
      }
    }
  }
}

@Composable
fun ProgressChart(
  weeklyPlans: List<WeeklyPlan>,
  dailyLogs: List<DailyLog>,
  startingWeight: Float,
  targetWeight: Float,
) {
  if (weeklyPlans.isEmpty()) return

  val maxWeight = maxOf(startingWeight, weeklyPlans.maxOfOrNull { it.targetWeight } ?: 0f) + 2f
  val minWeight = minOf(targetWeight, weeklyPlans.minOfOrNull { it.targetWeight } ?: 0f) - 2f
  val weightRange = maxWeight - minWeight

  val totalWeeks = weeklyPlans.size
  val sortedPlans = weeklyPlans.sortedBy { it.weekNumber }

  val textMeasurer = rememberTextMeasurer()

  Canvas(modifier = Modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    val paddingLeft = 40.dp.toPx()
    val paddingBottom = 30.dp.toPx()
    val graphWidth = width - paddingLeft
    val graphHeight = height - paddingBottom - 16.dp.toPx() // Room for x-axis labels

    // Draw axes
    drawLine(
      color = Color.LightGray,
      start = Offset(paddingLeft, 0f),
      end = Offset(paddingLeft, graphHeight),
      strokeWidth = 2f
    )
    drawLine(
      color = Color.LightGray,
      start = Offset(paddingLeft, graphHeight),
      end = Offset(width, graphHeight),
      strokeWidth = 2f
    )

    // Draw Y axis labels
    val yStep = weightRange / 4
    for (i in 0..4) {
      val yVal = minWeight + (i * yStep)
      val yPos = graphHeight - ((yVal - minWeight) / weightRange) * graphHeight
      drawLine(
        color = Color(0xFFEEEEEE),
        start = Offset(paddingLeft, yPos),
        end = Offset(width, yPos),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
      )
      val textLayoutResult = textMeasurer.measure(
        text = String.format(java.util.Locale.getDefault(), "%.0f", yVal),
        style = TextStyle(color = Color.Gray, fontSize = 10.sp)
      )
      drawText(
        textLayoutResult = textLayoutResult, topLeft = Offset(
          paddingLeft - textLayoutResult.size.width - 12f, yPos - textLayoutResult.size.height / 2f
        )
      )
      drawLine(
        color = Color.Gray,
        start = Offset(paddingLeft - 10f, yPos),
        end = Offset(paddingLeft, yPos),
        strokeWidth = 2f
      )
    }

    // Draw X axis ticks
    if (totalWeeks > 0) {
      val xStep = graphWidth / totalWeeks
      for (i in 0 until totalWeeks) {
        val xPos = paddingLeft + (i * xStep)
        if (i % 4 == 0) { // draw tick every 4 weeks
          drawLine(
            color = Color.Gray,
            start = Offset(xPos, graphHeight),
            end = Offset(xPos, graphHeight + 10f),
            strokeWidth = 2f
          )
          if (i > 0) {
            val textLayoutResult = textMeasurer.measure(
              text = "W$i", style = TextStyle(color = Color.Gray, fontSize = 10.sp)
            )
            drawText(
              textLayoutResult = textLayoutResult,
              topLeft = Offset(xPos - textLayoutResult.size.width / 2f, graphHeight + 16f)
            )
          }
        }
      }
    }

    // Draw Projected Line (Dotted)
    if (sortedPlans.isNotEmpty()) {
      val projectedPoints = mutableListOf<Offset>()
      projectedPoints.add(
        Offset(
          paddingLeft, graphHeight - ((startingWeight - minWeight) / weightRange) * graphHeight
        )
      )

      projectedPoints.addAll(sortedPlans.mapIndexed { index, plan ->
        val x = paddingLeft + ((index + 1) * (graphWidth / totalWeeks))
        val y = graphHeight - ((plan.targetWeight - minWeight) / weightRange) * graphHeight
        Offset(x, y)
      })

      for (i in 0 until projectedPoints.size - 1) {
        drawLine(
          color = DottedLineColor,
          start = projectedPoints[i],
          end = projectedPoints[i + 1],
          strokeWidth = 4f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
      }
    }

    // Draw Actual Line (Solid)
    val actualPoints = mutableListOf<Offset>()
    actualPoints.add(
      Offset(
        paddingLeft, graphHeight - ((startingWeight - minWeight) / weightRange) * graphHeight
      )
    )

    if (sortedPlans.isNotEmpty()) {
      sortedPlans.forEachIndexed { index, plan ->
        val logsThisWeek = dailyLogs.filter { log ->
          log.dateMillis >= plan.startOfWeekMillis && log.dateMillis <= plan.startOfWeekMillis + java.time.Duration.ofDays(
            6
          ).toMillis()
        }
        val avgWeight = logsThisWeek.mapNotNull { it.weight }.average()

        if (!avgWeight.isNaN()) {
          val x = paddingLeft + ((index + 1) * (graphWidth / totalWeeks))
          val y = graphHeight - ((avgWeight.toFloat() - minWeight) / weightRange) * graphHeight
          val point = Offset(x, y)
          actualPoints.add(point)
          drawCircle(
            color = SolidLineColor, radius = 8f, center = point
          )
        }
      }
    }

    drawCircle(
      color = SolidLineColor, radius = 8f, center = actualPoints.first()
    )

    for (i in 0 until actualPoints.size - 1) {
      drawLine(
        color = SolidLineColor, start = actualPoints[i], end = actualPoints[i + 1], strokeWidth = 6f
      )
    }
  }
}
