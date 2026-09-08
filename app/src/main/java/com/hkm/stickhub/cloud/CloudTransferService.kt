package com.hkm.stickhub.cloud

import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/** Keeps an explicit user-started transfer alive when the Activity is backgrounded. */
class CloudTransferService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("cloud_transfer", "Cloud backup", NotificationManager.IMPORTANCE_LOW))
        startForeground(7402, NotificationCompat.Builder(this, "cloud_transfer")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("StickHub cloud backup")
            .setContentText("Transferring your encrypted library…")
            .setOngoing(true).setOnlyAlertOnce(true).setProgress(0, 0, true).build())
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY
    override fun onTimeout(startId: Int, fgsType: Int) {
        CloudBackupOperations.getInstance(this).shutdown()
        stopSelf()
    }
    companion object {
        fun begin(context: Context) { ContextCompat.startForegroundService(context, Intent(context, CloudTransferService::class.java)) }
        fun end(context: Context) { context.stopService(Intent(context, CloudTransferService::class.java)) }
    }
}
