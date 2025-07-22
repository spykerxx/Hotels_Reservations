package com.example.hotelres.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.hotelres.R
import com.example.hotelres.data.model.Booking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    isLoading: Boolean = false,
    userName: String = "Alice Doe",
    userEmail: String = "alicedoe@example.com",
    userPhone: String = "+1 234 567 890",
    membershipLevel: String = "Gold Member",
    bookings: List<Booking> = emptyList(),
    savedProfileImageUri: String?,
    onProfileImageChange: (Uri?) -> Unit

) {
    val context = LocalContext.current

// Convert savedProfileImageUri String? to Uri? once
    val savedUri = remember(savedProfileImageUri) {
        savedProfileImageUri?.toUri()
    }

// Hold currently displayed image URI state, initialized from savedUri
    var selectedImageUri by remember { mutableStateOf<Uri?>(savedUri) }

// Update selectedImageUri if savedUri changes externally (e.g., after restart)
    LaunchedEffect(savedUri) {
        selectedImageUri = savedUri
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist permission for this URI
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            selectedImageUri = uri
            onProfileImageChange(uri)  // notify parent to save it
        }
    }


    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = { Text("Do you want to edit your profile details?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    onEditProfile()
                }) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile image (circle)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 🔽 Edit icon overlay — now placed correctly
                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp) // Moves it slightly out from the circle
                        .size(20.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Edit Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(userName, style = MaterialTheme.typography.titleLarge)
            Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(userPhone, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(membershipLevel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onToggleTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Edit Profile")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                enabled = !isLoading
            ) {
                Text("Log Out")
            }

            // 🔽 My Bookings Section
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "My Bookings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (bookings.isEmpty()) {
                Spacer(modifier = Modifier.height(34.dp))
                Text("You have no bookings yet.", color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings) { booking ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(booking.hotelName, style = MaterialTheme.typography.titleMedium)
                                Text("Room: ${booking.roomType}", color = Color.Gray)
                                Text("Guests: ${booking.guests}")
                                Text("From ${booking.checkIn} to ${booking.checkOut}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

