package com.example.elapsedtimedforegroundservice

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.elapsedtimedforegroundservice.ui.theme.ElapsedTimedForegroundServiceTheme
import com.example.elapsedtimedforegroundservice.ui.theme.Typography
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private var mService:MyService? = null
    private var mBound = false

    private var elapsedTime:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val tick = remember{ mutableStateOf(elapsedTime)}
            LaunchedEffect(Unit){
                while (true){
                    tick.value = elapsedTime + 1
                    elapsedTime = tick.value
                    delay(1000L)
                }
            }
            ElapsedTimedForegroundServiceTheme {
                Box(modifier =Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Column {
                        Text(text = Utils.formatTime(tick.value), style = Typography.h2)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onStop() {
        super.onStop()
        startForegroundService()
        unbindService()
    }
    private fun startForegroundService(){
        Log.d(TAG,"starting Foreground Service")
        Intent(this,MyService::class.java).also {
            it.action = MyService.UPDATE_ELAPSED_TIME
            it.putExtra(MyService.ELAPSED_TIME,elapsedTime.toString())
            startForegroundService(it)
        }
    }

    private fun stopService(){
        Log.d(TAG,"stopping service")
        Intent(this,MyService::class.java).also {
            stopService(it)
        }
    }

    private fun bindService(){
        Log.d(TAG,"bindingService")
        Intent(this,MyService::class.java).also {intent->
            bindService(intent,serviceConnection, 0)
        }
    }

    private fun unbindService(){
        Log.d(TAG,"unbindingService")
        unbindService(serviceConnection)
    }

    private val serviceConnection= object :ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            mService = (p1 as MyService.MyBinder).service
            mBound = true
            Log.d(TAG,"Service Connected")
            elapsedTime = mService?.elapsedTime ?: elapsedTime
            stopService()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mBound = false
            Log.d(TAG,"Service Disconnected")
        }

    }
}