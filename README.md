# Android 长图展示方案

## 问题

项目有个长图OOM的问题。项目长图使用 `ScrollView` 包裹 `ImageView` 的方式展示长图。就像下面这段代码
``` xml
  <ScrollView
        android:id="@+id/ivNorImageViewScrollView"
        android:layout_width="match_parent"
        android:visibility="gone"
        android:layout_height="match_parent">
        <ImageView
            android:id="@+id/ivNorImageView"
            android:adjustViewBounds="true"
            android:visibility="gone"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </ScrollView>
    
```
如果你确定图片不会内存溢出，仅仅是图片比控件长无法全部展示出来的话，可以使用这种方式。简单好用。滑动起来也很丝滑。

但是我们不行：图片太大，内存紧张。一下子加载到内存里必然会导致内存的暴增。一不小心可能就OOM了。于是就想着搞一个长图控件。

## LongImageView 萌芽

查阅了相关资料，学到了一个 `BitmapRegionDecoder` ，学习资料：[Android 高清加载巨图方案 拒绝压缩图片 -鸿洋_ (CSDN) ](https://blog.csdn.net/lmj623565791/article/details/49300989)

> BitmapRegionDecoder <br>
`BitmapRegionDecoder` 它就是把图片的截取指定的一部分给你一个返回给你一个 `bitmap`。`ImageView` 直接加载整张**长图片**加载到内存，但又不是直接展示出来，实在是浪费内存
`BitmapRegionDecoder` 就是把你能看到的部分给加载到内存就行了，相比 `ImageView`内存收益就越高。 

长图怎么加载。主要要弄清楚长图的特点与本质以及要解决的问题

- 特点：长 (List）
- 本质：图 (ImageView)
- 问题：内存太大 (Recycle 看不到的)

自然就联想到了 `RecyclerView` 又长，又有可以展示图片，又能回收看不到的。因此就敲定使用 `RecyclerView` + `ImageView`  的方案
去展示长图。那么将大象放进冰箱需要几步🧐？

### 步骤一： 伪切图

> 测试图片放到 `raw` 目录下，使用 openRawResource 拿到图片的 `inputStream`
>    ```kotlin 
>         val inputStream = resources.openRawResource(R.raw.sample)
>    ```

关键代码：存储图片的 Position

```kotlin
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
    
 ```
 上面代码可以看到 splitImages，并没有存放Bitmap 只是放了图片的 position ，这里的切图并没有把
 把图片切成 n 个 `bitmap` 而是算出来一共需要切几个 `bitmap` splitImages 里面存放着图片的
 position 当需要展示某个item 的时候在跟据  position 去切一块展示出来就好。

### 步骤二：设置图片

`RecyclerView` 使用都知道，这里贴出 `Adapter` 的关键代码说明一下

```kotlin
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
```

`recyclerView` 的 item 就是一个 `ImageView` 通过 getCurrentBitmap 方法做真正的切图。

```kotlin 
    /**
     * i: position
     * itemCount: 总的item数
     * lastItemHeight: 最后一个item高度
     */
    public fun getCurrentBitmap(i: Int, itemCount: Int): Bitmap? {
        mRect.top = i * needSplitHeight
        mRect.bottom = mRect.top + needSplitHeight
        if (i == itemCount) {
            mRect.bottom = mRect.top + lastItemHeight
        }
        return bitmapRegionDecoder?.decodeRegion(mRect, options)
    }

```

每一个 position 都读经一个 rect . 最终使用 `bitmapRegionDecoder` 传入rect 就能得到当前item的bitmap.

## 总结一下

`RecylerView` 负责拼接图片处理长图滑动事件

`BitmapRegionDecoder` 负责切图

`ImageView` 负责展示图片

**这只处于萌芽阶段**
