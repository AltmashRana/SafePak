package com.example.safepak.frontend.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.frontend.chatAdapters.RequestlistAdapter
import com.example.safepak.frontend.chatAdapters.SearchAdapter
import com.example.safepak.data.User
import com.example.safepak.databinding.ActivityAddFriendBinding
import com.example.safepak.frontend.home.HomeActivity
import com.example.safepak.frontend.login.LoginActivity
import com.example.safepak.logic.models.Friendship
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QueryDocumentSnapshot

import com.google.firebase.firestore.QuerySnapshot







class AddFriendActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddFriendBinding
    lateinit var db : FirebaseFirestore
    lateinit var searches: ArrayList<Pair<User, Int>>
    lateinit var requests: ArrayList<User>
    lateinit var requestsIds: ArrayList<String>
    lateinit var searchRecyclerView: RecyclerView
    lateinit var requestRecyclerView: RecyclerView
    lateinit var requestAdapter: RequestlistAdapter
    lateinit var searchAdapter : SearchAdapter


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

        searchRecyclerView = binding.searchRecycler
        searchAdapter = SearchAdapter(searches, FirebaseSession.userID)
        searchRecyclerView.adapter = searchAdapter
        searchRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

        requestRecyclerView = binding.requestsRecycler
        requestAdapter = FirebaseSession.userID?.let {
            RequestlistAdapter(requests,
                it, requestsIds)
        }!!
        requestRecyclerView.adapter = requestAdapter
        requestRecyclerView.layoutManager = LinearLayoutManager(view.context)

        loadRequests()
        loadSuggesstions()

        binding.searchLayout.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadSearches()
                binding.searchLayout.isEnabled = false
                binding.searchRecycler.visibility = View.GONE
                binding.searchBar.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({


                    binding.searchLayout.isEnabled = true
                    binding.searchRecycler.visibility = View.VISIBLE
                    binding.searchBar.visibility = View.GONE
                },  1000)
                true
            }
            false
        }


        binding.refreshrequestsBt.setOnClickListener{
            loadSuggesstions()
            loadRequests()
            binding.refreshrequestsBt.isEnabled = false
            binding.refreshrequestsBt.visibility = View.GONE
            binding.requestBar.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({

                binding.refreshrequestsBt.isEnabled = true
                binding.refreshrequestsBt.visibility = View.VISIBLE
                binding.requestBar.visibility = View.GONE
            },  3000)
        }
    }


    fun loadSuggesstions(){
        searches.clear()
        val query = db.collection("users").limit(10).
        addSnapshotListener (object : EventListener<QuerySnapshot>{
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore Error ", error.message.toString())
                    return
                }
                for (doc: DocumentChange in value?.documentChanges!!) {
                    if(doc.type == DocumentChange.Type.ADDED) {
                        val user = doc.document.toObject(User::class.java)
                        if (user?.userid != FirebaseSession.userID) {
                            var flag = 0
                            val query1 = db.collection("requests")
                                .whereEqualTo("userid", FirebaseSession.userID)
                                .whereEqualTo("friendid", user?.userid)
                                .whereIn("status", Arrays.asList("pending", "added"))

                            val query2 = db.collection("requests")
                                .whereEqualTo("userid", user?.userid)
                                .whereEqualTo("friendid", FirebaseSession.userID)
                                .whereIn("status", Arrays.asList("pending", "added"))

                            Tasks.whenAllSuccess<Any>(query1.get(Source.SERVER), query2.get(Source.SERVER))
                                .addOnSuccessListener { objects ->
                                     for (obj in objects) {
                                        val queryDocumentSnapshots = obj as QuerySnapshot
                                        for (documentSnapshot in queryDocumentSnapshots) {
//                                            val friendship = documentSnapshot.toObject(Friendship::class.java)
                                            flag = 1
                                            searches.add(Pair(user!!, 0))
                                        }
                                    }
                                    if(flag != 1) {
                                        searches.add(Pair(user!!, 1))
                                    }

                                    if(searches.size == 0)
                                        binding.emptysearchText.visibility = View.VISIBLE
                                    else
                                        binding.emptysearchText.visibility = View.GONE
                                    searchAdapter.notifyDataSetChanged()
                                }
                        }
                    }
                }
            }
        })
    }

    fun loadSearches(){
        searches.clear()
        val query = db.collection("users").
        whereEqualTo("firstname", binding.searchLayout.editText?.text.toString())
            .addSnapshotListener (object : EventListener<QuerySnapshot>{
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    Log.e("Firestore Error ", error.message.toString())
                    return
                }
                for (doc: DocumentChange in value?.documentChanges!!) {
                    if(doc.type == DocumentChange.Type.ADDED) {
                        val user = doc.document.toObject(User::class.java)
                        if (user.userid != FirebaseSession.userID) {
                            var flag = 0
                            val query1 = db.collection("requests")
                                .whereEqualTo("userid", FirebaseSession.userID)
                                .whereEqualTo("friendid", user.userid)
                                .whereIn("status", Arrays.asList("pending", "added"))

                            val query2 = db.collection("requests")
                                .whereEqualTo("userid", user.userid)
                                .whereEqualTo("friendid", FirebaseSession.userID)
                                .whereIn("status", Arrays.asList("pending", "added"))

                            Tasks.whenAllSuccess<Any>(query1.get(), query2.get())
                                .addOnSuccessListener { objects ->
                                    for (obj in objects) {
                                        val queryDocumentSnapshots = obj as QuerySnapshot
                                        for (documentSnapshot in queryDocumentSnapshots) {
//                                            val friendship = documentSnapshot.toObject(Friendship::class.java)
                                            flag = 1
                                            searches.add(Pair(user, 0))
                                        }
                                    }
                                    if(flag != 1) {
                                        searches.add(Pair(user, 1))
                                    }
                                    if(searches.size == 0)
                                        binding.emptysearchText.visibility = View.VISIBLE
                                    else
                                        binding.emptysearchText.visibility = View.GONE
                                    searchAdapter.notifyDataSetChanged()
                                }
                        }
                    }
                }
            }
        })
    }
    override fun onResume() {
        super.onResume()

//        if(searches.size == 0)
//            binding.emptysearchText.visibility = View.VISIBLE
//        else
//            binding.emptysearchText.visibility = View.GONE


//        loadRequests()

//        loadSuggestions()
    }

