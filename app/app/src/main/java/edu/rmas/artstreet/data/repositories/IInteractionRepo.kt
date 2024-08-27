package edu.rmas.artstreet.data.repositories

import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction

interface IInteractionRepo
{
    suspend fun getArtworkInteractions (
        artworkId: String
    ) : Resource<List<Interaction>>

    suspend fun getCurrentUserInteractions() : Resource<List<Interaction>>

    suspend fun markAsVisited (
        artworkId: String,
        artwork: Artwork
    ) : Resource<String>

    suspend fun markAsNotVisited (
        interactionId: String,
    ) : Resource<String>

    suspend fun getUserInteractions(userId: String): Resource<List<Interaction>>
    suspend fun getAllInteractions(): Resource<List<Interaction>>
}
