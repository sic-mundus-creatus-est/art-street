package edu.rmas.artstreet.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.view_models.ArtworkVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.RoundingMode
import kotlin.math.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterScreen(
    authVM: AuthVM?,
    artworkVM: ArtworkVM,
    sheetState: ModalBottomSheetState,
    artworks: List<Artwork>,
    isFiltered: MutableState<Boolean>,
    userLocation: LatLng?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val showSearchScreen = remember { mutableStateOf(false) }
    val selectedUser = remember { mutableStateOf<User?>(null) }
    val rangeValues = remember { mutableStateOf(1000f) }
    val sharedPreferences = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val options = sharedPreferences.getString("options", null)
    val range = sharedPreferences.getFloat("range", 1000f)
    val allUsersData = remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        authVM?.getAllUsersData()
        authVM?.allUsers?.collect { resource ->
            if (resource is Resource.Success) {
                allUsersData.value = resource.result
            }
        }
        options?.let {
            val savedUser = Gson().fromJson(it, User::class.java)
            selectedUser.value = savedUser
        }
        rangeValues.value = if (options != null) range else 1000f
    }

    if (showSearchScreen.value) {
        UserSearchScreen(
            allUsersData = allUsersData.value,
            onUserSelected = { user ->
                selectedUser.value = user
                showSearchScreen.value = false
            },
            onDismiss = { showSearchScreen.value = false }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPalette.BackgroundMainLighter)
                .border(
                    width = 7.dp,
                    color = ColorPalette.BackgroundMainEvenDarker,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Shared By User:",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorPalette.Yellow)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showSearchScreen.value = true },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPalette.Yellow,
                        contentColor = ColorPalette.White,
                    ),
                    shape = RectangleShape
                ) {
                    Text(
                        text = selectedUser.value?.let { "${it.fullName} [${it.username}]" }
                            ?: "Search for a username or full name",
                        modifier = Modifier.padding(vertical = 14.dp),
                        style = TextStyle(color = ColorPalette.Secondary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Distance",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorPalette.Yellow)
                    )
                    Text(
                        text = if (rangeValues.value != 1000f)
                            "${rangeValues.value.toBigDecimal().setScale(1, RoundingMode.UP)}m"
                        else "Unlimited",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorPalette.Yellow)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                RangeSliderExample(rangeValues = rangeValues)

                Spacer(modifier = Modifier.height(30.dp))

                CustomFilterButton {
                    applyFilters(
                        artworkVM = artworkVM,
                        artworks = artworks,
                        rangeValues = rangeValues,
                        selectedUser = selectedUser.value,
                        userLocation = userLocation,
                        isFiltered = isFiltered,
                        sharedPreferences = sharedPreferences,
                        coroutineScope = coroutineScope,
                        sheetState = sheetState
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CustomResetFilters {
                    resetFilters(
                        artworkVM = artworkVM,
                        isFiltered = isFiltered,
                        selectedUser = selectedUser,
                        rangeValues = rangeValues,
                        sharedPreferences = sharedPreferences,
                        coroutineScope = coroutineScope,
                        sheetState = sheetState
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


@Composable
fun UserSearchScreen(
    allUsersData: List<User>,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredUsers = remember(searchQuery.text) {
        allUsersData.filter {
            it.fullName.contains(searchQuery.text, ignoreCase = true) ||
                    it.username.contains(searchQuery.text, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by name or username") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredUsers) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUserSelected(user)
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "${user.fullName} [${user.username}]")
                }
            }
        }
    }
}

@Composable
fun RangeSliderExample(
    rangeValues: MutableState<Float>
) {
    Slider(
        value = rangeValues.value,
        onValueChange = { rangeValues.value = it },
        valueRange = 1f..1000f,
        steps = 50,
        colors = SliderDefaults.colors(
            thumbColor = ColorPalette.Yellow,
            activeTrackColor = ColorPalette.BackgroundMainEvenDarker,
            inactiveTrackColor = ColorPalette.BackgroundMainDarker
        )
    )
}

@Composable
fun CustomFilterButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(ColorPalette.Yellow, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPalette.Yellow,
            contentColor = ColorPalette.Black,
            disabledContainerColor = ColorPalette.Secondary,
            disabledContentColor = ColorPalette.White
        )
    ) {
        Text(
            "Filter",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPalette.Secondary
            )
        )
    }
}

@Composable
fun CustomResetFilters(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(ColorPalette.Yellow, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPalette.Yellow,
            contentColor = ColorPalette.Black,
            disabledContainerColor = ColorPalette.Secondary,
            disabledContentColor = ColorPalette.White
        )
    ) {
        Text(
            "Reset Filters",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPalette.Secondary
            )
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun applyFilters(
    artworkVM: ArtworkVM,
    artworks: List<Artwork>,
    rangeValues: MutableState<Float>,
    selectedUser: User?,
    userLocation: LatLng?,
    isFiltered: MutableState<Boolean>,
    sharedPreferences: SharedPreferences,
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState
) {
    artworkVM.updateFilteredArtworks(emptyList())

    val filteredList = mutableListOf<Artwork>().apply {
        if (rangeValues.value != 1000f && userLocation != null) {
            addAll(
                artworks.filter { artwork ->
                    calculateDistance(
                        userLocation.latitude,
                        userLocation.longitude,
                        artwork.location.latitude,
                        artwork.location.longitude
                    ) <= rangeValues.value
                }
            )
            sharedPreferences.edit().putFloat("range", rangeValues.value).apply()
        } else {
            addAll(artworks)
        }

        selectedUser?.let { user ->
            retainAll { it.capturerId == user.id }
            sharedPreferences.edit().putString("options", Gson().toJson(user)).apply()
        }
    }

    artworkVM.updateFilteredArtworks(filteredList)
    isFiltered.value = true

    coroutineScope.launch {
        sheetState.hide()
    }
}




@OptIn(ExperimentalMaterialApi::class)
private fun resetFilters(
    artworkVM: ArtworkVM,
    isFiltered: MutableState<Boolean>,
    selectedUser: MutableState<User?>,
    rangeValues: MutableState<Float>,
    sharedPreferences: SharedPreferences,
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState
) {
    selectedUser.value = null
    rangeValues.value = 1000f
    artworkVM.updateFilteredArtworks(emptyList())
    sharedPreferences.edit().clear().apply()
    isFiltered.value = false

    coroutineScope.launch {
        sheetState.hide()
    }
}




fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double
{//Haversine formula
    val earthRadius = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}
