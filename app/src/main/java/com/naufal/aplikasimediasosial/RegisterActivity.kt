package com.naufal.aplikasimediasosial

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ivImageAkun.setOnClickListener{
            checkPermission()
        }
    }
    val READIMAGE: Int=253
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READIMAGE
                )
                return
            }
        }
        loadImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        when (requestCode){READIMAGE->{
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                loadImage()
            }else{
                Toast.makeText(applicationContext, "gambar tidak dapat diakses",Toast.LENGTH_LONG).show()
            }
          }
            else ->  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
          }
    }
    val PICK_IMAGE_CODE = 123
    //Load gambar
    fun loadImage()
       {
         val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
         startActivityForResult(intent, PICK_IMAGE_CODE)
       }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK)
        {
            //Set foto profil
            val selectedImage = data.data
            val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = selectedImage?.let { contentResolver.query(it, filePathColum, null, null) }
            cursor?.moveToFirst()
            val coulomIndex = cursor?.getColumnIndex(filePathColum[0])
            val picturePath = coulomIndex?.let { cursor.getString(it) }
            cursor?.close()
            ivImageAkun.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    //Simpan gambar ke firebase
    @SuppressLint("SimpleDateFormat")
    fun SaveImageInFirebase(){
        //Memberi nama gambar yg akan kita save ke firebase
        var currentUser = mAuth!!.currentUser
        val email: String = currentUser!!.email.toString()
        val storage = FirebaseStorage.getInstance()

        //link dari firebase storage
        val storageRef = storage.getReferenceFromUrl("gs://aplikasi-sosial-media.appspot.com")

        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()
        val imagePath = SplitString(email) + "." + df.format(dataobj)+ ".jpg"
        val ImageRef = storageRef.child("gambar/" + imagePath)
        ivImageAkun.isDrawingCacheEnabled = true
        ivImageAkun.buildDrawingCache()

        //Merubah format dari gambar yang akan kita save
        val ivDrawable = ivImageAkun.drawable as BitmapDrawable
        val bitmap = ivDrawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = ImageRef.putBytes(data)

        var addss = FirebaseStorage.getInstance().equals("downloadTokens")
        var DownloadURLs =
            "https:firebasestorage.googleapis.com/v0/b/" +
                    "fir-socialmedia-2a0e9.appspot.com/o/gambar42f" +
                    SplitString(currentUser.email.toString()) + "." + df.format(dataobj) +
                    ".jpg" + "?alt=media&token" + addss.toString()

        myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
        myRef.child("Users").child(currentUser.uid)
            //.child("ProfileImage").setSampler.Value(DownloadURLs)
        LoadPost()
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext, "Gagal upload image",Toast.LENGTH_LONG).show()
        }.addOnSuccessListener { task ->
            var addss = FirebaseStorage.getInstance().equals("downloadTokens")
            var DownloadURLs =
                "https: firebasestorage.googleapis.com/v0/b/" +
                        "fir-socialmedia-2a0e9.appspot.com/o/gambar42f" +
                        SplitString(currentUser.email.toString()) + "." + df.format(dataobj) +
                        ".jpg" + "?alt=media&token" + addss.toString()

            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid)
                //.child("ProfileImage").setSampler.Value(DownloadURLs)
            LoadPost()
        }

    }


    //Untuk rename edittext
    private fun SplitString(email: String): Any {
        val split = email.split("@")
        return split
    }

    override fun onStart() {
        super.onStart()
        LoadPost()
    }

    private fun LoadPost() {
        var currentUser = mAuth!!.currentUser
        if(currentUser!=null) {
            //intent ke MainActivity.. Lalu pada MainActivity kita akan>>
            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", currentUser.uid)
            startActivity(intent)
            finish()
        }
    }

    //Button login
    fun btnDaftar(view: View){
        //Jika email tidak diisi
        if (etEmailRegister.text.isEmpty()){
            Toast.makeText(applicationContext,"Email tidak boleh kosong!", Toast.LENGTH_LONG).show()
        }
        //Jika password kosong
        else if (etPasswordRegister.text.isEmpty()){
            Toast.makeText(applicationContext,"Password tidak boleh kosong!", Toast.LENGTH_LONG).show()
        }
        //Jika sudah benar
        else{
            LoginToFirebase(etEmailRegister.text.toString(),
            etEmailRegister.text.toString())
        }
    }

    fun LoginToFirebase(email:String, password:String){
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task->
            if (task.isSuccessful){
                Toast.makeText(applicationContext, "Sukses login", Toast.LENGTH_LONG).show()
                SaveImageInFirebase()
            }else{
                Toast.makeText(applicationContext, "Gagal login", Toast.LENGTH_LONG).show()
            }
        }
    }

}
