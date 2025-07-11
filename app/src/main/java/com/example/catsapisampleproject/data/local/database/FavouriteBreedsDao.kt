package com.example.catsapisampleproject.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteBreedsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourites(favouriteEntities: List<FavouriteEntity>)

    @Delete
    suspend fun deleteFavourite(favourite: FavouriteEntity)

    @Query("SELECT * FROM favourites")
    fun getAllFavourites(): List<FavouriteEntity>

    @Query("SELECT * FROM favourites WHERE imageId = :imageId LIMIT 1")
    suspend fun getFavouriteByImageId(imageId: String): FavouriteEntity?

    @Query("DELETE FROM favourites")
    suspend fun deleteAllFavourites()

    @Query("SELECT * FROM favourites")
    fun observeAllFavourites(): Flow<List<FavouriteEntity>>

    @Query("SELECT * FROM favourites WHERE imageId = :imageId")
    fun observeFavouriteByImageId(imageId: String): Flow<FavouriteEntity?>

}