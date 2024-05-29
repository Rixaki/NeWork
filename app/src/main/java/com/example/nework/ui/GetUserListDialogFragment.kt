package com.example.nework.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.nework.R
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.toast

@AndroidEntryPoint
class GetUserListDialogFragment(
    private val listIds: List<Int>,
    private val titleId: Int
) : DialogFragment() {
    private var _listView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val inflater: LayoutInflater = this@GetUserListDialogFragment.getLayoutInflater()
        dialog.setTitle(getString(titleId))
        return dialog
    }

    override fun onDestroy() {
        _listView = null
        super.onDestroy()
    }

    private fun errorDestroy() {
        //toast has NOT lifecycle, but getString method links with fragment lifecycle
        this@GetUserListDialogFragment.toast(getString(R.string.error_with_list_user_display))
        this@GetUserListDialogFragment.onDestroy()
    }
}