package com.rashediqbal.privateeyelite

import android.Manifest.permission.*
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionActivity : AppCompatActivity() {

    private lateinit var permissionBtn: Button

    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View
    private lateinit var dot5: View
    private lateinit var lineLine: View

    private lateinit var dialog: Dialog

    lateinit var dialogDesc: TextView

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        sessionManager = SessionManager(this)

        initView()

        initDialog()

        val getPermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

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
        if (checkOverlays()) {
            dialog = Dialog(this)
            dialog.setContentView(R.layout.overlay_layout)
            dialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.dialog_bg
                )
            )
            dialog.setCancelable(false)
            dialog.window?.setGravity(Gravity.BOTTOM)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val dialogBtn = dialog.findViewById<TextView>(R.id.dialogBtn)
            dialogDesc = dialog.findViewById(R.id.dialogDesc)
            dialogBtn.setOnClickListener {
                overlaysIntent()
            }

            dialog.show()
        }
    }




    // Permission Result
    private val getPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            verifyPermission()
        }
    // Check Permission Granted or Not
    private fun isPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun verifyPermission() {

        fun setBackground(dot:View){
            dot.background = ContextCompat.getDrawable(this, R.drawable.ic_dot_green)
        }

        when {
            isPermission(READ_EXTERNAL_STORAGE) -> setBackground(dot1)
            isPermission(READ_SMS) -> setBackground(dot2)
            isPermission(READ_CALL_LOG) -> setBackground(dot3)
            isPermission(READ_CONTACTS) -> setBackground(dot4)
            isPermission(READ_CALL_LOG) -> setBackground(dot5)
        }

    }


    // Activity Result
    private val getActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    // Overlays Intent
    private fun overlaysIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            getActivityResult.launch(intent)
        }
    }

    // Battery Optimization Intent
    private fun batteryOptIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
                Uri.parse("package$packageName")
            )
            getActivityResult.launch(intent)
        }
    }

    // Check Overlays
    private fun checkOverlays(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this)
        }
        return false
    }

    // Check Battery Optimization
    private fun checkBatteryOpt(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(packageName)
        }
        return false
    }

    private fun verifyActivityResult(){
        if(checkOverlays()){

        }

        if (checkBatteryOpt()){

        }
    }

    // Init View
    private fun initView() {
        permissionBtn = findViewById(R.id.permissionBtn)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        dot4 = findViewById(R.id.dot4)
        dot5 = findViewById(R.id.dot5)
        lineLine = findViewById(R.id.lineLine)
    }


}