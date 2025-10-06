package com.std.sol.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.RoyalBright
import com.std.sol.ui.theme.Indigo
import org.intellij.lang.annotations.Language

@Composable
fun SpaceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(Indigo, RoyalBright),
    shadowColor: Color = RoyalBright,
    borderColor: Color = RoyalBright.copy(alpha = 0.5f),
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    val shape = RoundedCornerShape(50.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "spaceButtonScale")

    // Animate alpha when enabled/disabled
    val targetAlpha = if (enabled) 1f else 0.5f
    val animatedAlpha by animateFloatAsState(targetAlpha, label = "spaceButtonAlpha")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = animatedAlpha
            }
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .height(60.dp)
    ) {
        // Gradient backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors.map { it.copy(alpha = it.alpha * animatedAlpha) }
                    )
                )
        )

        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            border = BorderStroke(
                width = 1.dp,
                color = borderColor.copy(alpha = if (enabled) 0.8f else 0.25f)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color(0xFF94A3B8)
            ),
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1426)
@Composable
fun SpaceButtonPreview() {
    SolTheme {
        Box(modifier = Modifier.padding(50.dp)) {
            SpaceButton(
                text = "Click Me",
                onClick = {},
                modifier = Modifier
                    .width(200.dp)
            )
        }
    }
}
