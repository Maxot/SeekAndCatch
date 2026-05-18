package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.LocalColorblindMode
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.FigureColor
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.toComposeColor
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res
import com.maxot.seekandcatch.feature.gameplay.generated.resources.*
import com.maxot.seekandcatch.feature.settings.generated.resources.Res as SettingsRes
import com.maxot.seekandcatch.feature.settings.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun GoalsLayout(
    modifier: Modifier = Modifier,
    goals: Set<Goal<Any>>,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val goalsLayoutContentDesc = stringResource(Res.string.goals_layout_content_desc)

    Row(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = goalsLayoutContentDesc },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(Res.string.label_goal), style = textStyle)
        goals.forEach { goal ->
            when (goal) {
                is Goal.Colored -> {
                    Text(text = stringResource(Res.string.label_list_of_goal), style = textStyle)
                    Box(
                        Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .background(goal.getGoal().toComposeColor()),
                        contentAlignment = Alignment.Center
                    ) {
                        if (LocalColorblindMode.current) {
                            val colorCode = when (goal.getGoal()) {
                                FigureColor.Red -> stringResource(SettingsRes.string.color_red)
                                FigureColor.Blue -> stringResource(SettingsRes.string.color_blue)
                                FigureColor.Green -> stringResource(SettingsRes.string.color_green)
                                FigureColor.Yellow -> stringResource(SettingsRes.string.color_yellow)
                                else -> stringResource(SettingsRes.string.color_unknown)
                            }
                            Text(
                                text = colorCode,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                    Text(text = "color", style = textStyle)
                }

                is Goal.Shaped -> {
                    Text(text = stringResource(Res.string.label_list_of_goal), style = textStyle)
                    ColoredFigureLayout(
                        modifier = Modifier
                            .focusable(false)
                            .clickable(enabled = false, onClick = {}),
                        figure = Figure(type = goal.getGoal()),
                        size = 50.dp
                    )
                    Text(text = "shape", style = textStyle)
                }
            }
        }
    }
}

@Composable
fun DetailedGoalsLayout(
    modifier: Modifier = Modifier,
    goalsSuitableFigures: Set<Figure>,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val goalsLayoutContentDesc = stringResource(Res.string.goals_layout_content_desc)

    Row(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = goalsLayoutContentDesc },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(Res.string.label_goal), style = textStyle)
        goalsSuitableFigures.forEach { figure ->
            ColoredFigureLayout(figure = figure, size = 50.dp)
        }
    }
}
