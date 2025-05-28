package com.example.catsapisampleproject.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds_images")
data class CatBreedImageEntity(
    val image_id: String,
    @PrimaryKey
    val breed_id: String,
    val url: String
)