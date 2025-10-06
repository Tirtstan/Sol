package com.std.sol.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.R
import com.std.sol.Screen
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.components.StaggeredItem
import com.std.sol.entities.User
import com.std.sol.ui.theme.Amber
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.SolTheme
import com.std.sol.utils.PasswordUtils
import com.std.sol.viewmodels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(navController: NavController, userViewModel: UserViewModel?) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val isPasswordValid = remember(password) { PasswordUtils.isPasswordValid(password) }
    val passwordsMatch = password == confirmPassword
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StaggeredItem(index = 0, durationMillis = 1500) {
            Text(
                text = stringResource(R.string.welcome_to),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic
                ),
                textAlign = TextAlign.Center
            )
        }

        StaggeredItem(index = 1, durationMillis = 1500) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 70.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Amber
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            tint = Ivory
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (password.isNotEmpty() && !isPasswordValid) {
            Text(
                text = "Password must be 8+ characters with an uppercase, number, and special character.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StaggeredItem(index = 4) {
            SpaceTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                placeholder = "Re-enter password",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = {
                        confirmPasswordVisible = !confirmPasswordVisible
                    }) {
                        Icon(
                            painter = painterResource(
                                if (confirmPasswordVisible) android.R.drawable.ic_menu_view
                                else android.R.drawable.ic_secure
                            ),
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = Ivory
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredItem(index = 5) {
            val loginText = buildAnnotatedString {
                withStyle(SpanStyle(color = Ivory)) {
                    append(stringResource(R.string.already_have_an_account))
                }

                withLink(
                    LinkAnnotation.Clickable(
                        tag = "login", linkInteractionListener = {
                            navController.navigate(Screen.Login.route)
                        })
                ) {
                    withStyle(
                        SpanStyle(
                            color = Amber, fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(stringResource(R.string.login))
                    }
                }
            }

            Text(
                text = loginText,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Unspecified)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredItem(index = 6) {
            SpaceButton(
                text = stringResource(R.string.sign_up),
                onClick = {
                    handleRegistration(
                        scope, userViewModel, username, password, context, navController
                    )
                },
                enabled = username.isNotBlank() && password.isNotBlank() && passwordsMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }

    }
}

private fun handleRegistration(
    scope: CoroutineScope,
    userViewModel: UserViewModel?,
    username: String,
    password: String,
    context: Context,
    navController: NavController
) {
    scope.launch {
        val existingUser = userViewModel?.getUserByUsername(username)
        if (existingUser != null) {
            Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
        } else {

            val passwordHash = PasswordUtils.hashPassword(password)
            val newUser = User(id = 0, username = username, passwordHash = passwordHash)

            val newUserId =
                userViewModel?.addUserAndWait(newUser)

            if (newUserId != null && newUserId > 0) {
                val createdUser = userViewModel.getUserByUsername(username)
                if (createdUser != null) {
                    userViewModel.setCurrentUser(createdUser)
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Registration failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Registration failed. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF25315E)
@Composable
fun RegisterScreenPreview() {
    SolTheme {
        RegisterScreen(
            navController = rememberNavController(), userViewModel = null
        )
    }
}
