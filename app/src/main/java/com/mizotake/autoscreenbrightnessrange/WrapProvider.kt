package com.mizotake.autoscreenbrightnessrange

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

val Context.canWrite: Boolean
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(this)
        }else{
            true
        }
    }


fun Context.allowWritePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }
}

var Context.rangeValue: Pair<Int, Int>
    get() {
        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        return Pair(pref.getInt("start", 0), pref.getInt("end", 0))
    }
    set(range: Pair<Int, Int>) {
        val (start, end) = range
        getSharedPreferences("settings", Context.MODE_PRIVATE).edit().apply {
            putInt("start", start)
            putInt("end", end)
            commit()
        }
    }

val Context.brightness: Int
    get() {
        return Settings.System.getInt(
            this.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            0
        )
    }

fun Context.setBrightness(value: Int) {
    Settings.System.putInt(
        this.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        value
    )
}


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.clamp(value: Int, start: Int, end: Int): Int {
    return when {
        value < start -> {
            start
        }
        value > end -> {
            end
        }
        else -> {
            value
        }
    }
}