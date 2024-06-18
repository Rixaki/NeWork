package com.example.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework.R
import com.example.nework.adapter.UserSelectableAdapter
import com.example.nework.databinding.FragmentListSelectorBinding
import com.example.nework.vm.PostViewModel
import com.example.nework.vm.PostViewModelFactory
import com.example.nework.vm.UsersSelectorViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import ru.netology.nmedia.util.StringArg

@AndroidEntryPoint
class SelectUserListByPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels(extrasProducer = {
        defaultViewModelCreationExtras.withCreationCallback<PostViewModelFactory> { factory ->
            @Suppress("DEPRECATION")
            factory.create(false)//no wall
        }
    })
    private val selectorModel: UsersSelectorViewModel by viewModels()

    companion object {
        var Bundle.titleArg: String? by StringArg
    }

    /*
    //throws Fragment$InstantiationException (doubled in on_create_view)
    init {
        selectorModel.setAllSelectorList(viewModel.list.value ?: emptyList())
        selectorModel.sortList()
    }
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentListSelectorBinding.inflate(
            layoutInflater,
            container,
            false
        )

        selectorModel.setAllSelectorList(viewModel.list.value ?: emptyList())
        selectorModel.sortList()

        selectorModel.usersState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                val snackbar = Snackbar.make(
                    binding.root,
                    getString(R.string.error_bar_start_text) + state.lastErrorAction,
                    10_000//milliseconds
                )
                snackbar
                    .setTextMaxLines(3)
                    .setAction("OK") {
                        snackbar.dismiss()
                    }
                    .show()
            }
        }

        binding.title.setText(arguments?.titleArg ?: getString(R.string.select_users))

        val adapter = UserSelectableAdapter()
        binding.list.adapter = adapter
        selectorModel.list.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.list.smoothScrollToPosition(0)
        }

        binding.okButton.setOnClickListener {
            val listIds = selectorModel.list.value
            val listSUs = selectorModel.getPickedIdsList(listIds ?: emptyList())
            viewModel.changeMentionList(listSUs)
            selectorModel.clearModels()
            findNavController().navigateUp()
        }

        binding.cancellButton.setOnClickListener {
            selectorModel.clearModels()
            findNavController().navigateUp()
        }

        return binding.root
    }
}