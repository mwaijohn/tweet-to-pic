package com.honetware.tweettopic.authentication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

import com.honetware.tweettopic.LoginActivity
import com.honetware.tweettopic.R
import com.honetware.tweettopic.utilities.AppUtils.Companion.logMassage
import com.honetware.tweettopic.utilities.AppUtils.Companion.toastMassage
import im.delight.android.webview.AdvancedWebView

class WebActivity : AppCompatActivity(),AdvancedWebView.Listener {
    private lateinit var mWebView: AdvancedWebView
    private lateinit var progressBar: ProgressBar
    companion object{ var EXTRA_URL = "extra_url"
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        progressBar = findViewById(R.id.progress_bar)

        mWebView = findViewById(R.id.advanced_webview)
        mWebView.setListener(this, this)
        mWebView.webViewClient = MyWebViewClient()

        val url = this.intent.getStringExtra(EXTRA_URL)

        if (null == url) {
            Log.e("Twitter", "URL cannot be null")
            finish()
        }

        mWebView.loadUrl(url)

    }

    override fun onPageFinished(url: String?) {
        logMassage("load","load finished")
        progressBar.visibility = View.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
        toastMassage(this,description +"on page error")
        progressBar.visibility = View.GONE
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
        logMassage("load","download requested")
    }

    override fun onExternalPageRequest(url: String?) {
        logMassage("load",url +"")
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        logMassage("load", "page started $url")
    }


    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.contains(resources.getString(R.string.twitter_callback))) {
                val uri = Uri.parse(url)

                /* Sending results back */
                val verifier = uri.getQueryParameter(getString(R.string.twitter_oauth_verifier))
                val resultIntent = Intent(this@WebActivity, LoginActivity::class.java)
                resultIntent.putExtra(getString(R.string.twitter_oauth_verifier), verifier)
                setResult(Activity.RESULT_OK, resultIntent)

                finish()
                return true
            }
            return false
        }
    }

}
