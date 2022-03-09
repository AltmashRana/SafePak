package com.example.safepak.frontend.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityAddFriendBinding
import com.example.safepak.databinding.ActivityFriendsProfileBinding
import com.example.safepak.frontend.chatAdapters.SearchlistItem
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.session.StorageSession
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import java.util.*

class FriendsProfileActivity : AppCompatActivity() {

    lateinit var binding : ActivityFriendsProfileBinding
    lateinit var user : User
    var closeflag : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var color = ContextCompat.getDrawable(this, com.example.safepak.R.drawable.chatlist_bg)
        supportActionBar?.setBackgroundDrawable(color)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        user = intent.extras?.get("user") as User

        Glide.with(this)
            .load(user.img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(binding.friendprofileDp)

        binding.friendprofilenameText.text = "${user.firstname} ${user.lastname}"

        binding.friendemailText.text = user.email

        binding.friendphoneText.text = user.phone

        binding.friendbloodText.text = user.bloodgroup

        setChatCount(user)

        setFriendshipStatus(user)

        setFriendCallsCount(user)


    }

    override fun onStart() {
        super.onStart()

        val animation1 = AnimationUtils.loadAnimation(this, R.anim.fall_down)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.lefttoright)


        binding.friendprofilenameText.startAnimation(animation2)

        binding.profileBox.startAnimation(animation1)
        binding.othersBox.startAnimation(animation1)
    }

    private fun setFriendshipStatus(user: User) {
        val db = FirebaseFirestore.getInstance()

        val query1 = db.collection("requests")
            .whereEqualTo("userid", FirebaseSession.userID)
            .whereEqualTo("friendid", user.userid)
            .whereIn("status", listOf("added", "close"))

        val query2 = db.collection("requests")
            .whereEqualTo("userid", user.userid)
            .whereEqualTo("friendid", FirebaseSession.userID)
            .whereIn("status", listOf("added", "close"))

        Tasks.whenAllSuccess<Any>(
            query1.get(),
            query2.get()
        )
            .addOnSuccessListener { objects ->
                for (obj in objects) {
                    val queryDocumentSnapshots = obj as QuerySnapshot
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val friendship = documentSnapshot.toObject(Friendship::class.java)
                            binding.friendstatusText.text = friendship.status
                        return@addOnSuccessListener
                    }
                }
            }
    }

    private fun setFriendCallsCount(user: User) {
        val db = FirebaseFirestore.getInstance()
        binding.friendhiscallsText.text = "Hidden"

        val query1 = db.collection("requests")
            .whereEqualTo("userid", FirebaseSession.userID)
            .whereEqualTo("friendid", user.userid)
            .whereIn("status", listOf("added", "close"))

        val query2 = db.collection("requests")
            .whereEqualTo("userid", user.userid)
            .whereEqualTo("friendid", FirebaseSession.userID)
            .whereIn("status", listOf("added", "close"))

        Tasks.whenAllSuccess<Any>(
            query1.get(),
            query2.get()
        )
            .addOnSuccessListener { objects ->
                for (obj in objects) {
                    val queryDocumentSnapshots = obj as QuerySnapshot
                    for (documentSnapshot in queryDocumentSnapshots) {
                        val friendship = documentSnapshot.toObject(Friendship::class.java)
                        if (friendship.status == "close")
                        {
                            val ref = Firebase.database.getReference("emergency-calls")

                            val listener = object : ValueEventListener {
                                override fun onCancelled(databaseError: DatabaseError) {
                                }

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    binding.friendhiscallsText.text = dataSnapshot.children.count().toString()
                                }
                            }
                            ref.addListenerForSingleValueEvent(listener)
                        }
                        return@addOnSuccessListener
                    }
                }
            }
    }

    private fun setChatCount(user: User) {
        val ref = Firebase.database.getReference("chat-channels/${FirebaseSession.userID}/${user.userid}")

        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                binding.friendchatcountText.text = dataSnapshot.children.count().toString()
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}