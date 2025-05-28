package com.example.catsapisampleproject.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity

@Dao
interface CatBreedImagesDao {

    @Query("SELECT * FROM cat_breeds_images WHERE breed_id = :breedId LIMIT 1")
    suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImageEntity?

    @Query("SELECT * FROM cat_breeds_images")
    suspend fun getAllCatBreedImages(): List<CatBreedImageEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImageEntity>)

}