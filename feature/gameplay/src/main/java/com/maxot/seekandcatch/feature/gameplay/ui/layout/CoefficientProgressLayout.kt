package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
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
            modifier = Modifier.weight(1f),
            text = "x${currentCoefficient}",
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.displayLarge
        )
        LinearProgressIndicator(
            modifier = Modifier
                .weight(4f)
                .padding(horizontal = 10.dp),
            progress = { progress.getDecimalPart() })
        Text(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            text = "x${(currentCoefficient + 1)}",
            style = MaterialTheme.typography.displayLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoefficientProgressLayoutPreview() {
    SeekAndCatchTheme {
        CoefficientProgressLayout(
            progress = 2.65f,
            currentCoefficient = 2
        )
    }
}
