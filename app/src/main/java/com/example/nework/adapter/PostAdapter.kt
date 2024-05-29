package com.example.nework.adapter

import com.example.nework.dto.Post
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.nework.BuildConfig.BASE_URL
import com.example.nework.R
import com.example.nework.databinding.ItemInFeedPostOrEventBinding
import com.example.nework.ui.MapDialogFragment
import com.example.nework.util.countToString
import ru.netology.nmedia.util.load
import ru.netology.nmedia.util.loadAvatar


interface OnIterationPostListener {
    fun onLikeLtn(post: Post) {}
    fun onEditLtn(post: Post) {}
    fun onRemoveLtn(post: Post) {}
    fun onPlayVideoLtn(post: Post) {}
    fun onRootLtn(post: Post) {}
    fun onListMentLtn(post: Post) {}
}

class PostAdapter(
    private val onIterationPostListener: OnIterationPostListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack) {
    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val post = getItem(position)
            payloads.forEach {
                (it as? PayloadPost)?.let { payload ->
                    holder.bind(payload, post.likes)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val view = ItemInFeedPostOrEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(view, onIterationPostListener)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    /*
    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNullOrEmpty()){
            onBindViewHolder(holder, position)
        } else {
            for (pl : payloads) {
                if (pl.id == likeIv) {
                //some set image/animation
                }
                if (pl.id == shareIv) {
                //some set image/animation
                }
            }
        }
    }
     */
}

class PostViewHolder(
    private val binding: ItemInFeedPostOrEventBinding,
    private val onIterationPostListener: OnIterationPostListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            eventGroup.visibility=View.GONE

            val baseUrl = "$BASE_URL/"
            avatar.loadAvatar(url = baseUrl + post.authorAvatar)

            authorName.text = post.author
            published.text = post.published
            cardContent.text = post.content

            likeIv.isChecked = post.likedByMe
            likeIv.text = countToString(post.likes)

            likeIv.setOnClickListener {
                //likeIv.isChecked = !post.likedByMe
                likeIv.text =
                    countToString(post.likes + (if (post.likedByMe) -1 else 1))
                onIterationPostListener.onLikeLtn(post)
            }

            mapPoint.visibility = if (post.coords == null) View.GONE else View.VISIBLE
            mapPoint.setOnClickListener {
                MapDialogFragment(post.coords?.lat, post.coords?.long)
            }

            list1Iv.text = countToString(post.mentionIds.size)

            if (post.attachment != null) {
                val baseAttUrl = "$BASE_URL/media"

                with(post.attachment) {
                    attachmentIv.load(
                        url = baseAttUrl + this.url,
                        options = RequestOptions().fitCenter(),
                        toFullWidth = true
                    )
                }
            } else {
                attachmentIv.visibility = View.GONE
            }

            videoWallpaper.setOnClickListener {
                onIterationPostListener.onPlayVideoLtn(post)
            }

            playVideo.setOnClickListener {
                onIterationPostListener.onPlayVideoLtn(post)
            }

            if (!post.videoLink.isNullOrBlank()) {
                videoGroup.visibility = View.VISIBLE
            } else {
                videoGroup.visibility = View.GONE
            }

            postConstrainLayout.setOnClickListener {
                onIterationPostListener.onRootLtn(post)
            }

            //TODO: REALISATION
            list1Iv.setOnClickListener{
                onIterationPostListener.onListMentLtn(post)
            }

            menu.isVisible = post.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_item)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onIterationPostListener.onEditLtn(post)
                                true
                            }

                            R.id.remove -> {
                                onIterationPostListener.onRemoveLtn(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }//with binding
    }

    fun bind(payloadPost: PayloadPost, startCount: Int) {
        payloadPost.likedByMe?.let { like ->
            binding.likeIv.setOnClickListener {
                //likeIv.isChecked = !post.likedByMe
                binding.likeIv.text =
                    countToString(startCount + (if (payloadPost.likedByMe) -1 else 1))
            }
            if (like) {
                ObjectAnimator.ofPropertyValuesHolder(
                    binding.likeIv,
                    PropertyValuesHolder.ofFloat(View.SCALE_X,  1.0F, 1.2F, 1.0F),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y,  1.0F, 1.2F, 1.0F),
                )
            } else {
                ObjectAnimator.ofFloat(binding.likeIv, View.ROTATION, 0F, 360F)
            }.start()
        }

        payloadPost.content?.let {
            binding.cardContent.text = it
        }
    }

    /*
    likeIv.setOnClickListener {
        onIterationListener.onLikeLtn(post)
        if (!post.likedByMe) {
            //likeCount.setTextColor(0xFF0000FF.toInt())
            GlobalScope.launch {
                delay(1500) // In ms
                //Code after sleep
                //likeCount.setTextColor(0xFF777777.toInt())
            }
        }
    }
    */
}

//for anti-flinking from updating
data class PayloadPost(
    val  likedByMe: Boolean? = null,
    val content: String? = null
)

object PostDiffCallBack :
    DiffUtil.ItemCallback<Post>() { //object without data better that class without data
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
        (oldItem.id == newItem.id)

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
        (oldItem == newItem)

    //for anti-flick
    override fun getChangePayload(oldItem: Post, newItem: Post): PayloadPost? =
        PayloadPost(
            likedByMe = newItem.likedByMe.takeIf { it != oldItem.likedByMe },
            content = newItem.content.takeIf { it != oldItem.content },
        )
}
