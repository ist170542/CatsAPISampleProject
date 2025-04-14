package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.catsapisampleproject.domain.model.CatBreedImage
import kotlinx.coroutines.flow.Flow

@Dao
interface CatBreedImagesDao {

    @Query("SELECT * FROM cat_breeds_images WHERE image_id = :imageId")
    suspend fun getCatBreedImageById(imageId: String): CatBreedImage?

    @Query("SELECT * FROM cat_breeds_images WHERE breed_id = :breedId LIMIT 1")
    suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImage?

    @Query("SELECT * FROM cat_breeds_images")
    suspend fun getAllCatBreedImages(): List<CatBreedImage>?

    @Update
    suspend fun updateCatBreedImage(catBreedImage: CatBreedImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreedImage(catBreedImage: CatBreedImage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImage>)

    @Query("SELECT * FROM cat_breeds_images")
    fun observeAllCatBreedImages(): Flow<List<CatBreedImage>>

    @Query("SELECT * FROM cat_breeds_images WHERE breed_id = :breedId")
    fun observeCatBreedImageByBreedId(breedId: String): Flow<CatBreedImage?>

}