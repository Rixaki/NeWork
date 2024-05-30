package com.example.nework.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.example.nework.R
import com.example.nework.adapter.UserAdapter
import com.example.nework.api.AppApi
import com.example.nework.auth.AppAuth
import com.example.nework.databinding.DialogListUserBinding
import com.example.nework.vm.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.toast
import javax.inject.Inject

@AndroidEntryPoint
class GetUserListDialogFragment(
    private val listIds: List<Int>,
    private val titleId: Int
) : DialogFragment() {
    private var _listView: View? = null

    private val userListModel : UsersViewModel by viewModels()

    //for DI in userListModel
    @Inject
    lateinit var userIds : List<Int>
    @Inject
    lateinit var appAuth: AppAuth
    /*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val inflater: LayoutInflater = this@GetUserListDialogFragment.getLayoutInflater()
        dialog.setTitle(getString(titleId))
        return dialog
    }
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogListUserBinding.inflate(layoutInflater, container, false)

        val adapter = UserAdapter()
        binding.list.adapter = adapter

        userIds = listIds
        userListModel.list.asLiveData().observe(viewLifecycleOwner){ list ->
            adapter.submitList(list) {
                binding.list.smoothScrollToPosition(0)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.let{
            if (it != null) {
                it.setTitle(titleId)
            } else {
                errorDestroy()
            }
        }
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