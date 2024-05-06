package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.getDecimalPart

@Composable
fun CoefficientProgressLayout(
    modifier: Modifier = Modifier,
    progress: Float,
    currentCoefficient: Int
) {
    val coefficientProgressLayoutContentDesc =
        stringResource(id = R.string.coefficient_progress_layout_content_desc)

    Row(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = coefficientProgressLayoutContentDesc }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "x${currentCoefficient}",
            style = MaterialTheme.typography.displaySmall
        )
        LinearProgressIndicator(progress = { progress.getDecimalPart() })
        Text(
            text = "x${(currentCoefficient + 1)}",
            style = MaterialTheme.typography.displayMedium
        )
    }
}
