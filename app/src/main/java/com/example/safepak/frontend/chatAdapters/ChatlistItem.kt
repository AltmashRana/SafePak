package com.example.safepak.frontend.chatAdapters

import com.example.safepak.data.User
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder


abstract class ChatlistItem(val user: User): Item<GroupieViewHolder>() {
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        viewHolder.itemView.username_textview_new_message.text = user.username
//
//        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_message)
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.user_row_new_message
//    }
}