package ru.netology.nmedia.util

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


fun View.hideKeyBoard() {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(
        windowToken,
        InputMethodManager.RESULT_UNCHANGED_HIDDEN
    )
    //imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_HIDDEN);
}

fun Fragment.toast(
    messageId: Int,
    context: Context = requireContext(),
    period: Int = Toast.LENGTH_LONG
    ) {
    Toast.makeText(
        context,
        getString(messageId),
        period
    ).show()
}

object AndroidUtils {

    fun hideKeyBoard(view: View) {
        val imm =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        //imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_HIDDEN);
    }


    fun View.focusAndShowKeyboard() {
        /**
         * This is to be called when the window already has focus.
         */
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It’s very important to remove this listener once we are done.
                            viewTreeObserver.removeOnWindowFocusChangeListener(
                                this
                            )
                        }
                    }
                })
        }
    }
}