package com.javloadserver

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.javloadserver.server.HttpServerService
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class MainViewModel : ViewModel() {
    var isServerRunning by mutableStateOf(false)
    var serverUrl by mutableStateOf<String?>(null)
    var serverPort by mutableStateOf(8080)
    var serverDirectory by mutableStateOf("/sdcard/Download")
    var hasPassword by mutableStateOf(false)
    var files by mutableStateOf<List<File>>(emptyList())
    var ipAddresses by mutableStateOf<List<String>>(emptyList())

    fun refreshIpAddresses() {
        val ips = mutableListOf<String>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addresses = Collections.list(intf.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress) {
                        val hostAddress = addr.hostAddress
                        if (hostAddress != null && hostAddress.indexOf(':') < 0) {
                            ips.add(hostAddress)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        ipAddresses = ips
    }

    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    fun startServer(context: Context, port: Int, directory: String, password: String?) {
        serverPort = port
        serverDirectory = directory
        hasPassword = password != null
        
        val intent = Intent(context, HttpServerService::class.java).apply {
            putExtra("port", port)
            putExtra("directory", directory)
            putExtra("password", password)
        }
        
        ContextCompat.startForegroundService(context, intent)
        
        registerNetworkCallback(context)
        refreshIpAddresses()
        updateServerUrl()
        isServerRunning = true
        loadFiles()
    }

    fun stopServer(context: Context) {
        val intent = Intent(context, HttpServerService::class.java)
        context.stopService(intent)
        unregisterNetworkCallback(context)
        isServerRunning = false
        serverUrl = null
    }

    private fun registerNetworkCallback(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                refreshIpAddresses()
                updateServerUrl()
            }
            override fun onLost(network: android.net.Network) {
                refreshIpAddresses()
                updateServerUrl()
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    private fun unregisterNetworkCallback(context: Context) {
        networkCallback?.let {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }

    private fun updateServerUrl() {
        if (ipAddresses.isNotEmpty()) {
            serverUrl = "http://${ipAddresses[0]}:$serverPort"
        } else {
            serverUrl = "http://localhost:$serverPort"
        }
    }

    fun loadFiles() {
        try {
            val dir = File(serverDirectory)
            if (dir.exists()) {
                files = dir.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })?.toList() ?: emptyArray<File>().toList()
            } else {
                files = emptyList()
            }
        } catch (e: Exception) {
            files = emptyList()
        }
    }
}
