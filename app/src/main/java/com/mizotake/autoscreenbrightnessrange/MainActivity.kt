package com.mizotake.autoscreenbrightnessrange

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import it.sephiroth.android.library.rangeseekbar.RangeSeekBar
import it.sephiroth.android.library.rangeseekbar.RangeSeekBar.OnRangeSeekBarChangeListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var resisted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val (start, end) = rangeValue
        rangeSeekBar.setProgress(start, end)
        text_view.text = "Screen Brightness : ${rangeSeekBar.progressStart} ~ ${rangeSeekBar.progressEnd}"

        if(!canWrite){
            rangeSeekBar.isEnabled = false
            allowWritePermission()
        }

        rangeSeekBar.setOnRangeSeekBarChangeListener(object : OnRangeSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: RangeSeekBar,
                progressStart: Int,
                progressEnd: Int,
                fromUser: Boolean
            ) {
                text_view.text = "Screen Brightness : ${progressStart} ~ ${progressEnd}"
                rangeValue = Pair(progressStart, progressEnd)
            }
            override fun onStartTrackingTouch(seekBar: RangeSeekBar) {}
            override fun onStopTrackingTouch(seekBar: RangeSeekBar?) {
                toast("設定を更新しました")
            }
        })

        val service = BackgroundService()
        val serviceIntent = Intent(application, service.javaClass)

        start_button.setOnClickListener {
            if (sensorManager == null) {
                this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                if (sensorManager != null) {
                    val lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
                    sensorManager!!.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    resisted = true
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            }
            Settings.System.putInt(applicationContext.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
        }

        stop_button.setOnClickListener {
            if (resisted) {
                sensorManager!!.unregisterListener(this)
                resisted = false
            }
            stopService(serviceIntent)
        }
    }

    private fun initlizeAdMob() {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)

        MobileAds.initialize(
            this,
            ""
        )

        val layout = RelativeLayout(this)
        setContentView(layout)

        val adMobView = AdView(this)
        adMobView.adUnitId = ""
        adMobView.adSize = AdSize.BANNER
        layout.addView(adMobView)

        adMobView.layoutParams = params

        val adRequest = AdRequest.Builder().build()
        adMobView.loadAd(adRequest)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (!canWrite) return
        var brightness = brightness
        val (start, end) = rangeValue
        brightness = clamp(brightness, start, end)
        setBrightness(brightness)
    }
}



