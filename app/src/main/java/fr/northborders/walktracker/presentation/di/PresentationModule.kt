package fr.northborders.walktracker.presentation.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.northborders.walktracker.presentation.PhotosAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PresentationModule {

    @Provides
    @Singleton
    fun providePhotoAdapter() = PhotosAdapter()
}