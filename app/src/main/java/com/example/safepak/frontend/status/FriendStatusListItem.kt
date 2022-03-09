package com.example.safepak.frontend.status

import android.annotation.SuppressLint
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.models.Status
import com.example.safepak.logic.session.StorageSession
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chatlist_item.view.*
import kotlinx.android.synthetic.main.status_item.view.*

class FriendStatusListItem (var status: Status, var user: User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.status_item
    }

    @SuppressLint("RestrictedApi")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.lefttoright)

        viewHolder.itemView.statusname_text.text = user.firstname+" "+user.lastname
        viewHolder.itemView.content_text.text = status.content
        viewHolder.itemView.statusdate_text.text = status.date

        Glide.with(viewHolder.itemView)
            .load(user.img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.itemView.status_dp)

        viewHolder.itemView.startAnimation(animation)

    }
}