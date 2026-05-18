package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res
import com.maxot.seekandcatch.feature.gameplay.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries

@Composable
fun GameDifficultSelectorLayout(
    modifier: Modifier = Modifier,
    variants: EnumEntries<GameDifficulty>,
    defaultVariant: GameDifficulty,
    onDifficultChanged: (GameDifficulty) -> Unit
) {
    val gameDifficultSelectorContentDesc = stringResource(Res.string.game_difficult_selector_content_desc)

    var selectedVariantIndex = variants.indexOf(defaultVariant)

    Row(
        modifier = modifier
            .then(modifier)
            .semantics { contentDescription = gameDifficultSelectorContentDesc }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {
                selectedVariantIndex =
                    (selectedVariantIndex - 1).coerceIn(0, variants.size - 1)
                onDifficultChanged(variants[selectedVariantIndex])
            }) {
            Icon(painter = painterResource(Res.drawable.ic_arrow_pixel_left), contentDescription = "Previous")
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
            Icon(painter = painterResource(Res.drawable.ic_arrow_pixel_right), contentDescription = "Next")
        }
    }
}
