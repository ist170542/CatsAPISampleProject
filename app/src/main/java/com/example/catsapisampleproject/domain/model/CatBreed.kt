package com.example.catsapisampleproject.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds")
data class CatBreed (
    @PrimaryKey val id: String,
    val name: String,
    val referenceImageId: String?,
    val minLifeSpan: Int?,
    val maxLifeSpan: Int?
)