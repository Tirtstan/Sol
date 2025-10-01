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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import com.std.sol.ui.theme.CosmicPurple40
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.StarGlow

@Composable
fun SpaceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    val shape = RoundedCornerShape(50.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "spaceButtonScale")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (enabled) 18.dp else 0.dp,
                shape = shape,
                ambientColor = CosmicPurple40,
                spotColor = CosmicPurple40
            )
    ) {
        // Gradient backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CosmicPurple40.copy(alpha = if (enabled) 1f else 0.5f),
                            CosmicPurple40.copy(alpha = if (enabled) 0.9f else 0.4f),
                            StarGlow.copy(alpha = if (enabled) 0.6f else 0.2f)
                        )
                    )
                )
        )

        // Transparent M3 button over the gradient (keeps ripple, semantics)
        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            border = BorderStroke(
                width = 1.dp,
                color = StarGlow.copy(alpha = if (enabled) 0.8f else 0.25f)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color(0xFF94A3B8)
            )
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

@Preview
@Composable
fun SpaceButtonPreview() {
    SolTheme {
        SpaceButton(
            text = "Click Me",
            onClick = {},
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        )
    }
}