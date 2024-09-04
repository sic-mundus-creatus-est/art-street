package edu.rmas.artstreet.screens

import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import edu.rmas.artstreet.R
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ArtworkGalleryUploadField
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.CopyrightText
import edu.rmas.artstreet.screens.components.DashedLineBackground
import edu.rmas.artstreet.screens.components.Header
import edu.rmas.artstreet.screens.components.InputField
import edu.rmas.artstreet.screens.components.InputFieldLabel
import edu.rmas.artstreet.screens.components.MultilineInputField
import edu.rmas.artstreet.screens.components.PostButton
import edu.rmas.artstreet.view_models.ArtworkVM
import java.util.Locale

@Composable
fun AddArtworkScreen(
    artworkVM: ArtworkVM?,
    location: MutableState<LatLng>?,
    navController: NavController
) {
    val context = LocalContext.current
    var addressText by remember { mutableStateOf("Fetching location...") }

    LaunchedEffect(location?.value) {
        location?.value?.let { latLng ->
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            addressText = if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val streetNumber = address.subThoroughfare ?: ""
                val streetName = address.thoroughfare ?: ""
                val locality = address.locality ?: ""
                val country = address.countryName ?: ""
                "$streetName $streetNumber, $locality, $country"
            } else {
                "Unknown location"
            }
        } ?: run {
            addressText = "Location not available"
        }
    }

    val artworksFlow = artworkVM?.artwork?.collectAsState()

    val title = remember {
        mutableStateOf("")
    }
    val isTitleError = remember { mutableStateOf(false) }
    val titleErrorText = remember { mutableStateOf("Oh, no!") }

    val description = remember {
        mutableStateOf("")
    }

    val selectedPhotos = remember {
        mutableStateOf<List<Uri>>(emptyList())
    }
    val buttonIsEnabled = remember {
        mutableStateOf(true)
    }
    val buttonIsLoading = remember {
        mutableStateOf(false)
    }

    val isAdded = remember {
        mutableStateOf(false)
    }

    DashedLineBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 24.dp, top = 44.dp, bottom = 14.dp)
        ) {
            // Header
            Header(header_text = "Add new artwork location")
            Spacer(modifier = Modifier.height(20.dp))

            // Image
            Image(
                painter = painterResource(id = R.drawable.street_view),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(bottom = 16.dp)
            )

            // Location text
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = addressText,
                    color = ColorPalette.LightGray,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 14.dp)
                )
            }

            Spacer(modifier = Modifier.height(17.dp))

            // Title
            InputFieldLabel(label = "Title")
            InputField(
                hint = "Example: Creative Title",
                value = title,
                isError = isTitleError,
                errorText = titleErrorText
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Photos
            InputFieldLabel(label = "Photos")
            ArtworkGalleryUploadField(selectedImages = selectedPhotos)

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            InputFieldLabel(label = "Description")
            Spacer(modifier = Modifier.height(10.dp))
            MultilineInputField(
                inputValue = description,
                inputText = "What makes this artwork unique? Share your thoughts and observations..."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Post Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PostButton(
                    onClick = {
                        isAdded.value = true
                        buttonIsLoading.value = true
                        artworkVM?.saveArtworkData(
                            title = title.value,
                            location = location,
                            description = description.value,
                            primaryImage = selectedPhotos.value[0],
                            galleryImages = selectedPhotos.value,
                        )
                    },
                    text = "ADD",
                    isEnabled = buttonIsEnabled,
                    isLoading = buttonIsLoading,
                    modifier = Modifier
                )
            }

            Spacer(modifier = Modifier.height(101.dp))

            // Copyright
            CopyrightText(
                year = 2024,
                owner = "18859",
                modifier = Modifier
                    .align(Alignment.End)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }

    artworksFlow?.value.let {
        when(it){
            is Resource.Failure -> {
                buttonIsLoading.value = false
                val context = LocalContext.current
                Toast.makeText(context, "Error while adding a new art location", Toast.LENGTH_LONG).show()
            }
            is Resource.Loading -> { }
            is Resource.Success -> {
                buttonIsLoading.value = false
                if(isAdded.value)
                    navController.navigate(Routes.mapScreen)
                artworkVM?.getAllArtworks()
            }
            null -> {}
        }
    }
}

//@Preview(showBackground = true, device = Devices.PIXEL_4)
//@Composable
//fun PreviewAddArtworkScreen() {
//    val mockLocation = remember {mutableStateOf(LatLng(37.7749, -122.4194))  } // Mock location (San Francisco)
//    val mockNavController = rememberNavController()
//
//    AddArtworkScreen(
//        artworkVM = null,
//        location = mockLocation,
//        navController = mockNavController
//    )
//}
