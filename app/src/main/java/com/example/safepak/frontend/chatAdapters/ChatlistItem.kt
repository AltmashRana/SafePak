package com.example.safepak.frontend.chatAdapters

import android.graphics.Typeface
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Message
import com.example.safepak.logic.session.StorageSession
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chatlist_item.view.*

class ChatlistItem(var message: Message, var flag : Boolean): Item<GroupieViewHolder>() {
    var user : User? = null
    var friend_flag : Boolean? = null

    override fun getLayout(): Int {
       return R.layout.chatlist_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.fall_down)
        val chatPartnerId: String

        chatPartnerId = if (message.senderid == FirebaseSession.userID)
            message.receiverid.toString()
        else
            message.senderid.toString()


        val get_user = FirebaseFirestore.getInstance().collection("users").document(chatPartnerId)
        get_user.get().addOnSuccessListener { doc ->

            user = doc.toObject(User::class.java)!!

            FirebaseDatabase.getInstance().getReference("/friends/${FirebaseSession.userID}/${user?.userid}")
                .get().addOnSuccessListener { friend ->
                    if (friend.children.count() > 0) {
                            friend_flag = true
                            Glide.with(viewHolder.itemView)
                                .load(user?.img?.let { StorageSession.pathToReference(it) })
                                .placeholder(R.drawable.empty_dp)
                                .into(viewHolder.itemView.chatlist_dp)

                            if (user?.status?.get("state") == "online")
                                viewHolder.itemView.chatlistonlinestatus_img.setImageResource(R.drawable.online_ic)
                            else
                                viewHolder.itemView.chatlistonlinestatus_img.setImageResource(R.drawable.offline_ic)
                        }
                        else
                            friend_flag = false
                    }
            viewHolder.itemView.chatlistname_text.text = "${user?.firstname} ${user?.lastname}"

            if (message.text.equals("")) {
                viewHolder.itemView.chatlist_text.text = "Send your first text"
                viewHolder.itemView.chatlist_text.setTypeface(null, Typeface.BOLD)
                viewHolder.itemView.chatlisttime_text.text = "00:00"
            } else {
                viewHolder.itemView.chatlist_text.text = message.text!!.split("\n")[0]
                viewHolder.itemView.chatlisttime_text.text =
                    message.time?.split(',')?.get(1)?.trim()
            }

            if (message.senderid.equals(FirebaseSession.userID) && !message.text.equals(""))
                viewHolder.itemView.chatlistseen_img.setImageResource(R.drawable.delivered_ic)
            else {
                viewHolder.itemView.chatlist_text.setTypeface(null, Typeface.BOLD)
                viewHolder.itemView.chatlistseen_img.setImageResource(R.drawable.empty)
            }

            if (flag) {
                viewHolder.itemView.chatlist_dp.startAnimation(animation)
                viewHolder.itemView.chatlist_text.startAnimation(animation)
                viewHolder.itemView.chatlistseen_img.startAnimation(animation)
                viewHolder.itemView.chatlistname_text.startAnimation(animation)
                viewHolder.itemView.chatlistonlinestatus_img.startAnimation(animation)
            }
        }
    }
}