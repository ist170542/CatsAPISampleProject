package com.example.catsapisampleproject.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.catsapisampleproject.data.local.entities.CatBreedDetailsEntity

@Dao
interface CatBreedDetailsDao {

    @Query("SELECT * FROM details WHERE breedId = :breedId")
    suspend fun getCatBreedDetails(breedId: String): CatBreedDetailsEntity?

    @Insert
    suspend fun insertCatBreedDetails(catBreedDetails: CatBreedDetailsEntity)

    @Insert
    suspend fun insertCatBreedsDetails(catBreedDetails: List<CatBreedDetailsEntity>)
}