package fr.northborders.walktracker.presentation

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import fr.northborders.walktracker.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.R
import fr.northborders.walktracker.databinding.FragmentPhotoListBinding
import fr.northborders.walktracker.domain.TrackingService
import timber.log.Timber

class PhotosFragment: Fragment() {

    lateinit var binding: FragmentPhotoListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPhotoListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_start_stop) {
            Timber.d("Btn Stop start clicked")
            sendServiceCommand(ACTION_START_OR_RESUME_SERVICE)
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