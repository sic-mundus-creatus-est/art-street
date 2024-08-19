package edu.rmas.artstreet.data.repositories

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import edu.rmas.artstreet.data.models.Artwork
import edu.rmas.artstreet.data.services.DatabaseService
import edu.rmas.artstreet.data.services.StorageService
import kotlinx.coroutines.tasks.await

class ArtworkRepo : IArtworkRepo {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()

    private val databaseService = DatabaseService(firestoreInstance)
    private val storageService = StorageService(storageInstance)


    override suspend fun getAllArtworks(): Resource<List<Artwork>> {
        return try{
            val snapshot = firestoreInstance.collection("artworks").get().await()
            val artworks = snapshot.toObjects(Artwork::class.java)
            Resource.Success(artworks)
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun saveArtworkData(
        title: String,
        location: LatLng,
        description: String,
        primaryImage: Uri,
        galleryImages: List<Uri>,
    ): Resource<String> {
        return try{
            val currentUser = firebaseAuth.currentUser
            if(currentUser!=null){
                val primaryImageUrl = storageService.uploadPrimaryArtworkImage(primaryImage)
                val galleryImagesUrls = storageService.uploadArtworkGalleryImages(galleryImages)
                val geoLocation = GeoPoint(
                    location.latitude,
                    location.longitude
                )
                val artwork = Artwork(
                    title = title,
                    capturerId = currentUser.uid,
                    location = geoLocation,
                    description = description,
                    primaryImage = primaryImageUrl,
                    galleryImages = galleryImagesUrls,
                )
                databaseService.saveArtworkData(artwork)
            }
            Resource.Success("[INFO] Successfully saved artwork data. (Title: ${title}, Capturer ID: ${currentUser?.uid})")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getCapturedByUserArtworks(uid: String): Resource<List<Artwork>> {
        return try {
            val snapshot = firestoreInstance.collection("artworks")
                .whereEqualTo("capturerId", uid)
                .get()
                .await()
            val beaches = snapshot.toObjects(Artwork::class.java)
            Resource.Success(beaches)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
}