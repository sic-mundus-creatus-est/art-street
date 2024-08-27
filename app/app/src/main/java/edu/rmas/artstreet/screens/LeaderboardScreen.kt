package edu.rmas.artstreet.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.components.ColorPalette
import edu.rmas.artstreet.screens.components.LeaderboardList
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM

@Composable
fun LeaderboardScreen( authVM: AuthVM, artworkVM: ArtworkVM, navController: NavController )
{
// -------------------------------------------------------------------------
// >> STATE COLLECTION
    val allUsersResource by authVM.allUsers.collectAsState()
    val artworksResource by artworkVM.artworks.collectAsState()
    val interactionsResource by artworkVM.interactions.collectAsState()
// -------------------------------------------------------------------------

    val userPointsMap = remember { mutableStateMapOf<User, Int>() }

    var isSharedLocationsSelected by remember { mutableStateOf(true) }

// -------------------------------------------------------------------------------------------------
// >> USER INTERFACE
    Box(modifier = Modifier.fillMaxSize())
    {
        Column (
            modifier = Modifier
                .padding(vertical = 25.dp, horizontal = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "LEADERBOARD",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Button(
                    onClick = { isSharedLocationsSelected = true },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSharedLocationsSelected) ColorPalette.LightGray else Color.Transparent
                    )
                ) {
                    Text(text = "Shared Locations")
                }

                Button(
                    onClick = { isSharedLocationsSelected = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSharedLocationsSelected) Color.Gray else Color.Transparent
                    )
                ) {
                    Text(text = "Visited Locations")
                }
            }

            LazyColumn(
                modifier = Modifier.padding(top = 70.dp)
            ) {
                item {
                    LeaderboardList(
                        userWithPoints = userPointsMap,
                        navController = navController
                    )
                }
            }
        }
    }
// -------------------------------------------------------------------------------------------------
// -> updates the userPointsMap based on the selection
    LaunchedEffect(artworksResource, interactionsResource, allUsersResource, isSharedLocationsSelected)
    {
        when
        {
            artworksResource is Resource.Success && isSharedLocationsSelected -> {
                val resource = artworksResource as Resource.Success
                updatePointsMapWithArtworkCount(resource.result, allUsersResource, userPointsMap)
            }
            interactionsResource is Resource.Success && !isSharedLocationsSelected -> {
                val resource = interactionsResource as Resource.Success
                updatePointsMapWithInteractionCount(resource.result, allUsersResource, userPointsMap)
            }
        }
    }
// -------------------------------------------------------------------------------------------------

}

// -------------------------------------------------------------------------------------------------
private fun updatePointsMapWithArtworkCount (
    artworks: List<Artwork>,
    allUsersResource: Resource<List<User>>?,
    userPointsMap: MutableMap<User, Int>
) {
    if (allUsersResource is Resource.Success)
    {
        val resource = allUsersResource as Resource.Success
        val entries = resource.result.map { user ->
            val count = artworks.count { artwork -> artwork.capturerId == user.id }
            user to count
        }

        // Log.d("LeaderboardScreen: UpdatedPointsMap", "Artwork Entries: $entries")

        userPointsMap.clear()
        userPointsMap.putAll(entries)
    }
}

// -------------------------------------------------------------------------------------------------
private fun updatePointsMapWithInteractionCount (
    interactions: List<Interaction>,
    allUsersResource: Resource<List<User>>?,
    userPointsMap: MutableMap<User, Int>
) {
    if (allUsersResource is Resource.Success)
    {
        val resource = allUsersResource as Resource.Success
        val entries = resource.result.map { user ->
            val count = interactions.count { inter -> inter.userId == user.id }
            user to count
        }

        // Log.d("LeaderboardScreen: UpdatedPointsMap", "Interaction Entries: $entries")

        userPointsMap.clear()
        userPointsMap.putAll(entries)
    }
}
// -------------------------------------------------------------------------------------------------

// sve ovo je moglo da se napravi mnogo jednostavnije i optimalnije, al kad sam vec istrosio toliko vremena na ovu glupost, ostace ovako :)
