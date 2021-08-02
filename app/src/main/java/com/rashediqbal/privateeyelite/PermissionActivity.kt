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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        initView()
        verifyActivityResult()
        verifyPermissions()
        permissionBtn.setOnClickListener {
            if (checkAllPermission()){
                goToFinishActivity()
            } else {
                requestPermissions()
            }
        }

        if (checkAllPermission()){
            goToFinishActivity()
        }

    }

    // Go To Finish Activity
    private fun goToFinishActivity(){
        sessionManager.setGranted(true)
        val intent = Intent(this,FinishActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Init Dialog
    private fun initDialog(type: String) {

        dialog.setCancelable(false)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.dialog_bg))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.attributes?.windowAnimations  = R.style.animation

        val dialogCount1 = dialog.findViewById<TextView>(R.id.dialogCount1)
        val dialogCount2 = dialog.findViewById<TextView>(R.id.dialogCount2)
        val dialogDesc = dialog.findViewById<TextView>(R.id.dialogDesc)
        val dialogLogo = dialog.findViewById<ImageView>(R.id.dialogLogo)
        val dialogBtn = dialog.findViewById<TextView>(R.id.dialogBtn)

        if (type == "overlays") {
            dialogCount1.background =
                ContextCompat.getDrawable(this, R.drawable.dialog_count_bg_active)

        }
        if (type == "battery") {
            dialogDesc.text = getString(R.string.dialog_desc)
            dialogCount1.background =
                ContextCompat.getDrawable(this, R.drawable.dialog_count_bg)
            dialogCount2.background =
                ContextCompat.getDrawable(this, R.drawable.dialog_count_bg_active)
            dialogLogo.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_battery_optimization))

        }

        dialogBtn.setOnClickListener {
            if (type == "overlays"){
                overlaysIntent()
            } else if(type == "battery"){
                batteryOptIntent()
            }
        }

        dialog.show()

    }

    // Permission Result
    private val getPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            verifyPermissions()
        }

    // Check Permission Granted or Not
    private fun isPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Verify Permission
    private fun verifyPermissions() {

        if (isPermission(READ_EXTERNAL_STORAGE)){
            dot1.background = ContextCompat.getDrawable(this, R.drawable.ic_dot_green)
        }
        if (isPermission(READ_SMS)){
            dot2.background = ContextCompat.getDrawable(this, R.drawable.ic_dot_green)
        }
        if (isPermission(READ_CALL_LOG)){
            dot3.background = ContextCompat.getDrawable(this, R.drawable.ic_dot_green)
        }
        if (isPermission(READ_CONTACTS)){
            dot4.background = ContextCompat.getDrawable(this, R.drawable.ic_dot_green)
        }
        if (isPermission(READ_CONTACTS)){
            dot5.background = ContextCompat.getDrawable(this, R.drawable.ic_dot_green)
        }

        if (checkAllPermission()){

            goToFinishActivity()
        }

    }

    // Permission Intent
    private fun requestPermissions(){
        val permissions = arrayOf(
            READ_SMS,
            READ_CONTACTS, READ_CALL_LOG, READ_EXTERNAL_STORAGE
        )

        getPermissions.launch(permissions)

    }

    // Check All Permission
    private fun checkAllPermission():Boolean{
        return isPermission(READ_EXTERNAL_STORAGE) &&
        isPermission(READ_SMS) &&
        isPermission(READ_CALL_LOG) &&
        isPermission(READ_CONTACTS)
    }

    // Activity Result
    private val getActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            verifyActivityResult()
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
            val intent = Intent()
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
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

    // Verify Activity Result
    private fun verifyActivityResult() {
        if (!checkOverlays()) {
            initDialog("overlays")
        } else if (!checkBatteryOpt()) {
            initDialog("battery")
        } else if (dialog.isShowing){
            dialog.dismiss()
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

        sessionManager = SessionManager(this)

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_required)
    }


}