package com.cs523.android.means_v2

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.switchmaterial.SwitchMaterial
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException


private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,  EasyPermissions.PermissionCallbacks {

    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMapsBinding

    private lateinit var geoCoder: Geocoder

    private lateinit var locationRequest: LocationRequest

    private var REQUEST_FINE_LOCATION_PERMISSION: Int = 10001

    private var REQUEST_SMS_PERMISSION: Int = 10002

    private var REQUEST_FINE_AND_SMS_PERMISSION: Int = 10003

    private var REQUEST_BACKGROUND_PERMISSION: Int = 10004

    private var REQUEST_COARSE_FINE_SMS_PERMISSION: Int = 1005

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var userLocationMarker: Marker? = null

    // TOGGLE SWITCH OBJECT
    private lateinit var enableLocationSwitch: SwitchMaterial

    private lateinit var tempButton: Button

//    private var contact: String = "6178774893"
//
//    private var message: String = "test message text..."

    private lateinit var geofencingClient: GeofencingClient

    private var geofenceRadius = 25f


    // VARIABLES FOR THE WPI CLINIC, USE THESE THE IN ADD GEOFENCE FUNCTION
    private var wpiClinic:LatLng = LatLng(42.27324,-71.8100491)
    private var wpiGeofenceId: String = "wpiGeoFence"

    // VARIABLES FOR TESTING ONLY, USE THESE THE IN ADD GEOFENCE FUNCTION
    private var home: LatLng = LatLng(42.271968,-71.746081)
    private var homeId: String = "homeGeoFence"
    private var home2: LatLng = LatLng(42.268574,-71.755511)
    private var homeId2: String = "home2GeoFence"

    // VARIABLE FOR THE GEOFENCE HELPER
    private lateinit var geofenceHelper: GeofenceHelper

    // VAR TO HOLD THE ER ACTIVITY INTENT WHEN CALLED
    private lateinit var erIntent: Intent

    // VAR FOR GEOCODING/REVERSE GEOCODING OF THE CURRENT LOCATION
    private var currentStreetAddress: String? = null


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // REQUEST PERMISSIONS FOR ALL REQUIRED PERMISSIONS AT THE SAME TIME.
        requestPermissions()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used. (moved to onrequestpermissions returned
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)


        //INIT THE GEOCODER FOR LAT/LONG TO ADDRESS AND BACK MAPPING
        geoCoder = Geocoder(this)

        // INIT THE FUSEDLOCATIONPROVIDER CLIENT
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // INIT THE LOCATION REQUEST
        locationRequest = LocationRequest.create()
        // SET INTERVAL OF THE LIVE LOCATION UPDATES
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(5000)
        // SET THE ACCURACY OF THE LOCATION UPDATES
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setWaitForAccurateLocation(false)

        // INIT THE GEOFENCE CLIENT
        geofencingClient = LocationServices.getGeofencingClient(this)

        // INIT THE GEOFENCEHELPER CLASS
        geofenceHelper = GeofenceHelper(this)

        // INIT THE LISTENER FOR THE TOGGLE SWITCH
        enableLocationSwitch = findViewById(R.id.switch1)

        val onString: String = getString(R.string.switch_on)
        val offString: String = getString(R.string.switch_off)

        // SET UP THE LISTENER FOR THE SWITCH TOGGLE (TURN ON/OFF LOCATION UPDATES)
        enableLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                println("The switch was unflipped")
                enableLocationSwitch.text = offString
                stopLocationUpdates()
            } else {
                println("The switch was flipped")
                enableLocationSwitch.text = onString
                startLocationUpdates()
            }
        }

        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>//
        //<<<< START OF TEMPORARY ACTION (BUTTON CLICK) USED TO CALL ALL FUNCTIONS REQUIRED WHEN A FALL/ER IS DETECTED>>>>>//
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>//
        // INIT THE ONCLICKLISTENER FOR THE CHECK ADDRESS BUTTON
        tempButton = findViewById(R.id.temp_button)

        // WHEN THE BUTTON CLICKED...
        tempButton.setOnClickListener {

            // CHECK PERMS FOR FINE LOCATION FOR LIVE UPDATES
            if (!EasyPermissions.hasPermissions(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS
                )
            ) {
                EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app.more than Q",
                    REQUEST_FINE_AND_SMS_PERMISSION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS
                )
                EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app.more than Q",
                    REQUEST_BACKGROUND_PERMISSION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                return@setOnClickListener
            } else {
                Log.d(TAG, "Fine location and SMS already granted")

                // ATTEMPT GEOCODE (OBTAIN CURRENT ADDRESS OF LOCATION).
                // IF NO INTERNET, CATCH IOEXCEPTION AND PROVIDE TOAST MESSAGE
                var currentLocationTask: Task<Location> = fusedLocationProviderClient.lastLocation

                // IF SUCCESSFUL IN GETTING THE LAST LOCATION...
                currentLocationTask.addOnSuccessListener { currentLocation ->
                    var currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

                    // TRY STATEMENT TO CONVERT THE CURRENTLOCATION (LAT/LONG) INTO A STREET ADDRESS
                    try {
                        var currentAddresses: List<Address> =
                            geoCoder.getFromLocation(
                                currentLatLng.latitude,
                                currentLocation.longitude,
                                1
                            ) as List<Address>
                        // TAKE THE FIRST ITEM IN THE LIST OF ADDRESSES
                        val currentAddress: Address = currentAddresses[0]

                        // TAKE THE ADDRESS LINE OF THAT OBJECT AND COVER TO STRING(FROM ADDRESS OBJECT)
                        currentStreetAddress = currentAddress.getAddressLine(0).toString()

                        //FALL NOTIFICATION....INITIATE AN INTENT USING THE FALLALERT CLASS
                        // SET UP TO CALL THE FALL ALERT ACTIVITY FROM THIS MAPS ACTIVITY
                        erIntent = Intent(this, FallAlert::class.java)

                        // ADD THE CURRENT LOCATION'S ADDRESS TO THE INTENT
                        erIntent.putExtra("address", currentStreetAddress)

                        // CALL THE ER ALERT ACTIVITY
                        startActivity(erIntent)

                    } catch (e: IOException) {
                        Toast.makeText(this, "IOException: No Internet access", Toast.LENGTH_LONG)
                            .show()
                    }
                }

            }
        }
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>//
        //<<<<<< END OF TEMPORARY ACTION (BUTTON CLICK) USED TO CALL ALL FUNCTIONS REQUIRED WHEN A FALL IS DETECTED>>>>>>>>//
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>//


    }

    // CREATE THE OPTIONS MENU AT THE TOP OF THE ACTIVITY
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu,menu)
        return true
    }

    // GENERATE THE GOOGLE MAP AND IT'S SETTINGS
    override fun onMapReady(googleMap: GoogleMap) {

        // CHECK PERMS FOR FINE LOCATION FOR LIVE UPDATES
        if (!EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_FINE_AND_SMS_PERMISSION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        mMap = googleMap

        // SET WHICH TYPE OF MAP DESIRED NORMAL OR SATELLITE
//        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL


        enableUserLocation()
    }

    // INITIALIZE THE LOCATION CALLBACK - IS CALLED WHENEVER A USER LOCATION UPDATE
    // IS RECEIVED
    private var locationCallBack = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if(mMap != null){
                locationResult.getLastLocation()?.let { setUserLocationMarker(it) }
            }
            for(location in locationResult.locations){
                Log.d(TAG, "onLocationResult: " + location.toString())
            }
        }
    }

    // SET UP THE MARKER THAT WILL FOLLOW THE USERS LOCATION AS
    // LOCATION IS UPDATED CONTINUOUSLY
    private fun setUserLocationMarker(location: Location){

        val latLng = LatLng(location.latitude,location.longitude)

        // IF THE LOCATION MARKER IS CURRENTLY EMPTY..CREATE ONE
        if(userLocationMarker == null){
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.goathead))
            markerOptions.rotation(location.bearing)
            userLocationMarker = mMap.addMarker(markerOptions)!!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

        } else{
            // IF MARKER WAS NOT EMPTY...USE IT
            userLocationMarker!!.position = latLng
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        }

    }

    // START THE CONTINUOUS LOCATION UPDATES
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper())

    }

    // STOP THE CONTINUOUS LOCATION UPDATES
    private fun stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()

        // CHECK PERMISSIONS, REQUEST IF NEEDED
        if(!PermissionTrackingUtility.hasLocationPermissions(this)){
            requestPermissions()

        }else{
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        }

        // CALL THE FUNCTION TO BRING LIVE UPDATES OF USERS LOCATION
        startLocationUpdates()

        // WPI GEOFENCE SETTINGS
