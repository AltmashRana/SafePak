package com.example.safepak.frontend.chatAdapters

import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.session.StorageSession
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder

abstract class ChatlistItem(val user: User): Item() {
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        viewHolder.chat
//
//        Glide.with(viewHolder.itemView)
//            .load(user.img?.let { StorageSession.pathToReference(it) })
//            .placeholder(R.drawable.empty_dp)
//            .into(viewHolder.dp)
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.chatlist_item
//    }
}