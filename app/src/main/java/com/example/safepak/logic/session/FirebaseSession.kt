
import android.content.ContentValues.TAG
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.safepak.data.User
import com.example.safepak.logic.models.Message
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.models.notification.NotificationData
import com.example.safepak.logic.models.notification.PushNotification
import com.example.safepak.logic.models.notification.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.HashMap


object FirebaseSession {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    val userID : String? by lazy { currentUser.id }

    private val currentUser: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
            ?: throw NullPointerException("UID is null.")}")

    fun updateCurrentUser(firstname: String = "", lastname: String = "", gender: String = "",bloodgroup: String = "",password: String = "",cnic: String = "",email: String = "",willing: Boolean = false, profilePicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (firstname.isNotBlank()) userFieldMap["firstname"] = firstname
        if (lastname.isNotBlank()) userFieldMap["lastname"] = lastname
        if (email.isNotBlank()) userFieldMap["email"] = email
        if (password.isNotBlank()) userFieldMap["password"] = password
        if (gender.isNotBlank()) userFieldMap["gender"] = gender
        if (bloodgroup.isNotBlank()) userFieldMap["bloodgroup"] = bloodgroup
        if (cnic.isNotBlank()) userFieldMap["cnic"] = cnic
        userFieldMap["willingToDonate"] = willing
        if (profilePicturePath != null)
            userFieldMap["img"] = profilePicturePath
        currentUser?.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUser?.get()?.addOnSuccessListener {
            onComplete(it.toObject(User::class.java)!!)
        }
    }

    fun updateUserStatus(status : String) {
        val calendar  = Calendar.getInstance()
        val currentDate = SimpleDateFormat( "dd/MM/yyyy")
        val saveCurrentDate = currentDate.format (calendar.time)
        val currentTime = SimpleDateFormat("hh:mm a")
        val saveCurrentTime = currentTime.format (calendar.time)

        val onlinestateMap = HashMap<String, String>()
        onlinestateMap["time"] = saveCurrentTime;
        onlinestateMap["date"] = saveCurrentDate;
        onlinestateMap["state"] = status;

        currentUser?.update(mapOf("status" to onlinestateMap))
    }

    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUser?.get()?.addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        currentUser?.update(mapOf("registrationTokens" to registrationTokens))
    }

    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch{
        try {
            val response =
                RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson (response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendText(user : User, text : String) {

        val fromId = FirebaseSession.getCurrentUser { current ->
            val fromId = current.userid
            val toId = user.userid

            if (fromId == null) return@getCurrentUser

            val send_query =
                FirebaseDatabase.getInstance().getReference("/chat-channels/$fromId/$toId").push()

            val receive_query =
                FirebaseDatabase.getInstance().getReference("/chat-channels/$toId/$fromId").push()

            val message = Message(
                send_query.key!!,
                fromId,
                toId,
                text,
                (System.currentTimeMillis() / 1000).toString(),
                getDatetime()
            )

            send_query.setValue(message)
                .addOnSuccessListener {
                    val textMap = mutableMapOf<String, Any>()
                    textMap["id"] = message.id.toString()
                    textMap["senderid"] = message.senderid.toString()
                    textMap["receiverid"] = message.receiverid.toString()
                    textMap["text"] = message.text.toString()
                    textMap["timestamp"] = message.timestamp.toString()
                    textMap["time"] = message.time.toString()
                    textMap["status"] = message.status.toString()

                    receive_query.setValue(message)

                    val sendlatest =
                        FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/$fromId/$toId")
                    sendlatest.child(fromId + toId).updateChildren(textMap)
                    val receivelatest =
                        FirebaseDatabase.getInstance()
                            .getReference("/latest-messages/$toId/$fromId")

                    receivelatest.child(toId + fromId).updateChildren(textMap)
                }

            if (user.registrationTokens.size > 0)
                sendNotification(
                    PushNotification(
                        NotificationData(
                            fromId,
                            "chat",
                            "${current.firstname}: $text",
                            "Message"
                        ), user.registrationTokens.last()
                    )
                )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDatetime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        val formatted = current.format(formatter)

        return formatted
    }


}