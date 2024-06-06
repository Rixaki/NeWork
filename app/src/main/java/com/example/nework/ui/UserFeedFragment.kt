package com.example.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
        val usersViewModel =
            ViewModelProvider(this).get(UsersSelectorViewModel::class.java)

        _binding = FragmentFeedUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = UserAdapter()
        binding.list.adapter = adapter

        usersViewModel.users.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) {
                binding.list.smoothScrollToPosition(0)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}