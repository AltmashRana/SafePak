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

class HelplistItem(var data: Pair<User, Call>): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.help_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.righttoleft)
        when (data.second.type) {
            "level1" -> {
                viewHolder.itemView.help_icon.setImageResource(R.drawable.level1_ic)
                viewHolder.itemView.help_text.text = "${data.first.firstname} is sharing location"
            }
            "medical" -> {
                viewHolder.itemView.help_icon.setImageResource(R.drawable.medical_ic)
                viewHolder.itemView.help_text.text = "${data.first.firstname} needs blood"
            }
            "level2" -> {
                    viewHolder.itemView.help_icon.setImageResource(R.drawable.level2_ic)
                    viewHolder.itemView.help_text.text = "${data.first.firstname} is in emergency"
            }
        }
        Glide.with(viewHolder.itemView)
            .load(data.first.img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into( viewHolder.itemView.help_dp)

        viewHolder.itemView.startAnimation(animation)

    }
}