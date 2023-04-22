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
import com.cs523.android.means_v2.databinding.ActivityFallAlertBinding
import pub.devrel.easypermissions.EasyPermissions

// !!!!!!!!!!!!!!!  THIS CONTACT NEEDS TO ULTIMATELY COME FROM
// !!!!!!!!!!!!!!! THE USERS SETTINGS ... IT IS A TEMP CARD CODED
//private var contact: String = "6178774893"
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
    private lateinit var fallBinding: ActivityFallAlertBinding

    // INIT VAR TO HOLD THE ADDRESS OF THE MEDICAL FACILITY
    private lateinit var textAddress: String

    // INIT VAR TO HOLD TEXT MESSAGE STRING TO SEND TO EMERGENCY CONTACT
//    private lateinit var textMessage: String

    // INIT VAR TO HOLD THE COUNTERTIMER OBJECT
    private lateinit var countDownTimer: CountDownTimer

    private lateinit var alarm: MediaPlayer

    private var UID: String? = null

    private var uName: String? = null

    private var erContact: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SET UP THE BINDING
        fallBinding= ActivityFallAlertBinding.inflate(layoutInflater)
        setContentView(fallBinding.root)

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


        // USE THE UID TO GET THE USERS EMERGENCY CONTACT FROM THE DATABASE
        // INSTANTIATE THE FIREBASE CLOUD FIRESTORE DATABASE SERVICE
//        val db = Firebase.firestore
//
//        // OBTAIN THE USER PROFILE DOCUMENT FROM THE DATABASE
//        // BUT PULLING ALL THE USERS DOCUMENT FILE, THEN ITERATE TO
//        // CHECK FOR THE USERS RECORD, FIND THE KEY MATCHING
//        // USERERCONTACTPHONE AND PULL THE VALUE FROM THAT KEY
//        // STORE IN CONTACT VARIABLE
//        db.collection("users")
//            .get()
//            .addOnSuccessListener { result ->
//
//                for (document in result) {
//
//                    if (document.id == UID) {
//
//                        val uidData = document.data
//
//                        // FILTER THE USERS PROFILE BASED ON THE USERERCONTACTPHONE STRING
//                        // GET THE EMERGENCY CONTACT PHONE NUMBER VALUE FROM THAT KEY
//                        var rawPhone =
//                            uidData.filterKeys { it == "UserErContactPhone" }.values.toString()
//                        // DROP THE LEADING SQUARE BRACKET
//                        var partialPhone = rawPhone.drop(1)
//                        /// DROP THE TRAILING SQUARE BRACKET
//                        contact = partialPhone.dropLast(1)
//
////                        // GET ALL THE KEYS FROM THE USERS PROFILE HASHMAP
////                        println("here are the hashmap keys: ${uidData.keys}")
//                        // GET ALL THE VALUES FROM THE USERS PROFILE HASHMAP
////                        println("here are the hashmap values: ${uidData.values}")
////                        var pattern = Regex("(UserErContactPhone=){1}[0-9]{10}")
////                        var result = pattern.containsMatchIn(uidData.toString())
////                        println("here is the regex result $result")
//
//                        Log.d(TAG, "Here is the users data: ${document.data}")
//                        Log.d(TAG, "Here is the users er contact: $contact")
//
//                    }
////                    Log.d(TAG, "${document.id} => ${document.data}")
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w(TAG, "Error getting documents.", exception)
//
//            }
//
        // ATTACH THE VARS TO ELEMENTS IN THE LAYOUT
        timerText=findViewById(R.id.timer_text)
        timer=findViewById(R.id.timer)

        alarm = MediaPlayer.create(this ,R.raw.alarm)
        alarm.start()

        // MESSAGE ADDRESS REC'D THE INTENT EXTRAS
        textAddress = intent.getStringExtra("address").toString()

        println("LINE83: FallAlert - value of textAddress is $textAddress")

//        textMessage= "Test message...$erContact and $uName"
////        textMessage ="$uName has fallen down and was unable to mark themselves are 'Okay'" +
////                " in the Goat Safe app fall. Please check on $uName. At the time of the " +
////                "fall, the user was located at $textAddress"

//        println("LINE182: FallAlert - value of erContact is  $erContact")
//
//        println("LINE184: FallAlert - value of uName is  $uName")
//
//        println("LINE186: FallAlert - value of contact is $erContact")


        // SET UP THE COUNTDOWN TIMER - 60 SECONDS
        countDownTimer = object : CountDownTimer(10000, 1000){

            // COUNT DOWN AND UPDATE THE TEXT VIEW AT 1 SEC INTERVALS
            override fun onTick(millisUntilFinished: Long) {
                timer.text = (millisUntilFinished / 1000).toString()
            }

            // WHEN THE TIMER EXPIRES, SEND THE TEXT MESSAGE
            override fun onFinish() {
//                sendSms(this@FallAlert, erContact, textMessage )
                sendSms(this@FallAlert, erContact)
                alarm.stop()

                Toast.makeText(this@FallAlert, "onFinish : Sent message to $erContact", Toast.LENGTH_SHORT).show()
            }
            // START THE TIMER
        }.start()


        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var notOkayButton: Button = findViewById(R.id.not_okay_button)

        // SET UP A LISTENER FOR CLICKS OF THE NOT OKAY BUTTON, IF CLICKED
        // SEND SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        notOkayButton.setOnClickListener {
