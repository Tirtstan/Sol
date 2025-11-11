package com.std.sol.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.std.sol.R
import androidx.compose.ui.text.font.Font

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val SpaceMonoFont = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
    Font(
        R.font.space_mono_italic,
        FontWeight.Normal,
        FontStyle.Italic
    ),
    Font(
        R.font.space_mono_bold_italic,
        FontWeight.Bold,
        FontStyle.Italic
    )
)

val InterFont = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(
        R.font.inter_italic_variable,
        FontWeight.Normal,
        FontStyle.Italic
    )
)

val LoraFont = FontFamily(
    Font(R.font.lora_variable, FontWeight.Normal),
    Font(
        R.font.lora_italic_variable,
        FontWeight.Normal,
        FontStyle.Italic
    )
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = LoraFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = Ivory
    ),
    headlineMedium = TextStyle(
        fontFamily = LoraFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
