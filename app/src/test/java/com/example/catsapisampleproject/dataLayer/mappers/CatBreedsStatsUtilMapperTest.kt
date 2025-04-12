package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
import com.example.catsapisampleproject.domain.model.CatBreed
import org.junit.Assert.assertEquals
import org.junit.Test

class CatBreedsStatsUtilMapperTest {

    @Test
    fun `computeAverageMinLifeSpan returns correct average`() {
        val breeds = listOf(
            BreedWithImage(CatBreed(id = "1",
                minLifeSpan = 10,
                maxLifeSpan = 15,
                referenceImageId = "img1",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ),
                null
            ),
            BreedWithImage(CatBreed(id = "2",
                minLifeSpan = 15,
                maxLifeSpan = 20,
                referenceImageId = "img2",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ), null),
            BreedWithImage(CatBreed(id = "3",
                minLifeSpan = 20,
                maxLifeSpan = 25,
                referenceImageId = "img3",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ), null)
        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(15.0, result!!, 0.001)
    }

    @Test
    fun `computeAverageMinLifeSpan handles null values`() {
        val breeds = listOf(
            BreedWithImage(CatBreed(id = "1",
                minLifeSpan = null,
                maxLifeSpan = 15,
                referenceImageId = "img1",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ),
                null
            ),
            BreedWithImage(CatBreed(id = "2",
                minLifeSpan = 15,
                maxLifeSpan = 20,
                referenceImageId = "img2",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ), null),
            BreedWithImage(CatBreed(id = "3",
                minLifeSpan = 20,
                maxLifeSpan = 25,
                referenceImageId = "img3",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ), null)
        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(17.5, result!!, 0.001)
    }

    @Test
    fun `computeAverageMinLifeSpan returns null for empty list`() {
        val breeds = emptyList<BreedWithImage>()
        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(null, result)
    }

    @Test
    fun `computeAverageMinLifeSpan handles all null values`() {
        val breeds = listOf(
            BreedWithImage(CatBreed(id = "1",
                minLifeSpan = null,
                maxLifeSpan = 15,
                referenceImageId = "img1",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ),
                null
            ),
            BreedWithImage(CatBreed(id = "2",
                minLifeSpan = null,
                maxLifeSpan = 20,
                referenceImageId = "img2",
                temperament = "Active",
                origin = "Egypt",
                description = "Active, playful cat",
                name = "Abyssinian"
            ), null)

        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(null, result)
    }

}