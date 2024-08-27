package edu.rmas.artstreet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.MainUserInfo
import edu.rmas.artstreet.screens.components.ProfileArtworkGrid
import edu.rmas.artstreet.screens.components.TheDivider
import edu.rmas.artstreet.view_models.ArtworkVM

@Composable
fun ProfileScreen(
    navController: NavController,
    artworkVM: ArtworkVM,
    user: User
) {
    artworkVM.getUserArtworks(user.id)
    val artworksResource = artworkVM.userArtworks.collectAsState()
    val artworks = remember { mutableStateListOf<Artwork>() }

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
                //
            }
            null -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize())
    {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPalette.BackgroundMainLighter)
        ) {
            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorPalette.BackgroundMainDarker)
                    .padding(top = 14.dp, bottom = 9.dp)
            )

            Box (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorPalette.BackgroundMainEvenDarker),
                contentAlignment = Alignment.CenterStart
            ) {
                MainUserInfo( imageUrl = user.profilePicture, name = user.fullName.replace("+", " "), phoneNumber = user.phoneNumber )
            }

            ProfileArtworkGrid(artworks = artworks, navController = navController)
        }
    }
}

