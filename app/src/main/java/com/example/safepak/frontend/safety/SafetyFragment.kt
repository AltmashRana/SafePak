package com.example.safepak.frontend.safety

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safepak.data.User
import com.example.safepak.databinding.FragmentSafetyBinding
import com.example.safepak.frontend.safetyAdapters.CloselistAdapter
import com.example.safepak.frontend.safetyAdapters.HelplistAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SafetyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SafetyFragment : Fragment() {
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

    lateinit var binding: FragmentSafetyBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSafetyBinding.inflate(inflater, container, false)
        binding.level1Bt.setOnClickListener {
            Toast.makeText(context, "Level-1 emergency initiated!", Toast.LENGTH_SHORT).show()
        }

        binding.medicalBt.setOnClickListener {
            Toast.makeText(context, "Medical emergency initiated!", Toast.LENGTH_SHORT).show()
        }

        binding.level2Bt.setOnClickListener {
            Toast.makeText(context, "Level-2 emergency initiated!", Toast.LENGTH_SHORT).show()
        }


        val fnames = arrayOf("Amina","Isha", "Ayesha", "Mariam", "Anaya")
        val lnames = arrayOf("Amina","Isha", "Ayesha", "Mariam", "Anaya")
        lnames.reverse()
        val users: ArrayList<User> = ArrayList()

        for (i in fnames.indices)
            users.add(User("0",fnames[i],lnames[i],"null","null","null",null,null,null,null,null))
        val close_recyclerView: RecyclerView = binding.closeRecycler
        val adapter1 = CloselistAdapter(users)
        close_recyclerView?.adapter = adapter1
        close_recyclerView?.layoutManager = LinearLayoutManager(this.context,RecyclerView.HORIZONTAL,false)

        val help_recyclerView: RecyclerView = binding.helpRecycler
        val adapter2 = HelplistAdapter(users)
        help_recyclerView?.adapter = adapter2
        help_recyclerView?.layoutManager = LinearLayoutManager(this.context)


        if (users.size == 0)
            binding.emptycloseText.visibility = View.VISIBLE

        if (users.size == 0)
            binding.emptyhelpText.visibility = View.VISIBLE

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SafetyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                SafetyFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}