package com.honetware.tweettopic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.honetware.tweettopic.utilities.AppUtils
import com.honetware.tweettopic.utilities.AppUtils.Companion.requestStoragePermission
import com.honetware.tweettopic.utilities.AppUtils.Companion.saveBitMap
import com.honetware.tweettopic.utilities.AppUtils.Companion.toastMassage
import com.honetware.tweettopic.utilities.CircleTransform
import com.honetware.tweettopic.utilities.SharedPrefs
import com.honetware.tweettopic.utilities.SharedPrefs.Companion.read
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import kotlinx.android.synthetic.main.tweet_card.*
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

        //config twitter with API Key and Secret Key
        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret)))
        .debug(true)
            .build();

        //initialize twitter
        Twitter.initialize(config)

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
                username.text = statusContent.userName
                name.text = statusContent.name
                tweet_text.text = Html.fromHtml(statusContent.statusText)
                Picasso.get()
                    .load(statusContent.userProfile)
                    .error(R.drawable.ic_launcher_background)
                    .transform(CircleTransform())
                    .into(thumbnail)
                if (statusContent.imageEntities?.isNotEmpty()!!){
                    Picasso.get()
                        .load(statusContent.imageEntities!![0].mediaURL)
                        .resize(400,300)
                        .into(image_entity)
                }else{
                    image_entity.visibility = View.GONE
                }
                retweet_count.text = AppUtils.convertToSuffix(statusContent.retweetCount.toLong())
                fav_count.text = AppUtils.convertToSuffix(statusContent.favCount.toLong())
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode  == 102){
            if (grantResults.size ==1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveBitMap(this,linearLayout as View)
            }else{
                toastMassage(this,getString(R.string.saving_failed))
            }
        }
    }
}
