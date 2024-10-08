package edu.rmas.artstreet.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import edu.rmas.artstreet.R
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.extractDateFromTimestamp
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.view_models.ArtworkVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterScreen (
    authVM: AuthVM?,
    artworkVM: ArtworkVM,
    sheetState: ModalBottomSheetState,
    artworks: List<Artwork>,
    userLocation: LatLng?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val showFilterByUserSection = remember { mutableStateOf(false) }

    val selectedUser = remember { mutableStateOf<User?>(null) }
    val selectedDistance = remember { mutableStateOf(1000f) }

    val sharedPrefsFilters = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val sharedPrefsOptions = sharedPrefsFilters.getString("options", null)
    val sharedPrefsRange = sharedPrefsFilters.getFloat("range", 1000f)

    val allUsersData = remember { mutableStateOf<List<User>>(emptyList()) }

    val isDistanceSliderEnabled = userLocation != null

    val startDate = remember { mutableStateOf<LocalDate?>(null) }
    val endDate = remember { mutableStateOf<LocalDate?>(null) }

// -------------------------------------------------------------------------------------------------
// >> USER INTERFACE
    Box ( modifier = Modifier
        .fillMaxSize()
        .background(ColorPalette.BackgroundMainLighter)
        .border(
            width = 4.dp,
            color = ColorPalette.BackgroundMainDarker,
            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
        )
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 19.dp)
                .align(Alignment.TopCenter)
                .height(5.dp)
                .width(124.dp)
                .background(ColorPalette.LightGray, RoundedCornerShape(5.dp))
        ) {}
        if (showFilterByUserSection.value)
        {// -[[ FILTER BY USER SECTION ]]-
            FilterByUser (
                allUsersData = allUsersData.value,
                onUserSelected = { user ->
                    selectedUser.value = user
                    showFilterByUserSection.value = false
                },
                onDismiss = { showFilterByUserSection.value = false }
            )
        } else {
            Column (// -[[ ALL FILTERING OPTIONS SECTION ]]-
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 47.dp, horizontal = 17.dp)
            ) {
            // -------------------------------------------------------------------------------------
            // PARAMETER: -[[ USER ]]-
                Text (
                    text = "Shared By User",
                    style = TextStyle (
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPalette.Yellow,
                        fontStyle = FontStyle.Italic
                    )
                )

                UserButton(
                    selectedUser = selectedUser,
                    showFilterByUserSection = showFilterByUserSection
                )
            // -------------------------------------------------------------------------------------
            // PARAMETER: -[[ DISTANCE ]]-
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Distance",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDistanceSliderEnabled) ColorPalette.Yellow else ColorPalette.LightGray,
                            fontStyle = FontStyle.Italic
                        )
                    )
                    Text(
                        text = if (isDistanceSliderEnabled) {
                            if (selectedDistance.value != 1000f)
                                "${selectedDistance.value.toBigDecimal().setScale(1, RoundingMode.UP)} m"
                            else "Unlimited (1 km or further)"
                        } else {
                            "Turn on location for this option"
                        },
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorPalette.LightGray,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                DistanceRangeSlider(rangeValues = selectedDistance, isDistanceSliderEnabled)
            // -------------------------------------------------------------------------------------
            // PARAMETER: -[[ DATE RANGE ]]-
                Text(
                    text = "Date Range",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPalette.Yellow,
                        fontStyle = FontStyle.Italic
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 17.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Starting from   ",
                        style = TextStyle(fontSize = 16.sp, color = ColorPalette.LightGray, fontWeight = FontWeight.Bold)
                    )

                    DatePickerButton(
                        date = startDate.value,
                        onDateSelected = { startDate.value = it },
                        label = "START DATE",
                    )

                    Text(
                        text = "   to   ",
                        style = TextStyle(fontSize = 16.sp, color = ColorPalette.LightGray, fontWeight = FontWeight.Bold)
                    )

                    DatePickerButton(
                        date = endDate.value,
                        onDateSelected = { endDate.value = it },
                        label = "END DATE",
                    )
                }
            // -------------------------------------------------------------------------------------
            // -[[ APPLY AND RESET FILTERS BUTTONS ]]-
                Spacer(modifier = Modifier.height(30.dp))

                ApplyFiltersButton {
                    applyFilters(
                        artworkVM = artworkVM,
                        artworks = artworks,
                        rangeValues = selectedDistance,
                        selectedUser = selectedUser.value,
                        userLocation = userLocation,
                        startDate = startDate.value,
                        endDate = endDate.value,
                        sharedPreferences = sharedPrefsFilters,
                        coroutineScope = coroutineScope,
                        sheetState = sheetState
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ResetFiltersButton {
                    resetFilters(
                        artworkVM = artworkVM,
                        selectedUser = selectedUser,
                        rangeValues = selectedDistance,
                        startDate = startDate,
                        endDate = endDate,
                        sharedPreferences = sharedPrefsFilters,
                        coroutineScope = coroutineScope,
                        sheetState = sheetState
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            // -------------------------------------------------------------------------------------
            }
        }
    }
// -------------------------------------------------------------------------------------------------

    // -------------------------------------------------------------------------------------
    LaunchedEffect(Unit)
    {
        authVM?.getAllUsersData()
        authVM?.allUsers?.collect { resource ->
            if (resource is Resource.Success)
            {
                allUsersData.value = resource.result
            }
        }
        sharedPrefsOptions?.let {
            val savedUser = Gson().fromJson(it, User::class.java)
            selectedUser.value = savedUser
        }

        selectedDistance.value = if (sharedPrefsOptions != null) sharedPrefsRange else 1000f
    }
    // -------------------------------------------------------------------------------------

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterByUser (
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
            .padding(vertical = 34.dp, horizontal = 17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by User",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorPalette.Yellow, fontStyle = FontStyle.Italic)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = ColorPalette.BackgroundMainEvenDarker
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Type a name or username") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(
                focusedTextColor = ColorPalette.Black,
                cursorColor = ColorPalette.Secondary,
                focusedIndicatorColor = ColorPalette.Yellow,
                unfocusedIndicatorColor = ColorPalette.LightGray,
                focusedLabelColor = ColorPalette.DarkGrey,
                unfocusedLabelColor = ColorPalette.LightGray,
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(filteredUsers) { user ->
                TheDivider(thickness = 1.dp)
                UserRow(user = user, onUserSelected = onUserSelected)
            }
        }
    }
}

