package edu.rmas.artstreet.view_models

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction
import edu.rmas.artstreet.data.repositories.ArtworkRepo
import edu.rmas.artstreet.data.repositories.InteractionRepo
import edu.rmas.artstreet.data.repositories.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtworkVM: ViewModel()
{
// -----------------------------------------------
// -[[ REPOSITORIES ]]-
    val artworkRepo = ArtworkRepo()
    val interactionRepo = InteractionRepo()
// -----------------------------------------------

    private val _artworkFlow = MutableStateFlow<Resource<String>?>(null)
    val artworkFlow: StateFlow<Resource<String>?> = _artworkFlow

    private val _artworks = MutableStateFlow<Resource<List<Artwork>>>(Resource.Success(emptyList()))
    val artworks: StateFlow<Resource<List<Artwork>>> get() = _artworks


    private val _userArtworks = MutableStateFlow<Resource<List<Artwork>>>(Resource.Success(emptyList()))
    val userArtworks: StateFlow<Resource<List<Artwork>>> get() = _userArtworks

    private val _newInteraction = MutableStateFlow<Resource<String>?>(null)
    val newInteraction: StateFlow<Resource<String>?> = _newInteraction

    private val _interactions = MutableStateFlow<Resource<List<Interaction>>>(Resource.Success(emptyList()))
    val interactions: StateFlow<Resource<List<Interaction>>> get() = _interactions

    init {
        getAllArtworks()
    }

    fun getAllArtworks() = viewModelScope.launch {
        _artworks.value = artworkRepo.getAllArtworks()
    }

    fun saveArtworkData(
        title: String,
        location: MutableState<LatLng>?,
        description: String,
        primaryImage: Uri,
        galleryImages: List<Uri>,
    ) = viewModelScope.launch{
        _artworkFlow.value = Resource.Loading
        artworkRepo.saveArtworkData(
            title = title,
            location = location!!.value,
            description = description,
            primaryImage = primaryImage,
            galleryImages = galleryImages,
        )
        _artworkFlow.value = Resource.Success("Successfully added an artwork location...")
    }

    fun getUserArtworks(
        uid: String
    ) = viewModelScope.launch {
        _userArtworks.value = artworkRepo.getCapturedByUserArtworks(uid)
    }

    fun markAsVisited ( artworkId: String, artwork: Artwork) = viewModelScope.launch {
        _newInteraction.value = interactionRepo.markAsVisited(artworkId, artwork)
    }

    fun markAsNotVisited(interactionId: String ) = viewModelScope.launch {
        _newInteraction.value = interactionRepo.markAsNotVisited(interactionId)
    }

    fun getArtworkInteractions ( artworkId: String ) = viewModelScope.launch {
        _interactions.value = Resource.Loading
        val result = interactionRepo.getArtworkInteractions(artworkId)
        _interactions.value = result
    }

    fun fetchUpdatedArtworkInteractions ( artworkId: String ) = viewModelScope.launch {
        _interactions.value = Resource.Loading
        interactionRepo.listenForInteractions(artworkId) { updatedInteractions ->
            _interactions.value = updatedInteractions
        }
    }
}

class ArtworkVMFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ArtworkVM::class.java))
        {
            return ArtworkVM() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}