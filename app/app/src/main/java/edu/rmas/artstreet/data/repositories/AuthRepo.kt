package edu.rmas.artstreet.data.repositories

import android.net.Uri
import com.google.firebase.auth.AuthResult
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.services.DatabaseService
import edu.rmas.artstreet.data.services.StorageService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepo : IAuthRepo
{
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()

    private val databaseService = DatabaseService(firestoreInstance)
    private val storageService = StorageService(storageInstance)

    override val user: FirebaseUser? get() = firebaseAuth.currentUser

    override suspend fun signIn(emailOrUsername: String, password: String): Resource<FirebaseUser>
    {
        return try
        {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            val email = if (emailOrUsername.matches(emailRegex.toRegex())) {
                emailOrUsername
            } else {
                getEmailFromUsername(emailOrUsername)
            }

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Resource.Success(it)
            } ?: Resource.Failure(Exception("User is null"))

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    private suspend fun getEmailFromUsername(username: String) : String
    {
        return suspendCoroutine { continuation ->
            firestoreInstance.collection("usernames").document(username).get()
                .addOnSuccessListener { document ->
                    val email = document.getString("email")

                    if (email != null)
                    {
                        continuation.resume(email)
                    } else {
                        continuation.resumeWithException(Exception("Email not found for username: $username"))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }


    override suspend fun signUp (
        fullName: String,
        phoneNumber: String,
        profileImage: Uri,
        email: String,
        username: String,
        password: String
    ) : Resource<FirebaseUser>
    {
        return try
        {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            if(result.user != null)
            {
                firestoreInstance.collection("usernames").document(username).set(
                    hashMapOf(
                        "email" to email
                    )
                )

                val profilePictureUrl = storageService.uploadUserPfp(result.user!!.uid, profileImage)

                val user = User (
                    username = username,
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    profilePicture = profilePictureUrl,
                )

                databaseService.saveUserData(result.user!!.uid, user)
            }

            Resource.Success(result.user!!)
        }
        catch(e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUser(): Resource<User>
    {
        return try
        {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Resource.Failure(Exception("[ERROR] No current user session found!"))

            val uid = currentUser.uid
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            val userSnapshot = userDocRef.get().await()

            if (!userSnapshot.exists())
            {
                return Resource.Failure(Exception("[ERROR] User document does not exist! (User ID: $uid)"))
            }

            val user = userSnapshot.toObject(User::class.java)
                ?: return Resource.Failure(Exception("[ERROR] Failed to map snapshot document to User! (User ID: $uid)"))

            Resource.Success(user)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getAllUsers(): Resource<List<User>>
    {
        return try
        {
            val db = FirebaseFirestore.getInstance()
            val usersCollectionRef = db.collection("users")
            val usersSnapshot = usersCollectionRef.get().await()

            if (usersSnapshot.isEmpty)
            {
                return Resource.Failure(Exception("[INFO] No users found in the database."))
            }

            val usersList = usersSnapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)
            }

            Resource.Success(usersList)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val userDocRef = firestoreInstance.collection("users").document(userId)
            val userSnapshot = userDocRef.get().await()

            if (!userSnapshot.exists()) {
                return Resource.Failure(Exception("[ERROR] User document does not exist! (User ID: $userId)"))
            }

            val user = userSnapshot.toObject(User::class.java)
                ?: return Resource.Failure(Exception("[ERROR] Failed to map snapshot document to User! (User ID: $userId)"))

            Resource.Success(user)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    override fun signOut()
    {
        firebaseAuth.signOut()
    }
}
