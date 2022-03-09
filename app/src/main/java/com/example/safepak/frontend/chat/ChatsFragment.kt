package com.example.safepak.frontend.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentChatsBinding
import com.example.safepak.frontend.chatAdapters.ChatlistItem
import com.example.safepak.frontend.chatAdapters.ReceiveItem
import com.example.safepak.frontend.chatAdapters.SendItem
import com.example.safepak.logic.models.Message
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import java.util.*



class ChatsFragment : Fragment() {

    lateinit var db: FirebaseFirestore
    private val latestMessagesMap = HashMap<String, Message>()
    private var adapter = GroupAdapter<GroupieViewHolder>()

    lateinit var binding: FragmentChatsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emptychatsText.visibility = View.VISIBLE

        loadUsers()

        db = FirebaseFirestore.getInstance()

        binding.addfriendFab.setOnClickListener {
            val intent = Intent(activity, AddFriendActivity::class.java)
            startActivity(intent)
        }

        binding.refreshchatsSwipe.setOnRefreshListener {
            loadUsers()
        }

    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun refreshRecyclerViewMessages(flag : Boolean) {
        adapter.clear()
        latestMessagesMap.values.forEach {
            val chat = ChatlistItem(it, flag)
            adapter.add(chat)
        }
    }

    private fun loadUsers() {
        val fromId = FirebaseSession.userID
        val latest_query = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        latest_query.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                p0.children.forEach {
                    val message = it.getValue(Message::class.java)
                    latestMessagesMap[it.key.toString()] = message!!
                    refreshRecyclerViewMessages(true)
                    binding.emptychatsText.visibility = View.GONE
                }
                binding.refreshchatsSwipe.isRefreshing = false
            }

            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                p0.children.forEach {
                    val message = it.getValue(Message::class.java)
                    latestMessagesMap[it.key.toString()] = message!!
                    refreshRecyclerViewMessages(false)
                    binding.emptychatsText.visibility = View.GONE
                }
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {
                p0.children.forEach {
                    val message = it.getValue(Message::class.java)
                    latestMessagesMap.remove(it.key.toString())
                    refreshRecyclerViewMessages(true)
                    binding.emptychatsText.visibility = View.GONE
                }
            }

        })

        adapter.setOnItemClickListener { item, view ->
            val userItem = item as ChatlistItem
            val intent = Intent(view.context, ChatBoxActivity::class.java)
            if (userItem.friend_flag != null) {
                intent.putExtra(USER_KEY, userItem.user)
                intent.putExtra("IS_FRIEND", userItem.friend_flag)
                startActivity(intent)
            }
        }

        binding.chatsRecycler.adapter = adapter
    }

}