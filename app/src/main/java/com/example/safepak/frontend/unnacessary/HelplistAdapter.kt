//package com.example.safepak.frontend.unnacessary
//
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import com.example.safepak.R
//import com.example.safepak.data.User
//import com.example.safepak.frontend.maps.LocationActivity
//
//class HelplistAdapter(private val users: ArrayList<User>) :
//    RecyclerView.Adapter<HelplistAdapter.ViewHolder>() {
//    /**
//     * Provide a reference to the type of views that you are using
//     * (custom ViewHolder).
//     */
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val dp: ImageView = view.findViewById(R.id.help_dp)
//        val helptext: TextView = view.findViewById(R.id.help_text)
//        val helpicon: ImageView = view.findViewById(R.id.help_icon)
//
//
//        init {
//            view.setOnClickListener{
//                val position : Int = adapterPosition
//                Toast.makeText(view.context, "${users[position].firstname} needs your help!", Toast.LENGTH_SHORT).show()
//                val intent = Intent(view.context, LocationActivity::class.java)
//                view.context.startActivity(intent)
//            }
//        }
//    }
//
//    // Create new views (invoked by the layout manager)
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        // Create a new view, which defines the UI of the list item
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.help_item, viewGroup, false)
//
//        return ViewHolder(view)
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//        // Get element from your dataset at this position and replace the
//        // contents of the view with that element
//
//        viewHolder.helptext.text = "${users[position].firstname} needs help"
//        viewHolder.dp.setImageResource(R.drawable.dp2)
//        viewHolder.helpicon.setImageResource(R.drawable.level1_ic)
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    override fun getItemCount() = users.size
//
//}