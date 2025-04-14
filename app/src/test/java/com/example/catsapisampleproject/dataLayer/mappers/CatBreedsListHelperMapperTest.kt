package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CatBreedsMapperTest {

    @Test
    fun `mapper returns correct favourite flag when favourite exists`() {
        // Arrange
        val breed = CatBreed(
            id = "1",
            name = "Abyssinian",
            referenceImageId = "img1",
            minLifeSpan = 10,
            maxLifeSpan = 15
        )
        val image = CatBreedImage(
            breed_id = "1",
            url = "http://example.com/abyssinian.jpg",
            image_id = "img1"
        )
        val favourite = FavouriteEntity(
            imageId = "img1",
            favouriteId = "fav1"
        )

        // Act
        val result = createBreedWithImageList(listOf(breed), listOf(image), listOf(favourite))

        // Assert
        assertThat(result).hasSize(1)
        val bwImage = result.first()
        assertThat(bwImage.isFavourite).isTrue()
        assertThat(bwImage.image?.url).isEqualTo("http://example.com/abyssinian.jpg")
    }

    @Test
    fun `mapper returns false favourite flag when no matching favourite`() {
        // Arrange
        val breed = CatBreed(
            id = "1",
            name = "Abyssinian",
            referenceImageId = "img1",
            minLifeSpan = 10,
            maxLifeSpan = 15
        )
        val image = CatBreedImage(
            breed_id = "1",
            url = "http://example.com/abyssinian.jpg",
            image_id = "img1"
        )
        // No favourite for img1
        val favourites = emptyList<FavouriteEntity>()

        // Act
        val result = createBreedWithImageList(listOf(breed), listOf(image), favourites)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result.first().isFavourite).isFalse()
    }
}