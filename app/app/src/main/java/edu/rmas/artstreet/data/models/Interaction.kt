package edu.rmas.artstreet.data.models

import com.google.firebase.firestore.DocumentId

data class Interaction (
    @DocumentId val id: String = "",
    val userId: String = "",
    val artworkId: String = "",

    var visitedByUser: Boolean = false
)
