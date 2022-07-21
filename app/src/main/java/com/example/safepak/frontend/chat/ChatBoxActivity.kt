package com.example.safepak.frontend.chat

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityChatBoxBinding
import com.example.safepak.frontend.chatAdapters.ReceiveItem
import com.example.safepak.frontend.chatAdapters.SendItem
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.models.Message
import com.example.safepak.logic.models.notification.NotificationData
import com.example.safepak.logic.models.notification.PushNotification
import com.example.safepak.logic.session.StorageSession
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.type.Date
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class ChatBoxActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBoxBinding
    lateinit var db: FirebaseDatabase
    lateinit var user: User
    lateinit var adapter: GroupAdapter<GroupieViewHolder>

    val close_remove = "Remove from close"
    var close_flag = 0

    var friend_flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBoxBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseDatabase.getInstance()

        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_down)

        binding.toolbar.startAnimation(animation)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.sendtextBt.setOnClickListener {
            if (binding.chatboxBox.text.toString().isNotBlank()) {
                FirebaseSession.sendText(user, friend_flag, binding.chatboxBox.text.toString())
                binding.chatboxRecyclerView.scrollToPosition(adapter.itemCount - 1)
                binding.chatboxBox.text.clear()
            }
        }

        binding.chatboxDp.setOnClickListener{
            val intent = Intent(this, FriendsProfileActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()

        user = intent.extras?.get("USER_KEY") as User
        friend_flag = intent.extras?.get("IS_FRIEND") as Boolean

        if (friend_flag)
        {
            Glide.with(this)
                .load(user.img?.let { StorageSession.pathToReference(it) })
                .placeholder(R.drawable.empty_dp)
                .into(binding.chatboxDp)
            binding.chatboxnameText.text = "${user.firstname} ${user.lastname}"

            binding.emptychatboxText.visibility = View.VISIBLE

            if (user.status["state"] == "online") {
                binding.chatboxpersonStatus.text = "online"
            }else
                binding.chatboxpersonStatus.text = "${user.status["time"]}, ${user.status["date"]}"

        }
        else {
            binding.chatboxDp.setImageResource(R.drawable.empty_dp)

            binding.chatboxnameText.text = "${user.firstname} ${user.lastname}"

            binding.chatboxpersonStatus.text = ""

            binding.chatboxDp.isEnabled = false
        }

        loadMessages()

        invalidateOptionsMenu()

    }

    private fun loadMessages() {
        adapter = GroupAdapter<GroupieViewHolder>()

        binding.chatboxRecyclerView.adapter = adapter

        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.userid
        val ref = FirebaseDatabase.getInstance().getReference("/chat-channels/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)

                if (message != null) {

                    if (message.senderid == FirebaseAuth.getInstance().uid) {
                        adapter.add(SendItem(message))
                    } else {
                        adapter.add(ReceiveItem(message))
                    }
                }
                if (adapter.spanCount == 0)
                    binding.emptychatboxText.visibility = View.VISIBLE
                else
                    binding.emptychatboxText.visibility = View.GONE

                binding.chatboxRecyclerView.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_dropdown, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (friend_flag) {
            db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                .get().addOnSuccessListener { doc ->
                    if (doc.exists()){
                        val friendship = doc.getValue(Friendship::class.java)
                        if (friendship?.status == "close") {
                            close_flag = 1
                            menu?.findItem(R.id.close_item)?.title = close_remove
                        }
                    }
                }
        }
        else {
            menu?.findItem(R.id.call_item)?.isEnabled = false
            menu?.findItem(R.id.unfriend_item)?.isEnabled = false
            menu?.findItem(R.id.details_item)?.isEnabled = false
            menu?.findItem(R.id.block_item)?.isEnabled = false
            menu?.findItem(R.id.close_item)?.isEnabled = false
        }
            return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.call_item -> {
                call()
            }
            R.id.close_item -> {
                showpopUp("close")
            }
            R.id.block_item -> {
                Toast.makeText(this, "Blocked", Toast.LENGTH_SHORT).show()
                binding.chatboxBar.visibility = View.GONE
            }
            R.id.unfriend_item -> {
                showpopUp("friend")
            }
            R.id.details_item -> {
                val intent = Intent(this, FriendsProfileActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
                binding.chatboxBar.visibility = View.GONE
            }
            R.id.delete_item -> {
                deleteChats(user)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun call() {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + "${user.phone}")
        startActivity(dialIntent)
    }

    fun showpopUp(type: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Are you sure?")
        builder.setCancelable(true)
        builder.setIcon(R.drawable.accept)
        builder.setPositiveButton("Yes") { _, _ ->
            binding.chatboxBar.visibility = View.VISIBLE
            if (type == "close") {
                addRemoveClose()
            } else if (type == "friend") {
                removeFriend()
            }
        }
        builder.setNeutralButton("No") { _, _ ->

        }
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun removeFriend() {
        db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
            .get().addOnSuccessListener { doc ->
                if (doc.exists()) {

                    db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                        .removeValue().addOnSuccessListener {

                            db.getReference("/friends/${user.userid}/${FirebaseSession.userID}")
                                .removeValue().addOnSuccessListener {

//                                    deleteChats(user)

                                    Toast.makeText(this, "Friend Removed", Toast.LENGTH_SHORT).show()

                                    finish()

                                    binding.chatboxBar.visibility = View.GONE
                                }

                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                            binding.chatboxBar.visibility = View.GONE
                        }
                } else {
                    finish()
                }
            }
    }

    fun deleteChats(user : User){
        FirebaseDatabase.getInstance()
            .getReference("chat-channels/${user.userid}")
            .child(FirebaseSession.userID!!)
            .removeValue()

        FirebaseDatabase.getInstance()
            .getReference("chat-channels/${FirebaseSession.userID}")
            .child(user.userid!!)
            .removeValue()

        if (friend_flag) {
            FirebaseDatabase.getInstance()
                .getReference("latest-messages/${user.userid}/${FirebaseSession.userID!!}")
                .child("${user.userid}${FirebaseSession.userID!!}")
                .updateChildren(mapOf("text" to ""))

            FirebaseDatabase.getInstance()
                .getReference("latest-messages/${FirebaseSession.userID}/${user.userid}")
                .child("${FirebaseSession.userID!!}${user.userid}")
                .updateChildren(mapOf("text" to ""))
        } else {
            FirebaseDatabase.getInstance()
                .getReference("latest-messages/${user.userid}/${FirebaseSession.userID!!}")
                .child("${user.userid}${FirebaseSession.userID!!}")
                .removeValue()

            FirebaseDatabase.getInstance()
                .getReference("latest-messages/${FirebaseSession.userID}/${user.userid}")
                .child("${FirebaseSession.userID!!}${user.userid}")
                .removeValue()
        }
    }

    fun addRemoveClose() {
        if (close_flag == 0) {
            db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                .get().addOnSuccessListener { doc ->
                    if (doc.exists()){
                        val friendship = doc.getValue(Friendship::class.java)
                        if (friendship?.status == "added") {
                            db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                                .updateChildren(mapOf("status" to "close")).addOnSuccessListener {

                                    Toast.makeText(this, "Added to close", Toast.LENGTH_SHORT).show()
                                    close_flag = 1
                                    invalidateOptionsMenu()
                                    binding.chatboxBar.visibility = View.GONE
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                                    binding.chatboxBar.visibility = View.GONE
                                }
                        }

                    }
                    else {
                        finish()
                    }
                }
        } else {
            db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                .get().addOnSuccessListener { doc ->
                    if (doc.exists()){
                        val friendship = doc.getValue(Friendship::class.java)
                        if (friendship?.status == "close") {
                            db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                                .updateChildren(mapOf("status" to "added")).addOnSuccessListener {

                                    Toast.makeText(this, "Removed from close", Toast.LENGTH_SHORT).show()

                                    close_flag = 0

                                    invalidateOptionsMenu()

                                    binding.chatboxBar.visibility = View.GONE

                                }.addOnFailureListener {
                                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                                    binding.chatboxBar.visibility = View.GONE
                                }
                        }

                    }
                    else {
                        finish()
                    }
                }
        }
    }
}