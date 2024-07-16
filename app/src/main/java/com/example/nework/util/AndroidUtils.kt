package com.example.nework.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(
    message: String,
    context: Context = requireContext(),
    period: Int = Toast.LENGTH_LONG
) {
    Toast.makeText(
        context,
        message,
        period
    ).show()
}

object AndroidUtils {

    fun hideKeyBoard(view: View) {
        val imm =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}