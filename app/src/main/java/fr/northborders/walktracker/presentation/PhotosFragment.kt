package fr.northborders.walktracker.presentation

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import fr.northborders.walktracker.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.Constants.ACTION_STOP_SERVICE
import fr.northborders.walktracker.Constants.SERVICE_STATE
import fr.northborders.walktracker.R
import fr.northborders.walktracker.databinding.FragmentPhotoListBinding
import fr.northborders.walktracker.domain.TrackingService
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PhotosFragment: Fragment() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: FragmentPhotoListBinding
    private lateinit var toolbarMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPhotoListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        toolbarMenu = menu
        val serviceState = sharedPreferences.getString(SERVICE_STATE, getString(R.string.stop))

        toolbarMenu.findItem(R.id.action_start_stop).title = if (serviceState == getString(R.string.stop)) {
            getString(R.string.start)
        } else {
            getString(R.string.stop)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.title == requireContext().getString(R.string.start)) {
            Timber.d("Btn start clicked")
            sendServiceCommand(ACTION_START_OR_RESUME_SERVICE)
            item.title = requireContext().getString(R.string.stop)
        } else if (item.title == requireContext().getString(R.string.stop)) {
            Timber.d("Btn stop clicked")
            sendServiceCommand(ACTION_STOP_SERVICE)
            item.title = requireContext().getString(R.string.start)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendServiceCommand(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }
}