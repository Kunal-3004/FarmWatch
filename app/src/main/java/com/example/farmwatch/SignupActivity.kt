package com.example.farmwatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.farmwatch.databinding.ActivitySignupBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity(){

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signupBtnSignup.setOnClickListener {
            signUpUser()
        }
        binding.signGoogleBtnSignup.setOnClickListener {
            signUpWithGoogle()
        }
        binding.loginRedirectTextSignup.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun signUpUser() {
        val fullname = binding.nameEditSignup.text.toString().trim()
        val number = binding.numberEditSignup.text.toString().trim()
        val email = binding.emailEditSignup.text.toString().trim()
        val city = binding.cityEditSignup.text.toString().trim()
        val password = binding.passwdEditSignup.text.toString().trim()
        val confPassword = binding.confPasswdEditSignup.text.toString().trim()

        if (fullname.isEmpty()) {
            binding.nameEditSignup.error = "Enter your name"
            return
        }
        if (number.isEmpty()) {
            binding.numberEditSignup.error = "Enter your number"
            return
        }
        if (email.isEmpty()) {
            binding.emailEditSignup.error = "Enter your email"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditSignup.error = "Enter valid email"
            return
        }
        if (city.isEmpty()) {
            binding.cityEditSignup.error = "Enter your city"
            return
        }
        if (password.isEmpty()) {
            binding.passwdEditSignup.error = "Enter your password"
            return
        }
        if (password.length < 6) {
            binding.passwdEditSignup.error = "Password must be 6 characters long"
            return
        }
        if (confPassword.isEmpty()) {
            binding.confPasswdEditSignup.error = "Enter your confirm password"
            return
        }
        if (password != confPassword) {
            binding.confPasswdEditSignup.error = "Passwords do not match"
            return
        }

        binding.progressSignup.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressSignup.visibility = View.GONE
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "fullname" to fullname,
                        "number" to number,
                        "email" to email,
                        "city" to city
                    )

                    userId?.let {
                        db.collection("users").document(it)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserDataToFirestore(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun saveUserDataToFirestore(user: FirebaseUser?) {
        val userId = user?.uid
        val userData = hashMapOf(
            "fullname" to (user?.displayName ?: ""),
            "email" to (user?.email ?: ""),
        )
        userId?.let {
            db.collection("users").document(it)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
