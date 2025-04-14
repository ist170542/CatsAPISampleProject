package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.catsapisampleproject.domain.model.CatBreedImage

@Dao
interface CatBreedImagesDao {

    @Query("SELECT * FROM cat_breeds_images WHERE breed_id = :breedId LIMIT 1")
    suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImage?

    @Query("SELECT * FROM cat_breeds_images")
    suspend fun getAllCatBreedImages(): List<CatBreedImage>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImage>)

}