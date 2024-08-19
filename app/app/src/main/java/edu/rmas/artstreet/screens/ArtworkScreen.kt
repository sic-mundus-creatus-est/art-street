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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.screens.components.BackButton
import edu.rmas.artstreet.screens.components.Header
import edu.rmas.artstreet.screens.components.LocationView
import edu.rmas.artstreet.screens.components.ArtworkGalleryShowcase
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.PrimaryArtworkPhoto
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ArtworkScreen(
    navController: NavController,
    artworkVM: ArtworkVM,
    authVM: AuthVM,
    artwork: Artwork,
    artworks: MutableList<Artwork>?
) {

    val context = LocalContext.current

    val isLoading = remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxSize()
        .background(ColorPalette.BackgroundMainLighter)){

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ){
            item {
                BackButton {
                    if(artworks == null) {
                        navController.popBackStack()
                    }else{
                        val isCameraSet = true
                        val latitude = artwork.location.latitude
                        val longitude = artwork.location.longitude

                        val artworksJson = Gson().toJson(artworks)
                        val encodedArtworksJson = URLEncoder.encode(artworksJson, StandardCharsets.UTF_8.toString())
                        navController.navigate(Routes.mapScreen + "/$isCameraSet/$latitude/$longitude/$encodedArtworksJson")
                    }
                }
            }
            item{ PrimaryArtworkPhoto(imageUrl = artwork.primaryImage)}
            item{ Spacer(modifier = Modifier.height(20.dp)) }
            item { Header(header_text = artwork.title.replace('+', ' ')) }
            item{ Spacer(modifier = Modifier.height(20.dp))}
            item{ LocationView(location = LatLng(artwork.location.latitude, artwork.location.longitude), context = context)}
            item{ Spacer(modifier = Modifier.height(20.dp))}
            item{ Text(text = artwork.description.replace('+', ' '), color = ColorPalette.White)}
            item{ Spacer(modifier = Modifier.height(20.dp))}
            item { ArtworkGalleryShowcase(images = artwork.galleryImages) }
            item{ Spacer(modifier = Modifier.height(60.dp))}
        }

    }
}