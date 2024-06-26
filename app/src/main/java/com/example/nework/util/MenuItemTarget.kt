package com.example.nework.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.MenuItem
import androidx.core.graphics.drawable.toDrawable
import androidx.transition.Transition
import com.bumptech.glide.request.target.CustomTarget

internal class MenuItemTarget(
    private val context: Context,
    private val menuItem: MenuItem
) : CustomTarget<Bitmap>() {
    override fun onResourceReady(
        resource: Bitmap,
        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
    ) {
        menuItem.icon = resource.toDrawable(context.resources)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        // ignore
    }
}