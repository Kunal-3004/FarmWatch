import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.farmwatch.HomeActivity
import com.example.farmwatch.LoginActivity
import com.example.farmwatch.R
import com.example.farmwatch.databinding.ActivitySignupBinding
import com.example.farmwatch.room.AppDatabase
import com.example.farmwatch.room.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase
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
        db = AppDatabase.getDatabase(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        if (fullname.isEmpty() || number.isEmpty() || email.isEmpty() || city.isEmpty() ||
            password.isEmpty() || password != confPassword || password.length < 6) {
            Toast.makeText(this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressSignup.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressSignup.visibility = View.GONE
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = User(userId!!, fullname, number, email, city)

                    lifecycleScope.launch {
                        try {
                            db.userDao().insertUser(user)
                            Toast.makeText(this@SignupActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignupActivity, HomeActivity::class.java))
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@SignupActivity, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
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
                    saveUserDataToRoom(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserDataToRoom(user: FirebaseUser?) {
        val userId = user?.uid ?: return
        val userData = User(
            id = userId,
            fullname = user.displayName ?: "",
            number = "",
            email = user.email ?: "",
            city = ""
        )

        lifecycleScope.launch {
            try {
                db.userDao().insertUser(userData)
                Toast.makeText(this@SignupActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignupActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Error saving user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
