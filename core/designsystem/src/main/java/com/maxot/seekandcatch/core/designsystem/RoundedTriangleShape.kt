package com.maxot.seekandcatch.core.designsystem

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

class RoundedTriangleShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val width = size.width
        val height = size.height

        val radius = size.minDimension / 20f
        val smoothing = 0f
        val cornerRounding = CornerRounding(
            radius = radius,
            smoothing = smoothing
        )

        val points = floatArrayOf(
            width / 2, // x1
            0f, // y1
            width, // x2
            height, // y2
            0f, // x3
            height // y3
        )

        val roundedPolygon = RoundedPolygon(
            vertices = points,
            rounding = cornerRounding,
            centerX = size.width / 2,
            centerY = size.height / 2
        )
        val roundedPolygonPath = roundedPolygon
            .toPath()
            .asComposePath()

        return Outline.Generic(roundedPolygonPath)
    }
}
