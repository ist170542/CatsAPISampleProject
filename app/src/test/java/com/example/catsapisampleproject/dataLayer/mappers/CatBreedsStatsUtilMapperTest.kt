package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.domain.model.BreedWithImage
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
                name = "Abyssinian"
            ),
                null
            ),
            BreedWithImage(CatBreed(id = "2",
                minLifeSpan = 15,
                maxLifeSpan = 20,
                referenceImageId = "img2",
                name = "Abyssinian"
            ), null),
            BreedWithImage(CatBreed(id = "3",
                minLifeSpan = 20,
                maxLifeSpan = 25,
                referenceImageId = "img3",
                name = "Abyssinian"
            ), null)
        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(15.0, result!!, 0.001)
    }


    @Test
    fun `computeAverageMinLifeSpan handles zero values`() {
        val breeds = listOf(
            BreedWithImage(CatBreed(
                id = "4", minLifeSpan = 0, // Zero value
                maxLifeSpan = 5, name = "TestBreed",
                referenceImageId = "img4"
            ), null),
            BreedWithImage(CatBreed(
                id = "5", minLifeSpan = 10,
                maxLifeSpan = 15, name = "TestBreed",
                referenceImageId = "img5"
            ), null)
        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(5.0, result!!, 0.001)
    }

    @Test
    fun `computeAverageMinLifeSpan handles single valid value`() {
        val breeds = listOf(
            BreedWithImage(CatBreed(
                id = "6", minLifeSpan = 8,
                maxLifeSpan = 12, name = "TestBreed",
                referenceImageId = "img6"
            ), null)
        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(8.0, result!!, 0.001)
    }


    @Test
    fun `computeAverageMinLifeSpan handles all null values`() {
        val breeds = listOf(
            BreedWithImage(CatBreed(id = "1",
                minLifeSpan = null,
                maxLifeSpan = 15,
                referenceImageId = "img1",
                name = "Abyssinian"
            ),
                null
            ),
            BreedWithImage(CatBreed(id = "2",
                minLifeSpan = null,
                maxLifeSpan = 20,
                referenceImageId = "img2",
                name = "Abyssinian"
            ), null)

        )

        val result = CatBreedsStatsUtilMapper.computeAverageMinLifeSpan(breeds)
        assertEquals(null, result)
    }
}