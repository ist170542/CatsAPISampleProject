package com.example.catsapisampleproject.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.catsapisampleproject.data.local.entities.CatBreedEntity

@Dao
interface CatBreedsDao {

    @Query("SELECT * FROM cat_breeds WHERE id = :breedId")
    suspend fun getCatBreedById(breedId: String): CatBreedEntity?

    @Query("SELECT * FROM cat_breeds")
    suspend fun getAllCatBreeds(): List<CatBreedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatBreeds(catBreeds: List<CatBreedEntity>)

}