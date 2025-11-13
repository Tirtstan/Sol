package com.std.sol.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.ui.theme.SolTheme
import com.std.sol.Screen
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.std.sol.R
import com.std.sol.components.StaggeredItem
import com.std.sol.components.StarryBackground
import com.std.sol.ui.theme.Amber
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.AuthGradient
import com.std.sol.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel?) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()




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
                text = "Welcome Back,",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        StaggeredItem(index = 1, durationMillis = 1500) {
            Text(
                text = "Log in to continue...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = Amber
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        StaggeredItem(index = 2) {
            SpaceTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Enter your email",
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
                                if (passwordVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_secure
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Ivory
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StaggeredItem(index = 4) {
            val registerText = buildAnnotatedString {
                withStyle(SpanStyle(color = Ivory)) {
                    append(stringResource(R.string.don_t_have_an_account))
                }

                withLink(
                    LinkAnnotation.Clickable(
                        tag = "register",
                        linkInteractionListener = {
                            navController.navigate(Screen.Register.route)
                        }
                    )
                ) {
                    withStyle(
                        SpanStyle(
                            color = Amber,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(stringResource(R.string.register))
                    }
                }
            }

            Text(
                text = registerText,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Unspecified)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredItem(index = 5) {
            SpaceButton(
                text = "Log In",
                onClick = {
                    handleLogin(
                        scope,
                        userViewModel,
                        email,
                        password,
                        context,
                        navController
                    )
                },
                enabled = email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

private fun handleLogin(
    scope: CoroutineScope,
    userViewModel: UserViewModel?,
    email: String,
    password: String,
    context: Context,
    navController: NavController
) {
    scope.launch {
        val result = userViewModel?.login(email, password)
        if (result?.isSuccess == true) {
            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Dashboard.route)
        } else {
            val errorMessage = result?.exceptionOrNull()?.message ?: "Invalid email or password"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF25315E)
fun LoginScreenPreview() {
    SolTheme {
        LoginScreen(
            navController = rememberNavController(),
            userViewModel = null
        )
    }
}
