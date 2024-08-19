package edu.rmas.artstreet.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Artwork (
    @DocumentId val id: String = "",
    val capturerId: String = "",

    val title: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val description: String = "",

    val primaryImage: String = "",
    val galleryImages: List<String> = emptyList(),

    )