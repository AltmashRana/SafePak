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
import com.google.firebase.firestore.Source

class SearchAdapter(private val users: ArrayList<Pair<User, Int>>, private val userid: String?) :
RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    lateinit var db : FirebaseFirestore

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.searchname_text)
        var email: TextView = view.findViewById(R.id.searchemail_text)
        val dp: ImageView = view.findViewById(R.id.search_dp)
        val add: ImageView = view.findViewById(R.id.addfriend_bt)

        init {
            add.setOnClickListener{
                val position : Int = adapterPosition
                db = FirebaseFirestore.getInstance()

                val id = db.collection("requests").document().id
                val request = Friendship(id, userid, users[position].first.userid,"pending")

                val query = db.collection("requests").whereEqualTo("userid",users[position].first.userid)
                    .whereEqualTo("friendid",userid )
                query.get(Source.SERVER).addOnSuccessListener { doc ->
                    if(doc.documents.size == 0) {
                        val query1 = db.collection("requests").document(id)
                        query1.set(request).addOnSuccessListener {
                            Toast.makeText(view.context, "Friend request sent to ${users[position].first.firstname}", Toast.LENGTH_SHORT).show()
                            add.isEnabled = false
                            add.setImageResource(R.drawable.sent_ic)
                        }.addOnFailureListener {
                            Toast.makeText(view.context, "Not Sent", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else
                        Toast.makeText(view.context, "Already in pending list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.searchlist_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        if(users[position].second == 0) {
            viewHolder.add.isEnabled = false
            viewHolder.add.setImageResource(R.drawable.sent_ic)
        }
        viewHolder.name.text = "${users[position].first.firstname} ${users[position].first.lastname}"
        viewHolder.email.text = users[position].first.email ?: "No email"

        Glide.with(viewHolder.itemView)
            .load(users[position].first.img?.let {StorageSession.pathToReference(it) })
            .placeholder(R.drawable.empty_dp)
            .into(viewHolder.dp)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = users.size

}