package com.example.catsapisampleproject.di

import android.content.Context
import androidx.room.Room
import com.example.catsapisampleproject.dataLayer.local.AppDatabase
import com.example.catsapisampleproject.dataLayer.local.CatBreedDetailsDao
import com.example.catsapisampleproject.dataLayer.local.CatBreedImagesDao
import com.example.catsapisampleproject.dataLayer.local.CatBreedsDao
import com.example.catsapisampleproject.dataLayer.local.FavouriteBreedsDao
import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.local.LocalDataSourceImpl
import com.example.catsapisampleproject.util.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) : AppDatabase
        = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppConstants.APP_DATABASE_NAME
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

    @Singleton
    @Provides
    fun provideCatBreedDao(database: AppDatabase) : CatBreedsDao = database.catBreedsDao()

    @Singleton
    @Provides
    fun provideCatBreedImageDao(database: AppDatabase) : CatBreedImagesDao
        = database.catBreedImagesDao()

    @Singleton
    @Provides
    fun provideFavouriteCatBreedsDao(database: AppDatabase) : FavouriteBreedsDao
        = database.favouriteBreedsDao()

    @Singleton
    @Provides
    fun provideCatBreedDetailsDao(database: AppDatabase) : CatBreedDetailsDao
        = database.catBreedDetailsDao()

    @Singleton
    @Provides
    fun provideLocalDataSource(
        catBreedsDao: CatBreedsDao,
        catBreedImagesDao: CatBreedImagesDao,
        favouriteBreedsDao: FavouriteBreedsDao,
        catBreedsDetailsDao: CatBreedDetailsDao
    )
    : LocalDataSource = LocalDataSourceImpl(
        catBreedsDao = catBreedsDao,
        catBreedImagesDao = catBreedImagesDao,
        favouritesDao = favouriteBreedsDao,
        catBreedDetailsDao = catBreedsDetailsDao
    )

}