package edu.rmas.artstreet.data.services

import com.google.firebase.firestore.FirebaseFirestore
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.Resource
import kotlinx.coroutines.tasks.await

class DatabaseService( private val firestore: FirebaseFirestore)
{
    suspend fun saveUserData( userId: String, user: User ) : Resource<String>
    {
        return try
        {
            firestore.collection("users").document(userId).set(user).await()
            Resource.Success("[INFO] User data saved successfully. (User ID: ${userId})")
        }
        catch (e: Exception)
        {
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
}