package edu.rmas.artstreet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.app_navigation.Routing
import edu.rmas.artstreet.data.services.LocationService
import edu.rmas.artstreet.view_models.ArtworkVM

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ArtStreet(authVM: AuthVM, artworkVM: ArtworkVM)
{
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val isFollowingServiceEnabled = sharedPreferences.getBoolean("following_location", true)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    } else {
        if( isFollowingServiceEnabled ) {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_FIND_NEARBY
                context.startForegroundService(this)
            }
        }
        else {
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                context.startForegroundService(this)
            }
        }

    }

    Surface(modifier = Modifier.fillMaxSize()){
        Routing( authVM = authVM, artworkVM = artworkVM )
    }
}