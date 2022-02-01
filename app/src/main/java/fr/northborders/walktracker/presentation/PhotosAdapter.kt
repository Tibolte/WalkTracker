package fr.northborders.walktracker.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import fr.northborders.walktracker.databinding.ItemPhotoBinding
import fr.northborders.walktracker.presentation.model.PhotoUI

class PhotosAdapter()
    : ListAdapter<PhotoUI,PhotosAdapter.ViewHolder>(DiffCallback) {

    private val photos: MutableList<PhotoUI> = mutableListOf()

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
        val photo = photos[position]
        holder.bind(photo)
    }

    override fun getItemCount(): Int = photos.size

    fun addPhoto(photo: PhotoUI) {
        photos.add(0, photo)
        notifyItemInserted(0)
    }

    fun addPhotos(photoItems: List<PhotoUI>) {
        for (photo in photoItems) { // have to do this because DiffCallback doesn't seem to work?
            if (photos.all { currentPhoto -> currentPhoto.id != photo.id }) {
                photos.add(photo)
            }
        }
        submitList(photos)
    }

    fun clearPhotoList() {
        photos.clear()
        notifyDataSetChanged()
    }
}

object DiffCallback: DiffUtil.ItemCallback<PhotoUI>() {
    override fun areItemsTheSame(oldItem: PhotoUI, newItem: PhotoUI): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PhotoUI, newItem: PhotoUI): Boolean {
        return oldItem == newItem
    }
}