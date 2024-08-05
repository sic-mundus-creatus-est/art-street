package edu.rmas.artstreet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.app_navigation.Routes
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import edu.rmas.artstreet.screens.components.ColorPalette


@Composable
fun TestScreen(
    navController: NavController,
    authVM: AuthVM
) {
    val buttonIsEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.BackgroundMain),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "You are signed in!", color = ColorPalette.Yellow, style = TextStyle(fontSize = 34.sp))
            Button(
                onClick = {
                    authVM.signOut()
                    navController.navigate(Routes.signInScreen)
                },
                enabled = buttonIsEnabled.value,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(width = 200.dp, height = 50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPalette.Yellow,
                    contentColor = ColorPalette.Secondary,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out",
                    tint = ColorPalette.Secondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Sign Out", color = ColorPalette.Secondary)
            }
        }
    }
}