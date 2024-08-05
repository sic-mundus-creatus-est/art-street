package edu.rmas.artstreet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.view_models.AuthVMFactory


class MainActivity : ComponentActivity(){
    private val userAuthVM: AuthVM by viewModels {
        AuthVMFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            ArtStreet(userAuthVM)
        }
    }
}
