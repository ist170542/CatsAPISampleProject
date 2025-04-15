package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity

@Database(
    entities = [
        CatBreedEntity::class,
        CatBreedImageEntity::class,
        FavouriteEntity::class,
        CatBreedDetailsEntity::class], version = 22, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catBreedsDao(): CatBreedsDao
    abstract fun catBreedImagesDao(): CatBreedImagesDao
    abstract fun favouriteBreedsDao(): FavouriteBreedsDao
    abstract fun catBreedDetailsDao(): CatBreedDetailsDao
}