package ie.wit.showcase2.firebase

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import ie.wit.showcase2.utils.customTransformation
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*

object FirebaseImageManager {

    var storage = FirebaseStorage.getInstance().reference
    var imageUri = MutableLiveData<Uri>()
    var imageUriPortfolio = MutableLiveData<Uri>()
    var imageUriProject = MutableLiveData<Uri>()
    var imageUriProject2 = MutableLiveData<Uri>()
    var imageUriProject3 = MutableLiveData<Uri>()

    // Function for generating random ID numbers
    internal fun generateRandomId(): Long {
        return Random().nextLong()
    }

    //function to check if profile pic already exists in storage
    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("photos").child("${userid}.jpg")
        val defaultImageRef = storage.child("homer.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            imageUri.value = Uri.EMPTY
        }
    }

    //function to upload an image to Firebase storage
    fun uploadImageToFirebase(userid: String, bitmap: Bitmap, updating : Boolean, path: String) {
        // Get the data from an ImageView as bytes
        val imageRef = storage.child("photos").child("${userid}.jpg")
        //val bitmap = (imageView as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            //if image exist it needs to be updated in database to account for change in reference
            if(updating) // Update existing Image
            {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUri.value = task.result!!
                        FirebaseDBManager.updateImageRef(userid,imageUri.value.toString(),path)
                    }
                }
            }
        }.addOnFailureListener { //File Doesn't Exist
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUri.value = task.result!!
                }
            }
        }
    }

    //function for updating user profile pic
    fun updateUserImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        //prepare image
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,updating,"profilePic")
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    //function to provide default image for profile if non-initially available
    fun updateDefaultImage(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,false, "profilePic")
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    //function to upload portfolio image to Firebase cloud
    fun uploadPortfolioImageToFirebase(userid: String, fileName: String, bitmap: Bitmap, updating : Boolean, path: String) {
        // Get the data from an ImageView as bytes
        //image ref based on name of image file
        val imageRef = storage.child("photos").child("${fileName}.jpg")

        //check if image already exists
        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUriPortfolio.value = task.result!!
                var imageUriPortfolioValue = imageUriPortfolio.value.toString()
                println("this is existing imageUriValue $imageUriPortfolioValue")
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            //val bitmap = (imageView as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            lateinit var uploadTask: UploadTask

            // Get the data from an ImageView as bytes
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUriPortfolio.value = task.result!!
                    var imageUriPortfolioValue = imageUriPortfolio.value.toString()
                    println("this is new imageUriValue $imageUriPortfolioValue")
                }
            }
        }
    }

    //function to prepare updated portfolio image
    fun updatePortfolioImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        println("this is imageUri $imageUri")
        var fileName = imageUri?.lastPathSegment
        println("this is fileName $fileName")

        Picasso.get().load(imageUri)
            .resize(450, 420)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadPortfolioImageToFirebase(userid, fileName!!, bitmap!!,updating, "image")
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    //function to upload project images to Firebase cloud
    fun uploadProjectImageToFirebase(userid: String, fileName: String, bitmap: Bitmap, updating : Boolean, imageName: String) {
        // Get the data from an ImageView as bytes
        //image named after file name
        val imageRef = storage.child("photos").child("${fileName}.jpg")
        println("this is imageRef $imageRef")

        imageRef.metadata.addOnSuccessListener { //File Exists
            //selection to assign new image to specific image within project (1, 2, or 3)
            if (imageName == "projectImage") {
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    imageUriProject.value = task.result!!
                    var imageUriProjectValue = imageUriProject.value.toString()
                    println("this is existing imageUriValue $imageUriProjectValue")
                }
            }
            if (imageName == "projectImage2") {
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    imageUriProject2.value = task.result!!
                    var imageUriProjectValue2 = imageUriProject2.value.toString()
                    println("this is existing imageUriValue2 $imageUriProjectValue2")
                }
            }
            if (imageName == "projectImage3") {
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    imageUriProject3.value = task.result!!
                    var imageUriProjectValue3 = imageUriProject3.value.toString()
                    println("this is existing imageUriValue3 $imageUriProjectValue3")
                }
            }

            //File Doesn't Exist
        }.addOnFailureListener {
            //val bitmap = (imageView as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            lateinit var uploadTask: UploadTask

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            uploadTask = imageRef.putBytes(data)
            if (imageName == "projectImage") {
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUriProject.value = task.result!!
                    }
                }
            }
            if (imageName == "projectImage2") {
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUriProject2.value = task.result!!
                    }
                }
            }
            if (imageName == "projectImage3") {
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUriProject3.value = task.result!!
                    }
                }
            }
        }
    }

    //function to prepare updated project images
    fun updateProjectImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean, imageName: String) {
        println("this is initial passed imageUriProject $imageUri")
        var fileName = imageUri?.lastPathSegment
        println("this is fileName $fileName")

        Picasso.get().load(imageUri)
            .resize(450, 420)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadProjectImageToFirebase(userid, fileName!!, bitmap!!,updating, imageName)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }
}
