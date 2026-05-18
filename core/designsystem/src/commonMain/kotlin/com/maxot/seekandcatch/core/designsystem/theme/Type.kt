package com.maxot.seekandcatch.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.maxot.seekandcatch.core.designsystem.generated.resources.Res
import com.maxot.seekandcatch.core.designsystem.generated.resources.press_start_2p
import org.jetbrains.compose.resources.Font

val appTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal)),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
)

val pixelFont = FontFamily(Font(Res.font.press_start_2p, FontWeight.Normal))
