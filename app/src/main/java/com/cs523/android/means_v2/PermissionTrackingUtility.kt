package com.cs523.android.means_v2

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions


// OBJECT CLASS FOR CHECKING APPLICATION PERMISSIONS
object PermissionTrackingUtility {

    fun hasLocationPermissions(context: Context) =
    // IF ANDROID VERSION IS LESS THAN 10, BACKGROUND LOCATION
        // IS AUTOMATIC WITH FINE_LOCATION
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            // IF NOT LESS THAN 10 (10 OR ABOVE), WILL NEED TO EXPLICITLY
            // ASK FOR ACCESS_BACKGROUND_LOCATION PERMISSIONS
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)


        }

    // FUNCTION TO PRINT THE VALUES FOR THE CURRENT PERMISSIONS
    fun returnValues(context: Context){

        var truthFine:Boolean = EasyPermissions.hasPermissions(context,Manifest.permission.ACCESS_FINE_LOCATION)
        var truthCoarse:Boolean = EasyPermissions.hasPermissions(context,Manifest.permission.ACCESS_COARSE_LOCATION)
        var truthBackground:Boolean = EasyPermissions.hasPermissions(context,Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        println("Truths of fine/coarse/background are : + ${truthFine.toString()} \n + ${truthCoarse.toString()} \n + ${truthBackground.toString()}")
    }
}