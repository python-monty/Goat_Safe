package com.cs523.android.means_v2


// GEOFENCE CLASS TO SET UP GEOFENCES


import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
//import android.graphics.RadialGradient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
//import com.google.android.gms.location.Geofence.TransitionTypes
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng





class GeofenceHelper(base: Context?) : ContextWrapper(base) {

    //private var TAG: String = "GeofenceHelper"
    private var pendingIntent: PendingIntent? = null
    private val GEO_REQUEST_CODE: Int = 20001
//    var intentExtras: String? = null


    fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(geofence!!)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .build()
    }


    // BUILD A GEOFENCE AND SETTINGS FOR EACH GEOFENCE THAT IS BUILT
    fun getGeofence(ID: String?, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setRequestId(ID!!)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    // GET OR CREATE A PENDING INTENT

    fun getPendingIntent(): PendingIntent? {

        if (pendingIntent != null) {
            return pendingIntent
        }

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
//        intent.putExtra("userID",intentExtras)


        if(Build.VERSION.SDK_INT >= 31) {
            pendingIntent =
                PendingIntent.getBroadcast(
                    this,
                    GEO_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            return pendingIntent
        }else{
            pendingIntent =
                PendingIntent.getBroadcast(
                    this,
                    GEO_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            return pendingIntent
        }
    }

    // ERROR REPORTING FOR GEOFENCES
    fun getErrorString(e: Exception): String {
        if (e is ApiException) {

            when (e.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return "GEOFENCE_NOT_AVAILABLE"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return "GEOFENCE_TOO_MANY_GEOFENCES"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
            }
        }
        return e.localizedMessage
    }

//    fun setExtra(uid: String){
//        println("value of uid inside geofencehelp/setextra is :$uid")
//
//        intentExtras = uid
//
//    }
//
//    fun printExtra(){
//        println("Value printed from extras using the geohelper print function is : $intentExtras")
//
//    }
//
//    fun getExtra(): String? {
//        println("Value printed from extras using the geohelper print function is : $intentExtras")
//        return intentExtras
//
//    }

    companion object {
        private const val TAG = "GeofenceHelper"
    }
}


