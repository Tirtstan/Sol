package com.std.sol.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.std.sol.viewmodels.UserViewModel

@Composable
fun MoreScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        user?.let {
            Text("Account Settings for: ${it.username}")
            Text("User ID: ${it.id}")
            // Add other settings UI here
        } ?: run {
            Text("Not logged in")
        }

        // Example: Logout button
        Button(onClick = {
            userViewModel.logout()
            // Navigate back to login screen, clearing the back stack
            // navController.navigate(Screen.Login.route) {
            //     popUpTo(navController.graph.startDestinationId) { inclusive = true }
            // }
        }) {
            Text("Logout")
        }
    }
}

@Preview
@Composable
fun MoreScreenPreview() {
//    MoreScreen(
//        navController = rememberNavController(),
//        userViewModel = object : UserViewModel(null) {
//            // dadwad
//        })
}
