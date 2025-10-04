package com.std.sol.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.entities.User
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.SolTheme
import com.std.sol.viewmodels.UserViewModel

@Composable
fun DashboardScreen(navController: NavController, userViewModel: UserViewModel?) {
    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = -1, username = "John Doe", passwordHash = ""))
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        text = "Dashboard",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        ),
        textAlign = TextAlign.Center,
        color = Ivory
    )
}


@Preview(showBackground = true, backgroundColor = 0xFF0C1327)
@Composable
fun DashboardScreenPreview() {
    SolTheme {
        DashboardScreen(rememberNavController(), null)
    }
}