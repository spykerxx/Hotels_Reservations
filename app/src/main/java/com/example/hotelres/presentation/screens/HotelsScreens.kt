package com.example.hotelres.presentation.screens

import android.annotation.SuppressLint
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.hotelres.data.model.Booking
import com.example.hotelres.data.model.Hotel
import com.example.hotelres.presentation.viewmodel.HotelViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelListScreen(
    hotels: List<Hotel>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onHotelClick: (Hotel) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Your Stay", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search hotels...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(hotels) { hotel ->
                    HotelCard(hotel = hotel, onClick = { onHotelClick(hotel) })
                }
            }
        }
    }
}

@Composable
fun HotelCard(hotel: Hotel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = hotel.image_url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                Text(hotel.hotel_name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${hotel.location.city}, ${hotel.location.country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${hotel.rating}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("${hotel.price_per_night} USD / night", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailsScreen(
    hotel: Hotel,
    navController: NavHostController,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hotel.hotel_name, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = hotel.image_url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(hotel.hotel_name, style = MaterialTheme.typography.headlineMedium)

            Text(
                text = "${hotel.location.city}, ${hotel.location.country}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${hotel.rating}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Price: $${hotel.price_per_night} / night",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Experience the best stay at ${hotel.hotel_name}, located in the beautiful ${hotel.location}. Enjoy luxurious amenities, stunning views, and exceptional service tailored to your comfort.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController.navigate("booking/${hotel.hotel_id}")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Book Now")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onToggleFavorite,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: HotelViewModel,
    hotelName: String,
    roomTypes: List<String> = listOf("Standard", "Deluxe", "Suite"),
    onBookingConfirmed: () -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = remember { LocalDate.now() }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }
    var selectedRoomType by remember { mutableStateOf(roomTypes.first()) }
    var showDialog by remember { mutableStateOf(false) }

    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }

    val checkInDate = remember { mutableStateOf<LocalDate?>(null) }
    val checkOutDate = remember { mutableStateOf<LocalDate?>(null) }

    val checkInDateFormatted = checkInDate.value?.format(formatter) ?: ""
    val checkOutDateFormatted = checkOutDate.value?.format(formatter) ?: ""

    fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isValidPhone(phone: String) = phone.length >= 7 && phone.all { it.isDigit() || it in "+- " }

    val checkInPickerState = rememberDatePickerState(
        initialSelectedDateMillis = checkInDate.value?.toEpochDay()?.times(86_400_000)
            ?: (today.toEpochDay() * 86_400_000)
    )

    val checkOutPickerState = rememberDatePickerState(
        initialSelectedDateMillis = checkOutDate.value?.toEpochDay()?.times(86_400_000)
            ?: (checkInDate.value?.plusDays(1)?.toEpochDay()?.times(86_400_000)
                ?: (today.toEpochDay() * 86_400_000))
    )

    val isFormValid = fullName.isNotBlank() &&
            isValidEmail(email) &&
            isValidPhone(phone) &&
            guests.toIntOrNull()?.let { it > 0 } == true &&
            checkInDate.value != null && checkOutDate.value != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking for $hotelName", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                shape = RoundedCornerShape(50),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                shape = RoundedCornerShape(50),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                shape = RoundedCornerShape(50),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = guests,
                onValueChange = { if (it.all(Char::isDigit)) guests = it },
                label = { Text("Number of Guests") },
                shape = RoundedCornerShape(50),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedRoomType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Room Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor() //
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roomTypes.forEach { roomType ->
                        DropdownMenuItem(
                            text = { Text(roomType) },
                            onClick = {
                                selectedRoomType = roomType
                                expanded = false
                            }
                        )
                    }
                }
            }


            // Check-in Date Picker Field with calendar icon
            OutlinedTextField(
                value = checkInDateFormatted,
                onValueChange = {},
                readOnly = true,
                label = { Text("Check-in Date") },
                shape = RoundedCornerShape(50),
                trailingIcon = {
                    IconButton(onClick = { showCheckInPicker = true }) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Select Check-in Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCheckInPicker = true }
            )

            // Check-out Date Picker Field with calendar icon
            OutlinedTextField(
                value = checkOutDateFormatted,
                onValueChange = {},
                readOnly = true,
                label = { Text("Check-out Date") },
                shape = RoundedCornerShape(50),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (checkInDate.value != null) {
                                showCheckOutPicker = true
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Select Check-out Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (checkInDate.value != null) {
                            showCheckOutPicker = true
                        }
                    }
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    showDialog = true
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Confirm Booking")
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Booking Confirmed") },
                text = {
                    Text(
                        "Thanks, $fullName!\n\n" +
                                "Your $selectedRoomType room at $hotelName " +
                                "is booked from $checkInDateFormatted to $checkOutDateFormatted " +
                                "for $guests guest(s)."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        // ✅ Create Booking with only relevant fields
                        if (checkInDate.value != null && checkOutDate.value != null) {
                            val booking = Booking(
                                hotelName = hotelName,
                                roomType = selectedRoomType,
                                guests = guests.toIntOrNull() ?: 1,
                                checkIn = checkInDateFormatted,
                                checkOut = checkOutDateFormatted
                            )
                            viewModel.addBooking(booking)
                        }

                        showDialog = false
                        onBookingConfirmed()
                    }) {
                        Text("OK")
                    }
                }

            )
        }


        if (showCheckInPicker) {
            DatePickerDialog(
                onDismissRequest = { showCheckInPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        checkInPickerState.selectedDateMillis?.let {
                            val selectedDate = LocalDate.ofEpochDay(it / 86_400_000)
                            if (!selectedDate.isBefore(today)) {
                                checkInDate.value = selectedDate
                                if (checkOutDate.value != null && checkOutDate.value!! <= selectedDate) {
                                    checkOutDate.value = null
                                }
                            }
                        }
                        showCheckInPicker = false
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = checkInPickerState)
            }
        }

        if (showCheckOutPicker && checkInDate.value != null) {
            DatePickerDialog(
                onDismissRequest = { showCheckOutPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        checkOutPickerState.selectedDateMillis?.let {
                            val selectedDate = LocalDate.ofEpochDay(it / 86_400_000)
                            if (selectedDate > checkInDate.value) {
                                checkOutDate.value = selectedDate
                            }
                        }
                        showCheckOutPicker = false
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = checkOutPickerState)
            }
        }
    }
}