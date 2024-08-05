package edu.rmas.artstreet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.app_navigation.Routing

@Composable
fun ArtStreet(authVM: AuthVM){
    Surface(modifier = Modifier.fillMaxSize()){
        Routing( authVM = authVM )
    }
}