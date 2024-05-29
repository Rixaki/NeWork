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
import com.example.nework.dto.Event
import com.example.nework.ui.MapDialogFragment
import com.example.nework.util.countToString
import ru.netology.nmedia.util.load
import ru.netology.nmedia.util.loadAvatar


interface OnIterationEventListener {
    fun onLikeLtn(event: Event) {}
    fun onEditLtn(event: Event) {}
    fun onRemoveLtn(event: Event) {}
    fun onPlayVideoLtn(event: Event) {}
    fun onRootLtn(event: Event) {}

    fun onListPartLtn(event: Event) {}
    fun onListSpeaksLtn(event: Event) {}
}

class EventAdapter(
    private val onIterationEventListener: OnIterationEventListener
) : ListAdapter<Event, EventViewHolder>(EventDiffCallBack) {
    override fun onBindViewHolder(
        holder: EventViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val event = getItem(position)
            payloads.forEach {
                (it as? PayloadEvent)?.let { payload ->
                    holder.bind(payload, event.likes)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventViewHolder {
        val view = ItemInFeedPostOrEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(view, onIterationEventListener)
    }


    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
    }
}

class EventViewHolder(
    private val binding: ItemInFeedPostOrEventBinding,
    private val onIterationEventListener: OnIterationEventListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        with(binding) {
            eventGroup.visibility = View.VISIBLE
            participantStatus.visibility =
                if (event.participatedByMe) View.VISIBLE else View.GONE

            val baseUrl = "$BASE_URL/"
            avatar.loadAvatar(url = baseUrl + event.authorAvatar)

            authorName.text = event.author
            published.text = event.published
            cardContent.text = event.content

            likeIv.isChecked = event.likedByMe
            likeIv.text = countToString(event.likes)

            likeIv.setOnClickListener {
                //likeIv.isChecked = !post.likedByMe
                likeIv.text =
                    countToString(event.likes + (if (event.likedByMe) -1 else 1))
                onIterationEventListener.onLikeLtn(event)
            }

            mapPoint.visibility = if (event.coords == null) View.GONE else View.VISIBLE
            mapPoint.setOnClickListener {
                MapDialogFragment(event.coords?.lat, event.coords?.long)
            }

            list1Iv.text = countToString(event.participantsIds.size)
            list2Iv.text = countToString(event.speakerIds.size)

            if (event.attachment != null) {
                val baseAttUrl = "$BASE_URL/media"

                with(event.attachment) {
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
                onIterationEventListener.onPlayVideoLtn(event)
            }

            playVideo.setOnClickListener {
                onIterationEventListener.onPlayVideoLtn(event)
            }

            if (!event.videoLink.isNullOrBlank()) {
                videoGroup.visibility = View.VISIBLE
            } else {
                videoGroup.visibility = View.GONE
            }

            postConstrainLayout.setOnClickListener {
                onIterationEventListener.onRootLtn(event)
            }

            //TODO: REALISATION
            list1Iv.setOnClickListener{
                onIterationEventListener.onListPartLtn(event)
            }
            list2Iv.setOnClickListener{
                onIterationEventListener.onListSpeaksLtn(event)
            }

            menu.isVisible = event.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_item)
                    menu.setGroupVisible(R.id.owned, event.ownedByMe)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onIterationEventListener.onEditLtn(event)
                                true
                            }

                            R.id.remove -> {
                                onIterationEventListener.onRemoveLtn(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }//with binding
    }

    fun bind(payloadEvent: PayloadEvent, startCount: Int) {
        payloadEvent.likedByMe?.let { like ->
            binding.likeIv.setOnClickListener {
                //likeIv.isChecked = !post.likedByMe
                binding.likeIv.text =
                    countToString(startCount + (if (payloadEvent.likedByMe) -1 else 1))
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

        payloadEvent.content?.let {
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
data class PayloadEvent(
    val likedByMe: Boolean? = null,
    val participantByMe: Boolean? = null,
    val content: String? = null
)

object EventDiffCallBack :
    DiffUtil.ItemCallback<Event>() { //object without data better that class without data
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
        (oldItem.id == newItem.id)

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
        (oldItem == newItem)

    //for anti-flick
    override fun getChangePayload(oldItem: Event, newItem: Event): PayloadEvent? =
        PayloadEvent(
            likedByMe = newItem.likedByMe.takeIf { it != oldItem.likedByMe },
            content = newItem.content.takeIf { it != oldItem.content },
            participantByMe = newItem.participatedByMe.takeIf { it != oldItem.participatedByMe }
        )
}
