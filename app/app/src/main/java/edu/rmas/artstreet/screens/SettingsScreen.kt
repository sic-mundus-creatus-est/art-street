package edu.rmas.artstreet.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.rmas.artstreet.data.services.LocationService
import edu.rmas.artstreet.screens.components.ColorPalette

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen(
    navController: NavController
){
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Check if the LocationService is running
    val isServiceRunning = isLocationServiceRunning(context)

    // Initialize the switch state based on the running service
    val checked = remember {
        mutableStateOf(isServiceRunning)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ColorPalette.BackgroundMainDarker)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    ColorPalette.BackgroundMainLighter,
                    RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                ){
                    Text(
                        text = "SERVICES",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = ColorPalette.Yellow,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                ColorPalette.White,
                                RoundedCornerShape(5.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nearby Artworks",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorPalette.Black
                            )
                        )
                        Switch(
                            checked = checked.value,
                            onCheckedChange = {
                                checked.value = it
                                if (it){
                                    Intent(context, LocationService::class.java).apply {
                                        action = LocationService.ACTION_FIND_NEARBY
                                        context.startForegroundService(this)
                                    }
                                    with(sharedPreferences.edit()) {
                                        putBoolean("following_location", true)
                                        apply()
                                    }
                                }else{
                                    Intent(context, LocationService::class.java).apply {
                                        action = LocationService.ACTION_STOP
                                        context.stopService(this)
                                    }
                                    with(sharedPreferences.edit()) {
                                        putBoolean("following_location", false)
                                        apply()
                                    }
                                }
                            },
                            thumbContent = if (checked.value) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            } else {
                                null
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ColorPalette.Yellow,
                                checkedTrackColor = ColorPalette.BackgroundMainLighter,
                                uncheckedThumbColor = ColorPalette.Black,
                                uncheckedTrackColor = ColorPalette.White,
                            )
                        )
                    }
                }
            }
        }
    }
}

// Helper function to check if the LocationService is running
@RequiresApi(Build.VERSION_CODES.O)
private fun isLocationServiceRunning(context: Context): Boolean
{
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in activityManager.getRunningServices(Int.MAX_VALUE))
    {
        if (LocationService::class.java.name == service.service.className)
        {
            return true
        }
    }
    return false
}