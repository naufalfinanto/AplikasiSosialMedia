package com.naufal.aplikasimediasosial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    //deklarasi variabel untuk firebase dkk
    private var mAuth: FirebaseAuth? = null
    private var database = FirebaseDatabase.getInstance()
    private var myref = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Get inxtance
        mAuth = FirebaseAuth.getInstance()

        //Onclick untuk daftar
        tvdaftar.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    //Event onclick login
    fun btnLoginPage(view: View){
        LoginToFirebase(
            etEmail.text.toString(),
            etEmail.text.toString()
        )
    }

    override fun onStart() {
        super.onStart()
        LoadPost()
    }

    //LoadPost berdasarkan email dan uid(unik ID)
    fun LoadPost() {
        val currentUser = mAuth!!.currentUser
        if(currentUser!=null) {
            //intent ke MainActivity.. Lalu pada MainActivity kita akan>>
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", currentUser.uid)
            startActivity(intent)
        }
    }

    //Login ke firebase
    fun LoginToFirebase(email:String, password:String){
        //firebase login dengan email dan password
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task->

            //Jika sukses
            if (task.isSuccessful){
                var currentUser = mAuth!!.currentUser
                Toast.makeText(applicationContext,"Sukses Login", Toast.LENGTH_LONG).show()
            //save data ke firebase berdasarkan input pada edittext
                myref.child("Users").child(currentUser!!.uid).child("email").setValue(currentUser.email)
                LoadPost()
            }
            //Jika gagal
            else{
                Toast.makeText(applicationContext,"Gagal Login", Toast.LENGTH_LONG).show()
            }
        }
    }

}