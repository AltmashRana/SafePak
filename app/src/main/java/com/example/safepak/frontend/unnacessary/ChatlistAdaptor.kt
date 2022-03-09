package com.example.safepak.frontend.unnacessary//package com.example.safepak.frontend.chatAdapters
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.safepak.R
//import com.example.safepak.data.User
//
//import android.content.Intent
//import com.example.safepak.frontend.chat.ChatBoxActivity
//
//
//class ChatlistAdaptor(private val users: ArrayList<User>, private val lasttexts: ArrayList<String>) :
//    RecyclerView.Adapter<ChatlistAdaptor.ViewHolder>() {
//    /**
//     * Provide a reference to the type of views that you are using
//     * (custom ViewHolder).
//     */
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val fullname: TextView = view.findViewById(R.id.name_text)
//        val text: TextView = view.findViewById(R.id.chatlist_text)
//        val status: ImageView = view.findViewById(R.id.onlinestatus_img)
//        val time: TextView = view.findViewById(R.id.chatlisttime_text)
//        val dp: ImageView = view.findViewById(R.id.chat_dp)
//        val seen: ImageView = view.findViewById(R.id.seen_img)
//
//        init {
//            view.setOnClickListener{
//                val position : Int = adapterPosition
//                val intent = Intent(view.context, ChatBoxActivity::class.java)
//                view.context.startActivity(intent)
//            }
//        }
//    }
//
//    // Create new views (invoked by the layout manager)
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        // Create a new view, which defines the UI of the list item
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.chatlist_item, viewGroup, false)
//
//        return ViewHolder(view)
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//        // Get element from your dataset at this position and replace the
//        // contents of the view with that element
//        viewHolder.fullname.text = users[position].firstname + " " + users[position].lastname
//        viewHolder.dp.setImageResource(R.drawable.dp)
//        viewHolder.text.text = lasttexts[position]
//        viewHolder.seen.setImageResource(R.drawable.delivered_ic)
//        viewHolder.status.setImageResource(R.drawable.offline_ic)
//        viewHolder.time.text = "10:30pm"
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    override fun getItemCount() = users.size
//
//}