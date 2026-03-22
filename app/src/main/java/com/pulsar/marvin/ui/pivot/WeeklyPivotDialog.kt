package com.pulsar.marvin.ui.pivot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

val ErrorOrange = Color(0xFFE65100)
val ErrorOrangeBg = Color(0xFFFFF3E0)
val OptionABg = Color(0xFFE3F2FD)
val OptionAText = Color(0xFF1565C0)
val OptionBBg = Color(0xFFF3E5F5)
val OptionBText = Color(0xFF6A1B9A)
val OptionCBg = Color(0xFFFFF3E0)
val OptionCText = Color(0xFFE65100)

@Composable
fun WeeklyPivotDialog(
    weekNumber: Int,
    targetWeight: Float,
    actualWeight: Float,
    averageCalories: Int,
    onDismiss: () -> Unit,
    onApply: (PivotOption) -> Unit
) {
    var selectedOption by remember { mutableStateOf<PivotOption?>(null) }

    Dialog(onDismissRequest = onDismiss,
           properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = ErrorOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Weekly Pivot", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                
                Text(
                    text = "Week $weekNumber - Choose your path forward",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 32.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Alert Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorOrangeBg, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Target not quite met this week", color = ErrorOrange, fontWeight = FontWeight.Bold)
                        Text("Target: $targetWeight kg", color = ErrorOrange)
                        Text("Actual: $actualWeight kg", color = ErrorOrange)
                        Text("Average calories: $averageCalories cal", color = ErrorOrange)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("No worries! Choose how you'd like to adjust your plan:", fontSize = 14.sp, color = Color(0xFF424242))

                Spacer(modifier = Modifier.height(16.dp))

                // Options
                PivotOptionCard(
                    title = "Option A: Make up for it in next week",
                    description = "Take care of it by reducing your calorie ceiling/increasing activity for next week",
                    icon = Icons.Rounded.Restaurant,
                    iconBg = OptionABg,
                    iconTint = OptionAText,
                    isSelected = selectedOption == PivotOption.AGGRESSIVE_NEXT_WEEK,
                    onClick = { selectedOption = PivotOption.AGGRESSIVE_NEXT_WEEK }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                PivotOptionCard(
                    title = "Option B: Recalculate Plan",
                    description = "Adjust timeline based on current weight (moves ETA out)",
                    icon = Icons.Rounded.CalendarToday,
                    iconBg = OptionCBg,
                    iconTint = OptionCText,
                    isSelected = selectedOption == PivotOption.RECALCULATE,
                    onClick = { selectedOption = PivotOption.RECALCULATE }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
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
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Text("Later", color = Color.Black)
                    }
                    Button(
                        onClick = { selectedOption?.let { onApply(it) } },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A8537)),
                        enabled = selectedOption != null
                    ) {
                        Text("Apply Choice", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PivotOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) iconTint else Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

enum class PivotOption {
    AGGRESSIVE_NEXT_WEEK, RECALCULATE
}
