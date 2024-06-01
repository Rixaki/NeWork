package com.example.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework.R
import com.example.nework.adapter.UserAdapter
import com.example.nework.adapter.UserSelectableAdapter
import com.example.nework.databinding.FragmentListSelectorBinding
import com.example.nework.databinding.FragmentNewOrEditPostOrEventBinding
import com.example.nework.dto.User
import com.example.nework.ui.NewOrEditPostFragment.Companion.textArg
import com.example.nework.vm.PostViewModel
import com.example.nework.vm.UsersSelectorViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.StringArg

@AndroidEntryPoint
class SelectUserListFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    private val selectorModel: UsersSelectorViewModel by viewModels()
    companion object {
        var Bundle.titleArg: String? by StringArg
    }

    init {
        selectorModel.setAllSelectorList(viewModel.list.value ?: emptyList())
    }

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

        binding.title.setText(arguments?.titleArg ?: getString(R.string.select_users))

        val adapter = UserSelectableAdapter()
        binding.list.adapter = adapter
        selectorModel.list.observe(viewLifecycleOwner){ list ->
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