package ru.surf.firebasesample

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig


abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isTaskRoot) {
            setTheme()
        }
        super.onCreate(savedInstanceState)
    }

    private fun setTheme() {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val theme = firebaseRemoteConfig.getString("theme")
        val themeId= when (theme) {
            "red" -> R.style.AppTheme_Red
            "purple" -> R.style.AppTheme_Purple
            else -> R.style.AppTheme
        }
        setTheme(themeId)
    }
}