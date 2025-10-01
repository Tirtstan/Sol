package com.std.sol.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.std.sol.ui.theme.*

@Composable
fun SpaceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceMonoFont,
                    color = StarGlow,
                    fontSize = 14.sp
                )
            )
        },
        placeholder = {
            Text(
                placeholder,
                color = Color(0xFF64748B)
            )
        },
        singleLine = singleLine,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CosmicPurple40,
            unfocusedBorderColor = StarGlow,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = CosmicPurple40
        ),
        shape = RoundedCornerShape(50.dp)
    )
}

@Preview
@Composable
fun SpaceTextFieldPreview() {
    SolTheme {
        SpaceTextField(
            value = "",
            onValueChange = {},
            label = "Username",
            placeholder = "Enter your username"
        )
    }
}
