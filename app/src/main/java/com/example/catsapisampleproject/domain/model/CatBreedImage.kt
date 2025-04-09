package com.example.catsapisampleproject.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds_images")
data class CatBreedImage (
    @PrimaryKey
    val image_id: String,
    val breed_id: String,
    val url: String
)