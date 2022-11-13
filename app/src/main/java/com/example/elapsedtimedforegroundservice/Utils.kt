package com.example.elapsedtimedforegroundservice

object Utils {
    fun formatTime(seconds:Long):String{
        val hh = seconds / 3600
        val mm = seconds % 3600 / 60
        val ss = seconds % 60
        return String.format("%02d:%02d:%02d", hh, mm, ss)
    }
}