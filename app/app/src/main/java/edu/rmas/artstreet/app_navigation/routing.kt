package edu.rmas.artstreet.app_navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.rmas.artstreet.screens.SignInScreen
import edu.rmas.artstreet.screens.SignUpScreen
import edu.rmas.artstreet.screens.TestScreen
import edu.rmas.artstreet.view_models.AuthVM

@Composable
fun Routing ( authVM: AuthVM )
{
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.signInScreen) {
        composable(Routes.signInScreen) {
            SignInScreen(navController = navController, authVM = authVM)
        }
        composable(Routes.signUpScreen) {
            SignUpScreen(navController = navController, authVM = authVM)
        }
        composable(Routes.testScreen) {
            TestScreen(navController = navController, authVM = authVM)
        }
    }
}