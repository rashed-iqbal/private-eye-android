package com.rashediqbal.privateeyelite

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.accounts.AccountManager
import android.content.Intent
import android.net.wifi.hotspot2.pps.Credential
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AuthActivity : AppCompatActivity() {

    private val db = Firebase.firestore

    private lateinit var inputCredential: OtpTextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar


    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        sessionManager = SessionManager(this)
        initView()

        inputCredential.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // fired when user types something in the Otpbox
                inputCredential.resetState()
                errorText.visibility = View.GONE
            }

            override fun onOTPComplete(otp: String) {
                progressBar.visibility = View.VISIBLE
                checkCredential { data ->
                    if (data == null) {
                        authFailed("Invalid Credential")
                    } else {
                        checkCredentialUses { isValid ->
                            when (isValid) {
                                null -> loginNewDevice(data){
                                    if (it){
                                        authSuccess()
                                    }else {
                                        authFailed("Something Went Wrong!")
                                    }
                                }
                                false -> authFailed("Credential Used Before")
                                true -> loginExistingDevice(data){
                                    if (it){
                                        authSuccess()
                                    } else {
                                        authFailed("Something Went Wrong!")
                                    }
                                }
                            }
                        }


                    }
                }

            }
        }


    }

    private fun loginExistingDevice(userData:Map<String,Any>,callback: (value: Boolean) -> Unit){
        sessionManager.saveUser(inputCredential.otp!!, userData["target"]!! as String)
        callback(sessionManager.checkCredential())
    }


    private fun loginNewDevice(userData:Map<String,Any>,callback: (value: Boolean) -> Unit) {

        val hashMap = hashMapOf<String,Any>(
            "device_id" to getPhoneDetails()["deviceId"]!!,
             "device_name" to getPhoneDetails()["phoneName"]!!,
             "gmail" to getPhoneDetails()["gmail"]!!
        )
        val userEmail:String = userData["email"] as String
        val target:String = getPhoneDetails()["target"]!!
        db.collection("target_users").document(target).set(hashMap)
            .addOnSuccessListener {
                db.collection("users").document(userEmail).update("target", target)
                    .addOnSuccessListener {
                        sessionManager.saveUser(inputCredential.otp!!,target)
                        callback(sessionManager.checkCredential())
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    fun checkCredentialUses(callback: (value: Boolean?) -> Unit) {
        db.collection("target_users").document(getPhoneDetails()["target"]!!).get()
            .addOnSuccessListener { result ->
                val data = result.data
                Log.d("CHECK_USER", "$data")
                if (data == null){
                    callback(null)
                } else {
                    val isCredentialUsed: Boolean = data["device_id"] == getPhoneDetails()["deviceId"]
                    callback(isCredentialUsed)
                }

            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Auth Failed
    private fun authFailed(text: String) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = text
        inputCredential.showError()
    }

    // Auth Success
    private fun authSuccess(){
        progressBar.visibility = View.GONE
        inputCredential.showSuccess()
        findViewById<TextView>(R.id.successText).visibility = View.VISIBLE
        Toast.makeText(this@AuthActivity, "Login Successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@AuthActivity, PermissionActivity::class.java))
        finish()
    }

    // Check Credential
    fun checkCredential(callback: (value: Map<String, Any>?) -> Unit) {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val isCredential = result!!.find { it.data["credential"] == inputCredential.otp }
                callback(isCredential?.data)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    // Get Gmail and PhoneName
    private fun getPhoneDetails(): HashMap<String, String> {
        val gmailPattern = Patterns.EMAIL_ADDRESS
        val accounts = AccountManager.get(this@AuthActivity).accounts
        val gmail = if (gmailPattern.matcher(accounts[0].name).matches()) accounts[0].name else ""
        val phoneName = Build.MANUFACTURER + " " + Build.MODEL
        val validate = Build.ID
        val target = "${Build.MODEL.lowercase()}${inputCredential.otp}"
        return hashMapOf(
            "gmail" to gmail,
            "phoneName" to phoneName,
            "deviceId" to validate,
            "target" to target
        )
    }

    // Initialize View
    private fun initView() {
        inputCredential = findViewById(R.id.inputCredential)
        errorText = findViewById(R.id.errorText)
        progressBar = findViewById(R.id.spin_kit)
        progressBar.indeterminateDrawable = ThreeBounce()
    }

}


//
//// fired when user has entered the OTP fully.
//
//// checking start
//
//db.collection("users").get()
//.addOnSuccessListener { result ->
//    val isCredential = result!!.find { it.data["credential"] == otp }
//    if (isCredential != null){
//        val data = isCredential.data
//        val email = isCredential.id
//
//        if(data["target"] != target){
//            val hashMap = hashMapOf<String, Any>(
//                "device_name" to phoneName,
//                "gmail" to gmail,
//                "device_id" to validate
//            )
//            db.collection("users").document(email).update("target", target)
//                .addOnSuccessListener {
//                    db.collection("target_users").document(target).set(hashMap)
//                        .addOnSuccessListener {

//
//                        }
//                }
//        }else if (data["target"] == target) {
//            db.collection("target_users").document(target).get()
//                .addOnSuccessListener { target_user ->
//                    val targetUserData = target_user.data
//                    if (targetUserData?.get("device_id") == validate){
//                        //success area
//
//                        progressBar.visibility = View.GONE
//                        inputCredential.showSuccess()
//                        findViewById<TextView>(R.id.successText).visibility = View.VISIBLE
//                        sessionManager.saveUser(otp,target)
//                        Toast.makeText(this@AuthActivity, "Login Successful", Toast.LENGTH_SHORT).show()
//                        startActivity(Intent(this@AuthActivity, PermissionActivity::class.java))
//                        finish()
//                    }
//                }
//        }
//
//    } else {

//    }
//
//
//}
//
