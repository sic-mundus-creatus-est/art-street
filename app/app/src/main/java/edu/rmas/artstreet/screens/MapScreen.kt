package edu.rmas.artstreet.screens

import android.Manifest
import android.location.Location
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import edu.rmas.artstreet.R
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.data.services.LocationService
import edu.rmas.artstreet.screens.components.AddNewArtworkLocationButton
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.ArtworkMarker
import edu.rmas.artstreet.screens.components.FilterBottomSheetButton
import edu.rmas.artstreet.screens.components.FilterStatusTextBadge
import edu.rmas.artstreet.screens.components.SearchBar
import edu.rmas.artstreet.screens.components.SignUpInButton
import edu.rmas.artstreet.screens.components.MainUserInfo
import edu.rmas.artstreet.screens.components.SideBarMenuDrawer
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.screens.components.myPositionIndicator
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapScreen (
    navController: NavController,
    authVM: AuthVM,
    cameraPosition: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.3247, 21.9033), 17f)
    },
    isCameraSet: MutableState<Boolean> = remember { mutableStateOf(false) },
    artworkVM: ArtworkVM,
    artworkMarkers: MutableList<Artwork>
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authVM.getUserData()
    }
    val userDataResource = authVM.currentUserFlow.collectAsState()
    val user = remember { mutableStateOf<User?>(null) }
    val currentUserLocation = remember { mutableStateOf<LatLng?>(null) }

    val mapProperties = remember { mutableStateOf(MapProperties(mapType = MapType.TERRAIN)) }
    val mapUISettings = remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

    val btnIsEnabled = remember { mutableStateOf(true) }
    val btnIsLoading = remember { mutableStateOf(false) }

    val artworksDataResource = artworkVM.artworks.collectAsState()
    val artworks = remember { mutableListOf<Artwork>() }
    val filteredArtworks by artworkVM.filteredArtworks.collectAsState()
    val filtersOn by artworkVM.filtersOn.collectAsState()

    val sidebarMenuDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val filterBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val gesturesEnabled = remember { mutableStateOf(false) }

    val searchInputValue = remember { mutableStateOf("") }

    val currentLocationMarkerState = rememberMarkerState(position = LatLng(43.3247, 21.9033))

    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val locationListener = rememberUpdatedState(LocationListener { location ->
        Log.d("LocationDebug", "Location updated: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
        currentUserLocation.value = LatLng(location.latitude, location.longitude)
    })

    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val isServiceRunning = isLocationServiceRunning(context)
    val nearbyArtworksOn = remember {
        mutableStateOf(isServiceRunning)
    }

    ModalBottomSheetLayout (
        modifier = Modifier.fillMaxSize(),
        sheetState = filterBottomSheetState,
        sheetContent = {
            FilterScreen (
                authVM = authVM,
                artworkVM = artworkVM,
                artworks = artworks,
                sheetState = filterBottomSheetState,
                userLocation = currentUserLocation.value
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
        ) {
            ModalNavigationDrawer (
                drawerState = sidebarMenuDrawerState,
                gesturesEnabled = gesturesEnabled.value,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = ColorPalette.BackgroundMainDarker, drawerShape = RectangleShape) {
                        Box (
                            modifier = Modifier
                                .background(ColorPalette.BackgroundMainEvenDarker)
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            if (user.value != null)
                                MainUserInfo (
                                    imageUrl = user.value!!.profilePicture,
                                    name = user.value!!.fullName,
                                    username = user.value!!.username
                                )
                        }
                        NavigationDrawerItem(
                            label = { Text(text = "Profile", color = ColorPalette.White) },
                            selected = false,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "profile",
                                    tint = ColorPalette.Yellow
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ColorPalette.BackgroundMainDarker,
                                unselectedContainerColor = ColorPalette.BackgroundMainDarker
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    sidebarMenuDrawerState.close()
                                    val userJson = Gson().toJson(user.value)
                                    val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
                                    navController.navigate("${Routes.profileScreen}/$encodedUserJson")
                                }
                            },
                            modifier = Modifier.clip(RectangleShape)
                        )
                        TheDivider(thickness = 1.dp)
                        NavigationDrawerItem(
                            label = { Text(text = "Art Feed", color = ColorPalette.White) },
                            selected = false,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.ArtTrack,
                                    contentDescription = "artworks",
                                    tint = ColorPalette.Yellow
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ColorPalette.BackgroundMainDarker,
                                unselectedContainerColor = ColorPalette.BackgroundMainDarker
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    sidebarMenuDrawerState.close()
                                    val artworksJson = Gson().toJson(
                                        artworkMarkers
                                    )
                                    val encodedArtworksJson = URLEncoder.encode(artworksJson, StandardCharsets.UTF_8.toString())
                                    navController.navigate(Routes.artFeedScreen + "/$encodedArtworksJson")
                                }
                            },
                            modifier = Modifier.clip(RectangleShape)
                        )
                        TheDivider(thickness = 1.dp)
                        NavigationDrawerItem(
                            label = { Text(text = "Leaderboard", color = ColorPalette.White) },
                            selected = false,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Leaderboard,
                                    contentDescription = "leaderboard",
                                    tint = ColorPalette.Yellow
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ColorPalette.BackgroundMainDarker,
                                unselectedContainerColor = ColorPalette.BackgroundMainDarker
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    navController.navigate(Routes.leaderboardScreen)
                                }
                            },
                            modifier = Modifier.clip(RectangleShape)
                        )
                        TheDivider(thickness = 1.dp)
                        NavigationDrawerItem(
                            modifier = Modifier.clip(RectangleShape),
                            label = { Text(text = "Settings", color = ColorPalette.White) },
                            selected = false,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ColorPalette.BackgroundMainDarker,
                                unselectedContainerColor = ColorPalette.BackgroundMainDarker
                            ),
                            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "settings", tint = ColorPalette.Yellow) },
                            onClick = {
                                coroutineScope.launch {
                                    sidebarMenuDrawerState.close()
                                    navController.navigate(Routes.settingsScreen)
                                }
                            },
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                SignUpInButton(
                                    text = "SIGN OUT",
                                    icon = Icons.AutoMirrored.Filled.Logout,
                                    isEnabled = btnIsEnabled,
                                    isLoading = btnIsLoading,
                                    onClick = {
                                        Intent(context, LocationService::class.java).apply {
                                            action = LocationService.ACTION_STOP
                                            context.stopService(this)
                                        }
                                        with(sharedPreferences.edit()) {
                                            putBoolean("following_location", false)
                                            apply()
                                        }
                                        authVM.signOut()
                                        navController.navigate(Routes.signInScreen)
                                    },
                                )
                            }
                        }
                    }
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPosition,
                        properties = mapProperties.value,
                        uiSettings = mapUISettings.value,
                    ) {
                        // Marker for current location
                        currentUserLocation.value?.let { location ->
                            currentLocationMarkerState.position = location
                            val icon = myPositionIndicator(context, R.drawable.current_location)
                            Marker (
                                state = currentLocationMarkerState,
                                title = "Your Location",
                                icon = icon,
                                snippet = ""
                            )
                        }
                        if (!filtersOn) {
                            artworkMarkers.forEach { artwork ->
                                val icon = myPositionIndicator(context, R.drawable.artwork_marker)
                                ArtworkMarker (
                                    artwork = artwork,
                                    icon = icon,
                                    navController = navController,
                                    notFiltered = true
                                )
                            }
                        } else {
                            filteredArtworks?.let { artworks ->
                                val mutableArtworks = artworks.toMutableList()
                                mutableArtworks.forEach { artwork ->
                                    val icon = myPositionIndicator(context, R.drawable.artwork_marker)
                                    ArtworkMarker (
                                        artwork = artwork,
                                        icon = icon,
                                        navController = navController,
                                        notFiltered = false
                                    )
                                }
                            }
                        }
                    }
                    Column {
                        Spacer(modifier = Modifier.height(15.dp))
                        // -----------------------------------------------------------------------------
                        // -[[ TOP BAR NAVIGATION ]]-
                        Row {
                            Spacer(modifier = Modifier.width(15.dp))

                            Box(
                                modifier = Modifier.background(
                                    ColorPalette.BackgroundMainLighter,
                                    shape = RoundedCornerShape(10.dp))
                            ) {
                                SideBarMenuDrawer(onClick = { coroutineScope.launch { sidebarMenuDrawerState.open() } })
                            }

                            Spacer(modifier = Modifier.width(5.dp))
                            SearchBar(
                                inputValue = searchInputValue,
                                artworkData = if (!filtersOn) artworkMarkers else filteredArtworks!!.toMutableList(),
                                cameraPositionState = cameraPosition,
                                navController = navController
                            )
                            Spacer(modifier = Modifier.width(5.dp))

                            Box(
                                modifier = Modifier.background(
                                    ColorPalette.BackgroundMainLighter,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            ) {
                                FilterBottomSheetButton(onClick = { coroutineScope.launch { filterBottomSheetState.show() } }, filtersOn)
                                FilterStatusTextBadge(isOn = filtersOn, modifier = Modifier.align(Alignment.BottomEnd))
                            }
                        }

                        // -----------------------------------------------------------------------------

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // -----------------------------------------------------------------------------
                            // -[[ BOTTOM BAR NAVIGATION ]]-
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(end = 19.dp, bottom = 4.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        if (currentUserLocation.value != null) {
                                            coroutineScope.launch {
                                                CenterOnTheLocation(
                                                    currentUserLocation = currentUserLocation.value,
                                                    cameraPositionState = cameraPosition,
                                                    movementThresholdInMeters = 5f
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .then(if (currentUserLocation.value == null) Modifier.alpha(0.5f) else Modifier) // semi-transparent if disabled
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Center on my location",
                                        tint = if (currentUserLocation.value == null) ColorPalette.LightGray else ColorPalette.Yellow,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                            }

                            Row {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = nearbyArtworksOn.value,
                                        onCheckedChange = {
                                            nearbyArtworksOn.value = it
                                            if (it) {
                                                Intent(context, LocationService::class.java).apply {
                                                    action = LocationService.ACTION_FIND_NEARBY
                                                    context.startForegroundService(this)
                                                }
                                                with(sharedPreferences.edit()) {
                                                    putBoolean("following_location", true)
                                                    apply()
                                                }
                                            } else {
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
                                        thumbContent = if (nearbyArtworksOn.value) {
                                            {
                                                androidx.compose.material.Icon(
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
                                            uncheckedThumbColor = ColorPalette.Yellow,
                                            uncheckedTrackColor = Color.Transparent,
                                        ),
                                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 1.dp)
                                    )

                                    ComicBubble()
                                }

                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier.background(
                                        ColorPalette.BackgroundMainLighter,
                                        shape = RoundedCornerShape(7.dp),
                                    )
                                ) {
                                    AddNewArtworkLocationButton(
                                        onClick = {
                                            if (currentUserLocation.value != null) {
                                                val location = currentUserLocation.value!!
                                                navController.navigate(route = "${Routes.addArtworkScreen}/${location.latitude}/${location.longitude}")
                                            } else {
                                                Toast.makeText(context, "Turn on location for this option!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        currentUserLocation = currentUserLocation
                                    )
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                            }
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                        // -----------------------------------------------------------------------------
                    }
                }
            }
        }
    }

// -------------------------------------------------------------------------------------------------
    userDataResource.value.let {
        when (it) {
            is Resource.Success -> {
                user.value = it.result
            }
            null -> {
                user.value = null
            }

            is Resource.Failure -> {}
            Resource.Loading -> {}
        }
    }

    artworksDataResource.value.let {
        when (it) {
            is Resource.Success -> {
                artworks.clear()
                artworks.addAll( it.result )
            }

            is Resource.Loading -> { }
            is Resource.Failure -> { }
            null -> { }
        }
    }
// -------------------------------------------------------------------------------------------------

// -------------------------------------------------------------------------------------------------
    LaunchedEffect(sidebarMenuDrawerState.isOpen) {
        gesturesEnabled.value = sidebarMenuDrawerState.isOpen
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


    LaunchedEffect(currentUserLocation.value, isCameraSet.value) {
        if (!isCameraSet.value && currentUserLocation.value != null) {
            currentUserLocation.value?.let { newLocation ->
                cameraPosition.position = CameraPosition.fromLatLngZoom(
                    LatLng(newLocation.latitude, newLocation.longitude), 17f
                )
                isCameraSet.value = true
            }
        }
    }



// -------------------------------------------------------------------------------------------------

}

@Composable
fun ComicBubble() {
    val isBubbleVisible = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        if (isBubbleVisible.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(
                        color = ColorPalette.BackgroundMainLighter,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
                    .padding(bottom = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Nearby Artworks\nNotifications",
                        style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = ColorPalette.Yellow
                    )
                }
            }
        }

        IconButton(
            onClick = { isBubbleVisible.value = !isBubbleVisible.value },
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = if (!isBubbleVisible.value) Icons.Default.Help else Icons.Default.Close,
                contentDescription = "Help Icon",
                tint = ColorPalette.Yellow
            )
        }

    }
}


suspend fun CenterOnTheLocation(
    currentUserLocation: LatLng?,
    cameraPositionState: CameraPositionState,
    movementThresholdInMeters: Float = 5f
) {
    currentUserLocation?.let { newLocation ->
        val currentCameraPosition = cameraPositionState.position.target

        val distance = FloatArray(1)
        Location.distanceBetween(
            currentCameraPosition.latitude, currentCameraPosition.longitude,
            newLocation.latitude, newLocation.longitude,
            distance
        )

        // If the user has moved more than the threshold
        if (distance[0] > movementThresholdInMeters) {
            val animationDuration = when {
                distance[0] > 1000 -> 3000L // Very far, longer duration
                distance[0] > 500 -> 2000L  // Medium distance
                else -> 1000L               // Close, short duration
            }

            try {
                val newCameraPosition = CameraPosition.Builder()
                    .target(LatLng(newLocation.latitude, newLocation.longitude))
                    .zoom(17f)
                    .build()

                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(newCameraPosition),
                    animationDuration.toInt()
                )
            } catch (e: Exception) {
                Log.e("MapError", "Failed to update camera position: ${e.localizedMessage}")
            }

            delay(animationDuration)
        }
    }
}
