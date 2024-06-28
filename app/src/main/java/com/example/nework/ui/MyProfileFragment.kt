package com.example.nework.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.nework.R
import com.example.nework.adapter.JobAdapter
import com.example.nework.adapter.OnIterationJobListener
import com.example.nework.adapter.OnIterationPostListener
import com.example.nework.adapter.PostAdapter
import com.example.nework.databinding.FragmentUserBinding
import com.example.nework.dto.Job
import com.example.nework.dto.Post
import com.example.nework.dto.User
import com.example.nework.ui.NewOrEditPostFragment.Companion.textArg
import com.example.nework.ui.PostFragment.Companion.longArg
import com.example.nework.ui.UserFragment.Companion.USER_ID
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.JobViewModel
import com.example.nework.vm.JobViewModelFactory
import com.example.nework.vm.PostByUserViewModel
import com.example.nework.vm.PostByUserViewModelFactory
import com.example.nework.vm.PostViewModel
import com.example.nework.vm.PostViewModelFactory
import com.example.nework.vm.UserViewModel
import com.example.nework.vm.UsersViewModel
import com.example.nework.vm.UsersViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.util.loadAvatar
import ru.netology.nmedia.util.toast

@AndroidEntryPoint
class MyProfileFragment : Fragment() {
    /*
    companion object {
        private const val USER_ID = "USER_ID"
        var Bundle.USER_ID: Long by LongArg//for value by main_activity

        //maybe set value in viewmodel
        fun createArgs(id: Long): Bundle =
            bundleOf(USER_ID to id)
    }
     */

    private val authModel by viewModels<AuthViewModel>()

    //only for wall list
    private val jobModel: JobViewModel by activityViewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<JobViewModelFactory> { factory ->
                @Suppress("DEPRECATION")
                factory.create(authModel.userId)//checked by prev fragment
            }
        }
    )
    private val wallModel: PostByUserViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<PostByUserViewModelFactory> { factory ->
                @Suppress("DEPRECATION")
                factory.create(authModel.userId)//checked by prev fragment
            }
        }
    )

    //for post manipulations
    private val postModel: PostViewModel by activityViewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<PostViewModelFactory> { factory ->
                @Suppress("DEPRECATION")
                factory.create(true)//wall
            }
        }
    )

    private val userModel by viewModels<UserViewModel>()
    /*
    private val userModel by viewModels<UsersViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<UsersViewModelFactory> { factory ->
                @Suppress("DEPRECATION")
                factory.create(emptyList())
            }
        }
    )
     */

    @SuppressLint("StringFormatMatches")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentUserBinding.inflate(layoutInflater, container, false)
        binding.addJob.isVisible = true
        binding.addPost.isVisible = true

        val myId = authModel.userId

        if ((authModel.authenticated) && (myId != null) && (myId != 0L)) {
            /*
            val user = try {
                userModel.getUserById(myId)
            } catch (_: Exception) {
                User(id=0, name = "", login = "", avatar = "404")//for none throwable
            }
             */
            userModel.setUserById(myId)

            userModel.state.asLiveData(Dispatchers.Default).observe(viewLifecycleOwner){ state ->
                if(state.error){
                    toast(
                        getString(
                            R.string.failure_with_users_info_error_code,
                            state.lastErrorAction
                        ))
                    findNavController().navigate(R.id.action_global_to_postsFeedFragment)
                    val bottomBarView = requireActivity().findViewById<NavigationBarView>(R.id.bottom_navigation)
                    bottomBarView.selectedItemId = R.id.posts
                }
            }

            binding.addJob.setOnClickListener {
                findNavController().navigate(
                    R.id.action_myProfileFragment_to_newOrEditJobFragment,
                    Bundle().apply {
                        USER_ID = authModel.userId
                    })
            }

            binding.addPost.setOnClickListener {
                findNavController().navigate(
                    R.id.action_myProfileFragment_to_newOrEditPostFragment
                )
            }

            val wallAdapter = PostAdapter(object : OnIterationPostListener {
                override fun onLikeLtn(post: Post) {
                    if (authModel.authenticated) {
                        postModel.likeById(post.id)
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
                    postModel.edited.value = post
                    findNavController().navigate(
                        R.id.action_postsFeedFragment_to_newOrEditPostFragment,
                        Bundle().apply {
                            textArg = post.content
                        })
                }

                override fun onRemoveLtn(post: Post) {
                    postModel.removeById(post.id)
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
                    val dialog =
                        GetUserListDialogFragment(post.mentionIds, R.string.list_of_mentioned_users)
                    dialog.show(activity!!.supportFragmentManager, "List mention dialog.")
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
            })//wall_adapter

            val jobAdapter = JobAdapter(object : OnIterationJobListener {
                override fun onEditLtn(job: Job) {
                    jobModel.changeJob(job)
                    findNavController().navigate(
                        R.id.action_myProfileFragment_to_newOrEditJobFragment
                    )
                }

                override fun onLinkLtn(job: Job) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.link))
                    try {
                        startActivity(intent)
                    } catch (ex: ActivityNotFoundException) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.link_does_not_lead_to_the_site),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        onPlayVideoLtn@ return
                    }
                }

                override fun onRemoveLtn(job: Job) {
                    jobModel.removeById(job.id)
                }
            })//job_adapter

            binding.nameAndLogin.text = "... loading user (id ${authModel.userId})"
            userModel.user.observe(viewLifecycleOwner) {
                val user = userModel.user.value
                with(binding) {
                    avatar.loadAvatar(user?.avatar ?: "404")
                    nameAndLogin.text = "${user?.name} / ${user?.login}"
                }
            }

            with(binding) {
                wall.setOnClickListener {
                    swiperefreshPost.isVisible = true
                    wallModel.isActiveView.value = true

                    swiperefreshJob.isVisible = false
                    jobModel.isActiveView.value = false
                }
                jobs.setOnClickListener {
                    swiperefreshPost.isVisible = false
                    wallModel.isActiveView.value = false

                    swiperefreshJob.isVisible = true
                    jobModel.isActiveView.value = true
                }

                wallModel.isActiveView.observe(viewLifecycleOwner) { state ->
                    if (state) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                wallModel.dataById.collectLatest(wallAdapter::submitData)
                            }
                        }
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                wallAdapter.loadStateFlow.collectLatest { state ->
                                    binding.swiperefreshPost.isRefreshing =
                                        state.refresh is LoadState.Loading
                                }
                            }
                        }
                    }
                }

                jobModel.isActiveView.observe(viewLifecycleOwner) { state ->
                    if (state) {
                        jobModel.jobList.asLiveData().observe(viewLifecycleOwner) { list ->
                            jobAdapter.submitList(list)
                        }
                    }
                }
            }
        } else {
            toast(getString(R.string.you_are_not_authenticated))
            this.onDestroy()
        }

        return binding.root
    }

    override fun onDestroy() {
        postModel.clearModels()
        userModel.clearModel()
        super.onDestroy()
    }
}