//            sendSms(this@FallAlert, erContact, textMessage )
            sendSms(this@FallAlert, erContact)
            countDownTimer.cancel()
            alarm.stop()
            Toast.makeText(this@FallAlert,
                "Sent message to $erContact\n\nUse your phone's " +
                        "back button to return to reset Goat Safe"
                , Toast.LENGTH_LONG).show()
        }

        // ATTACH BUTTON VAR TO THE NOT OKAY ELEMENT IN THE LAYOUT
        var okayButton: Button = findViewById(R.id.okay_button)


        // SET UP A LISTENER FOR CLICKS OF THE OKAY BUTTON, IF CLICKED
        // CANCEL SENDING OF SMS MESSAGE TO EMERGENCY CONTACT (TOAST MESSAGE TOO)
        okayButton.setOnClickListener {
            countDownTimer.cancel()
            alarm.stop()
            Toast.makeText(this,
                "           Emergency Cancelled!!\n\n " +
                    "Use your phone's back button to return to reset Goat Safe"
                , Toast.LENGTH_LONG).show()
        }

    }

    override fun onStop() {
        super.onStop()
        alarm.stop()
    }

    // CREATE THE OPTIONS MENU AT THE TOP OF THE ACTIVITY
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu2,menu)
        return true
    }

    // WHEN AN ITEM IN THE OPTIONS MENU IS SELECTED, CHECK WHICH ITEM
    // WAS SELECTED AND THEN DO SOMETHING
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){

            R.id.sign_out ->{
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

//        var message = "Test message...$contact and $uName"
        var message ="$uName has fallen down and was unable to mark himself/herself as 'Okay'" +
                " in the Goat Safe phone app. Please check on $uName. At the time of the " +
                "fall, $uName was located at $textAddress."



        val Sent = "SMS_SENT"

        val Delivered = "SMS_DELIVERED"

        println("in the send sms function")
        // CHECK PERMISSIONS...
        // CHECK PERMS FOR SEND SMS
        if (!EasyPermissions.hasPermissions(
                this@FallAlert,
                android.Manifest.permission.SEND_SMS
            )
        ) {
            EasyPermissions.requestPermissions(
                this@FallAlert,
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

////////////////  NEW ADDITIONS ABOVE ////////////////

                val manager = this@FallAlert.getSystemService(SmsManager::class.java)

////////////////  NEW ADDITIONS BELOW ///////////////
                if (contact != null) {
                    val smsParts: ArrayList<String> = manager.divideMessage(message)
////////////////  NEW ADDITIONS ABOVE ////////////////

//                sms.sendTextMessage(contact, null, message, sentPI, deliveredPI)

////////////////  NEW ADDITIONS BELOW ////////////////
                    manager.sendMultipartTextMessage(
                        contact, null, smsParts, sentPI, deliveredPI)
////////////////  NEW ADDITIONS ABOVE ////////////////

//                    manager.sendTextMessage(contact, null, message, null, null)

                } else {
                    println("The emergency contact field is empty.....")
                    Log.d(TAG, "The emergency contact field is empty.....")
                }
//                manager.sendTextMessage(contact, null, message, null, null)
            } else {

                // IF ANDROID VERSION 11 OR BELOW, DO THE FOLLOWING
                println("LINE185 - FallAlert - inside the SendSms and android 30 and below")
                //val manager = SmsManager.getDefault()

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
//
                val smsParts: ArrayList<String> = sms.divideMessage(message)

//                sms.sendTextMessage(contact, null, message, sentPI, deliveredPI)
                sms.sendMultipartTextMessage(
                    contact, null, smsParts, sentPI, deliveredPI)

            }

//
//                if(contact != null){
//                    println("Contact is not empty..old version value is : $contact")
//                    manager.sendTextMessage(contact, null, message, null, null)
//
//                }else{
//                    println("Old version...it's empty: $contact")
//
//                }
////                manager.sendTextMessage(contact, null, message, null, null)
//            }
//
//        }
//    }

        }
    }




}

private fun SmsManager.sendMultipartTextMessage(contact: String?, nothing: Nothing?, smsParts: ArrayList<String>, sentPI: PendingIntent?, deliveredPI: PendingIntent?) {

}
