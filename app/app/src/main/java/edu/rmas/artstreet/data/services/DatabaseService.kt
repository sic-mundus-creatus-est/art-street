package edu.rmas.artstreet.data.services

import com.google.firebase.firestore.FirebaseFirestore
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.models.Interaction
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import kotlinx.coroutines.tasks.await

class DatabaseService( private val firestore: FirebaseFirestore )
{
    suspend fun saveUserData( userId: String, user: User ) : Resource<String>
    {
        return try
        {
            firestore.collection("users").document(userId).set(user).await()
            Resource.Success("[INFO] User data saved successfully. (User ID: ${userId})")
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun getUserData( userId: String ) : Resource<String>
    {
        return try
        {
            val userDocRef = firestore.collection("users").document(userId)
            val userSnapshot = userDocRef.get().await()

            if(userSnapshot.exists())
            {
                val user = userSnapshot.toObject(User::class.java)
                if(user != null) {
                    Resource.Success(user)
                }
                else {
                    Resource.Failure(Exception("[ERROR] User not found! (User ID: ${userId})"))
                }
            }
            else {
                Resource.Failure(Exception("[ERROR] User snapshot not found (User ID: ${userId})"))
            }

            Resource.Success("[INFO] Successfully retrieved user data. (User ID: ${userId})")

        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun saveArtworkData ( artwork: Artwork ) : Resource<String>
    {
        return try
        {
            firestore.collection("artworks").add(artwork).await()
            Resource.Success("[INFO] Successfully saved artwork data. (Artwork ID: ${artwork.id}, Capturer ID: ${artwork.capturerId})")
        }
        catch(e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun saveInteraction ( interaction: Interaction ) : Resource<String>
    {
        return try
        {
            val result = firestore.collection("interactions").add(interaction).await()

            Resource.Success(result.id)
        }
        catch(e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun updateInteraction ( interactionId: String ) : Resource<String>
    {
        return try
        {
            val documentRef = firestore.collection("interactions").document(interactionId)
            documentRef.update("visitedByUser", false).await()

            Resource.Success(interactionId)
        }
        catch(e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun deleteInteraction(interactionId: String): Resource<String>
    {
        return try
        {
            val documentRef = firestore.collection("interactions").document(interactionId)
            documentRef.delete().await()

            Resource.Success(interactionId)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

}
