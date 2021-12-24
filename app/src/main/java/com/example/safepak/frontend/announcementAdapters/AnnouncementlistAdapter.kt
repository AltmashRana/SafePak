package com.example.safepak.frontend.announcementAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.R
import com.example.safepak.logic.models.Call
import kotlin.collections.ArrayList

class AnnouncementlistAdapter(private val calls: ArrayList<Call>) :
RecyclerView.Adapter<AnnouncementlistAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.announcement_icon)
        val text: TextView = view.findViewById(R.id.announcement_text)
        val location: TextView = view.findViewById(R.id.announcementlocation_text)
        var type: TextView = view.findViewById(R.id.announcementtype_text)
        val date: TextView = view.findViewById(R.id.announcementdate_text)

        init {
            view.setOnClickListener{
                val position : Int = adapterPosition
                Toast.makeText(view.context, "You cannot do anything about!" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.announcement_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.location.text = calls[position].location
        when(calls[position].type){
            1 -> {viewHolder.text.text = "Person is feeling unsafe"
                viewHolder.icon.setImageResource(R.drawable.level1_ic)
                viewHolder.type.text = "Level-1"}
            2 -> {viewHolder.text.text = "Person needs medical help"
                viewHolder.icon.setImageResource(R.drawable.level2_ic)
                viewHolder.type.text = "Medical"}
            3 -> {viewHolder.text.text = "Person needs critical help"
                viewHolder.icon.setImageResource(R.drawable.medical_ic)
                viewHolder.type.text = "Level-2"}
        }
        viewHolder.date.text = calls[position].date.toString()

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = calls.size

}