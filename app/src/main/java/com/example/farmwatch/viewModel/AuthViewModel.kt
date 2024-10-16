package com.example.farmwatch.viewModel

import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmwatch.Repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

class AuthViewModel : ViewModel(){

    var name:String?=null
    var mobNo: String? = null
    var email: String? = null
    var city: String? = null
    var password: String? = null
    var confPassword: String? = null
    var userType: String? = "normal"
    var authListener: AuthListener? = null

    var loginmail:String?=null
    var loginpwd :String?=null

    lateinit var authRepository: AuthRepository
    lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    val userPosts= arrayListOf<String>()
    fun signupButtonClicked(view:View){
        authListener!!.onStarted()
        if (name.isNullOrEmpty() || mobNo.toString().length != 10 || mobNo == null || password.isNullOrEmpty() || confPassword.isNullOrEmpty() || city.isNullOrEmpty()) {
            // Failure
            authListener!!.onFailure("Error Occurred")
            return
        }

        val data = hashMapOf(
            "name" to name,
            "mobNo" to mobNo,
            "email" to email,
            "city" to city,
            "userType" to userType,
            "posts" to  userPosts,
            "profileImage" to ""
        )
        val authRepo = AuthRepository().signInWithEmail(email!!, password!!, data)
        authListener?.onSuccess(authRepo)
    }
    fun returnActivityResult(requestCode: Int, resultCode:Int,data: Intent?){
        authListener!!.onStarted()

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    var data2 = hashMapOf(
                        "userType" to userType,
                        "posts" to userPosts,
                        "name" to account.displayName.toString(),
                        "profileImage" to account.photoUrl.toString()
                    )
                    authRepository = AuthRepository()
                    var returned = authRepository.signInToGoogle(
                        account.idToken!!,
                        account.email.toString(),
                        data2
                    )
                    Log.d("AuthView", returned.value.toString())
                    authListener?.onSuccess(returned)
                } catch (e: ApiException) {
                    authListener!!.onFailure(e.message.toString())
                }
            } else {
            }
        }
    }
    fun loginButtonClicked(view: View) {
        if (authListener == null) {
            Log.e(TAG, "AuthListener is null")
            return
        }
        authListener!!.onStarted()
        if (loginmail.isNullOrEmpty() || loginpwd.isNullOrEmpty()) {
            authListener?.onFailure("Invalid email or password")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(loginmail).matches()) {
            authListener?.onFailure("Please enter a valid email")
            return
        }
        // Success
        val authRepo = AuthRepository().logInWithEmail(loginmail!!, loginpwd!!)
        authListener?.onSuccess(authRepo)
    }
}