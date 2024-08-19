package edu.rmas.artstreet

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.ArtworkVMFactory
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.view_models.AuthVMFactory


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity()
{
    private val userAuthVM: AuthVM by viewModels {
        AuthVMFactory()
    }

    private val artworkVM: ArtworkVM by viewModels{
        ArtworkVMFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtStreet(userAuthVM, artworkVM)
        }
    }
}
