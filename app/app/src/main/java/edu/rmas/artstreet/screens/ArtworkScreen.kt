package edu.rmas.artstreet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.screens.components.ArtworkDescription
import edu.rmas.artstreet.screens.components.Header
import edu.rmas.artstreet.screens.components.LocationTag
import edu.rmas.artstreet.screens.components.ArtworkPhotoGrid
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.screens.components.PrimaryArtworkPhoto
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM

@Composable
fun ArtworkScreen(
    artwork: Artwork,
) {
    val context = LocalContext.current
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
                    Header(header_text = artwork.title.replace('+', ' '))
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
}
