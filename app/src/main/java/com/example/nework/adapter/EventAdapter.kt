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
import com.example.nework.BuildConfig
import com.example.nework.BuildConfig.BASE_URL
import com.example.nework.R
import com.example.nework.databinding.CardAdBinding
import com.example.nework.databinding.CardTimeHeaderBinding
import com.example.nework.databinding.FragmentPostOrEventBinding
import com.example.nework.databinding.ItemInFeedPostOrEventBinding
import com.example.nework.dto.Ad
import com.example.nework.dto.Event
import com.example.nework.dto.EventType
import com.example.nework.dto.FeedItem
import com.example.nework.dto.TimeHeader
import com.example.nework.dto.TimeType
import com.example.nework.ui.MapDialogFragment
import com.example.nework.util.countToString
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.example.nework.util.load
import com.example.nework.util.loadAvatar


interface OnIterationEventListener {
    fun onLikeLtn(event: Event) {}
    fun onParticipantLtn(event: Event) {}
    fun onEditLtn(event: Event) {}
    fun onRemoveLtn(event: Event) {}
    fun onPlayVideoLtn(event: Event) {}
    fun onRootLtn(event: Event) {}
    fun onMapLtn(event: Event) {}

    fun onListPartLtn(event: Event) {}
    fun onListSpeaksLtn(event: Event) {}
}

class EventAdapter(
    private val onIterationEventListener: OnIterationEventListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(ItemDiffEventCallBack) {
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is TimeHeader -> R.layout.card_time_header
            is Event -> R.layout.item_in_feed_post_or_event
            else -> error("unknown item type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is TimeHeader -> (holder as? TimeHeaderViewHolder)?.bind(item)
            is Event -> (holder as? EventInFeedViewHolder)?.bind(item)
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
            is Event -> {
                if (payloads.isEmpty()) {
                    onBindViewHolder(holder as EventInFeedViewHolder, position)
                } else {
                    val event = getItem(position) as Event
                    payloads.forEach {
                        (it as? PayloadEvent)?.let { payload ->
                            (holder as? EventInFeedViewHolder)?.bind(
                                payload,
                                event.participantsIds.size
                            )
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
        //): EventInFeedViewHolder {
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
            EventInFeedViewHolder(view, onIterationEventListener)
        }
        else -> error("unknown item type: $viewType")
    }

}

class EventInFeedViewHolder(
    private val binding: ItemInFeedPostOrEventBinding,
    private val onIterationEventListener: OnIterationEventListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        with(binding) {
            eventGroup.visibility = View.VISIBLE

            val timeText : String = with (event.datetime) {
                this.take(10) + " " + this.take(16).drop(11) + " UTC"
            }
            eventTime.text = timeText

            val yesTextId = R.string.you_participants_in_this_event
            val noTextId = R.string.your_participation_not_been_noted
            participantStatus.isSelected = event.participatedByMe
            participantStatus.setText(if (participantStatus.isSelected) yesTextId else noTextId)
            participantStatus.setOnClickListener {
                onIterationEventListener.onParticipantLtn(event)
            }

            val isOnline = event.type == EventType.ONLINE
            binding.eventType.isSelected = isOnline
            binding.eventType.setText(if (isOnline)
                R.string.online else R.string.offline)
            binding.eventType.text = "  " + binding.eventType.text

            avatar.loadAvatar(url = event.authorAvatar ?: "404")

            authorName.text = event.author
            published.text = event.published
            cardContent.text = event.content

            likeIv.isChecked = event.likedByMe
            likeIv.text = countToString(event.likes)

            likeIv.setOnClickListener {
                likeIv.text =
                    countToString(event.likes + (if (event.likedByMe) -1 else 1))
                onIterationEventListener.onLikeLtn(event)
            }

            mapPoint.visibility = if (event.coords == null) View.GONE else View.VISIBLE
            mapPoint.setOnClickListener {
                MapDialogFragment(event.coords?.lat ?: 0.0, event.coords?.long ?: 0.0)
            }

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

            constrainLayout.setOnClickListener {
                onIterationEventListener.onRootLtn(event)
            }

            list1Iv.text = countToString(event.participantsIds.size)
            list2Iv.text = countToString(event.speakerIds.size)
            list1Iv.setOnClickListener {
                onIterationEventListener.onListPartLtn(event)
            }
            list2Iv.setOnClickListener {
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
                binding.likeIv.text =
                    countToString(startCount + (if (payloadEvent.likedByMe) -1 else 1))
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

        payloadEvent.content?.let {
            binding.cardContent.text = it
        }
        payloadEvent.participantByMe?.let { partStatus ->
            binding.participantStatus.setOnClickListener {
                //button settext in fragment
                binding.list1Iv.text =
                    countToString(startCount + (if (payloadEvent.participantByMe) -1 else 1))
            }
            ObjectAnimator.ofPropertyValuesHolder(
                binding.participantStatus,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F),
            ).start()
        }
    }
}//eventinfeedviewholder

class EventInCardViewHolder(
    private val binding: FragmentPostOrEventBinding,
    private val onIterationEventListener: OnIterationEventListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Event) {
        with(binding) {
            eventGroup.visibility = View.VISIBLE
            participantStatus.setOnClickListener {
                //textset: in fragment
                onIterationEventListener.onParticipantLtn(event)
            }

            avatar.loadAvatar(url = event.authorAvatar ?: "404")

            author.text = event.author
            publishedTime.text = event.published
            cardContent.text = event.content

            likeIv.isChecked = event.likedByMe
            likeIv.text = countToString(event.likes)

            likeIv.setOnClickListener {
                //likeIv.isChecked = !event.likedByMe
                likeIv.text =
                    countToString(event.likes + (if (event.likedByMe) -1 else 1))
                onIterationEventListener.onLikeLtn(event)
            }

            mapGroup.visibility = if (event.coords == null) View.GONE else View.VISIBLE
            if (event.coords != null) {
                mapView.mapWindow.map.move(
                    CameraPosition(
                        Point(event.coords.lat, event.coords.long),
                        17.0f, 150.0f, 30.0f
                    )
                )
            }

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

            constrainLayout.setOnClickListener {
                onIterationEventListener.onRootLtn(event)
            }

            list1Iv.text = countToString(event.participantsIds.size)
            list1Iv.setOnClickListener {
                onIterationEventListener.onListPartLtn(event)
            }
            list2Iv.text = countToString(event.speakerIds.size)
            list2Iv.setOnClickListener {
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
}//eventincardviewholder

//for anti-flinking from updating
data class PayloadEvent(
    val likedByMe: Boolean? = null,
    val participantByMe: Boolean? = null,
    val content: String? = null
)

object ItemDiffEventCallBack :
    DiffUtil.ItemCallback<FeedItem>() { //object without data better that class without data
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        (oldItem.id == newItem.id)

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        (oldItem == newItem)

    //for anti-flick
    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): PayloadEvent? =
        if ((oldItem is Event) && (newItem is Event)) {
            PayloadEvent(
                likedByMe = newItem.likedByMe.takeIf { it != oldItem.likedByMe },
                participantByMe = newItem.participatedByMe.takeIf { it != oldItem.participatedByMe },
                content = newItem.content.takeIf { it != oldItem.content },
            )
        } else null
}
