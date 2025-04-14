package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.model.CatBreed

@Database(entities = [
    CatBreed::class,
    CatBreedImage::class,
    FavouriteEntity::class,
    CatBreedDetailsEntity::class]
    , version = 21, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catBreedsDao(): CatBreedsDao
    abstract fun catBreedImagesDao(): CatBreedImagesDao
    abstract fun favouriteBreedsDao(): FavouriteBreedsDao
    abstract fun catBreedDetailsDao(): CatBreedDetailsDao
}