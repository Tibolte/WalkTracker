package fr.northborders.walktracker.core.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.northborders.walktracker.core.platform.NetworkHandler
import fr.northborders.walktracker.features.photos.data.db.PhotoDao
import fr.northborders.walktracker.features.photos.data.db.PhotoDatabase
import fr.northborders.walktracker.features.photos.data.network.PhotoRepositoryImpl
import fr.northborders.walktracker.features.photos.data.network.PhotoService
import fr.northborders.walktracker.features.photos.domain.PhotoRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PhotoDatabase =
        Room.databaseBuilder(
            context,
            PhotoDatabase::class.java,
            "photos_db"
        ).build()

    @Singleton
    @Provides
    fun providePhotoDao(db: PhotoDatabase) = db.photoDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(
        HttpLoggingInterceptor.Level.BODY)

    @Provides
    @Singleton
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient.Builder =
        OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor)

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient.Builder): Retrofit =
        Retrofit.Builder()
            .baseUrl(PhotoService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient.build())
            .build()

    @Provides
    @Singleton
    fun provideService(retrofit: Retrofit): PhotoService =
        retrofit.create(PhotoService::class.java)

    @Provides
    @Singleton
    fun provideNetworkHandler(@ApplicationContext context: Context) = NetworkHandler(context)

    @Provides
    @Singleton
    fun providePhotoRepository(networkHandler: NetworkHandler, photoDao: PhotoDao, photoService: PhotoService) =
        PhotoRepositoryImpl(networkHandler, photoService, photoDao) as PhotoRepository
}