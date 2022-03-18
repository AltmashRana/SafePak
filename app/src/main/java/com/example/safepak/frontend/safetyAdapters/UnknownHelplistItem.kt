package com.example.safepak.frontend.safetyAdapters

import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Call
import com.example.safepak.logic.session.StorageSession
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.help_item.view.*

class UnknownHelplistItem(var data: Pair<Call, Pair<User, Boolean>>): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.unknownhelp_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.righttoleft)
        viewHolder.itemView.startAnimation(animation)
    }
}