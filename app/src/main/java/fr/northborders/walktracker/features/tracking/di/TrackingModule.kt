package fr.northborders.walktracker.features.tracking.di

import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.northborders.walktracker.features.tracking.domain.GetLocation
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TrackingModule {

    @Singleton
    @Provides
    fun provideGetLocation(fusedLocationProviderClient: FusedLocationProviderClient)
        = GetLocation(fusedLocationProviderClient)
}