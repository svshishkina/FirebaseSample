package ru.surf.firebasesample

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


class App : Application() {

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    override fun onCreate() {
        super.onCreate()
        initRemoteConfig()
        fetchTheme()

        FirebaseMessaging.getInstance().subscribeToTopic("chat_notifications");
    }

    private fun initRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        firebaseRemoteConfig.setConfigSettings(configSettings)
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)
    }

    private fun fetchTheme() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        var cacheExpiration: Long = 1000
        if (firebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }

        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activateFetched()
                    } else {
                        Log.e("RemoteConfig", task.exception?.message)
                    }
                }
    }
}