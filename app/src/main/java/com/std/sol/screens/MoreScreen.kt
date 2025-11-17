package com.std.sol.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.Screen
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.entities.User
import com.std.sol.ui.theme.Ember
import com.std.sol.ui.theme.Indigo
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.Rose
import com.std.sol.ui.theme.RoyalBright
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.SpaceMonoFont
import com.std.sol.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun MoreScreen(navController: NavController, userViewModel: UserViewModel?) {
    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = "", username = "John Doe"))
    }

    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Account", style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic
                ), color = Ivory, textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            if (user != null) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(120.dp),
                    tint = Ivory
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = user!!.username,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont,
                    color = Ivory
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Manage your account settings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = SpaceMonoFont,
                    color = Ivory.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(0.2f))

                SpaceButton(
                    text = "Change Username",
                    onClick = { showChangeUsernameDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    gradientColors = listOf(Indigo, RoyalBright),
                    shadowColor = RoyalBright,
                    borderColor = RoyalBright.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SpaceButton(
                    text = "Change Password",
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    gradientColors = listOf(Indigo, RoyalBright),
                    shadowColor = RoyalBright,
                    borderColor = RoyalBright.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                SpaceButton(
                    text = "Logout",
                    onClick = {
                        userViewModel?.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    gradientColors = listOf(Rose, Ember),
                    shadowColor = Rose,
                    borderColor = Ember
                )
            } else {
                Text(
                    "Not logged in", 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = Ivory
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }

        if (showChangeUsernameDialog) {
            ChangeUsernameDialog(
                currentUser = user,
                onDismiss = { showChangeUsernameDialog = false },
                onConfirm = { newUsername ->
                    scope.launch {
                        user?.let {
                            val updatedUser = it.copy(username = newUsername)
                            userViewModel?.updateUser(updatedUser)
                            Toast.makeText(context, "Username updated!", Toast.LENGTH_SHORT)
                                .show()
                            showChangeUsernameDialog = false
                        }
                    }
                })
        }

        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = { showChangePasswordDialog = false },
                onConfirm = { newPassword ->
                    scope.launch {
                        // Password is managed by Firebase Auth, not stored in User entity
                        val firebaseUser = userViewModel?.getCurrentFirebaseUser()
                        if (firebaseUser != null) {
                            try {
                                firebaseUser.updatePassword(newPassword).await()
                                Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                                showChangePasswordDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
        }
    }
}

@Composable
fun ChangeUsernameDialog(
    currentUser: User?, onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUser?.username ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Username", fontWeight = FontWeight.Bold) },
        text = {
            SpaceTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = "New Username",
                placeholder = "Enter new username"
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newUsername) },
                enabled = newUsername.isNotBlank() && newUsername != currentUser?.username
            ) {
                Text(
                    "Confirm", style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel", style = MaterialTheme.typography.labelLarge
                )
            }
        })
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val passwordsMatch = newPassword.isNotBlank() && newPassword == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                SpaceTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New Password",
                    placeholder = "Enter new password"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SpaceTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    placeholder = "Re-enter new password"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(newPassword) }, enabled = passwordsMatch) {
                Text(
                    "Confirm", style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel", style = MaterialTheme.typography.labelLarge
                )
            }
        })
}


@Preview(showBackground = true, backgroundColor = 0xFF0C1327)
@Composable
fun MoreScreenPreview() {
    SolTheme {
        MoreScreen(
            navController = rememberNavController(), userViewModel = null
        )
    }
}
