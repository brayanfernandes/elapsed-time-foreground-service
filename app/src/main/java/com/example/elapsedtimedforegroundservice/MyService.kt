package com.example.elapsedtimedforegroundservice

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


private const val TAG = "MyService"

class MyService : Service() {
    private val mBinder = MyBinder()
    private var serviceRunning = false
    private var job:Job = Job()
    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    var elapsedTime: Long = 0
        private set

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
        createNotification()
        serviceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        intent?.action?.also {
            if(it == STOP_SERVICE_ACTION){
                stopForeground(true)
                stopSelf()
            }
            else if(it == UPDATE_ELAPSED_TIME){
                intent.getStringExtra(ELAPSED_TIME)?.also {
                    elapsedTime = it.toLong()
                    startTimer()
                }
            }
        }


        createNotification()
        return START_NOT_STICKY
    }

    private fun startTimer() {
        runBlocking {
            job.cancelAndJoin()
        }
        job = coroutineScope.launch {
            while (isActive) {
                elapsedTime += 1
                createNotification(update=true)
                delay(1000L)
                Log.d(TAG, elapsedTime.toString())
            }
        }
    }

    override fun onDestroy() {
        serviceRunning = false
        coroutineScope.cancel()
        job.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun createNotification(update:Boolean = false) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val pendindIntent: PendingIntent = Intent(
            this,
            MainActivity::class.java
        ).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        }
        val formattedTime = Utils.formatTime(elapsedTime)

        val stopIntent = Intent(this,MyService::class.java).apply {
            action = STOP_SERVICE_ACTION
        }
        val stopPendingIntent = PendingIntent.getService(this,0,stopIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_baseline_access_time_24)
            .setContentTitle("Elapsed Time")
            .setContentText("Seconds: ${formattedTime}")
            .setContentIntent(pendindIntent)
            .addAction(R.drawable.ic_baseline_stop_circle_24,"Stop",stopPendingIntent)
            .build()
        if(update){
            notificationManager.notify(NOTIFICATION_ID,notification)
        }
        else{
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    inner class MyBinder : Binder() {
        val service: MyService
            get() = this@MyService
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "ChannelId"
        const val STOP_SERVICE_ACTION = "StopService"
        const val ELAPSED_TIME = "ElapsedTime"
        const val UPDATE_ELAPSED_TIME = "UpdateElapsedTime"
    }
}