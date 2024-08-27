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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
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
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.ArtworkMarker
import edu.rmas.artstreet.screens.components.SearchBar
import edu.rmas.artstreet.screens.components.SignUpInButton
import edu.rmas.artstreet.screens.components.MainUserInfo
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.screens.components.myPositionIndicator
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapScreen(
    navController: NavController,
    authVM: AuthVM,
    cameraPosition : CameraPositionState = rememberCameraPositionState(){
        position = CameraPosition.fromLatLngZoom(LatLng(43.3247, 21.9033), 17f)
    },
    isCameraSet: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    artworkVM: ArtworkVM,
    artworkMarkers: MutableList<Artwork>
) {
    authVM.getUserData()
    val userDataResource = authVM.currentUserFlow.collectAsState()
    val user = remember {
        mutableStateOf<User?>(null)
    }
    val markers = remember { mutableStateListOf<LatLng>() }
    val properties = remember {
        mutableStateOf(MapProperties(mapType = MapType.TERRAIN))
    }
    val uiSettings = remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

    val myLocation = remember {
        mutableStateOf<LatLng?>(null)
    }

    val buttonIsEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    val artworksData = artworkVM.artworks.collectAsState()
    val allArtworks = remember {
        mutableListOf<Artwork>()
    }
    artworksData.value.let {
        when (it) {
            is Resource.Success -> {
                allArtworks.clear()
                allArtworks.addAll(it.result)
            }

            is Resource.Loading -> { }
            is Resource.Failure -> { }
            null -> { }
        }
    }

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    val latitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LATITUDE, 0.0)
                    val longitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LONGITUDE, 0.0)

                    myLocation.value = LatLng(latitude, longitude)
                }
            }
        }
    }

    val context = LocalContext.current

    val filtersOn = remember {
        mutableStateOf(false)
    }
    val filteredArtworks = remember { mutableListOf<Artwork>() }

    DisposableEffect(context) {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(LocationService.ACTION_LOCATION_UPDATE))
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(myLocation.value) {
        myLocation.value?.let {
            if (!isCameraSet.value) {
                cameraPosition.position = CameraPosition.fromLatLngZoom(it, 17f)
                isCameraSet.value = true
            }
            markers.clear()
            markers.add(it)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val gesturesEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(drawerState.isOpen) {
        gesturesEnabled.value = drawerState.isOpen
    }

    val inputValue = remember {
        mutableStateOf("")
    }

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val isFilteredIndicator = remember {
        mutableStateOf(false)
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            FilterScreen(
                viewModel = authVM,
                artworks = allArtworks,
                sheetState = sheetState,
                isFiltered = filtersOn,
                isFilteredIndicator = isFilteredIndicator,
                filteredArtwork = filteredArtworks,
                artworkMarkers = artworkMarkers,
                userLocation = myLocation.value
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
        )
        {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = gesturesEnabled.value,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor=ColorPalette.BackgroundMainDarker, drawerShape = RectangleShape){
                        Box(
                            modifier = Modifier
                                .background(ColorPalette.BackgroundMainEvenDarker)
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            if (user.value != null)
                                MainUserInfo(
                                    imageUrl = user.value!!.profilePicture,
                                    name = user.value!!.fullName,
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
                                    drawerState.close()
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
                                    drawerState.close()
                                    val artworksJson = Gson().toJson(
                                        if (filtersOn.value)
                                            filteredArtworks
                                        else
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
                                    drawerState.close()
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
                                    isEnabled = buttonIsEnabled,
                                    isLoading = isLoading,
                                    onClick = {
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
                        properties = properties.value,
                        uiSettings = uiSettings.value,
                    ) {
                        markers.forEach { marker ->
                            val icon = myPositionIndicator(
                                context, R.drawable.current_location
                            )
                            Marker(
                                state = rememberMarkerState(position = marker),
                                title = "Current location",
                                icon = icon,
                                snippet = "",
                            )
                        }
                        if (!filtersOn.value) {
                            artworkMarkers.forEach { artwork ->
                                val icon = myPositionIndicator(
                                    context, R.drawable.artwork_marker
                                )
                                ArtworkMarker(
                                    artwork = artwork,
                                    icon = icon,
                                    artworksMarkers = artworkMarkers,
                                    navController = navController,
                                    notFiltered = true
                                )
                            }
                        } else {
                            filteredArtworks.forEach { artwork ->
                                val icon = myPositionIndicator(
                                    context, R.drawable.artwork_marker
                                )
                                ArtworkMarker(
                                    artwork = artwork,
                                    icon = icon,
                                    artworksMarkers = filteredArtworks,
                                    navController = navController,
                                    notFiltered = false
                                )
                            }
                        }
                    }
                    Column {
                        Spacer(modifier = Modifier.height(15.dp))
                        Row {
                            Spacer(modifier = Modifier.width(15.dp))
                            Box(
                                modifier = Modifier.background(
                                    ColorPalette.BackgroundMainLighter,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.open()
                                        }
                                    },
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .border(
                                            1.dp,
                                            ColorPalette.BackgroundMainDarker,
                                            shape = RoundedCornerShape(10.dp),
                                        )
                                        .clip(
                                            RoundedCornerShape(10.dp)
                                        ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        tint = ColorPalette.Yellow
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            SearchBar(
                                inputValue = inputValue,
                                artworkData = artworkMarkers,
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
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            sheetState.show()
                                        }
                                    },
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .border(
                                            1.dp,
                                            ColorPalette.BackgroundMainDarker,
                                            shape = RoundedCornerShape(10.dp),
                                        )
                                        .clip(
                                            RoundedCornerShape(10.dp)
                                        ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = ColorPalette.Yellow
                                    )
                                }
                            }

                        }
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Bottom
                        ) {

                            Row {
                                Box(
                                    modifier = Modifier.background(
                                        ColorPalette.BackgroundMainLighter,
                                        shape = RoundedCornerShape(7.dp)
                                    )
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (myLocation.value != null) {
                                                val location = myLocation.value!!
                                                navController.navigate(route = "${Routes.addArtworkScreen}/${location.latitude}/${location.longitude}")
                                            } else {
                                                Toast.makeText(context, "Please enable GPS!", Toast.LENGTH_SHORT).show()
                                            }
                                          },
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(50.dp)
                                            .clip(
                                                RoundedCornerShape(10.dp)
                                            ),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = ColorPalette.Yellow
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(15.dp))
                            }
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                    }
                }
            }
        }
    }

    userDataResource.value.let {
        when(it){
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

}