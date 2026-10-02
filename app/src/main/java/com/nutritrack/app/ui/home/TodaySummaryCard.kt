package com.nutritrack.app.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R

/** Today's totals vs. goals. Values are placeholders until food logging and DRI targets exist. */
data class TodaySummary(
    val caloriesConsumed: Int,
    val caloriesGoal: Int,
    val proteinGrams: Int,
    val proteinGoalGrams: Int,
    val waterMl: Int,
    val waterGoalMl: Int
) {
    companion object {
        val Placeholder = TodaySummary(
            caloriesConsumed = 0,
            caloriesGoal = 2000,
            proteinGrams = 0,
            proteinGoalGrams = 60,
            waterMl = 0,
            waterGoalMl = 2000
        )
    }
}

@Composable
fun TodaySummaryCard(summary: TodaySummary, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.home_today),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalorieRing(consumed = summary.caloriesConsumed, goal = summary.caloriesGoal)
                    Text(
                        text = stringResource(R.string.home_label_calories),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GoalProgress(
                        label = R.string.home_label_protein,
                        value = stringResource(R.string.home_value_grams, summary.proteinGrams, summary.proteinGoalGrams),
                        progress = fraction(summary.proteinGrams, summary.proteinGoalGrams),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    GoalProgress(
                        label = R.string.home_label_water,
                        value = stringResource(R.string.home_value_ml, summary.waterMl, summary.waterGoalMl),
                        progress = fraction(summary.waterMl, summary.waterGoalMl),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }
        }
    }
}

/** Ring showing calories eaten out of the goal, with the numbers in the middle. */
@Composable
private fun CalorieRing(consumed: Int, goal: Int, modifier: Modifier = Modifier) {
    val ringColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.secondaryContainer
    val progress = fraction(consumed, goal)
    Box(modifier = modifier.size(128.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(inset, inset)
            drawArc(trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            if (progress > 0f) {
                drawArc(ringColor, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 18.dp)
        ) {
            Text(
                text = consumed.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.home_calories_of_goal, goal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GoalProgress(
    @StringRes label: Int,
    value: String,
    progress: Float,
    color: Color,
    trackColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.titleSmall
        )
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun fraction(value: Int, goal: Int): Float =
    if (goal <= 0) 0f else (value.toFloat() / goal).coerceIn(0f, 1f)
