package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.domain.model.BreedWithImage

object CatBreedsStatsUtilMapper {
    fun computeAverageMinLifeSpan(breeds: List<BreedWithImage>): Double? {
        val values = breeds.mapNotNull { it.breed.minLifeSpan?.toDouble() }
        return if (values.isNotEmpty()) values.average() else null
    }
}