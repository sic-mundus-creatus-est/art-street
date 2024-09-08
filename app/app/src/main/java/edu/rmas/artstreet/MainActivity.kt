package edu.rmas.artstreet

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import edu.rmas.artstreet.view_models.ArtworkVM
import edu.rmas.artstreet.view_models.ArtworkVMFactory
import edu.rmas.artstreet.view_models.AuthVM
import edu.rmas.artstreet.view_models.AuthVMFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity()
{
    private var isAppLoaded = false

    private val userAuthVM: AuthVM by viewModels {
        AuthVMFactory()
    }

    private val artworkVM: ArtworkVM by viewModels{
        ArtworkVMFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !isAppLoaded
        }

        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            isAppLoaded = true
        }

        setContent {
            ArtStreet(userAuthVM, artworkVM)
        }
    }
}
