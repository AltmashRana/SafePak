package com.example.safepak.frontend.status

import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.safepak.R
import com.example.safepak.logic.models.Status
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.mystatus_item.view.*

class MyStatusListItem (var status: Status,  val adapter : GroupAdapter<GroupieViewHolder>): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.mystatus_item
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.fall_down)

        viewHolder.itemView.mycontent_text.text = status.content
        viewHolder.itemView.mystatusdate_text.text = status.date

        viewHolder.itemView.mystatusdelete_bt.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val query = db.collection("statuses").document(status.statusid!!)
            query.delete().addOnCompleteListener {
                Toast.makeText(viewHolder.itemView.context, "Deleted", Toast.LENGTH_SHORT).show()
                adapter.removeGroupAtAdapterPosition(position)
            }.addOnFailureListener {
                Toast.makeText(viewHolder.itemView.context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }

        viewHolder.itemView.startAnimation(animation)

    }
}