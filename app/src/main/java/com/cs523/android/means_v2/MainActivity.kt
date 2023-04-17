package com.cs523.android.means_v2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cs523.android.means_v2.databinding.ActivityMainBinding

//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.LocationSettingsRequest
//import com.google.android.gms.location.SettingsClient
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.gms.tasks.OnFailureListener
//import com.google.android.gms.tasks.OnSuccessListener


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var REQUEST_LOCATION_PERMISSION: Int = 10001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askLocationPermission()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu,menu)
        return true
    }


    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this ,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            askLocationPermission()
        } else {
            return
        }
    }

    override fun onStop() {
        super.onStop()
//        stopLocationUpdates()
    }

    fun askLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

//    // IF PERMISSIONS NEEDED TO BE REQUESTED, RECEIVE THE RESULT OF THAT REQUEST HERE, IF WAS GRANTED DO STUFF
//    // IF NOT GRANTED...REQUEST AGAIN
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
//                                            grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        println("inside the requestpermissionresult function...after perms where requested...")
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                // GETS THE LAST LOCATION (JUST ONCE ON START)
////                getLastLocation()
//
//                // GET LOCATION TO CONTINUOUSLY UPDATE
//                checkSettingsAndStartLocationUpdates()
//
//            } else {
//                Log.d(TAG, "Permission is not granted")
//                askLocationPermission()
//            }
//        }
//    }

}