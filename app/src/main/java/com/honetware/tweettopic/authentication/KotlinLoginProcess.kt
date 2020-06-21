package com.honetware.tweettopic.authentication

import android.content.Context
import android.content.Intent
import com.honetware.tweettopic.LoginActivity
import com.honetware.tweettopic.MainActivity
import com.honetware.tweettopic.utilities.SharedPrefs
import com.honetware.tweettopic.utilities.SharedPrefs.Companion.write
import com.honetware.tweettopic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder

class KotlinLoginProcess {
    companion object{
        //get callback url
        suspend fun getCallBackUrl(context: Context): String?{

            val builder = ConfigurationBuilder()

            builder.setOAuthAuthenticationURL("https://api.twitter.com/oauth/request_token")
            builder.setOAuthAccessTokenURL("https://api.twitter.com/oauth/access_token")
            builder.setOAuthAuthorizationURL("https://api.twitter.com/oauth/authorize")
            builder.setOAuthRequestTokenURL("https://api.twitter.com/oauth/request_token")
            builder.setRestBaseURL("https://api.twitter.com/1.1/")

            builder.setOAuthConsumerKey(context.getString(R.string.twitter_consumer_key))
            builder.setOAuthConsumerSecret(context.getString(R.string.twitter_consumer_secret))

            builder.setHttpConnectionTimeout(100000)
            val configuration = builder.build()

            val factory = TwitterFactory(configuration)

            val twitter = withContext(Dispatchers.IO){factory.instance}

            val requestToken: RequestToken?

            return try {
                requestToken =
                    withContext(Dispatchers.IO){twitter.getOAuthRequestToken(context.getString(R.string.twitter_callback))}
                LoginActivity.requestToken = requestToken
                requestToken.authenticationURL

            } catch (e: TwitterException) {
                null
            }

        }

        suspend fun getAccessToken(context: Context, verifier: String?){

            val builder = ConfigurationBuilder()
            builder.setOAuthAuthenticationURL("https://api.twitter.com/oauth/request_token")
            builder.setOAuthAccessTokenURL("https://api.twitter.com/oauth/access_token")
            builder.setOAuthAuthorizationURL("https://api.twitter.com/oauth/authorize")
            builder.setOAuthRequestTokenURL("https://api.twitter.com/oauth/request_token")
            builder.setRestBaseURL("https://api.twitter.com/1.1/")

            builder.setOAuthConsumerKey(context.getString(R.string.twitter_consumer_key))
            builder.setOAuthConsumerSecret(context.getString(R.string.twitter_consumer_secret))

            // builder.setHttpConnectionTimeout(100000)

            val configuration = withContext(Dispatchers.IO){builder.build()}

            val factory = withContext(Dispatchers.IO){TwitterFactory(configuration)}

            val twitter = withContext(Dispatchers.IO){factory.instance}
            LoginActivity.twitterGlobal = twitter

            val requestToken = LoginActivity.requestToken
            val accessToken = withContext(Dispatchers.IO){twitter.getOAuthAccessToken(requestToken, verifier)}

            write(SharedPrefs.PREF_KEY_OAUTH_SECRET, accessToken.tokenSecret)
            write(SharedPrefs.PREF_KEY_OAUTH_TOKEN, accessToken.token)

            val userID = withContext(Dispatchers.IO){accessToken.userId}

            val user =  withContext(Dispatchers.IO){twitter.showUser(userID)}
            val screenName = user.screenName
            val name = user.name
            val profile = user.biggerProfileImageURL

            write(SharedPrefs.PREF_NAME, name)
            write(SharedPrefs.PREF_USERNAME, screenName)
            write(SharedPrefs.PROFILE_URL, profile)
            write(SharedPrefs.PREF_USER_ID, userID)
            write(SharedPrefs.PREF_KEY_OAUTH_SECRET, accessToken.tokenSecret)
            write(SharedPrefs.PREF_KEY_OAUTH_TOKEN, accessToken.token)
            write(SharedPrefs.IS_LOGGEDIN,true)

            context.startActivity(Intent(context, MainActivity::class.java))
        }

        //get twitter object
        fun getTwitterObject(context: Context,token: String,tokenSecret: String): Twitter?{
            val builder = ConfigurationBuilder()

            builder.setOAuthConsumerKey(context.getString(R.string.twitter_consumer_key))
            builder.setOAuthConsumerSecret(context.getString(R.string.twitter_consumer_secret))

            var twitter: Twitter? = null
            try {
                val accessToken = AccessToken(token, tokenSecret)
                twitter = TwitterFactory(builder.build()).getInstance(accessToken)
                return twitter
            } catch (ex: IllegalArgumentException) {
            }
            return twitter
        }

        @Throws(TwitterException::class)
        fun logout(twitter: Twitter, context: Context?) {
            twitter.invalidateOAuth2Token()
        }
    }
}