package edu.rmas.artstreet.app_navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.compose.rememberCameraPositionState
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.screens.AddArtworkScreen
import edu.rmas.artstreet.screens.ArtworkScreen
import edu.rmas.artstreet.screens.ArtFeedScreen
import edu.rmas.artstreet.screens.LeaderboardScreen
import edu.rmas.artstreet.screens.MapScreen
import edu.rmas.artstreet.screens.ProfileScreen
import edu.rmas.artstreet.screens.SettingsScreen
import edu.rmas.artstreet.screens.SignInScreen
import edu.rmas.artstreet.screens.SignUpScreen
import edu.rmas.artstreet.screens.TestScreen
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.AuthVM

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Routing ( authVM: AuthVM, artworkVM: ArtworkVM)
{
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.signInScreen)
    {
    //-----------------------------------------------------------------------------
    // -[[ SIGNIN SCREEN ]]-
        composable(Routes.signInScreen)
        {
            SignInScreen(navController = navController, authVM = authVM)
        }
    //-----------------------------------------------------------------------------
    // -[[ SIGNUP SCREEN ]]-
        composable(Routes.signUpScreen)
        {
            SignUpScreen(navController = navController, authVM = authVM)
        }
    //-----------------------------------------------------------------------------
    // -[[ TEST SCREEN ]]- // only to check if signin works...
//        composable(Routes.testScreen)
//        {
//            TestScreen(navController = navController, authVM = authVM)
//        }
    //-----------------------------------------------------------------------------
    // -[[ MAP SCREEN ]]- (default screen)
        composable(Routes.mapScreen)
        {
            val artworkData = artworkVM.artworks.collectAsState()
            val artworkMarkers = remember { mutableListOf<Artwork>() }

            artworkData.value.let {
                when(it)
                {
                    is Resource.Success -> {
                        artworkMarkers.clear()
                        artworkMarkers.addAll(it.result)
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

            MapScreen(navController = navController, authVM = authVM, artworkVM = artworkVM, artworkMarkers = artworkMarkers)
        }
        composable (
            route = Routes.mapScreen + "/{camera}/{latitude}/{longitude}/{artworks}",
            arguments = listOf (
                navArgument("camera") { type = NavType.BoolType },
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType },
                navArgument("artworks") { type = NavType.StringType }
            )
        ) {
                backStackEntry ->
            val camera = backStackEntry.arguments?.getBoolean("camera")
            val latitude = backStackEntry.arguments?.getFloat("latitude")
            val longitude = backStackEntry.arguments?.getFloat("longitude")
            val artworksJson = backStackEntry.arguments?.getString("artworks")
            val artworks = Gson().fromJson(artworksJson, Array<Artwork>::class.java).toList()

            MapScreen (
                navController = navController,
                authVM = authVM,
                artworkVM = artworkVM,
                artworkMarkers= artworks.toMutableList(),
                isCameraSet = remember { mutableStateOf(camera!!) },
                cameraPosition = rememberCameraPositionState{
                    position = CameraPosition.fromLatLngZoom(LatLng(latitude!!.toDouble(), longitude!!.toDouble()), 17f)
                }
            )
        }
    //-----------------------------------------------------------------------------
    // -[[ PROFILE SCREEN ]]-
        composable (
            route = Routes.profileScreen + "/{user}",
            arguments = listOf(navArgument("user")
            {
                type = NavType.StringType
            } )
        ) {
                backStackEntry ->
            val userDataJson = backStackEntry.arguments?.getString("user")
            val userData = Gson().fromJson(userDataJson, User::class.java)

            ProfileScreen (
                navController = navController,
                artworkVM = artworkVM,
                user = userData
            )
        }
    //-----------------------------------------------------------------------------
    // -[[ ADD ARTWORK SCREEN ]]-
        composable (
            Routes.addArtworkScreen+"/{latitude}/{longitude}",
            arguments = listOf (
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType }
            )
        ){ backStackEntry ->
            val latitude = backStackEntry.arguments?.getFloat("latitude")
            val longitude = backStackEntry.arguments?.getFloat("longitude")

            val location = remember {
                mutableStateOf(LatLng(latitude!!.toDouble(), longitude!!.toDouble()))
            }

            AddArtworkScreen(artworkVM = artworkVM, location = location, navController)
        }
    //-----------------------------------------------------------------------------
    // -[[ ARTWORK SCREEN ]]-
        composable (
            route = Routes.artworkScreen + "/{artwork}",
            arguments = listOf(
                navArgument("artwork"){ type = NavType.StringType }
            )
        ){
                backStackEntry ->
            val artworkJson = backStackEntry.arguments?.getString("artwork")
            val artwork = Gson().fromJson(artworkJson, Artwork::class.java)

            artworkVM.getArtworkInteractions(artwork.id)

            ArtworkScreen (
                artwork = artwork,
                artworkVM = artworkVM,
                authVM = authVM,
                navController = navController
            )
        }
    //-----------------------------------------------------------------------------
    // -[[ ARTFEED SCREEN ]]-
        composable (
            route = Routes.artFeedScreen + "/{artworks}",
            arguments = listOf(navArgument("artworks") { type = NavType.StringType })
        ) {
                backStackEntry ->
            val artworksJson = backStackEntry.arguments?.getString("artworks")
            val artworks = Gson().fromJson(artworksJson, Array<Artwork>::class.java).toList()
            ArtFeedScreen(artworks = artworks, navController = navController, artworkVM = artworkVM, authVM = authVM)
        }

    //-----------------------------------------------------------------------------
    // -[[ LEADERBOARD SCREEN ]]-
        composable(Routes.leaderboardScreen)
        {

            artworkVM.getAllInteractions()

            LeaderboardScreen (
                authVM = authVM,
                artworkVM = artworkVM,
                navController = navController
            )
        }
    //-----------------------------------------------------------------------------
    // -[[ SETTINGS SCREEN ]]-
        composable(Routes.settingsScreen) {
            SettingsScreen(navController = navController)
        }
    }
    //-----------------------------------------------------------------------------
}
