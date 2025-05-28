package com.example.catsapisampleproject.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "details")
data class CatBreedDetailsEntity (
    @PrimaryKey val breedID: String,
    val description: String,
    val temperament: String,
    val origin: String,
)