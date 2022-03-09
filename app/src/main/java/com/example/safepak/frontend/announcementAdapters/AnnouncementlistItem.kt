package com.example.safepak.frontend.announcementAdapters

import android.view.animation.AnimationUtils
import com.example.safepak.R
import com.example.safepak.logic.models.Call
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_profile.view.*
import kotlinx.android.synthetic.main.announcement_item.view.*

class AnnouncementlistItem(var call: Call): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.announcement_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.fall_down)


        when (call.type) {
            "level1" -> {
                viewHolder.itemView.announcement_icon.setImageResource(R.drawable.level1_ic)
                viewHolder.itemView.announcement_text.text = "A Person shared location"
                viewHolder.itemView.announcementtype_text.text = "Level-1"
            }
            "medical" -> {
                viewHolder.itemView.announcement_icon.setImageResource(R.drawable.medical_ic)
                viewHolder.itemView.announcement_text.text = "A Person needed blood"
                viewHolder.itemView.announcementtype_text.text = "Medical"
            }
            "level2" -> {
                viewHolder.itemView.announcement_icon.setImageResource(R.drawable.level2_ic)
                viewHolder.itemView.announcement_text.text = "A Person was in emergnecy"
                viewHolder.itemView.announcementtype_text.text = "Level-2"
            }
        }

        viewHolder.itemView.announcementdate_text.text = call.time
        viewHolder.itemView.announcementlocation_text.text = call.location

        viewHolder.itemView.announcement_icon.startAnimation(animation)
        viewHolder.itemView.announcement_text.startAnimation(animation)
        viewHolder.itemView.announcementtype_text.startAnimation(animation)
        viewHolder.itemView.announcementlocation_text.startAnimation(animation)
        viewHolder.itemView.announcementdate_text.startAnimation(animation)

    }
}