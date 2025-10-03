package com.std.sol.ui.theme

import androidx.compose.ui.graphics.Color

// Base palette (from vines-flexible-linear-ramps)
val DeepSpaceBase = Color(0xFF0C1327)
val Ivory = Color(0xFFFFFDF0)
val IndigoDark = Color(0xFF19102E)
val Indigo = Color(0xFF25315E)
val IndigoLight = Color(0xFF3A5C85)
val Ocean = Color(0xFF56A1BF)
val Sky = Color(0xFF2AC0F2)
val Mist = Color(0xFF97DBD2)
val Royal = Color(0xFF222D81)
val RoyalBright = Color(0xFF465BE7)
val PlumDeep = Color(0xFF1A112E)
val Plum = Color(0xFF291945)
val MagentaDeep = Color(0xFF5E1C5A)
val Magenta = Color(0xFF8F1767)
val Rose = Color(0xFFF45D92)
val Peach = Color(0xFFFEB58B)
val LeafDark = Color(0xFF118337)
val Leaf = Color(0xFF57C52B)
val Lime = Color(0xFFB9ED5E)
val EmberDark = Color(0xFF431E1E)
val Ember = Color(0xFFB42313)
val Amber = Color(0xFFF4C047)
val Orange = Color(0xFFF4680B)

// Utility to derive shades only by mixing palette colors
private fun Color.blend(toward: Color, ratio: Float): Color {
    val r = red + (toward.red - red) * ratio
    val g = green + (toward.green - green) * ratio
    val b = blue + (toward.blue - blue) * ratio
    val a = alpha + (toward.alpha - alpha) * ratio
    return Color(r, g, b, a)
}

// Gradients
val AuthGradient = listOf(
    DeepSpaceBase,      // deep background
    Indigo,    // mid tone
    RoyalBright // bright accent
)

val MoreScreenGradient = listOf(
    IndigoDark,
    DeepSpaceBase,
    Indigo
)
