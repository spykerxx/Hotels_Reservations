package com.example.hotelres

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.hotelres.data.model.Booking
import com.example.hotelres.data.model.Hotel
import com.example.hotelres.data.model.Location
import com.example.hotelres.presentation.viewmodel.HotelViewModel
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HotelViewModelTest {

    //region -------- Setup --------

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HotelViewModel
    private lateinit var fakeRepository: FakeHotelRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Application>()

        fakeRepository = FakeHotelRepository()

        // Inject fake repo into ViewModel
        viewModel = HotelViewModel(context, fakeRepository)
    }

    //endregion

    // -------- Initial State Tests --------
    @Test
    fun searchHotels_filtersByCityOrName() = runTest {
        val dummyHotels = listOf(
            Hotel(
                1,
                "Luxury Inn",
                Location("New York", "USA"),
                "desc",
                "http://",
                200.0,
                5.0f,
                50
            ),
            Hotel(
                2,
                "Budget Stay",
                Location("Cairo", "Egypt"),
                "desc",
                "http://",
                80.0,
                4.0f,
                40
            )
        )

        // Set dummy hotels into fake repository before collecting in ViewModel
        fakeRepository.setDummyHotels(dummyHotels)

        // Advance coroutines so ViewModel collects the hotels
        advanceUntilIdle()

        // Act: Search for 'cairo'
        viewModel.updateSearchQuery("cairo")

        // Advance to propagate search query filtering
        advanceUntilIdle()

        val filtered = viewModel.hotels.first()
        Assert.assertEquals(1, filtered.size)
        Assert.assertEquals("Cairo", filtered[0].location.city)
    }


    // -------- Favorites Tests --------
    @Test
    fun toggleFavorite_addsAndRemovesFavoriteHotel() = runTest {
        val dummyHotel = Hotel(
            1,
            "Luxury Inn",
            Location("New York", "USA"),
            "desc",
            "http://",
            200.0,
            5.0f,
            50
        )

        fakeRepository.setDummyHotels(listOf(dummyHotel))

        // Make sure favorites are initially empty
        fakeRepository.setFavoriteIds(emptySet())

        advanceUntilIdle() // Collect data in ViewModel

        val initialFavs = viewModel.favoriteHotels.first()
        Assert.assertTrue(initialFavs.isEmpty())

        // Add to favorites
        viewModel.toggleFavorite(dummyHotel)
        advanceUntilIdle()

        val favsAfterAdd = viewModel.favoriteHotels.first()
        Assert.assertEquals(1, favsAfterAdd.size)
        Assert.assertEquals(dummyHotel.hotel_id, favsAfterAdd[0].hotel_id)

        // Remove from favorites
        viewModel.toggleFavorite(dummyHotel)
        advanceUntilIdle()

        val favsAfterRemove = viewModel.favoriteHotels.first()
        Assert.assertTrue(favsAfterRemove.isEmpty())
    }

    @Test
    fun initialState_isEmptyOrNull() = runTest {
        // Advance coroutines to let all flows emit initial values
        advanceUntilIdle()

        // Check that searchQuery starts empty
        val initialSearchQuery = viewModel.searchQuery.first()
        Assert.assertEquals("", initialSearchQuery)

        // Check that hotels list is initially empty
        val initialHotels = viewModel.hotels.first()
        Assert.assertTrue(initialHotels.isEmpty())

        // Check that favoriteHotels list is initially empty
        val initialFavorites = viewModel.favoriteHotels.first()
        Assert.assertTrue(initialFavorites.isEmpty())

        // Check that bookings list is initially empty
        val initialBookings = viewModel.bookings.first()
        Assert.assertTrue(initialBookings.isEmpty())

        // Check that profileImageUri is initially null
        val initialProfileUri = viewModel.profileImageUri.first()
        Assert.assertNull(initialProfileUri)
    }

    @Test
    fun searchQuery_edgeCases_behaveCorrectly() = runTest {
        val dummyHotels = listOf(
            Hotel(
                1,
                "Luxury Inn",
                Location("New York", "USA"),
                "desc",
                "http://",
                200.0,
                5.0f,
                50
            ),
            Hotel(
                2,
                "Budget Stay",
                Location("Cairo", "Egypt"),
                "desc",
                "http://",
                80.0,
                4.0f,
                40
            ),
            Hotel(
                3,
                "Beach Resort",
                Location("Miami", "USA"),
                "desc",
                "http://",
                150.0,
                4.5f,
                30
            )
        )

        // Inject dummy hotels via reflection
        val field = viewModel::class.java.getDeclaredField("_allHotels")
        field.isAccessible = true
        val currentFlow = field.get(viewModel) as MutableStateFlow<List<Hotel>>
        currentFlow.value = dummyHotels

        advanceUntilIdle()

        // Case 1: Empty search query returns all hotels
        viewModel.updateSearchQuery("")
        advanceUntilIdle()
        var filteredHotels = viewModel.hotels.first()
        Assert.assertEquals(dummyHotels.size, filteredHotels.size)

        // Case 2: Case-insensitive search matches hotel name, city, or country
        viewModel.updateSearchQuery("cAirO") // mixed case search
        advanceUntilIdle()
        filteredHotels = viewModel.hotels.first()
        Assert.assertEquals(1, filteredHotels.size)
        Assert.assertEquals("Cairo", filteredHotels[0].location.city)

        // Also check search by hotel name
        viewModel.updateSearchQuery("beach")
        advanceUntilIdle()
        filteredHotels = viewModel.hotels.first()
        Assert.assertEquals(1, filteredHotels.size)
        Assert.assertEquals("Beach Resort", filteredHotels[0].hotel_name)

        // Also check search by country
        viewModel.updateSearchQuery("usa")
        advanceUntilIdle()
        filteredHotels = viewModel.hotels.first()
        Assert.assertEquals(2, filteredHotels.size)

        // Case 3: Search query that doesn’t match any hotel returns empty list
        viewModel.updateSearchQuery("NonExistingCity")
        advanceUntilIdle()
        filteredHotels = viewModel.hotels.first()
        Assert.assertTrue(filteredHotels.isEmpty())
    }

    @Test
    fun favoriteHotels_filteringAndUpdates_workCorrectly() = runTest {
        val hotel1 = Hotel(
            1,
            "Luxury Inn",
            Location("New York", "USA"),
            "desc",
            "http://",
            200.0,
            5.0f,
            50
        )
        val hotel2 = Hotel(
            2,
            "Budget Stay",
            Location("Cairo", "Egypt"),
            "desc",
            "http://",
            80.0,
            4.0f,
            40
        )
        val hotel3 = Hotel(
            3,
            "Beach Resort",
            Location("Miami", "USA"),
            "desc",
            "http://",
            150.0,
            4.5f,
            30
        )

        val allHotels = listOf(hotel1, hotel2, hotel3)

        // Inject dummy hotel list into _allHotels via reflection
        val allHotelsField = viewModel::class.java.getDeclaredField("_allHotels")
        allHotelsField.isAccessible = true
        val allHotelsFlow = allHotelsField.get(viewModel) as MutableStateFlow<List<Hotel>>
        allHotelsFlow.value = allHotels

        // Start with no favorites
        val favoriteIdsField = viewModel::class.java.getDeclaredField("_favoriteHotelIds")
        favoriteIdsField.isAccessible = true
        val favoriteIdsFlow = favoriteIdsField.get(viewModel) as MutableStateFlow<Set<Int>>
        favoriteIdsFlow.value = emptySet()

        advanceUntilIdle()

        // Initially, favoriteHotels should be empty
        var favorites = viewModel.favoriteHotels.first()
        Assert.assertTrue(favorites.isEmpty())

        // Add hotel1 to favorites by toggling
        viewModel.toggleFavorite(hotel1)
        advanceUntilIdle()
        favorites = viewModel.favoriteHotels.first()
        Assert.assertEquals(1, favorites.size)
        Assert.assertEquals(hotel1.hotel_id, favorites[0].hotel_id)

        // Add hotel2 to favorites by toggling
        viewModel.toggleFavorite(hotel2)
        advanceUntilIdle()
        favorites = viewModel.favoriteHotels.first()
        Assert.assertEquals(2, favorites.size)
        Assert.assertTrue(favorites.any { it.hotel_id == hotel1.hotel_id })
        Assert.assertTrue(favorites.any { it.hotel_id == hotel2.hotel_id })

        // Remove hotel1 from favorites by toggling
        viewModel.toggleFavorite(hotel1)
        advanceUntilIdle()
        favorites = viewModel.favoriteHotels.first()
        Assert.assertEquals(1, favorites.size)
        Assert.assertEquals(hotel2.hotel_id, favorites[0].hotel_id)

        // Remove hotel2 from favorites
        viewModel.toggleFavorite(hotel2)
        advanceUntilIdle()
        favorites = viewModel.favoriteHotels.first()
        Assert.assertTrue(favorites.isEmpty())
    }

    @Test
    fun addBooking_updatesBookingsListCorrectly() = runTest {
        val booking1 = Booking(
            hotelName = "Luxury Inn",
            roomType = "Suite",
            guests = 2,
            checkIn = "2025-07-20",
            checkOut = "2025-07-25"
        )

        val booking2 = Booking(
            hotelName = "Budget Stay",
            roomType = "Single",
            guests = 1,
            checkIn = "2025-08-01",
            checkOut = "2025-08-05"
        )

        // Initially bookings list should be empty
        var bookings = viewModel.bookings.first()
        Assert.assertTrue(bookings.isEmpty())

        // Add first booking
        viewModel.addBooking(booking1)
        advanceUntilIdle()
        bookings = viewModel.bookings.first()
        Assert.assertEquals(1, bookings.size)
        Assert.assertEquals("Luxury Inn", bookings[0].hotelName)

        // Add second booking
        viewModel.addBooking(booking2)
        advanceUntilIdle()
        bookings = viewModel.bookings.first()
        Assert.assertEquals(2, bookings.size)
        Assert.assertTrue(bookings.any { it.hotelName == "Luxury Inn" })
        Assert.assertTrue(bookings.any { it.hotelName == "Budget Stay" })
    }

    @Test
    fun saveProfileImageUri_updatesAndClearsValueCorrectly() = runTest {
        val testUri = "content://com.example.hotelres/user/profile.jpg"

        // Initially, profileImageUri should be null
        var uri = viewModel.profileImageUri.first()
        Assert.assertNull(uri)

        // Save non-null URI
        viewModel.saveProfileImageUri(testUri)
        advanceUntilIdle()
        uri = viewModel.profileImageUri.first()
        Assert.assertEquals(testUri, uri)

        // Save null URI to clear
        viewModel.saveProfileImageUri(null)
        advanceUntilIdle()
        uri = viewModel.profileImageUri.first()
        Assert.assertNull(uri)
    }


    // -------- Concurrency Tests --------

    @Test
    fun updateSearchQuery_concurrentCalls_stateIsConsistent() = runTest {
        // Simulate concurrent updates to search query
        val job1 = launch { viewModel.updateSearchQuery("New") }
        val job2 = launch { viewModel.updateSearchQuery("York") }

        job1.join()
        job2.join()
        advanceUntilIdle()

        // The searchQuery value should be either "New" or "York"
        val currentQuery = viewModel.searchQuery.first()
        assert(currentQuery == "New" || currentQuery == "York")

        // Hotels list should not be null
        val hotels = viewModel.hotels.first()
        assertNotNull(hotels)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
