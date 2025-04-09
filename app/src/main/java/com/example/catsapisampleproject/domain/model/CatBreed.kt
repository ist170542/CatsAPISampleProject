package com.example.catsapisampleproject.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds")
data class CatBreed (
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val temperament: String,
    val origin: String,
    val referenceImageId: String?,
    val isFavourite: Boolean
)