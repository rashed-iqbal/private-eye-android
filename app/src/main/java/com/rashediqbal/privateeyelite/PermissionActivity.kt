package com.rashediqbal.privateeyelite

import android.Manifest.permission.*
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionActivity : AppCompatActivity() {

    private lateinit var permissionBtn:Button

    private lateinit var dot1: View
    private lateinit var dot2:View
    private lateinit var dot3:View
    private lateinit var dot4:View
    private lateinit var dot5:View
    private lateinit var lineLine:View

    private lateinit var dialog:Dialog

    lateinit var dialogDesc:TextView

    private lateinit var sessionManager:SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        sessionManager = SessionManager(this)
        initView()

        initDialog()

        val getPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            checkPermission()
        }

        val permissions = arrayOf(
            READ_SMS,
            READ_CONTACTS, READ_CALL_LOG, READ_EXTERNAL_STORAGE
        )

        permissionBtn.setOnClickListener {
            getPermissions.launch(permissions)
        }

    }

    private fun initDialog() {

        checkOverlays {
            if (!it){
                dialog = Dialog(this)
                dialog.setContentView(R.layout.overlay_layout)
                dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.dialog_bg))
                dialog.setCancelable(false)
                dialog.window?.setGravity(Gravity.BOTTOM)
                dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                val dialogBtn = dialog.findViewById<TextView>(R.id.dialogBtn)
                dialogDesc = dialog.findViewById(R.id.dialogDesc)
                dialogBtn.setOnClickListener {
                    overlaysIntent()
                }

                dialog.show()
            }
        }

    }

    private fun initView() {
        permissionBtn = findViewById(R.id.permissionBtn)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        dot4 = findViewById(R.id.dot4)
        dot5 = findViewById(R.id.dot5)
        lineLine = findViewById(R.id.lineLine)

        checkPermission()

    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this, READ_SMS) == PackageManager.PERMISSION_GRANTED ) {
            dot2.background = ContextCompat.getDrawable(this,R.drawable.ic_dot_green)
        }

        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            dot4.background = ContextCompat.getDrawable(this,R.drawable.ic_dot_green)
        }

        if (ContextCompat.checkSelfPermission(this, READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED){
            dot3.background = ContextCompat.getDrawable(this,R.drawable.ic_dot_green)
        }

        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            dot1.background = ContextCompat.getDrawable(this,R.drawable.ic_dot_green)
        }

        if (ContextCompat.checkSelfPermission(this, READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            lineLine.setBackgroundColor(ContextCompat.getColor(this,R.color.primary_color))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.canDrawOverlays(this)){
                    sessionManager.setGranted(true)
                    startActivity(Intent(this,FinishActivity::class.java))
                    finish()
                }
            }

        }
    }

    private val getActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        checkOverlays {
            if (it){
                Toast.makeText(this, "Permission Enabled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                dialogDesc.text = "Oh! you haven't enable Appear on Top permission yet.Please enable it.This will help you to run app automatically after restart."
            }
        }
    }

    private fun overlaysIntent(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${this.packageName}"))
            getActivityResult.launch(intent)
        }
    }

    private fun checkOverlays(callBack:(result:Boolean)->Unit){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            callBack(Settings.canDrawOverlays(this))
        }
    }

}