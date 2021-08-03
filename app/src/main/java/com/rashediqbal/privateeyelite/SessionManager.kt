package com.rashediqbal.privateeyelite

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SessionManager(context:Context) {

    private val keyCredential = "credential"
    private val keyTarget = "target"
    private val keyLogin = "isLogin"

    private val keyPermission = "isGranted"

    private var session :SharedPreferences = context.getSharedPreferences("userSession",Context.MODE_PRIVATE)
    private var editor :SharedPreferences.Editor = session.edit()

    private val keyFirstTime = "isFirstTime"

    fun saveUser(credential:String,target:String){
        editor.putString(keyCredential,credential)
        editor.putString(keyTarget,target)
        editor.putBoolean(keyLogin,true)
        editor.commit()
    }

    fun getUser(): HashMap<String, String?> {
        val map = HashMap<String,String?>()
        map[keyCredential] = session.getString(keyCredential,null)
        map[keyTarget] = session.getString(keyTarget,null)
        map[keyPermission] = session.getBoolean(keyPermission,false).toString()
        map[keyLogin] = session.getBoolean(keyLogin,false).toString()
        return map
    }

    fun isFirstTime():Boolean{
        return session.getBoolean(keyFirstTime,false)
    }

    fun setFirstTime(){
        editor.putBoolean(keyFirstTime,true)
        editor.commit()
    }

    fun setGranted(value:Boolean){
        editor.putBoolean(keyPermission,value)
        editor.commit()
    }

    fun checkCredential(): Boolean {
        return session.getBoolean(keyLogin,false)
    }


    fun checkLogin():Boolean{
        return session.getBoolean(keyLogin,false) && session.getBoolean(keyPermission,false)
    }


    fun saveContactsCount(){

    }

    fun getContactsCount(){

    }

    fun saveConversationsCount(){

    }

    fun getConversationCount(){

    }

    fun saveCallsCount(){

    }

    fun getCallsCount() {

    }

}