package com.honetware.tweettopic.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.honetware.tweettopic.R
import com.honetware.tweettopic.model.ImageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.ln
import kotlin.math.pow


class AppUtils{
    companion object{
        fun getStatus(twitter: Twitter, tweetId: Long): Status{
            return twitter.showStatus(tweetId)
        }

        suspend fun getStatusContent(status: Status): ImageContent{
            //get status owner
            val user = withContext(Dispatchers.IO){status.user}
            val profileUrl = user.profileImageURL
            val name = user.name
            val userName = user.screenName
            val statusText = status.text
            val date = status.createdAt
            val favCount = status.favoriteCount
            val retweetCount = status.retweetCount

            //get if has media entity
            val mediaEntity = status.mediaEntities

            return ImageContent(profileUrl,name,userName,statusText,mediaEntity,favCount,retweetCount,date)
        }

        fun getTwitterObject(context: Context,token: String?,tokenSecret: String?): Twitter{
            val builder = ConfigurationBuilder()

            builder.setOAuthConsumerKey(context.getString(R.string.twitter_consumer_key))
            builder.setOAuthConsumerSecret(context.getString(R.string.twitter_consumer_secret))
            var accessToken: AccessToken? = null
            try {
                accessToken = AccessToken(token, tokenSecret)

            }catch (ex: Exception){

            }
            return TwitterFactory(builder.build()).getInstance(accessToken)!!
        }

        fun getTwitterObject(context: Context): Twitter{
            val builder = ConfigurationBuilder()

            builder.setOAuthAuthenticationURL("https://api.twitter.com/oauth/request_token")
            builder.setOAuthAccessTokenURL("https://api.twitter.com/oauth/access_token")
            builder.setOAuthAuthorizationURL("https://api.twitter.com/oauth/authorize")
            builder.setOAuthRequestTokenURL("https://api.twitter.com/oauth/request_token")
            builder.setRestBaseURL("https://api.twitter.com/1.1/")

            builder.setOAuthConsumerKey("zmjGjIyma0vzbeF2XIpnhlO78")
            builder.setOAuthConsumerSecret("8qpa199vuiHINtQi54PYvFtp8qvCB8AUNMAbgGiatf9MOz873V")

            builder.setHttpConnectionTimeout(100000)
            val configuration = builder.build()

            val factory = TwitterFactory(configuration)

            return factory.instance
        }

        fun toastMassage(context: Context, massage: String) {
            Toast.makeText(context, massage, Toast.LENGTH_SHORT).show()
        }

        fun logMassage(tag: String,massage: String){
            Log.d(tag,massage)
        }

        fun requestStoragePermission(view: View, activity: Activity){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Enable write permission to external storage",Snackbar.LENGTH_INDEFINITE)
                    .setAction("enable"
                    ) {
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            102)
                    }.show()
            }else{
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    102)
            }
        }

        fun convertToSuffix(count: Long): String? {
            if (count < 1000) return "" + count
            val exp =
                (ln(count.toDouble()) / ln(1000.0)).toInt()
            return String.format(
                "%.1f%c",
                count / 1000.0.pow(exp.toDouble()),
                "kmgtpe"[exp - 1]
            )
        }

        fun saveBitMap(context: Context, drawView: View): File? {
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
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, oStream)
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
        private fun scanGallery(context: Context, path: String) {
            try {
                MediaScannerConnection.scanFile(context, arrayOf(path), null) { path, uri -> }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}