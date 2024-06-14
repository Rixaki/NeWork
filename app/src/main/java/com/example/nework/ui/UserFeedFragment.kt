package com.example.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.nework.adapter.UserAdapter
import com.example.nework.databinding.FragmentFeedUserBinding
import com.example.nework.vm.UsersSelectorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFeedFragment : Fragment() {

    private var _binding: FragmentFeedUserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val usersViewModel by viewModels<UsersSelectorViewModel>()

        _binding = FragmentFeedUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = UserAdapter()
        binding.list.adapter = adapter

        usersViewModel.users.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                binding.list.smoothScrollToPosition(0)
            }
            binding.emptyText.isVisible = list.size == 0
        }

        usersViewModel.usersState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.statusText.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
        }

        binding.swiperefresh.setOnRefreshListener {
            usersViewModel.refresh()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}