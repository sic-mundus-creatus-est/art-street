package edu.rmas.artstreet.data.services

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageService( private val storage: FirebaseStorage )
{
    suspend fun uploadUserPfp( uid: String, image: Uri) : String
    {
        return try
        {
            val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
            val uploadTask = storageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        }
        catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun uploadPrimaryArtworkImage(
        image: Uri
    ): String{
        return try{
            val fileName = "${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("artwork_photos/primary_images/$fileName")
            val uploadTask = storageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        }catch (e: Exception){
            e.printStackTrace()
            ""
        }
    }

    suspend fun uploadArtworkGalleryImages(
        images: List<Uri>
    ): List<String>{
        val downloadUrls = mutableListOf<String>()
        for (image in images) {
            try {
                val fileName = "${System.currentTimeMillis()}.jpg"
                val storageRef = storage.reference.child("artwork_photos/gallery_images/$fileName")
                val uploadTask = storageRef.putFile(image).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                downloadUrls.add(downloadUrl.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return downloadUrls
    }
}