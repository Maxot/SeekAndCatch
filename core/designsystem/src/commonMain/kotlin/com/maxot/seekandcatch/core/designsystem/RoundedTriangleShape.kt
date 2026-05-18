package com.maxot.seekandcatch.core.designsystem

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.GenericShape

val RoundedTriangleShape: Shape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}
