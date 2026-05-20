package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.Goal
import com.maxot.seekandcatch.feature.gameplay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameInfoPanel(
    modifier: Modifier = Modifier,
    maxLifeCount: Int = 5,
    lifeCount: Int = 3,
    goals: Set<Goal<Any>> = emptySet(),
    goalsSuitableFigures: Set<Figure>,
    score: Int,
    coefficient: Float,
    gameDuration: Long,
    showScoreAndTime: Boolean = true,
    showCoefficient: Boolean = true,
    showLives: Boolean = true,
) {
    PixelBorderBox(modifier = modifier.padding(6.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (showScoreAndTime) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(id = R.string.feature_gameplay_label_score, score),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Time: ${formatMilliseconds(gameDuration)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else if (!showLives) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (showLives) {
                    Column(
                        modifier = if (!showScoreAndTime) Modifier.weight(1f) else Modifier,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.End
                        ) {
                            repeat((maxLifeCount - lifeCount).coerceAtLeast(0)) {
                                Icon(
                                    painter = painterResource(SaCIcons.UnselectedFavoriteRes),
                                    contentDescription = null,
                                    tint = null
                                )
                            }
                            repeat(lifeCount.coerceAtLeast(0)) {
                                Icon(
                                    painter = painterResource(SaCIcons.FavoriteRes),
                                    contentDescription = null,
                                    tint = null
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            DetailedGoalsLayout(
                modifier = Modifier.fillMaxWidth(),
                goalsSuitableFigures = goalsSuitableFigures
            )

            if (showCoefficient) {
                Spacer(modifier = Modifier.height(8.dp))

                CoefficientProgressLayout(
                    progress = coefficient,
                    currentCoefficient = coefficient.toInt(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatMilliseconds(milliseconds: Long): String {
    if (milliseconds <= 0L) return "00:00"
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun GameInfoPanelPreview() {
    SeekAndCatchTheme {
        GameInfoPanel(
            maxLifeCount = 5,
            lifeCount = 3,
            goals = emptySet(),
            goalsSuitableFigures = setOf(
                Figure.getRandomFigure(),
                Figure.getRandomFigure(),
                Figure.getRandomFigure(),
            ),
            score = 42,
            coefficient = 0.5f,
            gameDuration = 90_000L,
        )
    }
}
