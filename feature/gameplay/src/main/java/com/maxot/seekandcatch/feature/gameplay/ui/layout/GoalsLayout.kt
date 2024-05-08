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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.getSuitableFigures
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun GoalsLayout(
    modifier: Modifier = Modifier,
    goals: Set<Goal<Any>>,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val goalsLayoutContentDesc = stringResource(id = R.string.goals_layout_content_desc)

    Row(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = goalsLayoutContentDesc },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.label_goal),
            style = textStyle
        )
        goals.forEach { goal ->
            when (goal) {
                is Goal.Colored -> {
                    Text(
                        text = stringResource(id = R.string.label_list_of_goal),
                        style = textStyle
                    )
                    Box(
                        Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .background(goal.getGoal())
                    )
                    Text(
                        text = "color",
                        style = textStyle
                    )
                }

                is Goal.Shaped -> {
                    Text(
                        text = stringResource(id = R.string.label_list_of_goal),
                        style = textStyle
                    )
                    ColoredFigureLayout(
                        modifier = Modifier
                            .focusable(false)
                            .clickable(enabled = false, onClick = {}),
                        figure = Figure(type = goal.getGoal()),
                        size = 50.dp
                    )
                    Text(
                        text = "shape",
                        style = textStyle
                    )
                }
            }
        }
    }
}

@Composable
fun DetailedGoalsLayout(
    modifier: Modifier = Modifier,
    goals: Set<Goal<Any>>,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val goalsLayoutContentDesc = stringResource(id = R.string.goals_layout_content_desc)

    Row(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = goalsLayoutContentDesc },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.label_goal),
            style = textStyle
        )
        goals.forEach { goal ->
            goal.getSuitableFigures().forEach { figure ->
                ColoredFigureLayout(figure = figure, size = 50.dp)
            }

        }
    }
}

@Preview
@Composable
fun GoalsLayoutPreview() {
    SeekAndCatchTheme {
        GoalsLayout(goals = setOf(Goal.getRandomGoal()))
    }
}

@Preview
@Composable
fun DetailedGoalsLayoutPreview() {
    SeekAndCatchTheme {
        DetailedGoalsLayout(goals = setOf(Goal.getRandomGoal()))
    }
}
