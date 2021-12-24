package com.example.safepak.frontend.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.frontend.chatAdapters.ChatlistAdaptor
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentChatsBinding
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentChatsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)

        binding.addfriendFab.setOnClickListener {
            val intent = Intent(activity, AddFriendActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onStart() {
        super.onStart()

        val fnames = arrayOf("Amina","Isha", "Ayesha", "Mariam", "Anaya","ABC")
        val lnames = arrayOf("Amina","Isha", "Ayesha", "Mariam", "Anaya","ABC")
        lnames.reverse()
        val users: ArrayList<User> = ArrayList()
        val messages: ArrayList<String> = ArrayList()

        for (i in fnames.indices)
        {
            users.add(User("0",fnames[i],lnames[i],"null","null","090078601",null,null,null,null,null))
            messages.add("Thid is ${fnames[i]}'s message")
        }
        val recyclerView: RecyclerView = binding.chatsRecycler
        val adapter = ChatlistAdaptor(users, messages)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        if (users.size == 0)
            binding.emptychatsText.visibility = View.VISIBLE
    }
//    companion object {
//        val USER_KEY = "USER_KEY"
//    }
//
//    private fun loadUsers() {
//        val ref = FirebaseDatabase.getInstance().getReference("/users")
//        ref.addListenerForSingleValueEvent(object: ValueEventListener {
//
//            override fun onDataChange(p0: DataSnapshot) {
//                val adapter = GroupAdapter<ViewHolder>()
//
//                p0.children.forEach {
//                    Log.d("NewMessage", it.toString())
//                    val user = it.getValue(User::class.java)
//                    if (user != null) {
//                        adapter.add(UserItem(user))
//                    }
//                }
//
//                adapter.setOnItemClickListener { item, view ->
//
//                    val userItem = item as UserItem
//
//                    val intent = Intent(view.context, ChatLogActivity::class.java)
////          intent.putExtra(USER_KEY,  userItem.user.username)
//                    intent.putExtra(USER_KEY, userItem.user)
//                    startActivity(intent)
//
//                    finish()
//
//                }
//
//                recyclerview_newmessage.adapter = adapter
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//
//            }
//        }
//    }

companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}