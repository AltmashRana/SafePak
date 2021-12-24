package com.example.safepak.frontend.announcement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.frontend.announcementAdapters.AnnouncementlistAdapter
import com.example.safepak.databinding.FragmentAnnouncementsBinding
import com.example.safepak.logic.models.Call
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnnouncementsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnnouncementsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentAnnouncementsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

        val fnames = arrayOf("Amina","Isha", "Ayesha", "Mariam")
        val locations = arrayOf("Johar Town, Lahore","Model Town, Lahore", "Faisal Town, Lahore", "Iqbal Town, Lahore")
        val types = arrayOf(2, 1, 3, 2)
        fnames.reverse()
        val calls: ArrayList<Call> = ArrayList()

        for (i in fnames.indices)
        {
            calls.add(Call(0,fnames[i],types[i], locations[i], Date()))
        }
        val recyclerView: RecyclerView = binding.announcementsrecycler
        val adapter = AnnouncementlistAdapter(calls)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(this.context)

        if (calls.size == 0)
            binding.emptyannouncementsText.visibility =View.VISIBLE
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnnouncementsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AnnouncementsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}