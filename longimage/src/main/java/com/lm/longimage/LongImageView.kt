package com.lm.longimage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.InputStream
import java.util.*

/**
 *
 * @author liumengchn@gmail.com
 * @date 11/16/2019
 * @version 1.0
 * @des 展示长图的View
 * @Copyright 2018 Shanghai Tuile Information Technology Service Co., Ltd. All rights reserved.
 */
val TAG = "LongImageView"

class LongImageView : RecyclerView {

    // 原始图片宽和高
    private var mImageHeight: Int = 0
    private var mImageWidth: Int = 0

    //是否需要裁剪
    private var needSplit = false
    // 高度大于 600 就要裁剪
    private var needSplitHeight = 600

    private var bitmapRegionDecoder: BitmapRegionDecoder? = null
    /**
     * 绘制区域
     */
    private var mRect: Rect = Rect()

    private var options: BitmapFactory.Options = BitmapFactory.Options()
    private var lastItemHeight = 0

    init {
        options.inPreferredConfig = Bitmap.Config.RGB_565
    }

    var splitImages = ArrayList<Int>()

    fun setImageRaw(raw: Int) {
        val inputStream = resources.openRawResource(raw)
        splitInputStream(inputStream)
    }

    fun setImageUrl(imageUrl: String) {
    }

    var imageAdapter: ImageAdapter? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        // 获取屏幕三分之一高度为默认高度
        val screenHeight = getScreenHeight(context)
        if (screenHeight > 500) {
            needSplitHeight = screenHeight / 3
            Log.d(TAG, "needSplitHeight = $needSplitHeight")
        }
    }


    /**
     * 根据图片流创建一个 BitmapRegionDecoder
     * @param ins The input stream that holds the raw data to be decoded into a
     *           BitmapRegionDecoder.
     */
    private fun splitInputStream(ins: InputStream) {
        bitmapRegionDecoder = BitmapRegionDecoder.newInstance(ins, false)
        mImageHeight = bitmapRegionDecoder?.height as Int
        mImageWidth = bitmapRegionDecoder?.width as Int
        if (mImageHeight > needSplitHeight) {
            needSplit = true
        }
        if (needSplit) {
            doSplit()
        } else {
            // todo 小于最小高度后
            val decodeStream = BitmapFactory.decodeStream(ins)
            splitImages.add(0)
        }
        ins.close()
        updateRecyclerView()
    }


    private fun doSplit() {
        splitImages.clear()
        var itemCount = mImageHeight / needSplitHeight
        lastItemHeight = mImageHeight % needSplitHeight
        if (lastItemHeight > 0) {
            itemCount += 1
        }
        mRect.left = 0
        mRect.right = mImageWidth

        for (i in 0 until itemCount) {
            splitImages.add(i)
        }
    }

    /**
     * i: position
     * itemCount: 总的item数
     * lastItemHeight: 最后一个item高度
     */
    public fun getCurrentBitmap(
        i: Int,
        itemCount: Int
    ): Bitmap? {
        mRect.top = i * needSplitHeight
        mRect.bottom = mRect.top + needSplitHeight
        if (i == itemCount) {
            mRect.bottom = mRect.top + lastItemHeight
        }
        return bitmapRegionDecoder?.decodeRegion(mRect, options)
    }


    private fun updateRecyclerView() {
        imageAdapter = ImageAdapter(R.layout.image_item, splitImages)
        layoutManager = LinearLayoutManager(context)
        adapter = imageAdapter
        imageAdapter?.bindToRecyclerView(this)
    }


    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    private fun getScreenHeight(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }

}