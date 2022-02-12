package fr.northborders.walktracker.features.tracking.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import arrow.core.None
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.northborders.walktracker.R
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.util.Constants.ACTION_PAUSE_SERVICE
import fr.northborders.walktracker.core.util.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.core.util.Constants.ACTION_STOP_SERVICE
import fr.northborders.walktracker.core.util.Constants.MAP_ZOOM
import fr.northborders.walktracker.core.util.Constants.POLYLINE_COLOR
import fr.northborders.walktracker.core.util.Constants.POLYLINE_WIDTH
import fr.northborders.walktracker.core.util.Utils
import fr.northborders.walktracker.databinding.FragmentTrackingBinding
import fr.northborders.walktracker.features.tracking.Polyline
import fr.northborders.walktracker.features.tracking.TrackingService
import fr.northborders.walktracker.features.tracking.domain.GetLocation
import fr.northborders.walktracker.features.walks.WalkEntity
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    @Inject
    lateinit var getLocation: GetLocation

    private lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null
    private var pathPoints = mutableListOf<Polyline>()
    private var isTracking = false
    private var currentTimeInMillis = 0L

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
            addAllPolylines()
        }
        binding.txtTimer.text = Utils.getFormattedStopWatchTime(0L, true)
        binding.btnToggleWalk.setOnClickListener {
            toggleWalk()
        }
        binding.btnFinishWalk.setOnClickListener {
            zoomToSeeWholeTrack()
            endWalkAndSaveToDb()
        }
//        binding.btnPause.setOnClickListener {
//            sendServiceCommand(Constants.ACTION_PAUSE_SERVICE)
//        }
//        binding.btnStop.setOnClickListener {
//            sendServiceCommand(Constants.ACTION_STOP_SERVICE)
//        }

        subscribeToObservers()

        // TODO put that in view model? (also path points)
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

    private fun subscribeToObservers() {
        TrackingService.timeWalkInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            binding.txtTimer.text = Utils.getFormattedStopWatchTime(it, true)
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) {
            handleTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }
    }

    private fun handleLocation(location: Location?) {
        location?.let {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun handleLocationFailure(failure: Failure) {
        Snackbar.make(
            requireView(),
            getString(R.string.failure_location_error),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun toggleWalk() {
        if(isTracking) {
            sendServiceCommand(ACTION_PAUSE_SERVICE)
        } else {
            sendServiceCommand(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun handleTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            binding.btnToggleWalk.text = getString(R.string.start)
            binding.btnFinishWalk.visibility = View.GONE
        } else {
            binding.btnToggleWalk.text = getString(R.string.stop)
            binding.btnFinishWalk.visibility = View.VISIBLE
        }
    }

    private fun endWalkAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += Utils.calculatePolylineLength(polyline).toInt()
            }
            val dateTimestamp = Calendar.getInstance().timeInMillis
            // TODO use a repository and so
            val walkEntity = WalkEntity(timestamp = dateTimestamp, distanceInMeters = distanceInMeters, timeInMillis = currentTimeInMillis)
            Snackbar.make(
                requireView(),
                "Walk saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            // TODO show fragment with walk list?
            stopWalk()
        }
    }

    private fun stopWalk() {
        sendServiceCommand(ACTION_STOP_SERVICE)
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun sendServiceCommand(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }
}