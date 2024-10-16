package com.example.farmwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.farmwatch.databinding.ActivityLoginBinding
import com.example.farmwatch.utilitis.hide
import com.example.farmwatch.utilitis.show
import com.example.farmwatch.utilitis.toast
import com.example.farmwatch.viewModel.AuthListener
import com.example.farmwatch.viewModel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), AuthListener {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    val firebaseAuth=FirebaseAuth.getInstance()
    lateinit var viewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel= ViewModelProvider(this).get(AuthViewModel::class.java)
        binding.authViewModel=viewModel
        viewModel.authListener=this

        if(firebaseAuth.currentUser!=null){
            Intent(this,MainActivity::class.java).also{
                startActivity(it)
            }
        }
        binding.signGoogleBtnLogin.setOnClickListener{
            signIn()
        }
        binding.createaccountText.setOnClickListener{
            val intent=Intent(this,SignupActivity::class.java)
            startActivity(intent)
        }
        binding.forgotPasswdTextLogin.setOnClickListener{
            val userEmail=binding.emailEditLogin.text.toString()
            if(userEmail.isNullOrEmpty()){
                Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show()
            }
            else{
                firebaseAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.returnActivityResult(requestCode, resultCode, data)
    }

    fun signIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }
    companion object{
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onStarted() {
        binding.progressLogin.show()
    }

    override fun onSuccess(authRepo: LiveData<String>) {
        authRepo.observe(this, Observer {
            binding.progressLogin.hide()
            if (it.toString() == "Success") {
                toast("Logged In")
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()

            }
        })
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
    }

    override fun onFailure(message: String) {
        binding.progressLogin.hide()
        toast("Failure")
    }
}