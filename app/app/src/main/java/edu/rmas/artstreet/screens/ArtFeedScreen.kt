package edu.rmas.artstreet.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.gson.Gson
import edu.rmas.artstreet.R
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ArtFeedPost
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.CopyrightText
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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ColorPalette.BackgroundMainDarker))
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPalette.BackgroundMainEvenDarker)
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp)
                .padding(top = 10.dp, bottom = 10.dp)
                .zIndex(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Logo image on the far left
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "ArtStreet",
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = ColorPalette.Yellow
                    ),
                )

                Spacer(modifier = Modifier.weight(1f))

                // Search icon on the far right
                IconButton(onClick = {
                    // TODO: Handle search click here
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(30.dp),
                        tint = ColorPalette.White
                    )
                }
            }
        }


        if (artworks.isNullOrEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 70.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Cannot locate any art right now...\nCheck back later!",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = ColorPalette.Yellow,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Image(
                    painter = painterResource(id = R.drawable.not_found),
                    contentDescription = "not_found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                CopyrightText(year = 2024)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 70.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(id = R.drawable.grafitti),
                        contentDescription = "feed",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Some art to check out today:",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = ColorPalette.Yellow,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(artworks.size) { index ->
                    val artwork = artworks[index]
                    ArtFeedPost(
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
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    CopyrightText(year = 2024)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

