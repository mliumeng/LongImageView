package com.lm.longimageview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    val bitmapArray: ArrayList<Bitmap> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logtext.setOnClickListener {
        }

        object : CountDownTimer(1 * 1000 * 60 * 60, 500) {
            override fun onTick(millisUntilFinished: Long) {
                printMemory()
            }

            override fun onFinish() {

            }
        }.start()

    }


    fun longView(view: View) {
        ivNorImageView.visibility = View.GONE
        longImageView.visibility = View.VISIBLE
        ivNorImageViewScrollView.visibility = View.GONE
        longImageView.setImageRaw(R.raw.sample_long)
    }

    fun norView(view: View) {
        longImageView.visibility = View.GONE
        ivNorImageView.visibility = View.VISIBLE
        ivNorImageViewScrollView.visibility = View.VISIBLE
        ivNorImageView.setImageResource(R.drawable.ic_sample_long)
    }

    private fun printMemory() {
        val M = 1024 * 1024
        val r = Runtime.getRuntime()

        val max = ("最大可用内存:" + r.maxMemory())
        val currentEnable = ("当前可用内存:" + r.totalMemory())
        val currentFree = ("当前空闲内存:" + r.freeMemory())
        val currentUse = ("当前已使用内存:" + (r.totalMemory() - r.freeMemory()))
        val currentView =
            ("当前已显示的View :" + if (ivNorImageView.visibility == View.VISIBLE) "ivNorImageView" else "LongImageView"
                    )
        logtext.text = max + "\n" +
                currentEnable + "\n" +
                currentFree + "\n" +
                currentUse + "\n" +
                currentView + "\n"


    }

    fun clearAllView(view: View) {
        longImageView.visibility = View.GONE
        ivNorImageView.visibility = View.GONE
        ivNorImageViewScrollView.visibility = View.GONE
    }


}
