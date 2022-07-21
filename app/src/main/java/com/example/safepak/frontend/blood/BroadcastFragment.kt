package com.example.safepak.frontend.blood

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentBroadcastBinding
import com.example.safepak.databinding.FragmentRequestBinding
import com.example.safepak.frontend.chat.ChatBoxActivity
import com.example.safepak.frontend.chat.ChatsFragment
import com.example.safepak.frontend.chatAdapters.ChatlistItem
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.Message
import com.example.safepak.logic.models.UserLocation
import com.example.safepak.logic.models.notification.NotificationData
import com.example.safepak.logic.models.notification.PushNotification
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_blood_response.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.*

class BroadcastFragment : Fragment() {

    lateinit var binding: FragmentBroadcastBinding
    lateinit var comm: IBloodBroadcast
    lateinit var blood : String
    lateinit var circle_center : UserLocation
    lateinit var callid : String
    var circle_radius : Double = 0.0

    var respondant_count = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBroadcastBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comm = activity as IBloodBroadcast

        blood = arguments?.getString("BLOOD")!!
        circle_center = arguments?.getParcelable("CIRCLE")!!
        circle_radius = arguments?.getDouble("RADIUS")!!

        sendBroadcast()

        checkForRespondants()

        binding.bloodbroadcastcancelBt.setOnClickListener {
            comm.stopBroadcast()
            onDestroy()
        }
    }

    private fun checkForRespondants() {
        val fromId = FirebaseSession.userID
        val time = System.currentTimeMillis() / 1000
        val latest_query = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        latest_query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                p0.children.forEach{
                    val message = it.getValue(Message::class.java)
                    if (message!!.timestamp != "" && message.timestamp.toDouble() >= time && message.text!!.startsWith("\uD83E\uDE78") && message.text!!.endsWith("\uD83E\uDE78")) {
                        respondant_count = +1
                        if (respondant_count > 1)
                            binding.broadcastText.text = "$respondant_count persons have responded!"
                        else
                            binding.broadcastText.text = "$respondant_count person has responded!"
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                p0.children.forEach {
                    val message = it.getValue(Message::class.java)
                    if (message!!.timestamp != "" && message.timestamp.toDouble() >= time && message.text!!.startsWith("\uD83E\uDE78") && message.text!!.endsWith("\uD83E\uDE78")) {
                        respondant_count = +1
                        if (respondant_count > 1)
                            binding.broadcastText.text = "$respondant_count persons have responded!"
                        else
                            binding.broadcastText.text = "$respondant_count person has responded!"
                    }
                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDatetime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        val formatted = current.format(formatter)

        return formatted
    }

    private fun sendBroadcast() {
        val q = FirebaseDatabase.getInstance().getReference("/emergency-calls/${FirebaseSession.userID}/")
        callid = q.push().key!!
        val call = Call(callid, FirebaseSession.userID, "medical", circle_center, getDatetime(),"stopped")
        q.child(callid).setValue(call)

        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("bloodgroup", blood)
            .whereEqualTo("willingToDonate", true)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore Error ", error.message.toString())
                        return
                    }
                    for (doc: DocumentChange in value?.documentChanges!!) {
                        if (doc.type == DocumentChange.Type.ADDED) {
                            val user = doc.document.toObject(User::class.java)
                            if (user.userid != FirebaseSession.userID) {
                                searchUserInRadius(user)
                            }
                        }
                    }
                }
            })
    }

    fun checkInside(radius : Double, circle : LatLng, position : LatLng) : Boolean {
        val distance = SphericalUtil.computeDistanceBetween(circle, position);

        return distance < radius * 1000
    }


    fun searchUserInRadius(user : User){
        val ref = FirebaseDatabase.getInstance().getReference("/users-location/${user.userid}")

        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot: DataSnapshot in dataSnapshot.children) {
                    val location = snapshot.getValue(UserLocation::class.java)
                    if (location != null) {
                        val start = LatLng(circle_center.latitude!!.toDouble(), circle_center.longitude!!.toDouble())
                        val end = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
                        if(checkInside(circle_radius, start, end)){
                            FirebaseSession.sendNotification(
                                PushNotification(
                                    NotificationData(FirebaseSession.userID!!, callid,
                                        false,"medical","Need ${blood} blood.", "Medical Emergency", blood)
                                    , user.registrationTokens.last())
                            )
                        }
                    }
                }
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    //
//    fun calculateDistance (circleLat : Double , circleLong : Double
//                           , userLat : Double , userLong : Double) : Double{
//        var c = sin(Math.toRadians(circleLat)) *
//                sin(Math.toRadians(userLat)) +
//                cos(Math.toRadians(circleLat)) *
//                cos(Math.toRadians(userLat)) *
//                cos(Math.toRadians(userLong) -
//                        Math.toRadians(circleLong))
//
//        c = if (c > 0)
//            min(1.0, c)
//        else
//            max(-1.0, c)
//
//        return 3959 * 1.609 * 1000 * acos(c);
//    }
}