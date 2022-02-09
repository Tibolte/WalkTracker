package fr.northborders.walktracker.features.tracking.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import fr.northborders.walktracker.core.util.Utils
import fr.northborders.walktracker.databinding.FragmentTrackingBinding
import fr.northborders.walktracker.features.tracking.TrackingService

class TrackingFragment: Fragment() {

    private lateinit var binding: FragmentTrackingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)

        TrackingService.timeWalkInMillis.observe(viewLifecycleOwner, Observer {
            binding.txtTimer.text = Utils.getFormattedStopWatchTime(it, true)
        })

        return binding.root
    }
}