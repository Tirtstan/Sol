// file: 'app/src/main/java/com/std/sol/components/StaggeredItem.kt'
package com.std.sol.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay

/**
 * Wrap any section to animate it in with a staggered fade + vertical offset.
 *
 * @param index 0-based position to compute the delay
 * @param delayPerItem ms added per index for staggering
 * @param durationMillis fade/slide duration
 * @param initialOffsetFraction how far to slide from (fraction of its height)
 * @param forceVisible if true, shows final state immediately (useful for previews/tests)
 */
@Composable
fun StaggeredItem(
    index: Int,
    delayPerItem: Int = 400,
    durationMillis: Int = 500,
    initialOffsetFraction: Float = 0.2f,
    forceVisible: Boolean = false,
    content: @Composable () -> Unit
) {
    val inPreview = LocalInspectionMode.current
    val skipAnimations = inPreview || forceVisible

    var visible by remember { mutableStateOf(skipAnimations) }

    LaunchedEffect(index, skipAnimations) {
        if (!skipAnimations) {
            delay(index.toLong() * delayPerItem)
            visible = true
        }
    }

    if (skipAnimations) {
        content()
    } else {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis),
                        initialOffsetY = { fullHeight -> (fullHeight * initialOffsetFraction).toInt() }
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis / 2))
        ) {
            content()
        }
    }
}
