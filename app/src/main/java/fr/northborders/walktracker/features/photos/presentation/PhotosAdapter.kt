package fr.northborders.walktracker.features.photos.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.northborders.walktracker.features.photos.presentation.model.PhotoUI
import fr.northborders.walktracker.databinding.ItemPhotoBinding

class PhotosAdapter()
    : ListAdapter<PhotoUI,PhotosAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemPhotoBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhotoUI) {
            binding.imgPhoto.load(item.buildUri())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo)
    }
}

private class DiffCallback: DiffUtil.ItemCallback<PhotoUI>() {
    override fun areItemsTheSame(oldItem: PhotoUI, newItem: PhotoUI): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PhotoUI, newItem: PhotoUI): Boolean {
        return oldItem == newItem
    }
}