@Composable
fun UserRow(
    user: User,
    onUserSelected: (User) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserSelected(user) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.profilePicture.ifEmpty { R.drawable.user},
            contentDescription = "${user.fullName}'s profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(47.dp)
                .height(47.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Box {
                Text(
                    text = user.fullName,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPalette.BackgroundMainEvenDarker,
                        fontFamily = FontFamily.Default
                    ),
                    modifier = Modifier
                        .offset(x = 1.dp, y = 1.dp) // Offset for stroke effect
                )

                Text(
                    text = user.fullName,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPalette.Yellow,
                        fontFamily = FontFamily.Default
                    )
                )
            }

            Text(
                text = "@${user.username}",
                style = TextStyle(fontSize = 14.sp, color = ColorPalette.LightGray)
            )
        }
    }
}


@Composable
fun UserButton(
    selectedUser: MutableState<User?>,
    showFilterByUserSection: MutableState<Boolean>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp, horizontal = 17.dp)
            .background(ColorPalette.White, RoundedCornerShape(4.dp))
            .border(
                width = 2.dp,
                color = ColorPalette.BackgroundMainDarker,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        UserRow(
            user = selectedUser.value ?: User(
                fullName = "Anyone",
                username = "username",
                profilePicture = ""
            ),
            onUserSelected = { showFilterByUserSection.value = true }
        )

        if (selectedUser.value != null) {
            IconButton(
                onClick = {
                    selectedUser.value = null
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = ColorPalette.DarkGrey
                )
            }
        }
    }
}





