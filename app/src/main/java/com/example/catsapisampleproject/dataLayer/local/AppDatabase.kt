package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.model.CatBreed

@Database(entities = [CatBreed::class, CatBreedImage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catBreedsDao(): CatBreedsDao
    abstract fun catBreedImagesDao(): CatBreedImagesDao
}