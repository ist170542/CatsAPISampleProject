package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage

object CatBreedsStatsUtilMapper {
    fun computeAverageMinLifeSpan(breeds: List<BreedWithImage>): Double? {
        val values = breeds.mapNotNull { it.breed.minLifeSpan?.toDouble() }
        return if (values.isNotEmpty()) values.average() else null
    }
}