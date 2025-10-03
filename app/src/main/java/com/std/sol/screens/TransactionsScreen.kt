package com.std.sol.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.std.sol.viewmodels.UserViewModel

@Composable
fun TransactionsScreen(navController: NavController, userViewModel: UserViewModel) {
    Text("Transactions")
}