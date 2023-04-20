package com.cs523.android.means_v2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "RegistrationActivity"


class registration : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var UserName: String
    private lateinit var UserEmail: String
    private lateinit var UserPassword: String
    private lateinit var UserPhone: String
    private lateinit var UserErContactPhone: String
    private lateinit var UserErContactName: String
    private lateinit var UserMedicalConditions: String
    private lateinit var UserMedicalPrescripts: String
    private lateinit var UserMedicalProviderName: String
    private lateinit var UserMedicalProviderPhone: String
    private lateinit var userID: String
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // INSTANTIATE THE FIREBASE AUTHENTICATION SERVICE
        auth = FirebaseAuth.getInstance()

        // INSTANTIATE THE FIREBASE CLOUD FIRESTORE DATABASE SERVICE
        val fStore = Firebase.firestore


        binding.registrationButton.setOnClickListener{

            UserName = binding.name.text.toString()
            UserEmail = binding.email.text.toString()
            UserPassword = binding.password.text.toString()
            UserPhone = binding.userPhone.text.toString()
            UserErContactPhone = binding.emergencyContactPhone.text.toString()
            UserErContactName = binding.emergencyContactName.text.toString()
            UserMedicalConditions = binding.medicalConditions.text.toString()
            UserMedicalPrescripts = binding.medicalPrescripts.text.toString()
            UserMedicalProviderName = binding.medicalProviderName.text.toString()
            UserMedicalProviderPhone = binding.medicalProviderPhone.text.toString()


            if(UserEmail.isNotEmpty() && UserPassword.isNotEmpty()){
                auth.createUserWithEmailAndPassword(UserEmail, UserPassword).addOnCompleteListener{
                    if(it.isSuccessful){


                        // GET THE NEWLY CREATED USERS UID FROM THE AUTHENTICATION SERVICE
                        userID = auth.currentUser?.uid.toString()

                        // CREATE A NEW FIRESTORE DOCUMENT USING THE UID FROM THE AUTHENTICATION SERVICE
                        val documentReference: DocumentReference = fStore.collection("users").document(userID)

                        // CREATE A HASHMAP FOR STORING THE USER SETTINGS DATA
                        var user : HashMap<String, String> = HashMap<String, String>()

                        // STORE THE DATA IN THE HASHMAP
                        user["UserEmail"] = UserEmail
                        user["UserName"] = UserName
                        user["UserPassword"] = UserPassword
                        user["UserPhone"] = UserPhone
                        user["UserErContactPhone"] = UserErContactPhone
                        user["UserErContactName"] = UserErContactName
                        user["UserMedicalConditions"] = UserMedicalConditions
                        user["UserMedicalPrescripts"] = UserMedicalPrescripts
                        user["UserMedicalProviderName"] = UserMedicalProviderName
                        user["UserMedicalProviderPhone"] = UserMedicalProviderPhone


                        // STORE THE  DATA IN THE FIREBASE CLOUD FIRESTORE
                        documentReference.set(user).addOnSuccessListener{
                            Log.d(TAG, "onSuccess: user Profile is created for $userID")
                        }

                        documentReference.set(user).addOnFailureListener{
                            Log.d(TAG, "onFailure: user Profile is created for $userID")
                        }



                        val intent = Intent(this, login::class.java)




                        startActivity(intent)
                    }else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Empty field", Toast.LENGTH_SHORT).show()
            }
        }
    }
}