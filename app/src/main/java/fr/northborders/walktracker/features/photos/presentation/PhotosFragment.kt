package fr.northborders.walktracker.features.photos.presentation

import android.content.*
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import fr.northborders.walktracker.core.util.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.core.util.Constants.ACTION_STOP_SERVICE
import fr.northborders.walktracker.core.util.Constants.EXTRA_PHOTO
import fr.northborders.walktracker.core.util.Constants.INTENT_BROADCAST_PHOTO
import fr.northborders.walktracker.R
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.extension.failure
import fr.northborders.walktracker.core.extension.observe
import fr.northborders.walktracker.core.util.Constants.ACTION_PAUSE_SERVICE
import fr.northborders.walktracker.databinding.FragmentPhotoListBinding
import fr.northborders.walktracker.features.tracking.TrackingService
import fr.northborders.walktracker.features.photos.presentation.model.PhotoUI
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PhotosFragment: Fragment() {

    @Inject
    lateinit var photosAdapter: PhotosAdapter

    private lateinit var binding: FragmentPhotoListBinding
    private lateinit var photoReceiver: PhotoReceiver

    private val photosViewModel: PhotosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(photosViewModel) {
            observe(photos, ::renderPhotosList)
            failure(failure, ::handleFailure)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPhotoListBinding.inflate(inflater, container, false)
        photoReceiver = PhotoReceiver()

        binding.recyclerView.adapter = photosAdapter

        // clear photos first
        //photosAdapter.submitList(listOf())
        loadPhotosList()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            photoReceiver,
            IntentFilter(INTENT_BROADCAST_PHOTO)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(photoReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

//        val isServiceRunning = TrackingService.isServiceRunning
//
//        menu.findItem(R.id.action_start_stop).title = if (isServiceRunning) {
//            getString(R.string.stop)
//        } else {
//            getString(R.string.start)
//        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.title == requireContext().getString(R.string.start)) {
            Timber.d("Btn start clicked")
            sendServiceCommand(ACTION_START_OR_RESUME_SERVICE)
            //item.title = requireContext().getString(R.string.stop)
        } else if (item.title == requireContext().getString(R.string.stop)) {
            Timber.d("Btn stop clicked")
            sendServiceCommand(ACTION_STOP_SERVICE)
            //item.title = requireContext().getString(R.string.start)
        } else if (item.title == requireContext().getString(R.string.pause)) {
            Timber.d("Btn Pause clicked")
            sendServiceCommand(ACTION_PAUSE_SERVICE)
        } else if (item.title == getString(R.string.delete_photos)) {
            Timber.d("Btn delete photos clicked")
            deletePhotosList()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendServiceCommand(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun loadPhotosList() {
        showProgress()
        photosViewModel.loadPhotos()
    }

    private fun deletePhotosList() {
        showProgress()
        photosViewModel.deletePhotos()
    }

    private fun handleFailure(failure: Failure?) {
        when (failure) {
            is Failure.NetworkConnection -> renderFailure(R.string.failure_network_connection)
            is Failure.ServerError -> renderFailure(R.string.failure_server_error)
            is Failure.DatabaseError -> renderFailure(R.string.failure_photos_list_unavailable)
            else -> renderFailure(R.string.failure_server_error)
        }
    }

    private fun renderPhotosList(photos: List<PhotoUI>) {
        photosAdapter.submitList(photos)
        hideProgress()
    }

    private fun renderFailure(@StringRes message: Int) {
        hideProgress()
        notify(message)
    }

    private fun notify(@StringRes message: Int) =
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

    private fun showProgress() = progressStatus(View.VISIBLE)

    private fun hideProgress() = progressStatus(View.GONE)

    private fun progressStatus(viewStatus: Int) {
        binding.progressBar.visibility = viewStatus
    }

    private inner class PhotoReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val photo = intent.getParcelableExtra<PhotoUI>(EXTRA_PHOTO)
            if (photo != null && photo.id.isNotEmpty()) {
                Timber.d("BROADCAST RECEIVER sending photo $photo")
                photosViewModel.addPhoto(photo)
                // TODO do we need this?
                binding.recyclerView.smoothScrollToPosition(0)
            }
        }
    }
}
