package com.cs523.android.means_v2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityQrCodeBinding

class qrCode : AppCompatActivity() {


    private lateinit var qrCode: ActivityQrCodeBinding

    private lateinit var mapsIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        qrCode = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(qrCode.root)


        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var toStart: Button = findViewById(R.id.toStart)

        // SET UP A LISTENER FOR CLICKS OF THE NOT OKAY BUTTON, IF CLICKED
        // SEND SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        toStart.setOnClickListener {
            //QR CODE NOTIFICATION....INITIATE AN INTENT USING THE QRCODE CLASS
            // SET UP TO CALL QR ACTIVITY FROM THIS FALLALERT ACTIVITY
            mapsIntent = Intent(this@qrCode, MapsActivity::class.java)

            // CALL THE ER ALERT ACTIVITY
            startActivity(mapsIntent)
        }



    }
}