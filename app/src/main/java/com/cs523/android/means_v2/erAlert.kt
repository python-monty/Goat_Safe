package com.cs523.android.means_v2

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.cs523.android.means_v2.databinding.ActivityErAlertBinding
import pub.devrel.easypermissions.EasyPermissions


private var TAG = "erAlert"

private var REQUEST_SMS_PERMISSION: Int = 10002

class erAlert (): AppCompatActivity() {

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

    private var UID: String? = null

    private var uName: String? = null

    private var erContact: String? = null

    private lateinit var wpiText: String

    private lateinit var qrIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SET UP THE BINDING
        erBinding = ActivityErAlertBinding.inflate(layoutInflater)
        setContentView(erBinding.root)

        // SET UP AN OBSERVER OF THE USERID LIVE DATA IN THE VIEW MODEL
        // OBTAIN THE USERID WHEN IT UPDATES, USE IT TO PULL THE USERS DATA
        // FROM THE DB AS NEEDED
        (dataViewModel as DataViewModel).userID.observe(this, Observer {
            UID = it
            println("Value of UID in the observer is $UID")
        })

        // SET UP AN OBSERVER OF THE USERNAME LIVE DATA IN THE VIEW MODEL
        // OBTAIN THE USERNAME WHEN IT UPDATES, USE IT TO PULL THE USERS DATA
        // FROM THE DB AS NEEDED
        (dataViewModel as DataViewModel).userName.observe(this, Observer {
            uName = it
            println("Value of uName in the observer is $uName")
        })


        // SET UP AN OBSERVER OF THE USERNAME LIVE DATA IN THE VIEW MODEL
        // OBTAIN THE EMERGENCY CONTACT PHONE WHEN IT UPDATES, USE IT TO PULL THE USERS DATA
        // FROM THE DB AS NEEDED
        (dataViewModel as DataViewModel).erContact.observe(this, Observer {
            erContact = it
            println("Value of ercontact in the observer is $erContact")
        })


        // ATTACH THE VARS TO ELEMENTS IN THE LAYOUT
        timerText = findViewById(R.id.timer_text)
        timer = findViewById(R.id.timer)

        alarm = MediaPlayer.create(this@erAlert, R.raw.alarm)
        alarm.start()

        // MESSAGE ADDRESS REC'D THE INTENT EXTRAS
        textAddress = intent.getStringExtra("address").toString()
        wpiText = intent.getStringExtra("text").toString()

        // SET UP THE COUNTDOWN TIMER - 60 SECONDS
        countDownTimer = object : CountDownTimer(10000, 1000) {

            // COUNT DOWN AND UPDATE THE TEXT VIEW AT 1 SEC INTERVALS
            override fun onTick(millisUntilFinished: Long) {
                timer.text = (millisUntilFinished / 1000).toString()
            }

            // WHEN THE TIMER EXPIRES, SEND THE TEXT MESSAGE
            override fun onFinish() {
//                sendSms(this@erAlert, contact, textMessage)
                sendSms(this@erAlert, erContact)
                alarm.stop()

                callQR()

                Toast.makeText(this@erAlert, "Sent message to $erContact", Toast.LENGTH_SHORT).show()
            }
        // START THE TIMER
        }.start()


        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var notOkayButton: Button = findViewById(R.id.not_okay_button)

        // SET UP A LISTENER FOR CLICKS OF THE NOT OKAY BUTTON, IF CLICKED
        // SEND SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        notOkayButton.setOnClickListener {
            sendSms(this@erAlert, erContact)
            countDownTimer.cancel()
            alarm.stop()

            callQR()

            Toast.makeText(this@erAlert,
                "Sent message to $erContact\n\nUse your phone's " +
                        "back button to return to reset Goat Safe",
                Toast.LENGTH_LONG).show()
        }

        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var okayButton: Button = findViewById(R.id.okay_button)


        // SET UP A LISTENER FOR CLICKS OF THE OKAY BUTTON, IF CLICKED
        // CANCEL SENDING OF SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        okayButton.setOnClickListener {
            countDownTimer.cancel()
            alarm.stop()
            Toast.makeText(this@erAlert,
                "           Emergency Cancelled!!\n\n " +
                        "Use your phone's back button to return to reset Goat Safe",
                Toast.LENGTH_LONG).show()
        }

    }

    override fun onStop() {
        super.onStop()
        alarm.stop()
    }

