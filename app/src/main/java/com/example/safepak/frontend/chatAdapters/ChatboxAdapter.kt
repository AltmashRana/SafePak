package com.example.safepak.frontend.chatAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.R
import com.example.safepak.logic.models.Message

class ChatboxAdapter(private val messages: ArrayList<Message>, private val senderID: Int) :
    RecyclerView.Adapter<ChatboxAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    companion object {
        final val VIEW_TYPE_SENT = 1
        final val VIEW_TYPE_RECEIVED = 2
    }
        inner class ViewHolder(view: View, viewType: Int) : RecyclerView.ViewHolder(view) {
           lateinit var sendtext: TextView
           lateinit var sendtime: TextView
           lateinit var textstatus: ImageView

           lateinit var receivetext: TextView
           lateinit var receivetime: TextView

            init {
                if (viewType == VIEW_TYPE_SENT)
                {
                    sendtext = view.findViewById(R.id.sendmsg_text)
                    sendtime= view.findViewById(R.id.sendtime_text)
                    textstatus = view.findViewById(R.id.send_status)
                }
                else{
                    receivetext = view.findViewById(R.id.receivemsg_text)
                    receivetime= view.findViewById(R.id.receivetime_text)
                }

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        if (viewType == VIEW_TYPE_SENT) {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sendmsg_item, viewGroup, false)

        return ViewHolder(view,viewType)
        } else {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.receivemsg_item, viewGroup, false)

            return ViewHolder(view, viewType)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            viewHolder.sendtext.text = messages[position].text
            viewHolder.sendtime.text = messages[position].time
            viewHolder.textstatus.setImageResource(R.drawable.delivered_ic)

        } else{
            viewHolder.receivetext.text = messages[position].text
            viewHolder.receivetime.text = messages[position].time
            }
        }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
       if(messages[position].senderid.equals(senderID))
           return VIEW_TYPE_SENT
       else
           return VIEW_TYPE_RECEIVED
    }
}