//        addGeofence(wpiClinic,geofenceRadius )

        // TESTING GEOFENCE SETTINGS
//        addGeofence(home, geofenceRadius)
        addGeofence(home2, geofenceRadius)

    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    // SET UP ACCESS TO USER LOCATION..THIS ADDS THE LOCATE BUTTON IN THE UPPER RIGHT
    // OF SCREEN
    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        mMap.isMyLocationEnabled = true
    }

    // UPDATE THE UI WITH THE CURRENT ADDRESS LOCATION OF THE USER
    fun updateMapAddress(address: String){
        var mapAddressText: TextView = findViewById(R.id.current_location)
        mapAddressText.text = address
    }
//
//    // SEND SMS MESSAGE  <<<<<  MOVED TO THE ERALERT ACTIVITY >>>>>>>>>
//    fun sendSms(context: Context, contact: String?, message: String) {
//
//        // CHECK PERMISSIONS...
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
//        ) {
//
//            println("INSIDE Send sms...perms granted...sending message")
//            // IF GRANTED, SEND THE MESSAGE
//            val manager = SmsManager.getDefault()
//            manager.sendTextMessage(contact, null, message, null, null)
//
//
//        } else {
//
//            // IF NOT, GRANTED..ASK FOR PERMISSIONS
//            Log.d(TAG, "ERROR: No permission to send an SMS")
//            Toast.makeText(this , "ERROR: No permission to send an SMS", Toast.LENGTH_LONG).show()
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.SEND_SMS)){
//                // SHOW USER A DIALOG..THEN REQUEST ACCESS
//                ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.SEND_SMS),REQUEST_SMS_PERMISSION)
//            } else {
//                ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.SEND_SMS),REQUEST_SMS_PERMISSION)
//            }
//        }
//    }


    // ADD A GEOFENCE
    @SuppressLint("MissingPermission")
    fun addGeofence(latLng: LatLng, radius: Float) {

        // CHECK PERMS FOR FINE LOCATION FOR LIVE UPDATES
        if (!EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                "You need to 'Always Allow' location permissions to use this app.",
                REQUEST_BACKGROUND_PERMISSION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        } else {
            Log.d(TAG, "Background Location Permission is already granted")

            // WPI GEOFENCE SETTINGS
//            var geofence: Geofence = geofenceHelper.getGeofence(wpiGeofenceId, latLng, radius,

                // TESTING GEOFENCE SETTINGS
//            var geofence: Geofence = geofenceHelper.getGeofence(homeId, latLng, radius,
            var geofence: Geofence = geofenceHelper.getGeofence(homeId2, latLng, radius,

                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL
            )

            // SET UP THE GEOFENCING REQUEST
            var geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)

            // SET UP A PENDING INTENT FOR THE GEOFENCE
            var geoPendingIntent: PendingIntent? = geofenceHelper.getPendingIntent()

            // SET UP THE GEOFENCES
            if (geoPendingIntent != null) {
                geofencingClient?.addGeofences(geofencingRequest, geoPendingIntent)?.run {
                    addOnSuccessListener {
                        Log.d(TAG, "Geofence Added...")

                        // ADD A CIRCLE TO MARK THE GEOFENCE AREA
                        addGeofenceCircle(latLng,radius)

                        Toast.makeText(this@MapsActivity, "Geofence added...", Toast.LENGTH_SHORT)
                            .show()
                    }
                    addOnFailureListener {
                        Log.d(TAG, "Add Geofence Failed")
                        Toast.makeText(this@MapsActivity, "Geofence add failed...", Toast.LENGTH_SHORT)
                            .show()

                    }
                }
            }
        }
    }

    // SETTINGS FOR THE GEOFENCE CIRCLE
    fun addGeofenceCircle(latLng: LatLng, radius: Float){
        println("inside the add geofence cirle function")
        var circleOptions: CircleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255,255,0,0))
        circleOptions.fillColor(Color.argb(64,255,0,0))
        circleOptions.strokeWidth(4f)
        mMap.addCircle(circleOptions)

    }

    // FUNCTION FOR REQUESTING RUNTIME PERMISSIONS FOR THE APPLICATION
    private fun requestPermissions(){

        // CHECK IF PERMISSIONS ARE ALREADY GRANTED, IF YES...JUST RETURN
        if(PermissionTrackingUtility.hasLocationPermissions(this)) {
            // PRINT TRUTH VALUES OF THE CURRENT PERMISSIONS
            PermissionTrackingUtility.returnValues(this)
            return
        }else{
            // PRINT VALUE OF CURRENT PERMISSIONS IF ONE OR MORE WHERE FALSE
            PermissionTrackingUtility.returnValues(this)
        }
        // IF SOFTWARE VERSION IS LESS THAN ANDROID 10, ONLY REQUEST SMS, FINE AND COURSE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.....",
                REQUEST_COARSE_FINE_SMS_PERMISSION,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            // IF ANDROID 10 OR MORE, ASK FOR SMS, FINE/COARSE, AND BACKGROUND LOCATION
        } else {
            println("about the request perms for Q or above")
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.more than Q",
                REQUEST_FINE_AND_SMS_PERMISSION,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )

            EasyPermissions.requestPermissions(
                this,
                "You'll need to manually set location to 'Always Allow' under the applications " +
                        "'Location' field in 'Settings'",
                REQUEST_BACKGROUND_PERMISSION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

    }

    // EASYPERMS PERMISSION REQUEST CALLBACK FUNCTION. CATCHES THE RESULTS OF THE PERMISSION REQUEST
    // OF GRANTED PERMISSIONS.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // PASS THE RESULTS FROM THE DEFAULT ONREQUESTPERMRESULT TO THE EASYPERM VERION OF THE CALLBACK..
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        PermissionTrackingUtility.returnValues(this)
    }

    // WHEN RETURNED PERMISSIONS ARE GRANTED, THIS FUNCTION IS CALLED.
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

        PermissionTrackingUtility.returnValues(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //zoomToUserLocation()
        return
    }

    // EASYPERMS PERMISSION REQUEST CALLBACK FUNCTION. CATCHES THE RESULTS OF THE PERMISSION REQUEST
    // OF NON-GRANTED PERMISSIONS.
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // IF USER HAS PERMANENTLY DISABLED A PERMISSION, SHOW A DIALOG EXPLAINING THE REQUIREMENT
            // AND DIRECT THE USER TO THE APP SETTINGS SCREEN TO MANUALLY CHANGE THE SETTING.
            AppSettingsDialog.Builder(this).build().show()
        } else {
            // IF USE HAS NOT PERMANENTLY DISABLED A PERMISSION, REQUEST PERMISSIONS AGAIN.
            requestPermissions()
        }
    }


}