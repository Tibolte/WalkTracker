package fr.northborders.walktracker.features.tracking.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import arrow.core.None
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.northborders.walktracker.R
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.util.Constants
import fr.northborders.walktracker.core.util.Constants.MAP_ZOOM
import fr.northborders.walktracker.core.util.Utils
import fr.northborders.walktracker.databinding.FragmentTrackingBinding
import fr.northborders.walktracker.features.tracking.TrackingService
import fr.northborders.walktracker.features.tracking.domain.GetLocation
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    @Inject
    lateinit var getLocation: GetLocation

    private lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
        }
        binding.txtTimer.text = Utils.getFormattedStopWatchTime(0L, true)
        binding.btnStart.setOnClickListener {
            sendServiceCommand(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
        binding.btnPause.setOnClickListener {
            sendServiceCommand(Constants.ACTION_PAUSE_SERVICE)
        }
        binding.btnStop.setOnClickListener {
            sendServiceCommand(Constants.ACTION_STOP_SERVICE)
        }

        TrackingService.timeWalkInMillis.observe(viewLifecycleOwner, Observer {
            binding.txtTimer.text = Utils.getFormattedStopWatchTime(it, true)
        })

        // TODO put that in view model?
        getLocation(None) {
            it.fold(::handleLocationFailure, ::handleLocation)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun handleLocation(location: Location) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                MAP_ZOOM
            )
        )
    }

    private fun handleLocationFailure(failure: Failure) {
        Snackbar.make(
            requireView(),
            getString(R.string.failure_location_error),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun sendServiceCommand(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }
}