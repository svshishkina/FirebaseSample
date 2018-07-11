package ru.surf.firebasesample

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_profile.*
import ru.surf.firebasesample.ChatActivity.Companion.EXTRA_UID
import ru.surf.firebasesample.domain.User
import ru.surfstudio.android.imageloader.ImageLoader


class ProfileActivity : BaseActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
        private const val MALE = "male"
        private const val FEMALE = "female"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var userId = ""

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getStringExtra(EXTRA_UID)

        initViews()
        initFirebaseInstances()
        enableEditProfile(userId)
        loadUser()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        save_btn.setOnClickListener {
            editProfile()
        }
    }

    private fun initFirebaseInstances() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        dbReference = FirebaseDatabase.getInstance().reference
    }

    private fun loadUser() {
        progress.visibility = VISIBLE

        dbReference.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progress.visibility = GONE

                user = dataSnapshot.getValue(User::class.java)!!
                renderUserInfo()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progress.visibility = GONE
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException())
            }
        })
    }

    private fun enableEditProfile(userId: String) {
        if (userId == firebaseAuth.currentUser?.uid) {
            save_btn.visibility = VISIBLE
            name_et.isEnabled = true
        }
    }

    private fun renderUserInfo() {
        ImageLoader.with(this)
                .circle(true)
                .url(user.photoUrl)
                .into(profile_photo_iv)
        name_et.setText(user.name)
        name_et.setSelection(user.name.length)
        female_rb.isChecked = user.gender == FEMALE
        male_rb.isChecked = user.gender == MALE
    }

    private fun editProfile() {
        progress.visibility = VISIBLE

        val childUpdates = HashMap<String, Any>()
        childUpdates["/users/${user.id}/name"] = name_et.text.toString()
        childUpdates["/users/${user.id}/gender"] = getGender()
        dbReference.updateChildren(childUpdates)
                .addOnCompleteListener {
                    logProfileEdit()
                    setGenderUserProperty()
                    finish()
                }
                .addOnCanceledListener {
                    progress.visibility = GONE
                }
    }

    private fun logProfileEdit() {
        val bundle = Bundle()
        bundle.putString("new_name", name_et.text.toString())
        firebaseAnalytics.logEvent("edit_profile", bundle)
    }

    private fun setGenderUserProperty() {
        val gender = getGender()
        firebaseAnalytics.setUserProperty("gender", gender)
    }

    private fun getGender(): String = when {
        female_rb.isChecked -> FEMALE
        male_rb.isChecked -> MALE
        else -> ""
    }
}
