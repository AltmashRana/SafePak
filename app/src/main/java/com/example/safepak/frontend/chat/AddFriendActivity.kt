package com.example.safepak.frontend.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityAddFriendBinding
import com.example.safepak.frontend.chatAdapters.*
import com.example.safepak.logic.models.Friendship
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class AddFriendActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddFriendBinding
    lateinit var db: FirebaseFirestore
    lateinit var searches: ArrayList<Pair<User, Int>>
    lateinit var requests: ArrayList<User>
    lateinit var requestsIds: ArrayList<String>

    private var requestAdapter = GroupAdapter<GroupieViewHolder>()
    private var searchAdapter = GroupAdapter<GroupieViewHolder>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var color = ContextCompat.getDrawable(this, com.example.safepak.R.drawable.chatlist_bg)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setBackgroundDrawable(color)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        db = FirebaseFirestore.getInstance()
        requests = ArrayList()
        requestsIds = ArrayList()
        searches = ArrayList()


        binding.emptysearchText.visibility = View.VISIBLE

        binding.emptyrequestText.visibility = View.VISIBLE

        loadRequests()

        loadSuggesstions()

        binding.searchLayout.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.emptysearchText.visibility = View.VISIBLE
                loadSearches()
                binding.searchLayout.isEnabled = false
                binding.searchRecycler.visibility = View.GONE
                binding.searchBar.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({


                    binding.searchLayout.isEnabled = true
                    binding.searchRecycler.visibility = View.VISIBLE
                    binding.searchBar.visibility = View.GONE
                }, 1000)
                true
            }
            false
        }

        binding.refreshrequestsSwipe.setOnRefreshListener {
            loadSuggesstions()
            loadRequests()
        }
    }


    fun loadSuggesstions() {
        searchAdapter.clear()
        val query = db.collection("users")
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
                                binding.emptysearchText.visibility = View.GONE

                                var flag = 0

                                FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                                    .get().addOnSuccessListener { friends ->
                                        if (friends.children.count() == 0) {

                                            FirebaseDatabase.getInstance().getReference("/friend-requests/${user.userid}/${FirebaseSession.userID}")
                                                .get().addOnSuccessListener { doc ->
                                                    if (doc.children.count() > 0) {
                                                        flag = 1
                                                        searchAdapter.add(SearchlistItem(user, flag))
                                                    } else {
                                                        searchAdapter.add(SearchlistItem(user, flag))
                                                    }
                                                }
                                        } else {
                                            flag = 2
                                            searchAdapter.add(SearchlistItem(user, flag))
                                        }
                                    }
                            }
                        }
                    }
                    binding.refreshrequestsSwipe.isRefreshing = false
                }
            })
        binding.searchRecycler.adapter = searchAdapter
    }

    fun loadSearches() {
        searchAdapter.clear()
        val query = db.collection("users").whereEqualTo("firstname", binding.searchLayout.editText?.text.toString())
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
                                binding.emptysearchText.visibility = View.GONE

                                var flag = 0

                                FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                                    .get().addOnSuccessListener { friends ->
                                        if (friends.children.count() == 0) {

                                            FirebaseDatabase.getInstance().getReference("/friend-requests/${user.userid}/${FirebaseSession.userID}")
                                                .get().addOnSuccessListener { doc ->
                                                    if (doc.children.count() > 0) {
                                                        flag = 1
                                                        searchAdapter.add(SearchlistItem(user, flag))
                                                    } else {
                                                        searchAdapter.add(SearchlistItem(user, flag))
                                                    }
                                                }
                                        } else {
                                            flag = 2
                                            searchAdapter.add(SearchlistItem(user, flag))
                                        }
                                    }
                            }
                        }
                    }
                    binding.refreshrequestsSwipe.isRefreshing = false
                }
            })
        binding.searchRecycler.adapter = searchAdapter
    }


    fun loadRequests() {
        requestAdapter.clear()
        val requestQuery =
            FirebaseDatabase.getInstance().getReference("/friend-requests/${FirebaseSession.userID!!}")
                .get().addOnSuccessListener { docs ->
                    docs.children.forEach{
                        val friendship = it.getValue(Friendship::class.java)

                        db.collection("users").whereEqualTo("userid", friendship?.userid!!)
                            .get().addOnSuccessListener { docs ->
                                for (user in docs) {
                                    val requestuser = user.toObject(User::class.java)

                                    requestAdapter.add(
                                        RequestlistItem(requestuser, friendship, requestAdapter)
                                    )

                                    binding.emptyrequestText.visibility = View.GONE
                                }
                                binding.requestsCount.text = requestAdapter.groupCount.toString()
                            }
                    }


                }.addOnFailureListener {
            Toast.makeText(applicationContext, "Error fetching requests", Toast.LENGTH_SHORT).show()
        }

        binding.requestsRecycler.adapter = requestAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}