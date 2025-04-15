package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import junit.framework.Assert.assertEquals
import org.junit.Test

class CatBreedMapperTest {

    private val mapper = CatBreedEntityMapper

    @Test
    fun `maps valid dto with lifespan range`() {
        val dto = BreedDTO(
            id = "siam",
            name = "Siamese",
            description = "Descriptive text",
            temperament = "Active",
            origin = "Thailand",
            lifeSpan = "10 - 12",
            referenceImageId = "img123"
        )

        val result = mapper.fromDto(dto)

        assertEquals("siam", result.id)
        assertEquals(10, result.minLifeSpan)
        assertEquals(12, result.maxLifeSpan)
        assertEquals("img123", result.referenceImageId)
    }

    @Test
    fun `handles invalid lifespan format`() {
        val dto = BreedDTO(
            id = "pers",
            name = "Persian",
            lifeSpan = "About 12-15 years",
            referenceImageId = "img456",
            description = "",
            temperament = "",
            origin = ""
        )

        val result = mapper.fromDto(dto)

        assertEquals(null, result.minLifeSpan)
        assertEquals(null, result.maxLifeSpan)
    }
}