//    fun loadSearches(){
//        searches.clear()
//        requestsStatus.clear()
//        val query = db.collection("users").whereEqualTo("firstname",
//            binding.searchLayout.editText!!.text.toString())
//        query.get().addOnSuccessListener { collection ->
//            for(item in collection) {
//                val user = item.toObject(User::class.java)
//                user.password = null
//                if (user.userid != FirebaseSession.userID) {
//                    val innerquery = db.collection("requests")
//                        .whereEqualTo("userid", FirebaseSession.userID)
//                        .whereEqualTo("friendid", user.userid)
//                        .whereIn("status", Arrays.asList("pending", "added"))
//                    innerquery.get().addOnSuccessListener { friends ->
//                        //Will run only once
//                        if(friends.documents.size > 0){
//                            requestsStatus.add(0)
//                        }
//                        else{
//                            requestsStatus.add(1)
//                        }
//                        searches.add(user)
//                    }.addOnFailureListener {
//                        toast("Failed Search")
//                    }
//
//
//                    val query2 = db.collection("requests")
//                        .whereEqualTo("userid", user.userid)
//                        .whereEqualTo("friendid", FirebaseSession.userID)
//                        .whereIn("status", Arrays.asList("pending", "added"))
//                    query2.get().addOnSuccessListener { friends ->
//                        //Will run only once
//                        if (friends.documents.size > 0) {
//                            requestsStatus.add(0)
//                        } else {
//                            requestsStatus.add(1)
//                        }
//                        searches.add(user)
//                    }.addOnFailureListener {
//                        toast("Failed Search")
//                    }
//                }
//            }
//            searchRecyclerView = binding.searchRecycler
//            searchAdapter = SearchAdapter(searches, FirebaseSession.userID, requestsStatus)
//            searchRecyclerView.adapter = searchAdapter
//            searchRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
////            searchRecyclerView.adapter?.notifyDataSetChanged()
//        }.addOnFailureListener {
//            Toast.makeText(applicationContext, "Error fetching searches", Toast.LENGTH_SHORT).show()
//        }
//    }

//    fun loadSuggestions(){
//        searches.clear()
//        requestsStatus.clear()
//        val query = db.collection("users").limit(10)
//        query.get().addOnSuccessListener { collection ->
//            for (item in collection) {
//                val user = item.toObject(User::class.java)
//                user.password = null
//                if(user.userid != FirebaseSession.userID) {
//                    val query1 = db.collection("requests")
//                        .whereEqualTo("userid", FirebaseSession.userID)
//                        .whereEqualTo("friendid", user.userid)
//                        .whereIn("status", Arrays.asList("pending", "added"))
//                    query1.get().addOnSuccessListener { friends ->
//                        //Will run only once
//                        if (friends.documents.size > 0) {
//                            requestsStatus.add(0)
//                        } else {
//                            requestsStatus.add(1)
//                        }
//                        searches.add(user)
//                    }.addOnFailureListener {
//                        toast("Failed Search")
//                    }
//
//
//                    val query2 = db.collection("requests")
//                        .whereEqualTo("userid", user.userid)
//                        .whereEqualTo("friendid", FirebaseSession.userID)
//                        .whereIn("status", Arrays.asList("pending", "added"))
//                    query2.get().addOnSuccessListener { friends ->
//                        //Will run only once
//                        if (friends.documents.size > 0) {
//                            requestsStatus.add(0)
//                            searches.add(user)
//                        }
//
//                    }.addOnFailureListener {
//                        toast("Failed Search")
//                    }
//                }
//            }
//
//
//        }.addOnFailureListener {
//            Toast.makeText(applicationContext, "Error fetching Data", Toast.LENGTH_SHORT).show()
//        }
//    }

    fun loadRequests(){
        requestsIds.clear()
        requests.clear()
        val requestQuery = db.collection("requests").whereEqualTo("friendid", FirebaseSession.userID).
        whereEqualTo("status","pending")
        requestQuery.get().addOnSuccessListener { collection ->
            for (item in collection) {
                val friendship = item.toObject(Friendship::class.java)
                if (friendship.userid != FirebaseSession.userID) {
                    val result = friendship.userid?.let { db.collection("users").whereEqualTo("userid",it) }
                    result?.get()?.addOnSuccessListener { docs ->
                        for (user in docs) {
                            val requestuser = user.toObject(User::class.java)
                            requests.add(requestuser)
                            friendship.friendshipid?.let { requestsIds.add(it) }
                        }
                        requestRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(applicationContext, "Error fetching requests", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}