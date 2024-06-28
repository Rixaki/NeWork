package com.example.nework.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.example.nework.R
import com.example.nework.adapter.OnIterationUserListener
import com.example.nework.adapter.UserAdapter
import com.example.nework.auth.AppAuth
import com.example.nework.databinding.DialogListUserBinding
import com.example.nework.dto.User
import com.example.nework.vm.UsersViewModel
import com.example.nework.vm.UsersViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import ru.netology.nmedia.util.toast
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
@AndroidEntryPoint
class GetUserListDialogFragment(
    private val listIds: List<Long>,
    private val titleId: Int
) : DialogFragment() {
    private var _listView: View? = null

    /*
    companion object {
        private const val DATE_EXTRA = "DATE_EXTRA"

        fun createArgs(ids: List<Int>): Bundle =
            bundleOf(DATE_EXTRA to ids)
    }
     */

    //for DI in userListModel
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
    ): View {
        val binding = DialogListUserBinding.inflate(layoutInflater, container, false)

        val userListModel by viewModels<UsersViewModel>(
            extrasProducer = {
                defaultViewModelCreationExtras.withCreationCallback<UsersViewModelFactory> { factory ->
                    @Suppress("DEPRECATION")
                    factory.create(listIds)
                }
            }
        )

        val adapter = UserAdapter(
            object : OnIterationUserListener {
                override fun onRootLtn(user: User) {}
            }
        )
        binding.list.adapter = adapter

        userListModel.list.asLiveData().observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                binding.list.smoothScrollToPosition(0)
            }
        }

        //dismiss() default?
        binding.okButton.setOnClickListener {
            this@GetUserListDialogFragment.onDestroy()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.let {
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