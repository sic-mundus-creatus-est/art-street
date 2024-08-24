package edu.rmas.artstreet.data.models

import com.google.firebase.firestore.DocumentId

data class User (
    @DocumentId var id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePicture: String = "",
)
