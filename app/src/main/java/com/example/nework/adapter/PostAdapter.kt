package com.example.nework.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.example.nework.BuildConfig.BASE_URL
import com.example.nework.R
import com.example.nework.databinding.CardAdBinding
import com.example.nework.databinding.CardTimeHeaderBinding
import com.example.nework.databinding.FragmentPostOrEventBinding
import com.example.nework.databinding.ItemInFeedPostOrEventBinding
import com.example.nework.dto.Ad
import com.example.nework.dto.FeedItem
import com.example.nework.dto.Post
import com.example.nework.dto.TimeHeader
import com.example.nework.dto.TimeType
import com.example.nework.util.countToString
import com.example.nework.util.load
import com.example.nework.util.loadAvatar
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition


interface OnIterationPostListener {
    fun onLikeLtn(post: Post) {}
    fun onEditLtn(post: Post) {}
    fun onRemoveLtn(post: Post) {}
    fun onPlayVideoLtn(post: Post) {}
    fun onRootLtn(post: Post) {}
    fun onListMentLtn(post: Post) {}
    fun onMapLtn(post: Post) {}
}

class PostAdapter(
    private val onIterationPostListener: OnIterationPostListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(ItemDiffPostCallBack) {
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> {
                R.layout.card_ad
            }

            is TimeHeader -> {
                R.layout.card_time_header
            }

            is Post -> {
                R.layout.item_in_feed_post_or_event
            }

            else -> {
                error("unknown item type")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is TimeHeader -> (holder as? TimeHeaderViewHolder)?.bind(item)
            is Post -> (holder as? PostInFeedViewHolder)?.bind(item)
            else -> error("unknown item type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is TimeHeader -> (holder as? TimeHeaderViewHolder)?.bind(item)
            is Post -> {
                if (payloads.isEmpty()) {
                    onBindViewHolder(holder as PostInFeedViewHolder, position)
                } else {
                    val post = getItem(position) as Post
                    payloads.forEach {
                        (it as? PayloadPost)?.let { payload ->
                            (holder as? PostInFeedViewHolder)?.bind(payload, post.likes)
                        }
                    }
                }
            }

            else -> error("unknown item type")
        }
    }

    class AdViewHolder(
        private val binding: CardAdBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: Ad) {
            binding.apply {
                image.load(
                    url = "$BASE_URL/media/${ad.image}",
                    placeholderIndex = R.drawable.ad_placemarker_48,
                    errorIndex = R.drawable.ad_placemarker_48
                )
            }
        }
    }

    class TimeHeaderViewHolder(
        private val binding: CardTimeHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timeHeader: TimeHeader) {
            binding.title.setText(
                when (timeHeader.type) {
                    TimeType.TODAY -> R.string.today
                    TimeType.YESTERDAY -> R.string.yesterday
                    TimeType.LAST_WEEK -> R.string.last_week
                }
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = when (viewType) {
        R.layout.card_ad -> {
            val view = CardAdBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            AdViewHolder(view)
        }

        R.layout.card_time_header -> {
            val view = CardTimeHeaderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            TimeHeaderViewHolder(view)
        }

        R.layout.item_in_feed_post_or_event -> {
            val view = ItemInFeedPostOrEventBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            PostInFeedViewHolder(view, onIterationPostListener)
        }

        else -> error("unknown item type: $viewType")
    }
}

class PostInFeedViewHolder(
    private val binding: ItemInFeedPostOrEventBinding,
    private val onIterationPostListener: OnIterationPostListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            eventGroup.visibility = View.GONE

            avatar.loadAvatar(url = post.authorAvatar ?: "404")

            authorName.text = post.author
            published.text = post.published
            cardContent.text = post.content

            likeIv.isChecked = post.likedByMe
            likeIv.text = countToString(post.likes)

            likeIv.setOnClickListener {
                likeIv.text =
                    countToString(post.likes + (if (post.likedByMe) -1 else 1))
                onIterationPostListener.onLikeLtn(post)
            }

            mapPoint.visibility = if (post.coords == null) View.GONE else View.VISIBLE
            mapPoint.setOnClickListener {
                onIterationPostListener.onMapLtn(post)
            }

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

            constrainLayout.setOnClickListener {
                onIterationPostListener.onRootLtn(post)
            }

            list1Iv.text = countToString(post.mentionIds.size)
            list1Iv.setOnClickListener {
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
                binding.likeIv.text =
                    countToString(startCount + (if (payloadPost.likedByMe) -1 else 1))
            }
            if (like) {
                ObjectAnimator.ofPropertyValuesHolder(
                    binding.likeIv,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F),
                )
            } else {
                ObjectAnimator.ofFloat(binding.likeIv, View.ROTATION, 0F, 360F)
            }.start()
        }

        payloadPost.content?.let {
            binding.cardContent.text = it
        }
    }
}//postinfeedviewholder

class PostInCardViewHolder(
    private val binding: FragmentPostOrEventBinding,
    private val onIterationPostListener: OnIterationPostListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            eventGroup.visibility = View.GONE

            avatar.loadAvatar(url = post.authorAvatar ?: "404")

            author.text = post.author
            publishedTime.text = post.published
            cardContent.text = post.content

            likeIv.isChecked = post.likedByMe
            likeIv.text = countToString(post.likes)

            likeIv.setOnClickListener {
                likeIv.text =
                    countToString(post.likes + (if (post.likedByMe) -1 else 1))
                onIterationPostListener.onLikeLtn(post)
            }

            mapGroup.visibility = if (post.coords == null) View.GONE else View.VISIBLE
            if (post.coords != null) {
                mapView.mapWindow.map.move(
                    CameraPosition(
                        Point(post.coords.lat, post.coords.long),
                        17.0f, 150.0f, 30.0f
                    )
                )
            }

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

            constrainLayout.setOnClickListener {
                onIterationPostListener.onRootLtn(post)
            }

            list1Iv.text = countToString(post.mentionIds.size)
            list1Iv.setOnClickListener {
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
}//postincardviewholder

//for anti-flinking from updating
data class PayloadPost(
    val likedByMe: Boolean? = null,
    val content: String? = null
)

object ItemDiffPostCallBack :
    DiffUtil.ItemCallback<FeedItem>() { //object without data better that class without data
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        (oldItem.id == newItem.id)

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        (oldItem == newItem)

    //for anti-flick
    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): PayloadPost? =
        if ((oldItem is Post) && (newItem is Post)) {
            PayloadPost(
                likedByMe = newItem.likedByMe.takeIf { it != oldItem.likedByMe },
                content = newItem.content.takeIf { it != oldItem.content },
            )
        } else null
}
