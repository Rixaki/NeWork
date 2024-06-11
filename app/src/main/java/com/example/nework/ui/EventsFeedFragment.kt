package com.example.nework.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.nework.R
import com.example.nework.adapter.EventAdapter
import com.example.nework.adapter.ItemLoadingStateAdapter
import com.example.nework.adapter.OnIterationEventListener
import com.example.nework.databinding.FragmentFeedPostOrEventBinding
import com.example.nework.dto.Event
import com.example.nework.ui.NewOrEditPostFragment.Companion.textArg
import com.example.nework.ui.PostFragment.Companion.intArg
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.EventViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.util.toast

@AndroidEntryPoint
class EventsFeedFragment : Fragment() {
    private val viewModel: EventViewModel by activityViewModels()
    private val authModel : AuthViewModel by viewModels()

    private var _binding: FragmentFeedPostOrEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedPostOrEventBinding.inflate(inflater, container, false)
        //val root: View = binding.root

        val adapter = EventAdapter(object : OnIterationEventListener {
            override fun onLikeLtn(event: Event) {
                if (authModel.authenticated) {
                    viewModel.likeById(event.id)
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.action_not_yet_available))
                        .setMessage(getString(R.string.do_you_want_to_sign_in_for_like_add_posts_etc))
                        .setIcon(R.drawable.baseline_login_48)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.sign_in)) { _, _ ->
                            findNavController().navigate(R.id.action_global_to_signInFragment)
                        }
                        .show()
                }
            }

            override fun onParticipantLtn(event: Event) {
                if (authModel.authenticated) {
                    viewModel.takePartById(event.id)
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.action_not_yet_available))
                        .setMessage(getString(R.string.do_you_want_to_sign_in_for_like_add_posts_etc))
                        .setIcon(R.drawable.baseline_login_48)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setPositiveButton(getString(R.string.sign_in)) { _, _ ->
                            findNavController().navigate(R.id.action_global_to_signInFragment)
                        }
                        .show()
                }
            }

            override fun onEditLtn(event: Event) {
                viewModel.edited.value = event
                findNavController().navigate(
                    R.id.action_eventFragment_to_newOrEditEventFragment,
                    Bundle().apply {
                        textArg = event.content
                    })
            }

            override fun onRemoveLtn(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onPlayVideoLtn(event: Event) {
                if (!event.videoLink.isNullOrBlank()) {
                    val url = event.videoLink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    try {
                        startActivity(intent)
                    } catch (ex: ActivityNotFoundException) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.video_play_error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        onPlayVideoLtn@ return
                    }
                }
            }

            override fun onRootLtn(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFeedFragment_to_eventFragment,
                    Bundle().apply {
                        intArg = event.id
                    })
            }

            override fun onMapLtn(event: Event) {
                if (event.coords != null) {
                    val dialog = MapDialogFragment(event.coords.lat, event.coords.long)
                    dialog.show(activity!!.supportFragmentManager, "Map dialog.")
                } else {
                    toast("Map display error.")
                    onMapLtn@ return
                }
            }

            override fun onListPartLtn(event: Event) {
                if (event.participantsIds.isNotEmpty()) {
                    val dialog = GetUserListDialogFragment(
                        event.participantsIds,
                        R.string.list_of_participants
                    )
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "List participants dialog."
                    )
                } else {
                    toast(getString(R.string.no_participants_for_this_event))
                    onListPartLtn@ return
                }
            }

            override fun onListSpeaksLtn(event: Event) {
                if (event.speakerIds.isNotEmpty()) {
                    val dialog =
                        GetUserListDialogFragment(event.speakerIds, R.string.list_of_speakers)
                    dialog.show(requireActivity().supportFragmentManager, "List speakers dialog.")
                } else {
                    toast(getString(R.string.no_speakers_for_this_event))
                    onListSpeaksLtn@ return
                }
            }
        })//adapter

        binding.newPostButton.setOnClickListener {
            if (authModel.authenticated) {
                findNavController().navigate(R.id.action_postsFeedFragment_to_newOrEditPostFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.sign_in_for_sending_post),
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.action_global_to_signInFragment)
            }
        }

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ItemLoadingStateAdapter(adapter::refresh),
            footer = ItemLoadingStateAdapter(adapter::refresh)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swiperefresh.isRefreshing =
                        state.refresh is LoadState.Loading
                }
            }
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}