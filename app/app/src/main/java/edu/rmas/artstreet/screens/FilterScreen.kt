package edu.rmas.artstreet.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.view_models.AuthVM
import com.google.gson.Gson
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ColorPalette
import kotlinx.coroutines.launch
import java.math.RoundingMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterScreen(
    viewModel: AuthVM?,
    artworks: MutableList<Artwork>,
    sheetState: ModalBottomSheetState,
    isFiltered: MutableState<Boolean>,
    isFilteredIndicator: MutableState<Boolean>,
    filteredArtwork: MutableList<Artwork>,
    artworkMarkers: MutableList<Artwork>,
    userLocation: LatLng?
) {
    val context = LocalContext.current

    viewModel?.getAllUsersData()
    val allUsersResource = viewModel?.allUsers?.collectAsState()

    val allUsersNames = remember {
        mutableListOf<String>()
    }

    val sharedPreferences = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val options = sharedPreferences.getString("options", null)
    val range = sharedPreferences.getFloat("range", 1000f)

    val initialCheckedState = remember {
        mutableStateOf(List(allUsersNames.size) { false })
    }
    val rangeValues = remember { mutableFloatStateOf(1000f) }

    val filtersSet = remember {
        mutableStateOf(false)
    }

    if (isFilteredIndicator.value && options != null) {
        val type = object : TypeToken<List<Boolean>>() {}.type
        val savedOptions: List<Boolean> = Gson().fromJson(options, type) ?: emptyList()
        initialCheckedState.value = savedOptions
    }
    if(!filtersSet.value) {
        if (isFilteredIndicator.value) {
            rangeValues.floatValue = range
        }
        filtersSet.value = true
    }

    val allUsersData = remember {
        mutableListOf<User>()
    }

    val selectedOptions = remember {
        mutableStateOf(initialCheckedState.value)
    }

    val isSet = remember { mutableStateOf(false) }

    allUsersResource?.value.let {
        when(it){
            is Resource.Failure -> {}
            is Resource.Success -> {
                allUsersNames.clear()
                allUsersData.clear()
                allUsersNames.addAll(it.result.map { user -> user.fullName})
                allUsersData.addAll(it.result)
                if(!isSet.value) {
                    initialCheckedState.value =
                        List(allUsersNames.count()) { false }.toMutableList()
                    isSet.value = true
                }
            }
            Resource.Loading -> {}
            null -> {}
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val expanded = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Author",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))


        Column{
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded.value = !expanded.value })
                    .background(ColorPalette.Yellow, RoundedCornerShape(4.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Choose Authors")
                Icon(
                    if (expanded.value) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown icon"
                )
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorPalette.White)
            ) {
                allUsersNames.forEachIndexed { index, option ->
                    DropdownMenuItem(onClick = {
                        val updatedCheckedState = initialCheckedState.value.toMutableList()
                        updatedCheckedState[index] = !updatedCheckedState[index]
                        initialCheckedState.value = updatedCheckedState
                        selectedOptions.value = updatedCheckedState
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = initialCheckedState.value[index],
                                onCheckedChange = {
                                    val updatedCheckedState = initialCheckedState.value.toMutableList()
                                    updatedCheckedState[index] = it
                                    initialCheckedState.value = updatedCheckedState
                                    selectedOptions.value = updatedCheckedState
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Distance",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text =
                if(rangeValues.floatValue != 1000f)
                    rangeValues.floatValue.toBigDecimal().setScale(1, RoundingMode.UP).toString() + "m"
                else
                    "Unlimited"
                ,style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RangeSliderExample(rangeValues = rangeValues)
        Spacer(modifier = Modifier.height(30.dp))
        CustomFilterButton {
            artworkMarkers.clear()
            val filteredArtworks = artworks.toMutableList()

            if (rangeValues.floatValue != 1000f) {
                filteredArtworks.retainAll { artwork ->
                    calculateDistance(
                        userLocation!!.latitude,
                        userLocation.longitude,
                        artwork.location.latitude,
                        artwork.location.longitude
                    ) <= rangeValues.floatValue
                }
                with(sharedPreferences.edit()) {
                    putFloat("range", rangeValues.floatValue)
                    apply()
                }
            }


            if (selectedOptions.value.indexOf(true) != -1) {
                val selectedAuthors = allUsersData.filterIndexed { index, _ ->
                    selectedOptions.value[index]
                }
                val selectedIndices = selectedAuthors.map { item -> item.id }
                filteredArtworks.retainAll { it.capturerId in selectedIndices }



                val selectedOptionsJson = Gson().toJson(selectedOptions.value)
                with(sharedPreferences.edit()) {
                    putString("options", selectedOptionsJson)
                    apply()
                }
            }
            filteredArtwork.clear()
            filteredArtwork.addAll(filteredArtworks)

            isFiltered.value = false
            isFiltered.value = true

            coroutineScope.launch {
                sheetState.hide()
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        CustomResetFilters {
            artworkMarkers.clear()
            artworkMarkers.addAll(artworks)

            initialCheckedState.value =
                List(allUsersNames.count()) { false }.toMutableList()
            rangeValues.floatValue = 1000f

            isFiltered.value = true
            isFiltered.value = false
            isFilteredIndicator.value = false

            with(sharedPreferences.edit()) {
                putFloat("range", 1000f)
                putString("options", null)
                apply()
            }

            coroutineScope.launch {
                sheetState.hide()
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun RangeSliderExample(
    rangeValues: MutableState<Float>
) {
    androidx.compose.material3.Slider(
        value = rangeValues.value,
        onValueChange = { rangeValues.value = it },
        valueRange = 0f..1000f,
        steps = 50,
        colors = SliderDefaults.colors(
            thumbColor = ColorPalette.Secondary,
            activeTrackColor = ColorPalette.Purple500,
            inactiveTrackColor = ColorPalette.Purple200
        )
    )
}

@Composable
fun CustomFilterButton(
    onClick: () -> Unit
){
    androidx.compose.material3.Button(
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
        ),

        ) {
        Text(
            "Filter",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun CustomResetFilters(
    onClick: () -> Unit
){
    androidx.compose.material3.Button(
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
        ),

        ) {
        Text(
            "Reset Filters",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPalette.White
            )
        )
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}