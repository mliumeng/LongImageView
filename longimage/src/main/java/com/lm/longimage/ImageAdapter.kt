package com.lm.longimage

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder


/**
 *
 * @author liumengchn@gmail.com
 * @date 11/16/2019
 * @version 1.0
 * @des 简介
 * @Copyright 2018 Shanghai Tuile Information Technology Service Co., Ltd. All rights reserved.
 */
class ImageAdapter : BaseQuickAdapter<Int, BaseViewHolder> {
    var longImageView: LongImageView? = null

    constructor(layoutRes: Int, bitmaps: List<Int>) : super(layoutRes, bitmaps)

    override fun convert(helper: BaseViewHolder?, item: Int?) {
        val view = helper?.getView<ImageView>(R.id.ivLongImageItem)
        if (item != null) {
            val currentBitmap = longImageView?.getCurrentBitmap(item, itemCount)
            view?.setImageBitmap(currentBitmap)
            view?.tag = currentBitmap
        }
    }

    override fun bindToRecyclerView(recyclerView: RecyclerView?) {
        super.bindToRecyclerView(recyclerView)
        this.longImageView = recyclerView as LongImageView
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        val view = holder.getView<ImageView>(R.id.ivLongImageItem)
        if (view.tag is Bitmap) {
            (view.tag as Bitmap).recycle()
        }
        view.tag = null
    }
}