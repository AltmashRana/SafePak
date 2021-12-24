package com.example.safepak.frontend.chatAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.safepak.R
import com.example.safepak.data.User
import com.example.safepak.logic.models.Friendship
import com.example.safepak.logic.session.StorageSession
import com.google.firebase.firestore.FirebaseFirestore

class RequestlistAdapter(private val users: ArrayList<User>, userID: String, private val requestids: ArrayList<String>) :
    RecyclerView.Adapter<RequestlistAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    lateinit var db : FirebaseFirestore

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.requestname_text)
        var contact: TextView = view.findViewById(R.id.requestcontact_text)
        val dp: ImageView = view.findViewById(R.id.request_dp)
        val accept: ImageView = view.findViewById(R.id.accept_bt)
        val decline: ImageView = view.findViewById(R.id.decline_bt)

        init {
            accept.setOnClickListener {
                val position: Int = adapterPosition
                db = FirebaseFirestore.getInstance()

                val query = db.collection("requests").document(requestids[position])
                query.update(
                    mapOf(
                        "status" to "added"
                    )
                )
                    .addOnCompleteListener {
                        Toast.makeText(
                            view.context,
                            "${users[position].firstname} Added",
                            Toast.LENGTH_SHORT
                        ).show()
                        users.removeAt(position)
                        notifyItemRemoved(position)
                    }.addOnFailureListener {
                        Toast.makeText(view.context, "Failed", Toast.LENGTH_SHORT).show()
                    }

            }

            decline.setOnClickListener {
                val position: Int = adapterPosition
                db = FirebaseFirestore.getInstance()


                val query = db.collection("requests").document(requestids[position])
                query.delete().addOnCompleteListener {
                    Toast.makeText(view.context, "Request removed", Toast.LENGTH_SHORT).show()
                    users.removeAt(position)
                    notifyItemRemoved(position)
                }.addOnFailureListener {
                    Toast.makeText(view.context, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.requestlist_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.name.text = "${users[position].firstname} ${users[position].lastname}"

        Glide.with(viewHolder.itemView)
            .load(users[position].img?.let { StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.dp)
        viewHolder.contact.text  = users[position].phone

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = users.size

}