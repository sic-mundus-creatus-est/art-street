package edu.rmas.artstreet.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.BackButton
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.PhotosSection
import edu.rmas.artstreet.screens.components.ProfilePicture
import edu.rmas.artstreet.view_models.ArtworkVM

@Composable
fun ProfileScreen(
    navController: NavController,
    artworkVM: ArtworkVM,
    user: User
) {
    artworkVM.getUserArtworks(user.id)
    val artworksResource = artworkVM.userArtworks.collectAsState()
    val artworks = remember {
        mutableStateListOf<Artwork>()
    }

    artworksResource.value.let {
        when(it){
            is Resource.Success -> {
                artworks.clear()
                artworks.addAll(it.result)
            }
            is Resource.Loading -> {
                //
            }
            is Resource.Failure -> {
                // TODO
            }
            null -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPalette.White)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(400.dp)
                        .background(
                            ColorPalette.BackgroundMainLighter,
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfilePicture(imageUrl = user.profilePicture)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = user.fullName.replace("+", " "),
                            color = ColorPalette.White,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
//
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                Text(
                                    text = artworks.count().toString(),
                                    color = ColorPalette.White,
                                    fontSize = 15.sp,
                                    style = TextStyle(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Art Locations Added",
                                    color = ColorPalette.White,
                                    fontSize = 15.sp,
                                    style = TextStyle(fontWeight = FontWeight.Thin)
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(1.dp)
                                    .background(ColorPalette.White)
                            ) {}
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        BackButton {
                            navController.popBackStack()
                        }
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Phone, contentDescription = "")
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = user.phoneNumber)
                }
            }
            item { PhotosSection(artworks = artworks, navController = navController) }
        }
    }
}