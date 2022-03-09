package com.example.safepak.frontend.blood

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.safepak.R
import com.example.safepak.databinding.FragmentAnnouncementsBinding
import com.example.safepak.databinding.FragmentRequestBinding
import com.google.android.gms.ads.AdView
import com.google.android.material.slider.RangeSlider
import java.text.NumberFormat

class RequestFragment : Fragment() {

    lateinit var binding: FragmentRequestBinding
    lateinit var comm: IBloodBroadcast
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRequestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comm = activity as IBloodBroadcast

        binding.bloodbroadcastBt.setOnClickListener {
            val blood = binding.bloodbroadcastSpinner.selectedItem.toString()


            comm.generateBroadcast(blood, binding.rangeSlider.value.toDouble())
        }

        binding.rangeSlider.setLabelFormatter { value: Float ->
            "${value}km"
        }

        binding.rangeSlider.addOnChangeListener { _, value, _ ->
            // Responds to when slider's value is changed
            comm.changeCircle(value.toDouble())
        }
    }
}