package com.javloadserver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.javloadserver.server.HttpServerService
import com.javloadserver.ui.components.*
import com.javloadserver.ui.theme.JavLoadServerTheme
import java.io.File

@Composable
fun MainScreen(
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    var isServerRunning by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf<String?>(null) }
    var serverPort by remember { mutableStateOf(8080) }
    var serverDirectory by remember { mutableStateOf("/sdcard/Download") }
    var hasPassword by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    
    LaunchedEffect(isServerRunning) {
        if (isServerRunning) {
            loadFiles(serverDirectory) { files = it }
        }
    }
    
    if (!hasStoragePermission) {
        PermissionRequestScreen(
            onRequestPermission = onRequestPermission
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ServerStatusCard(
                isRunning = isServerRunning,
                serverUrl = serverUrl,
                port = serverPort,
                directory = serverDirectory,
                hasPassword = hasPassword,
                onStartServer = { showConfigDialog = true },
                onStopServer = { stopServer(context) { isServerRunning = false } },
                onShowQRCode = { showQRCodeDialog = true }
            )
            
            if (isServerRunning) {
                FileListCard(
                    files = files,
                    directory = serverDirectory,
                    onFileClick = { file ->
                        // Handle file click
                    },
                    onRefresh = { loadFiles(serverDirectory) { files = it } }
                )
            }
        }
        
        if (showConfigDialog) {
            ServerConfigDialog(
                isVisible = showConfigDialog,
                onDismiss = { showConfigDialog = false },
                onStartServer = { port, directory, password ->
                    serverPort = port
                    serverDirectory = directory
                    hasPassword = password != null
                    
                    startServer(context, port, directory, password) { url ->
                        serverUrl = url
                        isServerRunning = true
                        showConfigDialog = false
                    }
                }
            )
        }
        
        if (showQRCodeDialog && serverUrl != null) {
            QRCodeDialog(
                isVisible = showQRCodeDialog,
                serverUrl = serverUrl!!,
                onDismiss = { showQRCodeDialog = false }
            )
        }
    }
}

@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Storage Permission Required",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "JavLoadServer needs storage permission to serve files from your device.",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

private fun startServer(
    context: Context,
    port: Int,
    directory: String,
    password: String?,
    onStarted: (String) -> Unit
) {
    val intent = Intent(context, HttpServerService::class.java).apply {
        putExtra("port", port)
        putExtra("directory", directory)
        putExtra("password", password)
    }
    
    ContextCompat.startForegroundService(context, intent)
    
    // Generate server URL
    val serverUrl = "http://0.0.0.0:$port"
    onStarted(serverUrl)
}

private fun stopServer(context: Context, onStopped: () -> Unit) {
    val intent = Intent(context, HttpServerService::class.java)
    context.stopService(intent)
    onStopped()
}

private fun loadFiles(directory: String, onFilesLoaded: (List<File>) -> Unit) {
    try {
        val dir = File(directory)
        if (dir.exists()) {
            val fileList = dir.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })?.toList() ?: emptyList()
            onFilesLoaded(fileList)
        } else {
            onFilesLoaded(emptyList())
        }
    } catch (e: Exception) {
        onFilesLoaded(emptyList())
    }
}