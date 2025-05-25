package com.maxot.seekandcatch.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.maxot.seekandcatch.core.designsystem.R

// Set of Material typography styles to start with
val appTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    displayLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontSize = 10.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontSize = 8.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.press_start_2p, FontWeight.Normal)
        ),
        fontSize = 12.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

val pixelFont = FontFamily(
    Font(R.font.press_start_2p, FontWeight.Normal)
)