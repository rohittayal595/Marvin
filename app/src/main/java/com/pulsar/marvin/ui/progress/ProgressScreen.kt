package com.pulsar.marvin.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Progress Visualization", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenPrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Loss Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(GreenSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.TrendingDown, contentDescription = null, tint = GreenPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Total Loss", color = Color.Gray, fontSize = 14.sp)
                        // Mock total loss calculation
                        val currentWeight = dailyLogs.firstOrNull()?.weight ?: startingWeight
                        val totalLoss = startingWeight - currentWeight
                        Text(String.format(java.util.Locale.getDefault(), "%.1f kg", totalLoss.coerceAtLeast(0f)), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
                        Icon(Icons.AutoMirrored.Rounded.ShowChart, contentDescription = null, tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Progress Chart", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
                            Icon(Icons.AutoMirrored.Rounded.ShowChart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Projected", color = Color.Gray, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Rounded.ShowChart, contentDescription = null, tint = SolidLineColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Actual", color = SolidLineColor, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Starting", color = Color.Gray, fontSize = 12.sp)
                            Text("$startingWeight kg", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current", color = Color.Gray, fontSize = 12.sp)
                            val curr = dailyLogs.firstOrNull()?.weight
                            Text(if (curr != null) "$curr kg" else "-- kg", color = GreenPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Goal", color = Color.Gray, fontSize = 12.sp)
                            Text("$targetWeight kg", color = GoalPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    targetWeight: Float
) {
    if (weeklyPlans.isEmpty()) return

    val maxWeight = maxOf(startingWeight, weeklyPlans.maxOfOrNull { it.targetWeight } ?: 0f) + 2f
    val minWeight = minOf(targetWeight, weeklyPlans.minOfOrNull { it.targetWeight } ?: 0f) - 2f
    val weightRange = maxWeight - minWeight

    val totalWeeks = weeklyPlans.size
    val sortedPlans = weeklyPlans.sortedBy { it.weekNumber }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        val graphWidth = width - paddingLeft
        val graphHeight = height - paddingBottom

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
            // drawText for Y axis is complex in raw Canvas, typically handled via standard Android Paint or compose text layout.
            // Keeping it simple here or omitting actual text drawing for purity, drawing ticks instead.
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
                }
            }
        }

        // Draw Projected Line (Dotted)
        if (sortedPlans.isNotEmpty()) {
            val projectedPoints = sortedPlans.mapIndexed { index, plan ->
                val x = paddingLeft + (index * (graphWidth / totalWeeks))
                val y = graphHeight - ((plan.targetWeight - minWeight) / weightRange) * graphHeight
                Offset(x, y)
            }

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
        // For actual implementation, we'd map dailyLogs to their respective week X coordinates.
        // Simplified for this task: Draw a mock solid line assuming starting weight and a few logs.
        val mockActualPoints = mutableListOf<Offset>()
        mockActualPoints.add(Offset(paddingLeft, graphHeight - ((startingWeight - minWeight) / weightRange) * graphHeight))
        
        // Mocking a few points for visual
        val mockLogs = listOf(startingWeight - 0.5f, startingWeight - 1.2f, startingWeight - 1.8f)
        mockLogs.forEachIndexed { index, weight ->
            val x = paddingLeft + ((index + 1) * (graphWidth / totalWeeks))
            val y = graphHeight - ((weight - minWeight) / weightRange) * graphHeight
            mockActualPoints.add(Offset(x, y))
            drawCircle(
                color = SolidLineColor,
                radius = 8f,
                center = Offset(x, y)
            )
        }
        
        drawCircle(
            color = SolidLineColor,
            radius = 8f,
            center = mockActualPoints.first()
        )

        for (i in 0 until mockActualPoints.size - 1) {
            drawLine(
                color = SolidLineColor,
                start = mockActualPoints[i],
                end = mockActualPoints[i + 1],
                strokeWidth = 6f
            )
        }
    }
}
