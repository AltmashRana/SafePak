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
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Status
import com.example.safepak.logic.session.StorageSession

class StatusListAdapter(private val status_list: ArrayList<Pair<User, Status>>) :
    RecyclerView.Adapter<StatusListAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    lateinit var animation : Animation
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.content_text)
        val dp: ImageView = view.findViewById(R.id.status_dp)
        val name: TextView = view.findViewById(R.id.statusname_text)
        val date: TextView = view.findViewById(R.id.addstatusdate_text)

        init{
            view.setOnClickListener{
                val position : Int = adapterPosition
                Toast.makeText(view.context, "This is ${status_list[position].first.firstname}'s post!", Toast.LENGTH_SHORT).show()
            }
    }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.status_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        animation = AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.lefttoright)
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.name.text = status_list[position].first.firstname + " " + status_list[position].first.lastname

        Glide.with(viewHolder.itemView)
            .load(status_list[position].first.img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.dp)

        viewHolder.content.text = status_list[position].second.content
        viewHolder.date.text = status_list[position].second.date

        viewHolder.content.startAnimation(animation)
        viewHolder.date.startAnimation(animation)
        viewHolder.dp.startAnimation(animation)
        viewHolder.name.startAnimation(animation)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = status_list.size

}