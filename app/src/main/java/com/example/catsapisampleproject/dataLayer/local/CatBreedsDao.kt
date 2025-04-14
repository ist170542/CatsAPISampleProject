package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.catsapisampleproject.domain.model.CatBreed
import kotlinx.coroutines.flow.Flow

@Dao
interface CatBreedsDao {

    @Query("SELECT * FROM cat_breeds WHERE id = :breedId")
    suspend fun getCatBreedById(breedId: String): CatBreed?

    @Query("SELECT * FROM cat_breeds")
    suspend fun getAllCatBreeds(): List<CatBreed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreeds(catBreeds: List<CatBreed>)

}