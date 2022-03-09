package com.example.safepak.frontend.chatAdapters

import android.view.animation.AnimationUtils
import com.example.safepak.R
import com.example.safepak.logic.models.Message
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.sendmsg_item.view.*

class SendItem(var message: Message): Item<GroupieViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.sendmsg_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.righttoleft)

        viewHolder.itemView.sendmsg_text.text = message.text
        if (message.text?.length!! > 14)
            viewHolder.itemView.sendtime_text.text = message.time
        else {
            val time = message.time?.split(",")
            viewHolder.itemView.sendtime_text.text = time?.get(1)?.trim()
        }

        viewHolder.itemView.sendmsg_text.startAnimation(animation)
        viewHolder.itemView.sendtime_text.startAnimation(animation)
        viewHolder.itemView.send_status.startAnimation(animation)
    }
}