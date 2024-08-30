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
    private val artworkRepo = ArtworkRepo()
    private val interactionRepo = InteractionRepo()
// -----------------------------------------------

    private val _artworkFlow = MutableStateFlow<Resource<String>?>(null)
    val artwork: StateFlow<Resource<String>?> = _artworkFlow

    private val _artworksFlow = MutableStateFlow<Resource<List<Artwork>>>(Resource.Success(emptyList()))
    val artworks: StateFlow<Resource<List<Artwork>>> get() = _artworksFlow


    private val _userArtworksFlow = MutableStateFlow<Resource<List<Artwork>>>(Resource.Success(emptyList()))
    val userArtworks: StateFlow<Resource<List<Artwork>>> get() = _userArtworksFlow

    private val _newInteractionFlow = MutableStateFlow<Resource<String>?>(null)
    val newInteraction: StateFlow<Resource<String>?> = _newInteractionFlow

    private val _artworkInteractionsFlow = MutableStateFlow<Resource<List<Interaction>>>(Resource.Success(emptyList()))
    val artworkInteractions: StateFlow<Resource<List<Interaction>>> get() = _artworkInteractionsFlow


    private val _userInteractionsFlow = MutableStateFlow<Resource<List<Interaction>>>(Resource.Success(emptyList()))
    val userInteractions: StateFlow<Resource<List<Interaction>>> get() = _userInteractionsFlow

    private val _interactionsFlow = MutableStateFlow<Resource<List<Interaction>>>(Resource.Success(emptyList()))
    val interactions: StateFlow<Resource<List<Interaction>>> get() = _interactionsFlow

    private val _filteredArtworks = MutableStateFlow<List<Artwork>?>(emptyList())
    val filteredArtworks: StateFlow<List<Artwork>?> = _filteredArtworks

    init {
        getAllArtworks()
        getAllInteractions()
    }

    fun getAllArtworks() = viewModelScope.launch {
        _artworksFlow.value = artworkRepo.getAllArtworks()
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
        _userArtworksFlow.value = artworkRepo.getCapturedByUserArtworks(uid)
    }

// ------------------------------------------------------------------------------------------------------

    fun markAsVisited ( artworkId: String, artwork: Artwork) = viewModelScope.launch {
        _newInteractionFlow.value = interactionRepo.markAsVisited(artworkId, artwork)
    }

    fun markAsNotVisited(interactionId: String ) = viewModelScope.launch {
        _newInteractionFlow.value = interactionRepo.markAsNotVisited(interactionId)
    }

    fun getAllInteractions() = viewModelScope.launch {
        _interactionsFlow.value = interactionRepo.getAllInteractions()
    }

    fun getArtworkInteractions ( artworkId: String ) = viewModelScope.launch {
        _artworkInteractionsFlow.value = Resource.Loading
        val result = interactionRepo.getArtworkInteractions(artworkId)
        _artworkInteractionsFlow.value = result
    }

    fun fetchUpdatedArtworkInteractions ( artworkId: String ) = viewModelScope.launch {
        _artworkInteractionsFlow.value = Resource.Loading
        interactionRepo.listenForInteractions(artworkId) { updatedInteractions ->
            _artworkInteractionsFlow.value = updatedInteractions
        }
    }

    fun getUserInteractions(userId: String) = viewModelScope.launch {
        _userInteractionsFlow.value = Resource.Loading
        val result = interactionRepo.getUserInteractions(userId)
        _userInteractionsFlow.value = result
    }

// -------------------------------------------------------------------------------------------------
    fun updateFilteredArtworks(newArtworks: List<Artwork>?) {
        viewModelScope.launch {
            _filteredArtworks.value = newArtworks
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