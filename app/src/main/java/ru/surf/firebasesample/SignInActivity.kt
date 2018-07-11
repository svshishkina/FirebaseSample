package ru.surf.firebasesample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import ru.surf.firebasesample.domain.User
import ru.surfstudio.android.imageloader.ImageLoader

class SignInActivity : BaseActivity(), GoogleApiClient.OnConnectionFailedListener {

    companion object {
        private const val TAG = "SignInActivity"
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        initViews()
        initGoogleApiClient()
        checkUserLoggedIn()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                Log.e(TAG, "Google Sign In failed.")
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:$connectionResult")

        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    private fun initViews() {
        ImageLoader.with(this)
                .url(R.drawable.logo)
                .circle(true)
                .into(logo_iv)

        sign_in_button.setOnClickListener { signIn() }
    }

    private fun initGoogleApiClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    private fun checkUserLoggedIn() {
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        progress.visibility = VISIBLE

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithCredential", task.exception)
                        progress.visibility = GONE
                        Toast.makeText(this@SignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        handleFirebaseAuthResult(task.result)
                    }
                }
    }

    private fun handleFirebaseAuthResult(authResult: AuthResult?) {
        if (authResult != null) {
            val user = authResult.user
            addUserToDb(authResult)
            Toast.makeText(this, "Welcome " + user.email!!, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserToDb(authResult: AuthResult) {
        val usersReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        val user = createUser(authResult)
        usersReference.child(user.id).setValue(user)
                .addOnCompleteListener {
                    progress.visibility = GONE
                    startActivity(Intent(this, ChatActivity::class.java))
                    finish()
                }
    }

    private fun createUser(authResult: AuthResult): User {
        val user = authResult.user
        val userName = user.displayName ?: ""
        val uid = user.uid
        val photoUrl = user.photoUrl ?: ""

        return User(uid, userName, photoUrl.toString(), "")
    }
}
