package com.cs523.android.means_v2

// CLASS USED IN CONJUNCTION WITH THE USER SETTINGS ITEM IN THE SETTINGS MENU BAR
// ALLOWS THE USER TO CHANGE/UPDATE THEIR SETTINGS AFTER INITIAL SETUP THROUGH
// THE REGISTRATION ACTIVITY


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityRegistration2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


private const val TAG = "RegistrationActivity"

class registration2 : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
//    private lateinit var UserName: String
//    private lateinit var UserEmail: String
//    private lateinit var UserPassword: String
    private lateinit var UserPhone: String
    private lateinit var UserErContactPhone: String
    private lateinit var UserErContactName: String
    private lateinit var UserMedicalConditions: String
    private lateinit var UserMedicalPrescripts: String
    private lateinit var UserMedicalProviderName: String
    private lateinit var UserMedicalProviderPhone: String
    private lateinit var UserID: String


    private lateinit var binding: ActivityRegistration2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistration2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // INSTANTIATE THE FIREBASE AUTHENTICATION SERVICE
        auth = FirebaseAuth.getInstance()

        // INSTANTIATE THE FIREBASE CLOUD FIRESTORE DATABASE SERVICE
        val db = FirebaseFirestore.getInstance()

        // REC THE UID OF THE LOGGED IN USER FROM THE MAPSACTIVITY INTENT
        // .PUTEXTRA
        UserID = intent.getStringExtra("UID").toString()
//        UserEmail = intent.getStringExtra("email").toString()
//        UserPassword = intent.getStringExtra("password").toString()


        binding.updateButton.setOnClickListener {

            UserPhone = binding.userPhone.text.toString()
            UserErContactPhone = binding.emergencyContactPhone.text.toString()
            UserErContactName = binding.emergencyContactName.text.toString()
            UserMedicalConditions = binding.medicalConditions.text.toString()
            UserMedicalPrescripts = binding.medicalPrescripts.text.toString()
            UserMedicalProviderName = binding.medicalProviderName.text.toString()
            UserMedicalProviderPhone = binding.medicalProviderPhone.text.toString()

            if (UserPhone.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserPhone", UserPhone)
            }

            if (UserErContactPhone.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserErContactPhone", UserErContactPhone)
            }

            if (UserErContactName.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserErContactName", UserErContactName)
            }

            if (UserMedicalConditions.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserMedicalConditions", UserMedicalConditions)
            }

            if (UserMedicalPrescripts.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserMedicalPrescripts", UserMedicalPrescripts)
            }

            if (UserMedicalProviderName.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserMedicalProviderName", UserMedicalProviderName)
            }

            if (UserMedicalProviderPhone.isNotEmpty()) {
                db.collection("users")
                    .document(UserID)
                    .update("UserMedicalProviderPhone", UserMedicalProviderPhone)
            }


            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
}

