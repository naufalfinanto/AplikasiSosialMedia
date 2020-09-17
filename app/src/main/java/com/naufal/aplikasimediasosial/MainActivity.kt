package com.naufal.aplikasimediasosial

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.common.internal.Objects
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_post.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_list.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference
    private var mAuth: FirebaseAuth? = null
    private var firebaseStorage: FirebaseStorage? = null

    //Deklarasi Variabel list post
    var ListPost = ArrayList<DataPostingan>()

    //variabel inner class adapter
    var adapter: MyPostAdapter? = null

    //Deklarasi info user
    var myemail: String? = null
    var UserUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        var b: Bundle = intent.extras

        //myemail = email pada firebase database
        myemail = b.getString("email")

        //ID User
        UserUID = b.getString("uid")

        //Tambahkan postingan baru berdasarkan class data postingan
        ListPost.add(DataPostingan("0", "him", "url", "add"))

        //Set adapter
        adapter = MyPostAdapter(this, ListPost)
        lvTweets.adapter = adapter

        //Load post yang sudah ada
        LoadPost()
    }

    //Inner clas Adapter
    inner class MyPostAdapter : BaseAdapter{
        var listNotesAdapter = ArrayList<DataPostingan>()
        var context: Context

        constructor(context: Context, listNotesAdapter: ArrayList<DataPostingan>):super(){
            this.listNotesAdapter = listNotesAdapter
            this.context = context
        }

        //set tampilan main activity
        override fun getView(p0: Int, p1: View?, p2: ViewGroup?) {
            var mypost = listNotesAdapter[p0]
            if (mypost.postPersonUID.equals("add")){
                //Code untuk tambahkan postingan
                var myView = layoutInflater.inflate(R.layout.activity_add_post, null)

                //Button untuk pilih gambar
                myView.iv_gambar.setOnClickListener{
                    //Load gambar yang akan dipost
                    loadImage()
                }

                //Button untuk upload gambar
                myView.iv_post.setOnClickListener{
                    //Upload server
                    myRef.child("posts").push().setValue(
                        InfoPostingan(
                            UserUID!!,
                            myView.etPost.text.toString(),
                            DownloadURL!!
                        )
                    )
                    myView.etPost.setText("")
                }
                return myView
            }

            //Tampilkan loading ketika upload gambar
            else if(mypost.postPersonUID.equals("loading"))
            {
                val myView = layoutInflater.inflate(R.layout.loading_ticket, null)
                return myView
            }

            //Tampilkan Welcome to....
            else if (mypost.postPersonUID.equals("ada"))
            {
                val myView = layoutInflater.inflate(R.layout.layout_welcome, null)
                return myView
            }

            //Tampilkan postingan
            else
            {
                //Layout post (item list)
                val myView =layoutInflater.inflate(R.layout.item_list, null)

                //isi dari postingan
                myView.txt_detail_postingan.text = mypost.postText

                //Tampilkan gambar
                Glide.with(context).load(mypost.postImageURL)

                //Placeholder untuk tampilan ketika gambar masih loading
                    .placeholder(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(myView.gambar_postingan)

                //Tampilkan username dan foto user
                myRef.child("Users").child(mypost.postPersonUID!!)
                    .addValueEventListener(object :
                    ValueEventListener{
                    override fun onDataChange(
                        dataSnapshot:
                        DataSnapshot
                    ) {
                        try {
                            //Foto user
                            var td = dataSnapshot.value as HashMap<String, Any>
                            for (key in td.keys)
                            {
                                var userInfo = td[key] as kotlin.String
                                if (key == "ProfileImage"){
                                    Glide.with(context)
                                        .load(userInfo)
                                        .placeholder(R.mipmap.ic_launcher)
                                        .into(myView.poto_user)
                                }
                                //Username
                                else{
                                    myView.txtUsrName.text = SplitString(userInfo)
                                }
                            }
                        }catch (ex: Exception) {
                        }

        }

                        override fun onCancelled(p0: DatabaseError) {
                            p0:
                            DatabaseError
                        }

        //Tangkap jumlah item
        override fun getCount(): Int {
           return listNotesAdapter.size
        }

        //Tangkap data dari item
        override fun getItem(p0: Int): Any {
            return  listNotesAdapter[p0]
        }

        //Tangkap id utem
        override fun getItemId(p0: Int): Long {
            return  p0.toLong()
        }
    }

    //Load gambar
    val PICK_IMAGE_CODE = 123

    fun loadImage()
       {
        //Intent ke galeri atau aplikasi foto lainnya
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
       }
     startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    //Result
    override fun onActivityResult(
    requestCode: Int, resultCode: Int, data: Intent?
    ){
       super.onActivityResult(requestCode, resultCode, data)
       if (requestCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK
       ){
           val selectedImage = data.data
           val filePathColum = arrayOf(MediaStore.Images.Media.DATA)
           val cursor = contentResolver.query(selectedImage, filePathColum, null, null, null)
           cursor.moveToFirst()
           val coulumindex = cursor.getColumnIndex(filePathColum[0])
           val picturePath = cursor.getString(coulumindex)
           cursor.close()

           //upload gambar dengan format bitmap
           UploadImage(BitmapFactory.decodeFile(picturePath))
       }
    }


    //Download URL Gambar
    var DownloadURL: String? = null

    //Upload Gambar
    fun UploadImage(bitmap: Bitmap){
        ListPost.add(0, DataPostingan("0", "him", "url", "loading"))
        adapter!!.notifyDataSetChanged()

        //Upload ke firebase
        val storage = FirebaseStorage.getInstance()

        //Link firebase storage
        val storageRef = storage.getReferenceFromUrl("gs://aplikasi-sosial-media.appspot.com")

        //Save nama gambar berdasarkan waktu upload (Hari, Bulan, Tahun, Jam dan Detik)
        val formattanggal = SimpleDateFormat("ddMMyyHHmmss")
        val dataobject = Date()

        //Save sebagai jpg
        var imagePath = SplitString(myemail!!) + "." + formattanggal.format(dataobject) + ".jpg"

        //Save difolder image post
        var imageRef = storageRef.child("imagePost/" + imagePath)

        //Re-format gambar menjadi bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val datar = baos.toByteArray()
        val uploadTask = imageRef.putBytes(datar)

        //Upload gambar
        uploadTask.addOnSuccessListener { taskSnapshot ->
            var tokens = FirebaseStorage.getInstance().equals("downloadTokens")
            DownloadURL = "https:firebasestorage.googleapis.com/o/imagePost24f" + SplitString(myemail!!) + "." +
                    formattanggal.format(dataobject) + ".jpg" + "?alt=media&token" + tokens.toString()

            //Tampilkan Post
            ListPost.removeAt(0)
            adapter!!.notifyDataSetChanged()

            //JIKA GAGAL
        }.addOnFailureListener{Toast.makeText(applicationContext, "Failed to upload", Toast.LENGTH_LONG).show()}
    }
  }

  //Function untuk menghapus @gmail.com
  fun SplitString(email: String): String{
      val split = email.split("@")
      return split[0]
  }

  //Load Postingan
  fun LoadPost(){
      myRef.child("posts")
          .addValueEventListener(object : ValueEventListener{
              override fun onDataChange(p0: DataSnapshot) {
                  try {
                      ListPost.clear()
                      ListPost.add(DataPostingan("0", "him", "url", "add"))
                      ListPost.add(DataPostingan("0", "him", "url", "ada"))
                      var td = dataSnapshot!!.value as HashMap<String, Any>
                      for(key in td.keys){
                          var post = td[key] as HashMap<String, Any>
                          ListPost.add(
                              DataPostingan(
                                  key
                                  , post["text"] as String
                                  , post["postImage"] as String
                                  , post["userUID"] as String
                              )
                          )
                      }

                      adapter!!.notifyDataSetChanged()
                  }catch (ex: Exception)
                       {

                       }
              }

              override fun onCancelled(p0: DatabaseError) {
              }
        } )
    }

      //Tampilkan menu item
      override fun onCreateOptionsMenu(menu: Menu?): Boolean{
          menuInflater.inflate(R.menu.menu, menu)
          return super.onCreateOptionMenu(menu)
      }

      //Event on click pada menu item
      override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean{
        if (item != null){
            when (item.itemId){ R.id.logout -> {
                mAuth!!.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
      }
          return super.onOptionsItemSelected(item)
     }
    }}












