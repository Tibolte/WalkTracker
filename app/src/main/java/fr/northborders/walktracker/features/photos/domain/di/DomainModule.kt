package fr.northborders.walktracker.features.photos.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.northborders.walktracker.features.photos.domain.DeletePhotos
import fr.northborders.walktracker.features.photos.domain.GetPhotos
import fr.northborders.walktracker.features.photos.domain.PhotoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DomainModule {

    @Provides
    @Singleton
    fun provideGetPhotosUseCase(photoRepository: PhotoRepository) =
        GetPhotos(photoRepository)

    @Provides
    @Singleton
    fun provideDeletePhotos(photoRepository: PhotoRepository) =
        DeletePhotos(photoRepository)
}