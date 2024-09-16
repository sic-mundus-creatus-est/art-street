package edu.rmas.artstreet.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import edu.rmas.artstreet.MainActivity
import edu.rmas.artstreet.R
import edu.rmas.artstreet.data.repositories.LocationRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.O)
class LocationService : Service()
{
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationRepo: LocationRepo
    private val notifiedArtworks = mutableSetOf<String>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate()
    {
        super.onCreate()
        createNotificationChannel()
        locationRepo = LocationRepo (
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        when(intent?.action){
            ACTION_START -> {
                Log.d("LocationService", "Service started")
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
                start()
            }
            ACTION_STOP -> {
                Log.d("LocationService", "Service stopped")
                stop()
            }
            ACTION_FIND_NEARBY -> {
                Log.d("NearbyService", "Service started")
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
                start(nearby = true)
            }
        }
        return START_NOT_STICKY
    }

    private fun start ( nearby: Boolean = false )
    {
        locationRepo.LocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                // Log.d("Location", "${location.latitude} ${location.longitude}")
                val intent = Intent(ACTION_LOCATION_UPDATE).apply {
                    putExtra(EXTRA_LOCATION_LATITUDE, location.latitude)
                    putExtra(EXTRA_LOCATION_LONGITUDE, location.longitude)
                }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                if(nearby){
                    checkProximityToArtworks(location.latitude, location.longitude)
                }
            }.launchIn(serviceScope)
    }

    private fun stop()
    {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel()
    {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel (
                notificationChannelId,
                "Location",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Our app has started tracking your location in the background."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Location Service Activated")
            .setContentText("Our app has started tracking your location in the background.")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    private fun checkProximityToArtworks(latitude: Double, longitude: Double) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("artworks").get()
            .addOnSuccessListener { result ->
                for (document in result)
                {
                    val geoPoint = document.getGeoPoint("location")
                    geoPoint?.let {
                        val distance = calculateDistance(latitude, longitude, it.latitude, it.longitude)
                        if (distance <= 100 && !notifiedArtworks.contains(document.id)) {
                            sendNearbyArtworkNotification()
                            notifiedArtworks.add(document.id)
                            Log.d("Nearby artworks", document.toString())
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "Error fetching artworks", e)
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double
    {// Haversine formula
        val earthRadius = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun sendNearbyArtworkNotification()
    {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity (
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Artwork Nearby!")
            .setContentText("Tap to discover amazing art close to you.")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Explore local creativity now! Tap to open the map and find new art around you."))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NEARBY_ARTWORK_NOTIFICATION_ID, notification)
    }

    companion object
    {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_FIND_NEARBY = "ACTION_FIND_NEARBY"
        const val ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE"
        const val EXTRA_LOCATION_LATITUDE = "EXTRA_LOCATION_LATITUDE"
        const val EXTRA_LOCATION_LONGITUDE = "EXTRA_LOCATION_LONGITUDE"
        private const val NOTIFICATION_ID = 1
        private const val NEARBY_ARTWORK_NOTIFICATION_ID = 2
    }
}
