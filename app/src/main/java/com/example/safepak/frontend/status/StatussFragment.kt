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
import com.example.safepak.logic.models.Friendship
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.util.*
import kotlin.collections.ArrayList


class StatusFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    lateinit var myStatusAdapter : GroupAdapter<GroupieViewHolder>
    lateinit var friendStatusAdapter : GroupAdapter<GroupieViewHolder>

    lateinit var binding: FragmentStatusBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()

        binding.addstatusFab.setOnClickListener {
            val intent = Intent(activity, AddStatusActivity::class.java)
            startActivity(intent)
        }

        binding.refreshstatusSwipe.setOnRefreshListener {
            loadStatuses()

            Toast.makeText(context, "Timeline refreshed", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myStatusAdapter= GroupAdapter<GroupieViewHolder>()
        friendStatusAdapter= GroupAdapter<GroupieViewHolder>()
        binding.mystatusRecycler.adapter=myStatusAdapter
        binding.statusRecycler.adapter=friendStatusAdapter
    }


    override fun onStart() {
        super.onStart()

        binding.emptyMystatusText.visibility = View.VISIBLE
        binding.emptystatusText.visibility = View.VISIBLE

        loadStatuses()
        loadMyStatuses()
    }

    fun loadMyStatuses() {
        myStatusAdapter.clear()
        val query = db.collection("statuses").whereEqualTo("userid", FirebaseSession.userID)
        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val doc = document.toObject(Status::class.java)
                myStatusAdapter.add(MyStatusListItem(doc, myStatusAdapter))
                binding.emptyMystatusText.visibility = View.GONE
            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(view?.context, "Failed to load", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadStatuses() {
        friendStatusAdapter.clear()
            FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}")
                .get().addOnSuccessListener { docs ->
                    docs.children.forEach{

                        val friendship = it.getValue(Friendship::class.java)

                        db.collection("users").document(friendship?.friendid!!)
                            .get().addOnSuccessListener { data ->
                                val user = data.toObject(User::class.java)

                                db.collection("statuses")
                                    .whereEqualTo("userid", user?.userid)
                                    .get().addOnSuccessListener { statuslist ->
                                        for (status in statuslist) {
                                            val status = status.toObject(Status::class.java)
                                            friendStatusAdapter.add(FriendStatusListItem(status, user!!))
                                            binding.emptystatusText.visibility = View.GONE
                                        }
                                    }
                            }
                    }
                    binding.refreshstatusSwipe.isRefreshing = false
                }
    }
}