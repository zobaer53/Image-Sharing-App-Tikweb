package com.zobaer53.imagesharingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zobaer53.imagesharingapp.model.MyModel
import java.security.AccessController.getContext
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1
    private var progressDialog: ProgressDialog? = null
    var ImageList = ArrayList<Uri>()
    private var upload_count = 0
    var urlStrings: ArrayList<String>? = null
    lateinit var imageListFromFirebase: ArrayList<String>
    var mFirebaseDatabase: FirebaseDatabase? = null
    var mDatabaseReference: DatabaseReference? = null
    lateinit var  recyclerview:RecyclerView
    private lateinit var  addPhotoButton:MaterialButton
    var clearPhoto:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mFirebaseDatabase!!.reference

        addPhotoButton = findViewById(R.id.addPhotoButton)
        addPhotoButton.text = "Add Photos"
        addPhotoButton.visibility = View.VISIBLE
        // getting the recyclerview by its id
         recyclerview = findViewById<RecyclerView>(R.id.recyclerView)

        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog!!.setMessage("Uploading Images please Wait.........!!!!!!")


        //add photo part
        addPhotoButton.setOnClickListener {
                Dexter.withContext(applicationContext)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = "image/*" //allows any image file type
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            startActivityForResult(
                                Intent.createChooser(intent, "Select Picture"),
                                PICK_IMAGE
                            )
                        }

                        override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                        override fun onPermissionRationaleShouldBeShown(
                            permissionRequest: PermissionRequest,
                            permissionToken: PermissionToken
                        ) {
                            permissionToken.continuePermissionRequest()
                        }
                    }).check()
            }
            //loadImageFromFirebase()

        }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data!!.clipData != null) {
                    val countClipData = data.clipData!!.itemCount
                    var currentImageSelect = 0
                    while (currentImageSelect < countClipData) {
                        val imageUri = data.clipData!!.getItemAt(currentImageSelect).uri
                        ImageList.add(imageUri)
                        currentImageSelect = currentImageSelect + 1
                    }
                    Log.i("DEMO", "onActivityResult imageList size" + ImageList.size)
                    uploadToFirebase()
                } else {
                    Toast.makeText(this, "Please Select Multiple Images", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadToFirebase() {
        urlStrings = ArrayList()
        progressDialog!!.show()
        val ImageFolder = FirebaseStorage.getInstance().reference.child("ImageFolder")
       // Log.i("DEMO","upload to firebase imageList size"+ImageList.size());
        upload_count = 0
        while (upload_count < ImageList.size) {
            val IndividualImage = ImageList[upload_count]
            //Log.i("DEMO","upload to firebase individual imageList "+IndividualImage);
            val ImageName = ImageFolder.child("Images" + IndividualImage.lastPathSegment)
            ImageName.putFile(IndividualImage).addOnSuccessListener {
                  Log.i("DEMO","upload to firebase on success");
                ImageName.downloadUrl.addOnSuccessListener { uri ->
                    urlStrings!!.add(uri.toString())
                       Log.i("DEMO","uploadToFirebase url strings= "+ urlStrings);
                      Log.i("DEMO"," url string sizes = "+ urlStrings!!.size);
                    if (urlStrings!!.size == ImageList.size) {
                          Log.i("DEMO","Normal url strings= "+ urlStrings);
                        storeLink(urlStrings!!)
                    }
                }
            }
            upload_count++
        }
    }

    private fun storeLink(urlStrings: java.util.ArrayList<String>) {
        Log.i("DEMO", "store link called = $urlStrings")
        val hashMap = HashMap<String, String>()
        for (i in urlStrings.indices) {
            hashMap["ImgLink$i"] = urlStrings[i]
            Log.i("DEMO", "Store link hashmap = $hashMap")
        }
        mDatabaseReference!!.child("propertyFromDevice")

            .setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadImageFromFirebase()
                    Toast.makeText(
                        this@MainActivity,
                        "Successfully Uploaded",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    val hashMap1 = HashMap<String, Any>()
                    hashMap1["apartmentImage"] = urlStrings[0]

                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@MainActivity,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        progressDialog!!.dismiss()
        ImageList.clear()
    }

    private fun loadImageFromFirebase() {

        imageListFromFirebase = ArrayList()
        mDatabaseReference!!.child("propertyFromDevice")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children)
                        if (dataSnapshot.exists()) {
                        val item = snapshot.value.toString()
                        if (item != null) {
                            imageListFromFirebase.add(item)
                        }
                        Log.i("Tag", " Url1 = $imageListFromFirebase")
                    }
                    // this creates a vertical layout Manager
                    recyclerview.hasFixedSize()
                    recyclerview.layoutManager = GridLayoutManager(applicationContext,3)
                    val adapter = com.zobaer53.imagesharingapp.adapter.MyAdapter(applicationContext,imageListFromFirebase)
                    recyclerview.adapter = adapter
                    adapter.notifyDataSetChanged()
                    addPhotoButton.visibility =View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })


    }

    override fun onBackPressed() {
        this.finish()
        exitProcess(0)
    }
}