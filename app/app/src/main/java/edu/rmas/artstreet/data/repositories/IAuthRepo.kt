package edu.rmas.artstreet.data.repositories

import android.net.Uri
import edu.rmas.artstreet.data.models.User
import com.google.firebase.auth.FirebaseUser

interface IAuthRepo
{
    val user: FirebaseUser?

    suspend fun signUp(fullName: String, phoneNumber: String, profileImage: Uri, email: String, username: String, password: String): Resource<FirebaseUser>
    suspend fun signIn(emailOrUsername: String, password: String): Resource<FirebaseUser>
    fun signOut()

    suspend fun getUser(): Resource<User>
    suspend fun getAllUsers(): Resource<List<User>>
    suspend fun getUserById(userId: String): Resource<User>
}