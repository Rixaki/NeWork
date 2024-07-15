package com.example.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework.R
import com.example.nework.databinding.ItemInFeedJobBinding
import com.example.nework.dto.Job


interface OnIterationJobListener {
    fun onEditLtn(job: Job) {}
    fun onRemoveLtn(job: Job) {}
    fun onLinkLtn(job: Job) {}
}

class JobAdapter(
    private val onIterationJobListener: OnIterationJobListener
) : ListAdapter<Job, JobViewHolder>(JobDiffCallBack) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder {
        val view = ItemInFeedJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(view, onIterationJobListener)
    }


    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: ItemInFeedJobBinding,
    private val onIterationJobListener: OnIterationJobListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        with(binding) {

            companyName.text = job.name
            position.text = job.position

            startTime.text = job.start
            finishTime.text = job.finish ?: "No finish"

            link.setOnClickListener {
                onIterationJobListener.onLinkLtn(job)
            }

            menu.isVisible = job.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_item)
                    menu.setGroupVisible(R.id.owned, job.ownedByMe)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                onIterationJobListener.onEditLtn(job)
                                true
                            }

                            R.id.remove -> {
                                onIterationJobListener.onRemoveLtn(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }//with binding
    }
}

//no payload mechanic for job

object JobDiffCallBack :
    DiffUtil.ItemCallback<Job>() { //object without data better that class without data
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
        (oldItem.id == newItem.id)

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean =
        (oldItem == newItem)
}
