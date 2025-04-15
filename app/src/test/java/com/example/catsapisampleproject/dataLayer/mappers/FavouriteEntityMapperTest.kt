package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import org.junit.Assert.assertEquals
import org.junit.Test

class FavouriteEntityMapperTest {

    private val mapper = FavouriteEntityMapper

    @Test
    fun `maps valid dto correctly`() {
        val dto = FavouriteDTO(
            favouriteID = "fav_123",
            imageID = "img_456"
        )

        val result = mapper.fromDto(dto)

        assertEquals("fav_123", result.favouriteId)
        assertEquals("img_456", result.imageId)
    }

}