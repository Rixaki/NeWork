package com.example.nework.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nework.R
import com.example.nework.adapter.EventInCardViewHolder
import com.example.nework.adapter.OnIterationEventListener
import com.example.nework.databinding.FragmentPostOrEventBinding
import com.example.nework.dto.Event
import com.example.nework.dto.Post
import com.example.nework.ui.NewOrEditPostFragment.Companion.textArg
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.EventViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.util.IntArg

@AndroidEntryPoint
class EventFragment : Fragment() {
    companion object {
        var Bundle.intArg: Int by IntArg
    }

    private val viewModel: EventViewModel by activityViewModels()
    val authModel by viewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding =
            FragmentPostOrEventBinding.inflate(layoutInflater, container, false)

        val id = requireArguments().intArg

        val viewHolder = EventInCardViewHolder(binding, object : OnIterationEventListener {
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

            override fun onRootLtn(event: Event) {}
            override fun onMapLtn(event: Event) {}

            override fun onListPartLtn(event: Event) {
                val dialog =
                    GetUserListDialogFragment(event.participantsIds, R.string.list_of_participants)
                dialog.show(requireActivity().supportFragmentManager, "List participants dialog.")
            }

            override fun onListSpeaksLtn(event: Event) {
                val dialog = GetUserListDialogFragment(event.speakerIds, R.string.list_of_speakers)
                dialog.show(requireActivity().supportFragmentManager, "List speakers dialog.")
            }
        })// val viewHolder

        val event = viewModel.getEventById(id)
        if (event.published == "") {
            findNavController().navigateUp()
        } else {
            viewHolder.bind(event)
        }

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    binding.attachmentIv.setImageURI(fileUri)
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(data),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Image task Cancelled",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        /*
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val hasNewPost: Boolean = adapter.currentList.size < posts.size
            adapter.submitList(posts) {// update
                if (hasNewPost) {
                    binding.list.smoothScrollToPosition(0)//submitlist is ansync!!!
                }
            }
        }
        */

        return binding.root
    }//onCreateView
}