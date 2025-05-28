package com.example.catsapisampleproject.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.catsapisampleproject.data.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity

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