//    override fun onDestroy(){
//        super.onDestroy()
//        countDownTimer.cancel()
//
//    }

    // CREATE THE OPTIONS MENU AT THE TOP OF THE ACTIVITY
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu2, menu)
        return true
    }

    // WHEN AN ITEM IN THE OPTIONS MENU IS SELECTED, CHECK WHICH ITEM
    // WAS SELECTED AND THEN DO SOMETHING
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.sign_out -> {
                val intent = Intent(this, login::class.java)
                Toast.makeText(this, "Signed User Out", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    // SEND SMS MESSAGE
    private fun sendSms(context: Context, contact: String?) {



        var message ="The Goat Safe phone app noticed that $uName has entered the $wpiText " +
                "located at $textAddress. $uName was not able to mark " +
                "himself/herself 'Okay' within the app after entering. Please check on " +
                "$uName."


        val Sent = "SMS_SENT"

        val Delivered = "SMS_DELIVERED"

        // CHECK PERMS FOR SEND SMS
        if (!EasyPermissions.hasPermissions(
                this@erAlert,
                android.Manifest.permission.SEND_SMS
            )
        ) {
            EasyPermissions.requestPermissions(
                this@erAlert,
                "You need to accept location permissions to use this app.",
                REQUEST_SMS_PERMISSION,
                android.Manifest.permission.SEND_SMS
            )

        } else {

            // IF GRANTED, SEND THE MESSAGE
            val version = Build.VERSION.SDK_INT

            // IF ANDROID VERSION 12 OR ABOVE DO THE FOLLOWING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

////////////////  NEW ADDITIONS BELOW ///////////////
                val sentPI: ArrayList<PendingIntent> = ArrayList<PendingIntent>()

                sentPI.add(PendingIntent.getBroadcast(
                    this, 0, Intent(Sent), 0
                ))

                val deliveredPI: ArrayList<PendingIntent> = ArrayList<PendingIntent>()

                deliveredPI.add(PendingIntent.getBroadcast(
                    this, 0, Intent(Delivered), 0
                ))

                // WHEN THE SMS HAS BEEN SENT
                val br: BroadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(arg0: Context?, arg1: Intent?) {
                        when (resultCode) {
                            RESULT_OK -> Log.d(TAG, "Send Message Result Code OK")
                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.d(
                                TAG,
                                "Send Message Result Generic Failure"
                            )
                            SmsManager.RESULT_ERROR_NO_SERVICE -> Log.d(
                                TAG,
                                "Send Message Result Code No Service"
                            )
                            SmsManager.RESULT_ERROR_NULL_PDU -> Log.d(
                                TAG,
                                "Send Message Result Code Null PDU"
                            )
                            SmsManager.RESULT_ERROR_RADIO_OFF -> Log.d(
                                TAG,
                                "Send Message Result Code Radio Off"
                            )
                        }
                        unregisterReceiver(this)
                    }
                }
                registerReceiver(br, IntentFilter(Sent))

                //  WHEN THE SMS HAS BEEN DELIVERED
                val br2: BroadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(arg0: Context?, arg1: Intent?) {
                        when (resultCode) {
                            RESULT_OK -> Log.d(TAG, "Delivered Message Result OK")
                            RESULT_CANCELED -> Log.d(TAG, "Send Message Result Result Canceled")
                        }
                        unregisterReceiver(this)
                    }
                }
                registerReceiver(br2, IntentFilter(Delivered))


                val manager = this@erAlert.getSystemService(SmsManager::class.java)

                if (contact != null) {
                    val smsParts: ArrayList<String> = manager.divideMessage(message)


                    manager.sendMultipartTextMessage(
                        contact, null, smsParts, sentPI, deliveredPI)


                } else {
                    println("The emergency contact field is empty.....")
                    Log.d(TAG, "The emergency contact field is empty.....")
                }
            } else {

                // IF ANDROID VERSION 11 OR BELOW, DO THE FOLLOWING
                val sentPI: ArrayList<PendingIntent> = ArrayList<PendingIntent>()

                sentPI.add(PendingIntent.getBroadcast(
                    this, 0, Intent(Sent), 0
                ))

                val deliveredPI: ArrayList<PendingIntent> = ArrayList<PendingIntent>()

                deliveredPI.add(PendingIntent.getBroadcast(
                    this, 0, Intent(Delivered), 0
                ))

                // WHEN THE SMS HAS BEEN SENT
                val br: BroadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(arg0: Context?, arg1: Intent?) {
                        when (resultCode) {
                            RESULT_OK -> Log.d(TAG, "Send Message Result Code OK")
                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.d(
                                TAG,
                                "Send Message Result Generic Failure"
                            )
                            SmsManager.RESULT_ERROR_NO_SERVICE -> Log.d(
                                TAG,
                                "Send Message Result Code No Service"
                            )
                            SmsManager.RESULT_ERROR_NULL_PDU -> Log.d(
                                TAG,
                                "Send Message Result Code Null PDU"
                            )
                            SmsManager.RESULT_ERROR_RADIO_OFF -> Log.d(
                                TAG,
                                "Send Message Result Code Radio Off"
                            )
                        }
                        unregisterReceiver(this)
                    }
                }
                registerReceiver(br, IntentFilter(Sent))

                //  WHEN THE SMS HAS BEEN DELIVERED
                val br2: BroadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(arg0: Context?, arg1: Intent?) {
                        when (resultCode) {
                            RESULT_OK -> Log.d(TAG, "Delivered Message Result OK")
                            RESULT_CANCELED -> Log.d(TAG, "Send Message Result Result Canceled")
                        }
                        unregisterReceiver(this)
                    }
                }
                registerReceiver(br2, IntentFilter(Delivered))

                val sms: SmsManager = SmsManager.getDefault()

                val smsParts: ArrayList<String> = sms.divideMessage(message)


                sms.sendMultipartTextMessage(
                    contact, null, smsParts, sentPI, deliveredPI)
            }
        }
    }
    private fun callQR(){

        //QR CODE NOTIFICATION....INITIATE AN INTENT USING THE QRCODE CLASS
        // SET UP TO CALL QR ACTIVITY FROM THIS FALLALERT ACTIVITY
        qrIntent = Intent(this@erAlert, qrCode::class.java)

        // CALL THE ER ALERT ACTIVITY
        startActivity(qrIntent)
    }


}
private fun SmsManager.sendMultipartTextMessage(contact: String?, nothing: Nothing?, smsParts: ArrayList<String>, sentPI: PendingIntent?, deliveredPI: PendingIntent?) {

}