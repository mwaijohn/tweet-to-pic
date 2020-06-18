package com.honetware.tweettopic.utilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class SharedPrefs {

    companion object{

        var mSharedPref: SharedPreferences? = null
        const val PREF_KEY_OAUTH_TOKEN = "oauth_token"
        const val PREF_KEY_OAUTH_SECRET = "oauth_token_secret"
        const val PREF_USER_ID = "twitter_user_id"
        const val IS_LOGGEDIN = "is_loggedin"
        const val PREF_NAME = "name"
        const val PREF_USERNAME = "username"
        const val PROFILE_URL = "profile"

        fun init(context: Context) {
            if (mSharedPref == null) mSharedPref = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
        }

        fun read(key: String?, defValue: String?): String? {
            return mSharedPref?.getString(key, defValue)
        }

        fun read(key: String?,defValue: Boolean): Boolean {
            return mSharedPref?.getBoolean(key, defValue)!!
        }

        fun read(key: String?,defValue: Long): Long {
            return mSharedPref?.getLong(key, defValue)!!
        }

        fun write(key: String?, value: String?) {
            val prefsEditor = mSharedPref?.edit()
            prefsEditor?.putString(key, value)
            prefsEditor?.apply()
        }

        fun write(key: String?, value: Boolean) {
            val prefsEditor = mSharedPref?.edit()
            prefsEditor?.putBoolean(key, value)
            prefsEditor?.apply()
        }

        fun write(key: String?, value: Long) {
            val prefsEditor = mSharedPref?.edit()
            prefsEditor?.putLong(key, value)
            prefsEditor?.apply()
        }
    }
}