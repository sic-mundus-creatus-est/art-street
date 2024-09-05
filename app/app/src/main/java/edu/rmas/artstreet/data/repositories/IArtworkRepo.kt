package edu.rmas.artstreet.data.repositories

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import edu.rmas.artstreet.data.models.Artwork

interface IArtworkRepo {

    suspend fun getAllArtworks(): Resource<List<Artwork>>
    suspend fun saveArtworkData(
        title: String,
        location: LatLng,
        description: String,
        galleryImages: List<Uri>,
    ): Resource<String>

    suspend fun getCapturedByUserArtworks(
        uid: String
    ): Resource<List<Artwork>>
}