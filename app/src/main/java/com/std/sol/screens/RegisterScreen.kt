package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.std.sol.components.SpaceTextField
import com.std.sol.components.SpaceButton
import com.std.sol.components.StaggeredItem
import com.std.sol.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.std.sol.R
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.withLink
import androidx.navigation.NavController
import com.std.sol.Screen

@Composable
fun RegisterScreen(navController: NavController?) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1426),
                        Color(0xFF1E3A8A),
                        Color(0xFF2563EB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            StaggeredItem(index = 0, durationMillis = 1500) {
                Text(
                    text = stringResource(R.string.welcome_to),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Center
                )
            }

            StaggeredItem(index = 1, durationMillis = 1500) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 62.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = SunGlow
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            StaggeredItem(index = 2) {
                SpaceTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = stringResource(R.string.username),
                    placeholder = stringResource(R.string.enter_username),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StaggeredItem(index = 3) {
                SpaceTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.password),
                    placeholder = stringResource(R.string.enter_password),
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) android.R.drawable.ic_menu_view
                                    else android.R.drawable.ic_secure
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = StarGlow
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StaggeredItem(index = 4) {
                val loginText = buildAnnotatedString {
                    withStyle(SpanStyle(color = StarGlow)) {
                        append(stringResource(R.string.already_have_an_account))
                    }

                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "login",
                            linkInteractionListener = {
                                navController?.navigate(Screen.Login.route)
                            }
                        )
                    ) {
                        withStyle(
                            SpanStyle(
                                color = SunGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(stringResource(R.string.login))
                        }
                    }
                }

                Text(
                    text = loginText,
                    fontSize = 14.sp,
                    // prevent the outer Text style from forcing a color over the span styles
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Unspecified)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StaggeredItem(index = 5) {
                SpaceButton(
                    text = stringResource(R.string.sign_up),
                    onClick = {
                        // TODO: validation
                        navController?.navigate(Screen.NavScreen.route)
                    },
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    SolTheme {
        RegisterScreen(null)
    }
}