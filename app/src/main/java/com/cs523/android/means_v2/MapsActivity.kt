package com.cs523.android.means_v2


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.cs523.android.means_v2.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


private const val TAG = "MapsActivity"

var dataViewModel: ViewModel = DataViewModel()

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, SensorEventListener{

    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMapsBinding

    private lateinit var geoCoder: Geocoder

    private lateinit var locationRequest: LocationRequest

    private var REQUEST_FINE_AND_SMS_PERMISSION: Int = 10003

    private var REQUEST_BACKGROUND_PERMISSION: Int = 10004

    private var REQUEST_COARSE_FINE_SMS_PERMISSION: Int = 1005

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var userLocationMarker: Marker? = null

    // TOGGLE SWITCH OBJECT
    private lateinit var enableLocationSwitch: SwitchMaterial

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

    private lateinit var fallIntent: Intent

    private lateinit var userID: String

    private lateinit var userEmail: String

    private lateinit var userPassword: String

    // VAR FOR GEOCODING/REVERSE GEOCODING OF THE CURRENT LOCATION
    private var currentStreetAddress: String? = null

    // VARS FOR THE FALL DETECTION
    private lateinit var sensorManager : SensorManager
    private var accelerometer: Sensor? = null
    private var accelerationReaderPast : Float = SensorManager.GRAVITY_EARTH
    private var accelerationReader : Float = SensorManager.GRAVITY_EARTH
    private var mAccel: Float = 0.0F
    private var movementStart: Long = 0
    private val mTimer = Timer()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // REQUEST PERMISSIONS FOR ALL REQUIRED PERMISSIONS AT THE SAME TIME.
        requestPermissions()

        // GET THE UID OF THE LOGGED INTO USER, SUPPLIED FROM THE LOGINACTIVITY INTENT
        // AND ADDED AS .PUTEXTRA
        userID = intent.getStringExtra("UID").toString()
        userEmail = intent.getStringExtra("email").toString()
        userPassword = intent.getStringExtra("password").toString()

        // RETRIEVE USERS DATA IN THE FIREBASE DATABASE (USERNAME AND ER CONTACT)
        getFirebaseData(userID)

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

// START OF FALL DETECTION CODE
    // SET UP THE FALL DETECTION SENSOR
    setupSensor()

    }
    // GET USER FIREBASE DATABASE INFORMATION AND SEND TO THE VIEWMODEL
    private fun getFirebaseData(userID: String) {
        val db = Firebase.firestore

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {

                    if (document.id == userID) {

                        val uidData = document.data

                        // FILTER THE USERS PROFILE BASED ON THE USERERCONTACTPHONE STRING
                        // GET THE EMERGENCY CONTACT PHONE NUMBER VALUE FROM THAT KEY
                        var rawPhone =
                            uidData.filterKeys { it == "UserErContactPhone" }.values.toString()
                        // DROP THE LEADING SQUARE BRACKET
                        var partialPhone = rawPhone.drop(1)
                        /// DROP THE TRAILING SQUARE BRACKET
                        var erContact = partialPhone.dropLast(1)


                        // GET THE USERNAME OF CURRENT USER
                        var rawName =
                            uidData.filterKeys { it == "UserName" }.values.toString()
                        // DROP THE LEADING SQUARE BRACKET
                        var partialName = rawName.drop(1)
                        /// DROP THE TRAILING SQUARE BRACKET
                        var userName = partialName.dropLast(1)

                        Log.d(TAG, "Here is the users data: ${document.data}")
                        Log.d(TAG, "Here is the users er contact: $erContact")
                        Log.d(TAG, "Here is the users name: $userName")

                        // CALL THE UPDATE VIEWMODEL FUNCTION WITH USER ID, USERNAME AND ER CONTACT
                        updateViewModel(userID, userName, erContact, dataViewModel as DataViewModel)

                    }
//                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)

            }

    }

    // SET UP SENSOR FUNCTION
    private fun setupSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //register accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.also {
            sensorManager.registerListener(this@MapsActivity,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                movementStart = System.currentTimeMillis()

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                accelerationReaderPast = accelerationReader

                accelerationReader = sqrt(x.toDouble().pow(2.0) + y.toDouble()
                    .pow(2.0) + z.toDouble().pow(2.0))
                    .toFloat()

                if(accelerationReader<0.5){

                    Log.d("FreeFall", accelerationReader.toString())

//                    Toast.makeText(this@MapsActivity,"free fall",Toast.LENGTH_SHORT).show()

                    mTimer.schedule(object : TimerTask() {
                        //start after 2 second delay to make acceleration values "rest"
                        override fun run() {
                            firstTimer.start()
                            //Toast.makeText(this@MapsActivity,"free fall",Toast.LENGTH_SHORT).show()

                        }
                    }, 2000)
                }

                val precision = DecimalFormat("0.00")
                val ldAccRound = java.lang.Double.parseDouble(precision.format(accelerationReader))

                // UPDATE THE UI WITH A LIVE REPORTING OF THE CURRENT ACCELEROMETER VALUES
                binding.allTheNumbers.text = getString(R.string.acc_value, ldAccRound.toString())

            }
        }
    }


    // SET UP THE RECOVERY TIMER
    var firstTimer: CountDownTimer = object : CountDownTimer(30*1000, 1000) {

        //recovery timer
        override fun onTick(millisUntilFinished: Long) {
            //if there is movement before 30 seconds, cancel the timer
            val ms1 = millisUntilFinished/1000

            // UPDATED THE UI WITH THE COUNTDOWN UNTIL ALERT IS SENT.  IF USER HAS NOT MOVED
            // AT THE END OF 30 SECS, THE EVENT IS RECOGNIZED AS A FALL
            binding.allTheOtherNumbers.text = getString(R.string.timer , ms1.toString())

            if(accelerationReader>10.0f){

                binding.allTheOtherNumbers.text = getString(R.string.timer_default)
                Log.d("Moved", accelerationReader.toString())

                // CANCEL THE RECOVERY TIMER
                cancel()
            }
        }

        // WHEN THE RECOVERY TIMER EXPIRES, CALL THE FALL ALERT ACTIVITY
        @SuppressLint("MissingPermission")
        override fun onFinish() {

            // CHECK PERMS FOR FINE LOCATION FOR LIVE UPDATES
            if (!EasyPermissions.hasPermissions(
                    this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS))
            {
                EasyPermissions.requestPermissions(
                    this@MapsActivity,
                    "You need to accept location permissions to use this app.more than Q",
                    REQUEST_FINE_AND_SMS_PERMISSION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS)
                EasyPermissions.requestPermissions(
                    this@MapsActivity,
                    "You need to accept location permissions to use this app.more than Q",
                    REQUEST_BACKGROUND_PERMISSION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                return
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

                        // TAKE THE ADDRESS LINE OF THAT OBJECT AND CONVERT TO STRING(FROM ADDRESS OBJECT)
                        currentStreetAddress = currentAddress.getAddressLine(0).toString()

                        //FALL NOTIFICATION....INITIATE AN INTENT USING THE FALLALERT CLASS
                        // SET UP TO CALL THE FALL ALERT ACTIVITY FROM THIS MAPS ACTIVITY
                        fallIntent = Intent(this@MapsActivity, FallAlert::class.java)

                        // ADD THE CURRENT LOCATION'S ADDRESS TO THE INTENT
                        fallIntent.putExtra("address", currentStreetAddress)

                        // CALL THE ER ALERT ACTIVITY
                        startActivity(fallIntent)
//                        sendSms(this, contact, message)

                    } catch (e: IOException) {
                        Toast.makeText(this@MapsActivity, "IOException: No Internet access", Toast.LENGTH_LONG)
                            .show()
                    }
                }

            }

            Toast.makeText(applicationContext, "Fall Detected!!", Toast.LENGTH_SHORT)
                .show()
            cancel()

        }

    }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    // HANDLE WHEN USERS SELECTION MENU OPTIONS AT THE TOP OF THE ACTIVITY
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.user_settings -> {
                val intent = Intent(this, registration2::class.java)
                // ADD THE UID TO THE INTENT TO BE USED IN THE MAPS ACTIVITY
                intent.putExtra("UID", userID)
                intent.putExtra("email", userEmail)
                intent.putExtra("password", userPassword)
                startActivity(intent)
                true
            }
            R.id.sign_out ->{
                val intent = Intent(this, login::class.java)
                Toast.makeText(this, "Signed User Out", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //CREATE THE OPTIONS MENU
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
        addGeofence(home, geofenceRadius)
//        addGeofence(home2, geofenceRadius)

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
            var geofence: Geofence = geofenceHelper.getGeofence(homeId, latLng, radius,
//            var geofence: Geofence = geofenceHelper.getGeofence(homeId2, latLng, radius,

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

    // UPDATE THE VIEWMODEL WITH THE CURRENT USERS UID
    fun updateViewModel(UID: String, userName: String, erContact: String, dataViewModel: DataViewModel){

        println("values of UID, userName and erContact in the updateviewmodel function: " +
                "$UID\n$userName\n$erContact")


        dataViewModel.userID.value = UID
        dataViewModel.userName.value = userName
        dataViewModel.erContact.value = erContact

    }

    override fun onDestroy(){
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}