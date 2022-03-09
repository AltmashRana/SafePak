package com.example.safepak.frontend.chatAdapters

import android.view.animation.AnimationUtils
import com.example.safepak.R
import com.example.safepak.logic.models.Message
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.receivemsg_item.view.*
import kotlinx.android.synthetic.main.sendmsg_item.view.*

class ReceiveItem(var message: Message): Item<GroupieViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.receivemsg_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.lefttoright)
        viewHolder.itemView.receivemsg_text.text = message.text
        if(message.text?.length!! > 14)
            viewHolder.itemView.receivetime_text.text = message.time
        else {
            val time = message.time?.split(",")
            viewHolder.itemView.receivetime_text.text = time?.get(1)?.trim()
        }

        viewHolder.itemView.receivetime_text.startAnimation(animation)
        viewHolder.itemView.receivemsg_text.startAnimation(animation)
    }
}