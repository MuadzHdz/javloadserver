package com.javloadserver.server

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.javloadserver.JavLoadServerApplication
import com.javloadserver.MainActivity
import com.javloadserver.R

class HttpServerService : Service() {
    
    private var httpServer: AndroidHttpServer? = null
    private val NOTIFICATION_ID = 1001
    
    override fun onCreate() {
        super.onCreate()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = intent?.getIntExtra("port", 8080) ?: 8080
        val directory = intent?.getStringExtra("directory") ?: "/sdcard/Download"
        val password = intent?.getStringExtra("password")
        
        startForeground(NOTIFICATION_ID, createNotification())
        
        httpServer = AndroidHttpServer(port, directory, password)
        httpServer?.start()
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        httpServer?.stop()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, JavLoadServerApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("JavLoadServer")
            .setContentText("HTTP Server is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}