package com.example.catsapisampleproject.dataLayer.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity

@Dao
interface CatBreedDetailsDao {

    @Query("SELECT * FROM details WHERE breedId = :breedId")
    suspend fun getCatBreedDetails(breedId: String): CatBreedDetailsEntity?

    @Insert
    suspend fun insertCatBreedDetails(catBreedDetails: CatBreedDetailsEntity)

    @Insert
    suspend fun insertCatBreedsDetails(catBreedDetails: List<CatBreedDetailsEntity>)
}