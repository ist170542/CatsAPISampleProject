package com.example.catsapisampleproject.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds")
data class CatBreedEntity(
    @PrimaryKey val id: String,
    val name: String,
    val referenceImageId: String?,
    val minLifeSpan: Int?,
    val maxLifeSpan: Int?
)