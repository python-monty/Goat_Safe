package com.cs523.android.means_v2

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityErAlertBinding
import pub.devrel.easypermissions.EasyPermissions

// !!!!!!!!!!!!!!!  THIS CONTACT NEEDS TO ULTIMATELY COME FROM
// !!!!!!!!!!!!!!! THE USERS SETTINGS ... IT IS A TEMP CARD CODED
private var contact: String = "6178774893"
// !!!!!!!!!!!!!!!
// !!!!!!!!!!!!!!!

private var TAG = "FallAlert"

private var REQUEST_SMS_PERMISSION: Int = 10003

//private val textView: TextView? = null

//private lateinit var erBinding: ActivityErAlertBinding

class FallAlert: AppCompatActivity() {

    // INIT THE VAR TO HOLD THE TEXT ABOVE THE TIMER
    private lateinit var timerText: TextView

    // INIT THE VAR TO HOLD THE TIMER TEXTVIEW
    private lateinit var timer: TextView

    // INIT THE VAR TO HOLD THE BINDING
    private lateinit var erBinding: ActivityErAlertBinding


    // INIT VAR TO HOLD THE ADDRESS OF THE MEDICAL FACILITY
    private lateinit var textAddress: String

    // INIT VAR TO HOLD TEXT MESSAGE STRING TO SEND TO EMERGENCY CONTACT
    private lateinit var textMessage: String

    // INIT VAR TO HOLD THE COUNTERTIMER OBJECT
    private lateinit var countDownTimer: CountDownTimer

    private lateinit var alarm: MediaPlayer

//    private var qrCodeIntent: Intent? = null
//
//    private var FallContext: Context = applicationContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SET UP THE BINDING
        erBinding= ActivityErAlertBinding.inflate(layoutInflater)
        setContentView(erBinding.root)

        // ATTACH THE VARS TO ELEMENTS IN THE LAYOUT
        timerText=findViewById(R.id.timer_text)
        timer=findViewById(R.id.timer)


        alarm = MediaPlayer.create(this ,R.raw.alarm)
        alarm.start()

//        qrCodeIntent = Intent(FallContext,QrCode::class.java)
//        qrCodeIntent!!.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)


        // MESSAGE TEXT REC'D FROM THE INTENT EXTRAS
        //textMessage = intent.getStringExtra("text").toString()

        // MESSAGE ADDRESS REC'D THE INTENT EXTRAS
        textAddress = intent.getStringExtra("address").toString()

        textMessage ="The Goat Safe phone app received a fall notification from " +
                "the user. The user was unable to mark themselves are 'Okay' after the " +
                "fall. Please check on the user. At the time of the fall, the " +
                "user was located at $textAddress"


        // SET UP THE COUNTDOWN TIMER - 60 SECONDS
        countDownTimer = object : CountDownTimer(10000, 1000){

            // COUNT DOWN AND UPDATE THE TEXT VIEW AT 1 SEC INTERVALS
            override fun onTick(millisUntilFinished: Long) {
                timer.text = (millisUntilFinished / 1000).toString()
            }

            // WHEN THE TIMER EXPIRES, SEND THE TEXT MESSAGE
            override fun onFinish() {
                sendSms(this@FallAlert, contact, textMessage )
                alarm.stop()
//                startActivity(qrCodeIntent)

                Toast.makeText(this@FallAlert, "Sent message to $contact", Toast.LENGTH_SHORT).show()
            }
            // START THE TIMER
        }.start()


//

        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var notOkayButton: Button = findViewById(R.id.not_okay_button)

        // SET UP A LISTENER FOR CLICKS OF THE NOT OKAY BUTTON, IF CLICKED
        // SEND SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        notOkayButton.setOnClickListener {
            sendSms(this, contact, textMessage )
            countDownTimer.cancel()
            alarm.stop()
            Toast.makeText(this, "Sent message to $contact\n\n " +
                    "Use your phone's back button to return to reset Goat Safe", Toast.LENGTH_LONG).show()
        }

        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var okayButton: Button = findViewById(R.id.okay_button)


        // SET UP A LISTENER FOR CLICKS OF THE OKAY BUTTON, IF CLICKED
        // CANCEL SENDING OF SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        okayButton.setOnClickListener {
            countDownTimer.cancel()
            alarm.stop()
            Toast.makeText(this, "           Emergency Cancelled!!\n\n " +
                    "Use your phone's back button to return to reset Goat Safe"
                , Toast.LENGTH_LONG).show()
        }

    }

    // CREATE THE OPTIONS MENU AT THE TOP OF THE ACTIVITY
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu,menu)
        return true
    }


    // SEND SMS MESSAGE
    private fun sendSms(context: Context, contact: String?, message: String) {

        println("in the send sms function")
        // CHECK PERMISSIONS...
        // CHECK PERMS FOR SEND SMS
        if (!EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.SEND_SMS
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_SMS_PERMISSION,
                android.Manifest.permission.SEND_SMS
            )

        } else {
            println("permissions are granted...sending the sms...in the send sms function")
            // IF GRANTED, SEND THE MESSAGE
            val manager = SmsManager.getDefault()
            manager.sendTextMessage(contact, null, message, null, null)

        }
    }




}