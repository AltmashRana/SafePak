package com.example.safepak.frontend.statusAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.R
import com.example.safepak.logic.models.Status
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.ArrayList

class MyStatusListAdapter(private val status_list: ArrayList<Status>) :
    RecyclerView.Adapter<MyStatusListAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    lateinit var db : FirebaseFirestore
    lateinit var animation : Animation

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        val content: TextView = view.findViewById(R.id.mycontent_text)
        val date: TextView = view.findViewById(R.id.mystatusdate_text)
        val delete: ImageView = view.findViewById(R.id.mystatusdelete_bt)

        init {
            delete.setOnClickListener{
                val position : Int = adapterPosition
                db = FirebaseFirestore.getInstance()

                val query = db.collection("statuses").document(status_list[position].statusid!!)
                query.delete().addOnCompleteListener {
                    Toast.makeText(view.context, "Deleted", Toast.LENGTH_SHORT).show()
                    status_list.removeAt(position)
                    notifyItemRemoved(position)
                }.addOnFailureListener {
                    Toast.makeText(view.context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.mystatus_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.slide_down)
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.content.text = status_list[position].content
        viewHolder.date.text = status_list[position].date

        viewHolder.content.startAnimation(animation)
        viewHolder.date.startAnimation(animation)



    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = status_list.size

}