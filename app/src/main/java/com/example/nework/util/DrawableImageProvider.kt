package com.example.nework.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.yandex.runtime.image.ImageProvider

class DrawableImageProvider(
    private val context: Context,
    @DrawableRes
    private val id: Int,
    @ColorInt
    private val tintColor: Int? = null
) : ImageProvider() {
    private val imageInfo = ImageInfo(id, tintColor)

    override fun getImage() =
        context.getBitmapFromVectorDrawable(imageInfo.id, imageInfo.tintColor)

    override fun getId() = imageInfo.toString()
}

private data class ImageInfo(@DrawableRes val id: Int, @ColorInt val tintColor: Int? = null)

private fun Context.getBitmapFromVectorDrawable(drawableId: Int, tintColor: Int?): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    tintColor?.also {
        drawable.setTint(it)
    }

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}