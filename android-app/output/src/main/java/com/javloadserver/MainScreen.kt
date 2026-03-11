package com.javloadserver

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javloadserver.ui.theme.*
import java.io.File

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var showConfigDialog by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }
    
    // Permission handling (Simplified for audit - in real apps use accompanist)
    LaunchedEffect(Unit) {
        viewModel.refreshIpAddresses()
        viewModel.loadFiles()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ... (Background gradient remains the same)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryDark.copy(alpha = 0.15f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 800f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "JavLoad",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Premium File Server",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                    
                    IconButton(
                        onClick = { viewModel.refreshIpAddresses() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.padding(12.dp))

            // Status Dashboard
            PremiumStatusCard(viewModel, onShowQR = { showQRCodeDialog = true })

            Spacer(modifier = Modifier.padding(12.dp))

            // File Section
            Text(
                text = "Active Files",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (viewModel.files.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No files found", color = TextSecondary)
                        }
                    }
                }
                items(viewModel.files) { file ->
                    FileItemRow(file)
                }
            }
            
            // Footer Action
            if (!viewModel.isServerRunning) {
                Button(
                    onClick = { showConfigDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("START SERVER SESSION", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { viewModel.stopServer(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TERMINATE SESSION", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialogs
    com.javloadserver.ui.components.ServerConfigDialog(
        isVisible = showConfigDialog,
        onDismiss = { showConfigDialog = false },
        onStartServer = { port, directory, password ->
            viewModel.startServer(context, port, directory, password)
            showConfigDialog = false
        }
    )

    viewModel.serverUrl?.let { url ->
        com.javloadserver.ui.components.QRCodeDialog(
            isVisible = showQRCodeDialog,
            serverUrl = url,
            onDismiss = { showQRCodeDialog = false }
        )
    }
}

@Composable
fun PremiumStatusCard(viewModel: MainViewModel, onShowQR: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(24.dp)
            .let { 
                if (viewModel.isServerRunning) it.clickable { onShowQR() } else it
            }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(viewModel.isServerRunning)
                if (viewModel.isServerRunning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, size = 16.dp, tint = AccentCyan)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = Color.Green.copy(alpha = glowAlpha),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (viewModel.isServerRunning) {
                Column {
                    Text("Access URL (Tap for QR):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = viewModel.serverUrl ?: "Initializing...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column {
                    Text("System Status:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = "Standby",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color.White.copy(alpha = 0.05f))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                StatusInfoItem(Icons.Default.SettingsInputComponent, "PORT", viewModel.serverPort.toString())
                StatusInfoItem(Icons.Default.Folder, "ROOT", viewModel.serverDirectory.split("/").last())
            }
        }
    }
}

@Composable
fun StatusBadge(isRunning: Boolean) {
    val color = if (isRunning) Color(0xFF00C853) else Color(0xFFFFAB00)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = if (isRunning) "ACTIVE" else "STANDBY",
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusInfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, size = 18.dp, tint = AccentCyan)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun Icon(icon: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}

@Composable
fun FileItemRow(file: File) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
            contentDescription = null,
            tint = if (file.isDirectory) AccentCyan else AccentPurple
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(file.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                if (file.isDirectory) "Directory" else "${file.length() / 1024} KB",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f))
    }
}