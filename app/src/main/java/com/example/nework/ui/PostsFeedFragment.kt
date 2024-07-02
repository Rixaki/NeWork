package com.example.nework.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.nework.R
import com.example.nework.adapter.ItemLoadingStateAdapter
import com.example.nework.adapter.OnIterationPostListener
import com.example.nework.adapter.PostAdapter
import com.example.nework.databinding.FragmentFeedPostOrEventBinding
import com.example.nework.dto.Post
import com.example.nework.ui.NewOrEditPostFragment.Companion.textArg
import com.example.nework.ui.PostFragment.Companion.longArg
import com.example.nework.util.countToString
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.PostViewModel
import com.example.nework.vm.PostViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.util.toast

@AndroidEntryPoint
class PostsFeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<PostViewModelFactory> { factory ->
                factory.create(false)//no wall
            }
        }
    )
    private val authModel: AuthViewModel by viewModels()

    /*
    @Inject
    lateinit var appAuth: AppAuth
     */

    //private var _binding: FragmentFeedPostOrEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    //private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedPostOrEventBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : OnIterationPostListener {
            override fun onLikeLtn(post: Post) {
                if (authModel.authenticated) {
                    viewModel.likeById(post.id)
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

            override fun onEditLtn(post: Post) {
                viewModel.edited.value = post
                findNavController().navigate(
                    R.id.action_postsFeedFragment_to_newOrEditPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    })
            }

            override fun onRemoveLtn(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onPlayVideoLtn(post: Post) {
                if (!post.videoLink.isNullOrBlank()) {
                    val url = post.videoLink
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

            override fun onRootLtn(post: Post) {
                findNavController().navigate(
                    R.id.action_postsFeedFragment_to_postFragment,
                    Bundle().apply {
                        longArg = post.id
                    })
            }

            override fun onListMentLtn(post: Post) {
                if (post.mentionIds.isNotEmpty()) {
                    val dialog =
                        GetUserListDialogFragment(post.mentionIds, R.string.list_of_mentioned_users)
                    dialog.show(activity!!.supportFragmentManager, "List mention dialog.")
                } else {
                    toast(getString(R.string.no_mentioned_users_for_this_post))
                    onListMentLtn@ return
                }
            }

            override fun onMapLtn(post: Post) {
                if (post.coords != null) {
                    val dialog = MapDialogFragment(post.coords.lat, post.coords.long)
                    dialog.show(activity!!.supportFragmentManager, "Map dialog.")
                } else {
                    toast("Map display error.")
                    onMapLtn@ return
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

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.statusText.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
        }
        binding.retry.setOnClickListener {
            adapter.retry()
        }

        //NEWER BUTTON
        binding.freshPosts.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                delay(timeMillis = 10_000)
                viewModel.checkNewer()
            }
        }
        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            try { //this throwable
                binding.freshPosts.text =
                    getString(R.string.fresh_posts, countToString(count))
                val paddingTopPixels = if (count != 0) {
                    getResources().getDimensionPixelOffset(R.dimen.paddingListTop)
                } else 0
                binding.list.updatePadding(top = paddingTopPixels)
                binding.freshPosts.visibility =
                    if (count == 0) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                binding.freshPosts.visibility = View.GONE
            }
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }
}