package com.cs523.android.means_v2

// CLASS USED IN CONJUNCTION WITH THE USER SETTINGS ITEM IN THE SETTINGS MENU BAR
// ALLOWS THE USER TO CHANGE/UPDATE THEIR SETTINGS AFTER INITIAL SETUP THROUGH
// THE REGISTRATION ACTIVITY


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityRegistration2Binding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.auth.User
import kotlin.math.log

private const val TAG = "RegistrationActivity"

class registration2 : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
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
//
//        val docRef = db.collection("users").document(UserID).get()
        db.collection("users")
            .document(UserID)
            .get()
            .addOnSuccessListener {
                val currentErContactName = it.data?.get("UserErContactName")
                val currentErContactPhone = it.data?.get("UserErContactPhone")
                val currentMedicalConditions = it.data?.get("UserMedicalConditions")
                val currentMedicalPrescriptions =it.data?.get("UserMedicalPrescripts")
                val currentMedicalProviderName = it.data?.get("UserMedicalProviderName")
                val currentMedicalProviderPhone = it.data?.get("UserMedicalProviderPhone")

                binding.emergencyContactPhoneField.placeholderText = currentErContactPhone as CharSequence?
                binding.emergencyContactNameField.placeholderText = currentErContactName as CharSequence?
                binding.medicalConditionsField.placeholderText = currentMedicalConditions as CharSequence?
                binding.medicalPrescriptsField.placeholderText = currentMedicalPrescriptions as CharSequence?
                binding.medicalProviderNameField.placeholderText = currentMedicalProviderName as CharSequence?
                binding.medicalProviderPhoneField.placeholderText = currentMedicalProviderPhone as CharSequence?





//                Toast.makeText(this, "Your first name is ${it.documents[2].data?.get("UserErContactPhone")}", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{
                it.printStackTrace()
                Toast.makeText(this, "failed", Toast.LENGTH_LONG).show()
            }




        binding.updateButton.setOnClickListener {

            //UserPhone = binding.userPhone.text.toString()
            UserErContactPhone = binding.emergencyContactPhone.text.toString()
            UserErContactName = binding.emergencyContactName.text.toString()
            UserMedicalConditions = binding.medicalConditions.text.toString()
            UserMedicalPrescripts = binding.medicalPrescripts.text.toString()
            UserMedicalProviderName = binding.medicalProviderName.text.toString()
            UserMedicalProviderPhone = binding.medicalProviderPhone.text.toString()
//
//            if (UserPhone.isNotEmpty()) {
//                db.collection("users")
//                    .document(UserID)
//                    .update("UserPhone", UserPhone)
//            }

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

