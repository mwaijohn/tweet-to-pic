package com.honetware.tweettopic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.honetware.tweettopic.utilities.AppUtils
import com.honetware.tweettopic.utilities.AppUtils.Companion.requestStoragePermission
import com.honetware.tweettopic.utilities.AppUtils.Companion.toastMassage
import com.honetware.tweettopic.utilities.SharedPrefs
import com.honetware.tweettopic.utilities.SharedPrefs.Companion.read
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private var linearLayout: LinearLayout? = null
    private var saveBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linearLayout = findViewById(R.id.lyt)
        saveBtn = findViewById(R.id.button1)
        saveBtn?.setOnClickListener(this)


        //perform action using tweet
        val job = Job()
        val uiScope = CoroutineScope(Dispatchers.Main + job)

        val tokenSecret = read(SharedPrefs.PREF_KEY_OAUTH_SECRET, "no_token_secret")
        val token = read(SharedPrefs.PREF_KEY_OAUTH_TOKEN, "no token")

        uiScope.launch {
            val twitter = withContext(Dispatchers.IO){
                AppUtils.getTwitterObject(this@MainActivity,token,tokenSecret)
            }
            val status = withContext(Dispatchers.IO){
                try {
                    return@withContext  AppUtils.getStatus(twitter,1274564386275782656L)
                }catch (ex: Exception){
                    return@withContext null
                }
            }

            if (status != null){
                val statusContent = AppUtils.getStatusContent(status)
                toastMassage(this@MainActivity,statusContent.userName)
            }else{
                toastMassage(this@MainActivity,getString(R.string.unable_to_get_status))
            }
        }

    }

    override fun onClick(v: View?) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            saveBitMap(this,linearLayout as View)
        }else{
            requestStoragePermission(saveBtn as View,this)
        }
    }

    private fun saveBitMap(context: Context, drawView: View): File? {
        val pictureFileDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TweetToPic")
        if (!pictureFileDir.exists()) {
            val isDirectoryCreated: Boolean = pictureFileDir.mkdirs()
            if (!isDirectoryCreated) Log.i("ATG", "Can't create directory to save the image")
            return null
        }
        val filename: String = pictureFileDir.path + File.separator + System.currentTimeMillis() + ".jpg"
        val pictureFile = File(filename)
        val bitmap: Bitmap? = getBitmapFromView(drawView)
        try {
            pictureFile.createNewFile()
            val oStream = FileOutputStream(pictureFile)
            bitmap?.compress(CompressFormat.PNG, 100, oStream)
            oStream.flush()
            oStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("TAG", "There was an issue saving the image.")
        }
        scanGallery(context, pictureFile.absolutePath)
        return pictureFile
    }

    //create bitmap from view and returns it
    private fun getBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    // used for scanning gallery
    private fun scanGallery(cntx: Context, path: String) {
        try {
            MediaScannerConnection.scanFile(cntx, arrayOf(path), null) { path, uri -> }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode  == 102){
            if (grantResults.size ==1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //val imageName = project.name.replace(" ","")+System.currentTimeMillis().toString()
                //Utilities.saveToGallery(this,pieChart,imageName)
            }else{
                //Toast.makeText(this,"Saving to gallery failed",Toast.LENGTH_LONG).show()
            }
        }
    }
}
