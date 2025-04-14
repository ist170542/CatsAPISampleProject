package com.example.catsapisampleproject.dataLayer.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.local.entities.PendingOperation
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaosTest {

    private lateinit var db: AppDatabase
    private lateinit var catBreedDetailsDao: CatBreedDetailsDao
    private lateinit var catBreedImagesDao: CatBreedImagesDao
    private lateinit var catBreedsDao: CatBreedsDao
    private lateinit var favouriteBreedsDao: FavouriteBreedsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Create an in-memory database for testing.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // For tests only.
            .build()
        catBreedDetailsDao = db.catBreedDetailsDao()
        catBreedImagesDao = db.catBreedImagesDao()
        catBreedsDao = db.catBreedsDao()
        favouriteBreedsDao = db.favouriteBreedsDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // ----- Tests for CatBreedDetailsDao -----
    @Test
    fun insertAndRetrieveCatBreedDetails() = runTest {
        // Arrange
        val details = CatBreedDetailsEntity(
            breedID = "breed1",
            description = "Some detailed info",
            temperament = "Playful",
            origin = "Egypt"
        )
        // Act
        catBreedDetailsDao.insertCatBreedDetails(details)
        val retrieved = catBreedDetailsDao.getCatBreedDetails("breed1")
        // Assert
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.breedID).isEqualTo("breed1")
        assertThat(retrieved?.description).isEqualTo("Some detailed info")
        assertThat(retrieved?.temperament).isEqualTo("Playful")
        assertThat(retrieved?.origin).isEqualTo("Egypt")
    }

    @Test
    fun insertMultipleCatBreedDetails() = runBlocking {
        // Arrange
        val detailsList = listOf(
            CatBreedDetailsEntity(breedID = "breed1", description = "Info 1", temperament = "Calm", origin = "USA"),
            CatBreedDetailsEntity(breedID = "breed2", description = "Info 2", temperament = "Active", origin = "UK")
        )
        // Act
        catBreedDetailsDao.insertCatBreedsDetails(detailsList)
        val retrieved1 = catBreedDetailsDao.getCatBreedDetails("breed1")
        val retrieved2 = catBreedDetailsDao.getCatBreedDetails("breed2")
        // Assert
        assertThat(retrieved1).isNotNull()
        assertThat(retrieved1?.description).isEqualTo("Info 1")
        assertThat(retrieved2).isNotNull()
        assertThat(retrieved2?.description).isEqualTo("Info 2")
    }

    // ----- Tests for CatBreedImagesDao -----
    @Test
    fun insertAndRetrieveCatBreedImage() = runBlocking {
        // Arrange
        val image = CatBreedImage(
            breed_id = "breed1",
            image_id = "img1",
            url = "http://example.com/image.jpg"
        )
        // Act
        catBreedImagesDao.insertCatBreedImages(listOf(image))
        val retrieved = catBreedImagesDao.getCatBreedImageByBreedId("breed1")
        // Assert
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.image_id).isEqualTo("img1")
        assertThat(retrieved?.url).isEqualTo("http://example.com/image.jpg")
    }

    @Test
    fun getAllCatBreedImagesReturnsList() = runBlocking {
        // Arrange
        val image1 = CatBreedImage(breed_id = "breed1", image_id = "img1", url = "http://example.com/img1.jpg")
        val image2 = CatBreedImage(breed_id = "breed2", image_id = "img2", url = "http://example.com/img2.jpg")
        catBreedImagesDao.insertCatBreedImages(listOf(image1, image2))
        // Act
        val allImages = catBreedImagesDao.getAllCatBreedImages()
        // Assert
        assertThat(allImages).isNotNull()
        assertThat(allImages).hasSize(2)
    }

    // ----- Tests for CatBreedsDao -----
    @Test
    fun insertAndRetrieveCatBreed() = runBlocking {
        // Arrange
        val breed = CatBreed(
            id = "breed1",
            name = "Siamese",
            referenceImageId = "img1",
            minLifeSpan = 12,
            maxLifeSpan = 20
        )
        // Act
        catBreedsDao.insertCatBreeds(listOf(breed))
        val retrieved = catBreedsDao.getCatBreedById("breed1")
        // Assert
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Siamese")
    }

    @Test
    fun getAllCatBreedsReturnsList() = runBlocking {
        // Arrange
        val breed1 = CatBreed(
            id = "breed1",
            name = "Siamese",
            referenceImageId = "img1",
            minLifeSpan = 12,
            maxLifeSpan = 20
        )
        val breed2 = CatBreed(
            id = "breed2",
            name = "Persian",
            referenceImageId = "img2",
            minLifeSpan = 10,
            maxLifeSpan = 15
        )
        catBreedsDao.insertCatBreeds(listOf(breed1, breed2))
        // Act
        val allBreeds = catBreedsDao.getAllCatBreeds()
        // Assert
        assertThat(allBreeds).hasSize(2)
    }

    // ----- Tests for FavouriteBreedsDao -----
    @Test
    fun insertAndRetrieveFavourite() = runBlocking {
        // Arrange
        val fav = FavouriteEntity(
            imageId = "img1",
            favouriteId = "fav1",
            pendingOperation = PendingOperation.None
        )
        // Act
        favouriteBreedsDao.insertFavourite(fav)
        val retrieved = favouriteBreedsDao.getFavouriteByImageId("img1")
        // Assert
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.favouriteId).isEqualTo("fav1")
        assertThat(retrieved?.pendingOperation).isEqualTo(PendingOperation.None)
    }

    @Test
    fun insertFavouritesAndDeleteAll() = runBlocking {
        // Arrange
        val fav1 = FavouriteEntity(imageId = "img1", favouriteId = "fav1", pendingOperation = PendingOperation.None)
        val fav2 = FavouriteEntity(imageId = "img2", favouriteId = "fav2", pendingOperation = PendingOperation.None)
        favouriteBreedsDao.insertFavourites(listOf(fav1, fav2))
        // Act
        val allBefore = favouriteBreedsDao.getAllFavourites()
        assertThat(allBefore).hasSize(2)
        favouriteBreedsDao.deleteAllFavourites()
        val allAfter = favouriteBreedsDao.getAllFavourites()
        // Assert:
        assertThat(allAfter).isEmpty()
    }

    @Test
    fun observeFavouriteByImageIdEmitsData() = runBlocking {
        // Arrange
        val fav = FavouriteEntity(
            imageId = "img1",
            favouriteId = "fav1",
            pendingOperation = PendingOperation.None
        )
        favouriteBreedsDao.insertFavourite(fav)
        // Act
        val observed = favouriteBreedsDao.observeFavouriteByImageId("img1").first()
        // Assert:
        assertThat(observed).isNotNull()
        assertThat(observed?.favouriteId).isEqualTo("fav1")
    }
}