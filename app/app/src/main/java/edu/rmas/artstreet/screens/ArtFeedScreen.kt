package edu.rmas.artstreet.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import edu.rmas.artstreet.R
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ArtworkRow
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.view_models.ArtworkVM
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ArtFeedScreen(
    artworks: List<Artwork>?,
    navController: NavController,
    artworkVM: ArtworkVM
) {
    val artworksList = remember { mutableListOf<Artwork>() }
    if (artworks.isNullOrEmpty()) {
        val artworksResource = artworkVM.artworks.collectAsState()
        artworksResource.value.let {
            when (it) {
                is Resource.Success -> {
                    artworksList.clear()
                    artworksList.addAll(it.result)
                }
                is Resource.Loading -> {
                    // Handle loading state
                }
                is Resource.Failure -> {
                    // Handle error state
                }
                null -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorPalette.BackgroundMainDarker)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 60.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.grafitti),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (artworks.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(300.dp)
                            .background(ColorPalette.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.not_found),
                                contentDescription = "",
                                modifier = Modifier.size(150.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = "Cannot locate any art right now... Check back later!")
                        }
                    }
                } else {
                    artworks.forEach { artwork ->
                        ArtworkRow(
                            artwork = artwork,
                            artworkScreen = {
                                val artworkJson = Gson().toJson(artwork)
                                val encodedArtwork = URLEncoder.encode(artworkJson, StandardCharsets.UTF_8.toString())
                                navController.navigate(Routes.artworkScreen + "/$encodedArtwork")
                            },
                            artworkOnMap = {
                                val isCameraSet = true
                                val latitude = artwork.location.latitude
                                val longitude = artwork.location.longitude

                                val artworksJson = Gson().toJson(artworks)
                                val encodedArtworks = URLEncoder.encode(artworksJson, StandardCharsets.UTF_8.toString())
                                navController.navigate(Routes.mapScreen + "/$isCameraSet/$latitude/$longitude/$encodedArtworks")
                            }
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }

        // Title text that stays fixed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPalette.BackgroundMainDarker)
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp)
                .padding(top = 20.dp) // Padding for top of the screen
        ) {
            Text(
                text = "Some art to check out today",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = ColorPalette.Yellow
                ),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}


