package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.maxot.seekandcatch.data.model.GameDifficulty
import kotlin.enums.EnumEntries

@Composable
fun GameDifficultSelectorLayout(
    modifier: Modifier,
    variants: EnumEntries<GameDifficulty>,
    defaultVariant: GameDifficulty,
    onDifficultChanged: (GameDifficulty) -> Unit
) {
    var selectedVariantIndex = variants.indexOf(defaultVariant)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {
                selectedVariantIndex =
                    (selectedVariantIndex - 1).coerceIn(0, variants.size - 1)
                onDifficultChanged(variants[selectedVariantIndex])
            }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
        }
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            text = variants[selectedVariantIndex].name,
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(
            onClick = {
                selectedVariantIndex =
                    (selectedVariantIndex + 1).coerceIn(0, variants.size - 1)
                onDifficultChanged(variants[selectedVariantIndex])
            }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}