@Composable
fun DistanceRangeSlider(
    rangeValues: MutableState<Float>,
    isEnabled: Boolean
) {
    val sliderColors = if (isEnabled) {
        SliderDefaults.colors(
            thumbColor = ColorPalette.Yellow,
            activeTrackColor = ColorPalette.Yellow,
            inactiveTrackColor = ColorPalette.White,
            activeTickColor = ColorPalette.BackgroundMainDarker
        )
    } else {
        SliderDefaults.colors(
            thumbColor = ColorPalette.LightGray,
            activeTrackColor = ColorPalette.LightGray,
            inactiveTrackColor = ColorPalette.LightGray,
            activeTickColor = ColorPalette.LightGray
        )
    }

    Slider(
        value = rangeValues.value,
        onValueChange = { rangeValues.value = it },
        valueRange = 0f..1000f,
        steps = 19,  // Steps between 0 and 1000, stepping by 50
        enabled = isEnabled,
        colors = sliderColors
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerButton(
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Text(
        text = date?.format(formatter) ?: label,
        style = TextStyle(
            fontSize = 16.sp,
            color = ColorPalette.Yellow,
            textDecoration = TextDecoration.Underline,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        ),
        modifier = modifier.clickable {
            val today = LocalDate.now()
            DatePickerDialog( context, { _, year, month, dayOfMonth ->
                    onDateSelected (
                        LocalDate.of(
                            year,
                            month + 1,
                            dayOfMonth
                        )
                    )
                }, today.year, today.monthValue - 1, today.dayOfMonth).show()
        }
    )
}



@Composable
fun ApplyFiltersButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .width(400.dp)
                .height(54.dp)
                .background(ColorPalette.Yellow, RoundedCornerShape(5.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorPalette.Yellow,
                contentColor = ColorPalette.Black,
                disabledContainerColor = ColorPalette.Secondary,
                disabledContentColor = ColorPalette.White
            )
        ) {
            Text(
                "Apply Filters",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPalette.Secondary,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
    }
}

@Composable
fun ResetFiltersButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .width(240.dp)
                .height(37.dp)
                .background(ColorPalette.BackgroundMainEvenDarker, RoundedCornerShape(10.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorPalette.BackgroundMainEvenDarker,
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
                    color = ColorPalette.LightGray,
                    fontFamily = FontFamily.SansSerif
                )
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
private fun applyFilters(
    artworkVM: ArtworkVM,
    artworks: List<Artwork>,
    rangeValues: MutableState<Float>,
    selectedUser: User?,
    userLocation: LatLng?,
    startDate: LocalDate?,
    endDate: LocalDate?,
    sharedPreferences: SharedPreferences,
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState
) {
    artworkVM.updateFilteredArtworks(emptyList(), false)

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

        if (startDate != null && endDate != null) {
            retainAll { artwork ->
                val artworkDate = LocalDate.parse(extractDateFromTimestamp(artwork.galleryImages[0], true), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                (artworkDate.isAfter(startDate) || artworkDate.isEqual(startDate)) &&
                        (artworkDate.isBefore(endDate) || artworkDate.isEqual(endDate))
            }
        }
    }

    artworkVM.updateFilteredArtworks(filteredList, true)

    coroutineScope.launch {
        sheetState.hide()
    }
}




@OptIn(ExperimentalMaterialApi::class)
private fun resetFilters(
    artworkVM: ArtworkVM,
    selectedUser: MutableState<User?>,
    rangeValues: MutableState<Float>,
    startDate: MutableState<LocalDate?>,
    endDate: MutableState<LocalDate?>,
    sharedPreferences: SharedPreferences,
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState
) {
    selectedUser.value = null
    rangeValues.value = 1000f
    startDate.value = null
    endDate.value = null
    artworkVM.updateFilteredArtworks(emptyList(), false)
    sharedPreferences.edit().clear().apply()

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
