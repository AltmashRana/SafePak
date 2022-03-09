package com.example.safepak.frontend.safetyAdapters

import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.session.StorageSession
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.closelist_item.view.*

class CloselistItem(var user: User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.closelist_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.slide_down)

        viewHolder.itemView.closename_text.text = user.firstname
        Glide.with(viewHolder.itemView)
            .load(user.img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.itemView.close_dp)


        viewHolder.itemView.startAnimation(animation)

    }
}