package edu.rmas.artstreet.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction
import edu.rmas.artstreet.data.services.DatabaseService
import kotlinx.coroutines.tasks.await

class InteractionRepo : IInteractionRepo
{
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val dbService =  DatabaseService(firestore)

    override suspend fun getArtworkInteractions(artworkId: String) : Resource<List<Interaction>>
    {
        return try
        {
            val markReference = firestore.collection("interactions")
            val querySnapshot = markReference
                .whereEqualTo("artworkId", artworkId)
                .get()
                .await()

            val interactions = querySnapshot.documents.map { document ->
                Interaction (
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    artworkId = artworkId,
                    visitedByUser = document.getBoolean("visitedByUser") ?: false
                )
            }

            Resource.Success(interactions)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUserInteractions() : Resource<List<Interaction>>
    {
        return try
        {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Failure(IllegalStateException("User not authenticated"))

            val markReference = firestore.collection("interactions")
            val querySnapshot = markReference
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            val interactions = querySnapshot.documents.map { document ->
                Interaction (
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    artworkId = document.getString("artworkId") ?: "",
                    visitedByUser = document.getBoolean("visitedByUser") ?: false
                )
            }

            Resource.Success(interactions)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun markAsVisited(artworkId: String, artwork: Artwork) : Resource<String>
    {
        return try
        {
            val obj = Interaction (
                userId =  firebaseAuth.currentUser!!.uid,
                artworkId = artworkId,
                visitedByUser = true
            )

            val id = dbService.saveInteraction(obj)
            id
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun markAsNotVisited( interactionId: String ) : Resource<String>
    {
        return try
        {
            val id = dbService.deleteInteraction(interactionId)
            id
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

}
