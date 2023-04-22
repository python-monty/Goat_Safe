package com.cs523.android.means_v2

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.maps.model.LatLng

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var TAG: String = "GeofenceBroadcastReceiver"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var geoCoder: Geocoder

    private var currentStreetAddress: String? = null

    private var erIntent: Intent? = null

    //private lateinit var erIntent: Intent

    private var wpiClinic:LatLng = LatLng(42.27324,-71.8100491)
    private var locationText: String = "WPI Health Clinic"


    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {

        println("LINE18:GeofenceBcast - A geofence event was received....")

        geoCoder = Geocoder(context)
//
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//        println("value of fusedlocationprovider is $fusedLocationProviderClient")
//
//        var currentLocationTask: Task<Location> = fusedLocationProviderClient.lastLocation
//
//        println("current value of currentlocationtask is :$currentLocationTask")
//
//        currentLocationTask.addOnSuccessListener { currentLocation->

        var currentLatLng = LatLng(wpiClinic.latitude,wpiClinic.longitude)

        var currentAddresses: List<Address> =
            geoCoder.getFromLocation(
                currentLatLng.latitude,
                currentLatLng.longitude,
                1
            ) as List<Address>

        val currentAddress: Address = currentAddresses[0]

        currentStreetAddress = currentAddress.getAddressLine(0).toString()

        erIntent = Intent(context, erAlert::class.java)

        erIntent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        erIntent!!.putExtra("address", currentStreetAddress)
        erIntent!!.putExtra("text", locationText)


        var geoFencingEvent: GeofencingEvent? = GeofencingEvent.fromIntent(intent)


        if (geoFencingEvent != null) {
            if (geoFencingEvent.hasError()) {
                Log.d(TAG, "onReceive: Error receiving geofence event...")
                return
            }
        }
        var geoFenceList = geoFencingEvent?.triggeringGeofences as List<Geofence>
        for(i in geoFenceList){
            Log.d(TAG, "onReceive: " + i.requestId)
        }

        var transitionType: Int = geoFencingEvent.geofenceTransition


        when (transitionType) {
            // USE THE ENTER TRANSITION TRIGGER TEMPORARILY TO MORE EASILY TEST FUNCTIONALITY OF THE TRIGGERS
            // DISBLE THIS ONCE APP COMPLETE AND LEAVE ONLY THE TOAST MESSAGE FOR GEOFENCE ENTRY...THE DWELL
            // SHOULD BE THE TRIGGERING EVENT.
//            Geofence.GEOFENCE_TRANSITION_ENTER -> context.startActivity(erIntent)
            Geofence.GEOFENCE_TRANSITION_ENTER -> Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT)
                .show()

            Geofence.GEOFENCE_TRANSITION_EXIT -> Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT)
                .show()

            Geofence.GEOFENCE_TRANSITION_DWELL -> context.startActivity(erIntent)
//            Geofence.GEOFENCE_TRANSITION_DWELL -> Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT)
//                .show()

        }
    }
}