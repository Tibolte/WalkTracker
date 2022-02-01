package fr.northborders.walktracker.features.photos.presentation.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.northborders.walktracker.features.photos.presentation.PhotosAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PresentationModule {

    @Provides
    @Singleton
    fun providePhotoAdapter() = PhotosAdapter()
}