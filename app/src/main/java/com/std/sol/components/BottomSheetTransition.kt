package com.std.sol.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset


@Composable
fun BottomSheetTransition(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var sheetHeightPx by remember { mutableStateOf(0f) }
    var dragOffsetPx by remember { mutableStateOf(0f) }

    val animationSpec: AnimationSpec<Float> = tween(durationMillis = 320)

    // baseAnimatedOffset animates from sheetHeight (hidden) to 0 (visible)
    val baseAnimatedOffsetPx by animateFloatAsState(
        targetValue = if (visible) 0f else sheetHeightPx,
        animationSpec = animationSpec,
        label = "baseAnimatedOffset"
    )

    // final offset is base animation + current drag (clamped)
    val offsetPx = (baseAnimatedOffsetPx + dragOffsetPx).coerceIn(0f, sheetHeightPx)

    // if completely hidden and not animating/visible then don't compose content
    if (!visible && baseAnimatedOffsetPx >= sheetHeightPx - 1f && dragOffsetPx == 0f) return

    Box(modifier = Modifier.fillMaxSize()) {
        // Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { onDismiss() }
                .pointerInput(Unit) {
                    // consume pointer so taps are handled by backdrop
                    detectVerticalDragGestures { _, _ -> /* consume */ }
                }
        )

        // Sheet content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    // capture sheet height in px
                    sheetHeightPx = it.size.height.toFloat()
                }
                .offset { IntOffset(0, with(density) { offsetPx.toInt() }) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, deltaY ->
                            // positive deltaY = dragging down
                            dragOffsetPx =
                                (dragOffsetPx + deltaY).coerceIn(-sheetHeightPx, sheetHeightPx)
                        },
                        onDragEnd = {
                            // if dragged down > 25% of sheet height, dismiss
                            if (dragOffsetPx > sheetHeightPx * 0.25f) {
                                onDismiss()
                            }
                            dragOffsetPx = 0f
                        },
                        onDragCancel = {
                            dragOffsetPx = 0f
                        }
                    )
                }
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F1724),
                            Color(0xFF0B2134)
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // content slot
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
