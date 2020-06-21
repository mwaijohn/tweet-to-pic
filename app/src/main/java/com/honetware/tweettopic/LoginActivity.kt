package com.honetware.tweettopic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.Toast
import com.honetware.tweettopic.authentication.KotlinLoginProcess
import com.honetware.tweettopic.authentication.WebActivity
import com.honetware.tweettopic.authentication.WebActivity.Companion.EXTRA_URL
import com.honetware.tweettopic.utilities.SharedPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import twitter4j.Twitter
import twitter4j.auth.RequestToken

class LoginActivity : AppCompatActivity() {
    private var requestCode: Int = 100
    lateinit var uiScope: CoroutineScope
    companion object{
        lateinit var requestToken: RequestToken
        lateinit  var twitterGlobal : Twitter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val job = Job()
        uiScope = CoroutineScope(Dispatchers.Main + job)
        SharedPrefs.init(this)
        val isLoggedIn = SharedPrefs.read(SharedPrefs.IS_LOGGEDIN,false)
        Handler().postDelayed({
            if(isLoggedIn){
                startActivity(Intent(this,MainActivity::class.java))
            }else{
                val loginBtn: Button = findViewById(R.id.login)
                loginBtn.setOnClickListener {
                    uiScope.launch {
                        val url = KotlinLoginProcess.getCallBackUrl(this@LoginActivity)

                        if (url != null){
                            val myIntent = Intent(applicationContext, WebActivity::class.java)
                            myIntent.putExtra(EXTRA_URL, url)
                            startActivityForResult(myIntent, requestCode)
                        }else{
                            Toast.makeText(this@LoginActivity,getString(R.string.unable_to_login), Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }
            }

        },1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val verifier = data!!.extras!!.getString(getString(R.string.twitter_oauth_verifier))
        //LoginStore.getAccessToken(this,verifier)
        uiScope.launch {
            KotlinLoginProcess.getAccessToken(this@LoginActivity,verifier)
        }
        finish()
    }
}
