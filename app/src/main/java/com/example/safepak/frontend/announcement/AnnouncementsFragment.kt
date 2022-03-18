package com.example.safepak.frontend.announcement

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.databinding.FragmentAnnouncementsBinding
import com.example.safepak.frontend.announcementAdapters.AnnouncementlistItem
import com.example.safepak.logic.models.Call
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.announcement_item.*
import java.util.*
import kotlin.collections.ArrayList

class AnnouncementsFragment : Fragment() {

    private lateinit var announcement_recycler : RecyclerView
    lateinit var announcementAdapter : GroupAdapter<GroupieViewHolder>


    lateinit var binding: FragmentAnnouncementsBinding
    lateinit var ad: AdView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val adView = AdView(view.context)

//        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
//        adView.adSize = AdSize.BANNER
//        MobileAds.initialize(view.context) {}
//        ad = binding.adView
//        val adRequest = AdRequest.Builder().build()
//        ad.loadAd(adRequest)

        binding.refreshannouncementsSwipe.setOnRefreshListener {
            loadAnnouncements()
        }
    }

    override fun onStart() {
        super.onStart()

        binding.emptyannouncementsText.visibility = View.VISIBLE

        announcementAdapter = GroupAdapter<GroupieViewHolder>()

        announcement_recycler = binding.announcementsrecycler
        announcement_recycler.adapter = announcementAdapter

        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        announcementAdapter.clear()
        val ref = FirebaseDatabase.getInstance().getReference("/emergency-calls")

        val listener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot: DataSnapshot in dataSnapshot.children){
                    for (child: DataSnapshot in snapshot.children) {
                        child.getValue(Call::class.java)?.let {
                            announcementAdapter.add(AnnouncementlistItem(it))
                            binding.emptyannouncementsText.visibility = View.GONE
                        }
                    }
                }
                binding.refreshannouncementsSwipe.isRefreshing = false
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }
}