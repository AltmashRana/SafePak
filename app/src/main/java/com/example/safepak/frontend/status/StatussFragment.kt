package com.example.safepak.frontend.status

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentStatusBinding
import com.example.safepak.logic.models.Status
import com.example.safepak.frontend.statusAdapters.MyStatusListAdapter
import com.example.safepak.frontend.statusAdapters.StatusListAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var statuses: ArrayList<Pair<User, Status>>
    private lateinit var  mystatuses: ArrayList<Status>
    private lateinit var db : FirebaseFirestore
    private lateinit var myadapter : MyStatusListAdapter
    private lateinit var myrecyclerView: RecyclerView
    lateinit var recyclerView: RecyclerView
    lateinit var adapter : StatusListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentStatusBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatusBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()
        statuses = ArrayList()
        mystatuses = ArrayList()

        binding.addstatusFab.setOnClickListener {
            val intent = Intent(activity,AddStatusActivity::class.java)
            startActivity(intent)
        }

        binding.refreshBt.setOnClickListener {
            loadStatuses()
            Toast.makeText(context, "Timeline refreshed", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myrecyclerView = binding.mystatusRecycler
        myadapter = MyStatusListAdapter(mystatuses)
        myrecyclerView.adapter = myadapter
        myrecyclerView.layoutManager = LinearLayoutManager(this.context)


        recyclerView = binding.statusRecycler
        adapter = StatusListAdapter(statuses)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)

    }


    override fun onResume() {
        super.onResume()

        loadStatuses()
        loadMyStatuses()
//        if (statuses.size == 0)
//            binding.emptyMystatusText.visibility = View.VISIBLE
//        else
//            binding.emptyMystatusText.visibility = View.GONE
//
//        if (mystatuses.size == 0)
//            binding.emptystatusText.visibility = View.VISIBLE
//        else
//            binding.emptyMystatusText.visibility = View.GONE
    }

    fun loadMyStatuses(){
        mystatuses.clear()
        val query = db.collection("statuses")
            .whereEqualTo("userid", FirebaseSession.userID)
        query.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val doc = document.toObject(Status::class.java)
                    mystatuses.add(doc)
                }
            myadapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Toast.makeText(view?.context,"Failed to load", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadStatuses(){
        statuses.clear()

        val query = db.collection("users").
        addSnapshotListener (object : EventListener<QuerySnapshot> {
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
                            val query1 = db.collection("requests")
                                .whereEqualTo("userid", FirebaseSession.userID)
                                .whereEqualTo("friendid", user.userid)
                                .whereEqualTo("status", "added")

                            val query2 = db.collection("requests")
                                .whereEqualTo("userid", user.userid)
                                .whereEqualTo("friendid", FirebaseSession.userID)
                                .whereEqualTo("status", "added")

                            Tasks.whenAllSuccess<Any>(
                                query1.get(),
                                query2.get())
                                .addOnSuccessListener { objects ->
                                    for (obj in objects) {
                                        val queryDocumentSnapshots = obj as QuerySnapshot
                                        for (documentSnapshot in queryDocumentSnapshots) {
//                                            val friendship = documentSnapshot.toObject(Friendship::class.java)
                                            db.collection("statuses")
                                                .whereEqualTo("userid", user.userid)
                                                .get().addOnSuccessListener { statuslist ->
                                                    for (status in statuslist) {
                                                        val store = status.toObject(Status::class.java)
                                                        statuses.add(Pair(user, store))
                                                    }
                                                    adapter.notifyDataSetChanged()
                                                }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StoriesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StatusFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}