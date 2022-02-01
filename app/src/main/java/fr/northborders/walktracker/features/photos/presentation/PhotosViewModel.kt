package fr.northborders.walktracker.features.photos.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.None
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.features.photos.domain.DeletePhotos
import fr.northborders.walktracker.features.photos.domain.GetPhotos
import fr.northborders.walktracker.features.photos.domain.model.Photo
import fr.northborders.walktracker.features.photos.presentation.model.PhotoUI
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val getPhotos: GetPhotos,
    private val deletePhotos: DeletePhotos
): ViewModel() {
    private val _photos: MutableLiveData<List<PhotoUI>> = MutableLiveData()
    val photos: LiveData<List<PhotoUI>> = _photos

    private val _failure: MutableLiveData<Failure> = MutableLiveData()
    val failure: LiveData<Failure> = _failure

    fun loadPhotos() =
        getPhotos(None, viewModelScope) { it.fold(::handleFailure, ::handlePhotos) }

    fun deletePhotos() =
        deletePhotos(None) { it.fold(::handleFailure, ::handlePhotos) }

    private fun handlePhotos(photos: List<Photo>) {
        _photos.value = photos.map { it.toPhotoUI() }
    }

    private fun handleFailure(failure: Failure) {
        _failure.value = failure
    }
}