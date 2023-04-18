package com.cs523.android.means_v2


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs523.android.means_v2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.toRegister.setOnClickListener {
            val intent = Intent(this, registration::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener{
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty()){
                auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if(it.isSuccessful){
                        /*val intentMain = Intent(this, MainActivity::class.java)
                        startActivity(intentMain)
                         */
                        val intentMaps = Intent(this, MapsActivity::class.java)
                        startActivity(intentMaps)
                    }else{
                        Toast.makeText(this, it.exception.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
}