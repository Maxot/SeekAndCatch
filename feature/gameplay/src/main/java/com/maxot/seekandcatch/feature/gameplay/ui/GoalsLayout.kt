package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.data.Goal

@Composable
fun GoalsLayout(goals: Set<Goal<Any>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.label_goal),
            style = MaterialTheme.typography.titleMedium
        )
        goals.forEach { goal ->
            when (goal) {
                is Goal.Colored -> {
                    Text(
                        text = stringResource(id = R.string.label_list_of_goal),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Box(
                        Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .background(goal.getGoal())
                    )
                    Text(text = "color", style = MaterialTheme.typography.titleSmall)
                }

                is Goal.Figured -> {
                    Text(
                        text = stringResource(id = R.string.label_list_of_goal),
                        style = MaterialTheme.typography.titleSmall
                    )
                    ColoredFigureLayout(
                        size = 50.dp,
                        figure = goal.getGoal()
                    )
                }
            }
        }
    }
}
