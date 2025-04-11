package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.catsapisampleproject.domain.model.CatBreed

@Dao
interface CatBreedsDao {

    @Query("SELECT * FROM cat_breeds WHERE id = :breedId")
    suspend fun getCatBreedById(breedId: String): CatBreed?

    @Query("SELECT * FROM cat_breeds")
    suspend fun getAllCatBreeds(): List<CatBreed>

    @Update
    suspend fun updateCatBreed(catBreed: CatBreed)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreed(catBreed: CatBreed)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreeds(catBreeds: List<CatBreed>)

}