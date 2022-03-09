package com.example.safepak.frontend.chatAdapters

import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.models.Message
import com.example.safepak.logic.session.StorageSession
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.requestlist_item.view.*
import kotlinx.android.synthetic.main.searchlist_item.view.*

class RequestlistItem(var user: User, var friendship: Friendship, val adapter : GroupAdapter<GroupieViewHolder>): Item<GroupieViewHolder>() {

    private lateinit var db : FirebaseDatabase

    override fun getLayout(): Int {
        return R.layout.requestlist_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.slide_down)

        viewHolder.itemView.requestname_text.text = "${user.firstname} ${user.lastname}"
        viewHolder.itemView.requestcontact_text.text = user.phone

        Glide.with(viewHolder.itemView)
            .load(user.img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.itemView.request_dp)

        viewHolder.itemView.requestname_text.startAnimation(animation)
        viewHolder.itemView.requestcontact_text.startAnimation(animation)
        viewHolder.itemView.requestaccept_bt.startAnimation(animation)
        viewHolder.itemView.requestdecline_bt.startAnimation(animation)
        viewHolder.itemView.requestcontact_text.startAnimation(animation)

        viewHolder.itemView.requestaccept_bt.setOnClickListener {
            db = FirebaseDatabase.getInstance()

            db.getReference("/friend-requests/${FirebaseSession.userID!!}/${user.userid}")
                .removeValue().addOnSuccessListener {

                val id = db.getReference("/friends/${FirebaseSession.userID}/${user.userid}").push().key

                val my_friend = Friendship(id, FirebaseSession.userID, user.userid,"added")

                    db.getReference("/friends/${FirebaseSession.userID}/${user.userid}")
                        .setValue(my_friend)

                val friend = Friendship(id,  user.userid, FirebaseSession.userID,"added")
                    db.getReference("/friends/${user.userid}/${FirebaseSession.userID}")
                        .setValue(friend)



                val message1 = Message("", user.userid,FirebaseSession.userID, "", "", "-1", "")
                val create_latestsend = FirebaseDatabase.getInstance().getReference("/latest-messages/${FirebaseSession.userID}/${user.userid}")
                    .child(FirebaseSession.userID + user.userid).setValue(message1)

                val message2 = Message("", user.userid, FirebaseSession.userID, "", "-1", "","")
                val create_latestreceive = FirebaseDatabase.getInstance().getReference("/latest-messages/${user.userid}/${FirebaseSession.userID}")
                    .child(user.userid + FirebaseSession.userID).setValue(message2)

                Toast.makeText(viewHolder.itemView.context, "${user.firstname} Added", Toast.LENGTH_SHORT).show()

                adapter.removeGroupAtAdapterPosition(position)

            }.addOnFailureListener {
                Toast.makeText(viewHolder.itemView.context, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewHolder.itemView.requestdecline_bt.setOnClickListener {
            db = FirebaseDatabase.getInstance()

            db.getReference("/friend-requests/${FirebaseSession.userID!!}/${user.userid}")
                .removeValue().addOnSuccessListener {

                Toast.makeText(viewHolder.itemView.context, "Request removed", Toast.LENGTH_SHORT).show()
                adapter.removeGroupAtAdapterPosition(position)
            }.addOnFailureListener {
                Toast.makeText(viewHolder.itemView.context, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}