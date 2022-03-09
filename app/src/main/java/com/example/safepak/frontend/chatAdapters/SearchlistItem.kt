package com.example.safepak.frontend.chatAdapters


import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.session.StorageSession
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.searchlist_item.view.*

class SearchlistItem(var user: User, var flag : Int): Item<GroupieViewHolder>() {

    private lateinit var db : FirebaseDatabase

    override fun getLayout(): Int {
        return R.layout.searchlist_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation1 = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.lefttoright)
        val animation2 = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.righttoleft)

        when(flag) {
            1 -> {
                viewHolder.itemView.searchaddfriend_bt.isEnabled = false
                viewHolder.itemView.searchaddfriend_bt.setImageResource(R.drawable.requestsent_ic)
            }
            2 -> {
                viewHolder.itemView.searchaddfriend_bt.isEnabled = false
                viewHolder.itemView.searchaddfriend_bt.setImageResource(R.drawable.sent_ic)

            }
        }
        viewHolder.itemView.searchname_text.text = "${user.firstname} ${user.lastname}"
        viewHolder.itemView.searchemail_text.text = user.email ?: "No email"

        Glide.with(viewHolder.itemView)
            .load(user.img?.let {StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.itemView.search_dp)


        viewHolder.itemView.search_dp.startAnimation(animation1)
        viewHolder.itemView.searchaddfriend_bt.startAnimation(animation2)

        viewHolder.itemView.searchaddfriend_bt.setOnClickListener {
            db = FirebaseDatabase.getInstance()

            val id = db.getReference("/friend-requests/${FirebaseSession.userID!!}/${user.userid}").push().key
            val request = Friendship(id, FirebaseSession.userID, user.userid,"pending")

            val query = db.getReference("/friend-requests/${FirebaseSession.userID!!}/${user.userid}")
                .get().addOnSuccessListener { doc ->
                if(doc.childrenCount == 0L) {

                    db.getReference("/friend-requests/${user.userid}/${FirebaseSession.userID}")
                    .setValue(request).addOnSuccessListener {

                        Toast.makeText(viewHolder.itemView.context, "Friend request sent to ${user.firstname}", Toast.LENGTH_SHORT).show()
                        viewHolder.itemView.searchaddfriend_bt.isEnabled = false
                        viewHolder.itemView.searchaddfriend_bt.setImageResource(R.drawable.requestsent_ic)

                    }.addOnFailureListener {
                        Toast.makeText(viewHolder.itemView.context, "Not Sent", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                    Toast.makeText(viewHolder.itemView.context, "Already in pending list", Toast.LENGTH_SHORT).show()
            }
        }
    }
}