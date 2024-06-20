package com.example.nework.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import com.yandex.runtime.Runtime.init

//https://stackoverflow.com/a/5763815
class LockableScrollView : ScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    var isScrollable: Boolean = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                // if we can scroll pass the event to the superclass
                return if (isScrollable) {
                    super.onTouchEvent(ev)
                }
                // only continue to handle the touch event if scrolling enabled
                else {
                    isScrollable
                }
            }

            else -> { return super.onTouchEvent(ev) }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        return isScrollable && super.onInterceptTouchEvent(ev)
    }
}
