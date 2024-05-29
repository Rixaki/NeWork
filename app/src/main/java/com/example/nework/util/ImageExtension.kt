package ru.netology.nmedia.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.nework.R

fun ImageView.load(
    url: String,
    timeOut: Int = 30_000,
    placeholderIndex: Int = R.drawable.baseline_image_search_48,
    errorIndex: Int = R.drawable.baseline_image_not_supported_48,
    options: RequestOptions = RequestOptions(),
    toFullWidth: Boolean = false
) {
    Glide.with(this)
        .load(url)
        .timeout(timeOut)
        .placeholder(placeholderIndex)
        .error(errorIndex)
        .apply(options)
        .apply {
            if (toFullWidth)
                this.into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        this@load.setImageDrawable(resource)
                        val layoutParams = this@load.layoutParams
                        val widthOriginal = resource.intrinsicWidth
                        val heightOriginal = resource.intrinsicHeight

                        val displayMetrics = context.resources.displayMetrics
                        val screenWidth = displayMetrics.widthPixels
                        layoutParams.width = screenWidth

                        val calculatedHeight =
                            (screenWidth.toFloat() / widthOriginal.toFloat() * heightOriginal).toInt()
                        layoutParams.height = calculatedHeight
                        this@load.layoutParams = layoutParams
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        this@load.setImageDrawable(placeholder)
                    }
                })
        }
        .into(this)
}

fun ImageView.loadAvatar(url: String) =
    load(url = url,
        placeholderIndex = R.drawable.baseline_account_circle_48,
        options = RequestOptions().circleCrop())

    //method from lesson
    fun ImageView.simpleLoad(
        url: String,
        vararg transforms: BitmapTransformation = emptyArray()
    ) =
        Glide.with(this)
            .load(url)
            .timeout(10_000)
            .transform(*transforms)
            .into(this)//this method not used

    //method from lesson
    fun ImageView.loadCircleCrop(
        url: String,
        vararg transforms: BitmapTransformation = emptyArray()
    ) = simpleLoad(url, CircleCrop(), *transforms)//this method not used