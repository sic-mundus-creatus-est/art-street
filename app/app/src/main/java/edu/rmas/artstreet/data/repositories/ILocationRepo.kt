package edu.rmas.artstreet.data.repositories

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface ILocationRepo
{
    fun LocationUpdates(interval: Long): Flow<Location>
    class LocationException(message: String): Exception()
}