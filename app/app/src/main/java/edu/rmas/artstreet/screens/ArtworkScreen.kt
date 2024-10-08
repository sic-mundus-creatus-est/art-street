package edu.rmas.artstreet.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ArtworkDescription
import edu.rmas.artstreet.screens.components.Header
import edu.rmas.artstreet.screens.components.LocationTag
import edu.rmas.artstreet.screens.components.ArtworkPhotoGrid
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.screens.components.PrimaryArtworkPhoto
import edu.rmas.artstreet.screens.components.UsernameWithDate
import edu.rmas.artstreet.screens.components.VisitedInteractionButton
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ArtworkScreen(
    artwork: Artwork,
    artworkVM: ArtworkVM,
    authVM: AuthVM,
    navController: NavController
) {
    val context = LocalContext.current

    val artworksDataResource = artworkVM.artworks.collectAsState()
    val artworks = remember { mutableListOf<Artwork>() }

    val userByIdResource by authVM.userById.collectAsState()
    LaunchedEffect(artwork.capturerId) {
        authVM.getUserById(artwork.capturerId)
    }
    val user = when (val userResource = userByIdResource) {
        is Resource.Success -> userResource.result
        is Resource.Failure -> { null }
        is Resource.Loading -> { null }
        null -> null
    }

    val currentUserLocation = remember { mutableStateOf<LatLng?>(null) }
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val locationListener = rememberUpdatedState(LocationListener { location ->
        currentUserLocation.value = LatLng(location.latitude, location.longitude)
    })

    val isLoading = remember { mutableStateOf(false) }

    val interactionsResource by artworkVM.artworkInteractions.collectAsState()
    val numOfVisits by rememberUpdatedState(
        newValue = (interactionsResource as? Resource.Success)?.result?.count { interaction ->
            interaction.visitedByUser
        } ?: ""
    )

    val isNearby = currentUserLocation.value?.let { userLocation ->
        val distance = calculateDistance(
            userLocation.latitude, userLocation.longitude,
            artwork.location.latitude, artwork.location.longitude
        )
        distance <= 100
    } ?: false

    // For pixel to dp conversion
    val density = LocalDensity.current.density
    var fixedContentHeightDp by remember { mutableStateOf(0.dp) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ColorPalette.BackgroundMainEvenDarker)
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPalette.BackgroundMainEvenDarker)
                .onGloballyPositioned { coordinates ->
                    val heightInPixels = coordinates.size.height
                    fixedContentHeightDp = (heightInPixels / density).dp
                }
                .padding(bottom = 10.dp) // Padding to ensure space for LazyColumn
        ) {
            PrimaryArtworkPhoto(imageUrl = artwork.galleryImages[0])

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorPalette.BackgroundMainEvenDarker)
                    .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 0.dp)
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
                                text = "${numOfVisits}",
                                color = ColorPalette.Yellow,
                                style = TextStyle(fontSize = 24.sp, fontFamily = FontFamily.Monospace),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            VisitedInteractionButton(
                                isNearby = isNearby,
                                visited = (interactionsResource as? Resource.Success)?.result?.any {
                                    it.artworkId == artwork.id &&
                                            it.userId == authVM.currentUser?.uid &&
                                            it.visitedByUser
                                } == true,
                                onClick = {
                                    isLoading.value = true
                                    val existingInteraction = (interactionsResource as? Resource.Success)?.result?.firstOrNull {
                                        it.artworkId == artwork.id &&
                                                it.userId == authVM.currentUser?.uid
                                    }
                                    if (existingInteraction != null) {
                                        artworkVM.markAsNotVisited(existingInteraction.id)
                                    } else {
                                        artworkVM.markAsVisited(artwork.id, artwork)
                                    }
                                }
                            )
                        }
                    }

                    LocationTag(
                        location = LatLng(artwork.location.latitude, artwork.location.longitude),
                        context = context,
                        artworks = artworks,
                        navController = navController
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    TheDivider()
                    Spacer(modifier = Modifier.height(7.dp))

                    UsernameWithDate(
                        username = user?.username,
                        imageUrl = artwork.galleryImages[0],
                        user = user,
                        navController = navController
                    )
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

    LaunchedEffect(interactionsResource) {
        if (interactionsResource is Resource.Success) {
            Log.d("ArtworkScreen", "Interactions fetched: ${(interactionsResource as Resource.Success<List<Interaction>>).result.size}")
        }
    }

    when (val newInteractionRes = artworkVM.newInteraction.collectAsState().value)
    {
        is Resource.Success -> {
            artworkVM.fetchUpdatedArtworkInteractions(artwork.id)

            isLoading.value = false
        }
        is Resource.Loading -> { /* :) */ }
        is Resource.Failure -> {
            Toast.makeText(
                context,
                "Network error. Please check your connection and try again.",
                Toast.LENGTH_LONG
            ).show()
            isLoading.value = false
        }
        null -> {
            isLoading.value = false
        }
    }

    artworksDataResource.value.let {
        when (it) {
            is Resource.Success -> {
                artworks.clear()
                artworks.addAll(it.result)
            }

            is Resource.Loading -> { }
            is Resource.Failure -> { }
            null -> { }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            when {
                gpsEnabled -> {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,
                        5f,
                        locationListener.value
                    )
                }
                networkEnabled -> {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        5f,
                        locationListener.value
                    )
                }
                else -> {
                    val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    lastKnownLocation?.let {
                        currentUserLocation.value = LatLng(it.latitude, it.longitude)
                    } ?: Log.d("LocationDebug", "No last known location available")
                }
            }
        } else {
            Log.d("LocationDebug", "Location permission not granted")
        }
    }

    DisposableEffect(context) {
        onDispose {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener.value)
            }
        }
    }
}
