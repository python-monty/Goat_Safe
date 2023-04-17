package com.cs523.android.means_v2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityQrCodeBinding

class QrCode : AppCompatActivity() {


    private lateinit var qrCode: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        qrCode = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(qrCode.root)




    }
}