package edu.rmas.artstreet.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.model.LatLng
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.data.services.LocationService
import edu.rmas.artstreet.screens.components.ArtworkDescription
import edu.rmas.artstreet.screens.components.Header
import edu.rmas.artstreet.screens.components.LocationTag
import edu.rmas.artstreet.screens.components.ArtworkPhotoGrid
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.screens.components.PrimaryArtworkPhoto
import edu.rmas.artstreet.screens.components.VisitedInteractionButton
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ArtworkScreen (
    artwork: Artwork,
    artworkVM: ArtworkVM,
    authVM: AuthVM
) {
    val context = LocalContext.current
    val currentUserLocation = remember { mutableStateOf<LatLng?>(null) }

    val interactions = remember { mutableListOf<Interaction>() }

    val currentUserInteraction = remember { mutableStateOf("") }

    val numOfVisits = remember { mutableIntStateOf(0) }
    val isLoading = remember { mutableStateOf(false) }

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    val latitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LATITUDE, 0.0)
                    val longitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LONGITUDE, 0.0)

                    currentUserLocation.value = LatLng(latitude, longitude)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter)

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    val isNearby = currentUserLocation.value?.let { userLocation ->
        val distance = calculateDistance (
            userLocation.latitude, userLocation.longitude,
            artwork.location.latitude, artwork.location.longitude
        )
        distance <= 100
    } ?: false

    val density = LocalDensity.current.density // For pixel to dp conversion

    var fixedContentHeightDp by remember { mutableStateOf(0.dp) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ColorPalette.BackgroundMainEvenDarker)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPalette.BackgroundMainEvenDarker)
                .onGloballyPositioned { coordinates ->
                    val heightInPixels = coordinates.size.height
                    fixedContentHeightDp = (heightInPixels / density).dp
                }
                .padding(bottom = 10.dp) // Padding to ensure space for LazyColumn
        ) {
            PrimaryArtworkPhoto(imageUrl = artwork.primaryImage)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorPalette.BackgroundMainEvenDarker)
                    .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 10.dp)
            ) {
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Header(header_text = artwork.title.replace('+', ' '))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${numOfVisits.intValue}",
                                color = ColorPalette.Yellow,
                                style = TextStyle ( fontSize = 24.sp, fontFamily = FontFamily.Monospace),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            VisitedInteractionButton(
                                isNearby = isNearby,
                                visited = interactions.any {
                                    it.artworkId == artwork.id && it.userId == authVM.currentUser?.uid && it.visitedByUser
                                },
                                onClick = {
                                    val existingInteraction = interactions.firstOrNull {
                                        it.artworkId == artwork.id && it.userId == authVM.currentUser?.uid
                                    }
                                    if (existingInteraction != null) {
                                        isLoading.value = true
                                        artworkVM.markAsNotVisited(existingInteraction.id)
                                    } else {
                                        isLoading.value = true
                                        artworkVM.markAsVisited(
                                            artwork.id,
                                            artwork
                                        )
                                    }
                                }
                            )
                        }
                    }
                    LocationTag(
                        location = LatLng(artwork.location.latitude, artwork.location.longitude),
                        context = context
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    TheDivider()
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                ArtworkDescription(description = artwork.description)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = fixedContentHeightDp) // dynamic padding based on fixed content height
                .background(ColorPalette.BackgroundMainDarker)
                .align(Alignment.TopCenter)
        ) {
            item { ArtworkPhotoGrid(images = artwork.galleryImages) }
        }
    }

    val interactionRes = artworkVM.interactions.collectAsState()
    val newInteractionRes = artworkVM.newInteraction.collectAsState()

    interactionRes.value.let {
        when (it) {
            is Resource.Success -> {
                interactions.addAll(it.result)

                val visits = it.result.count { interaction ->
                    interaction.visitedByUser == true
                }
                numOfVisits.value = visits
            }
            is Resource.Loading -> { /* Handle loading state */ }
            is Resource.Failure -> { /* Handle failure state */ }
        }
    }


    newInteractionRes.value.let {
        when(it){
            is Resource.Success -> {
                isLoading.value = false

                val interaction = interactions.firstOrNull { interaction ->
                    interaction.id == it.result
                }

                if(interaction != null) {
                    if( currentUserInteraction.value != "" )
                        interaction.visitedByUser = currentUserInteraction.value.toBoolean()
                }
            }
            is Resource.Loading -> {
                //
            }
            is Resource.Failure -> {
                Toast.makeText(context, "Network error. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                isLoading.value = false
            }
            null -> {
                isLoading.value = false
            }
        }
    }
}
