package com.example.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework.BuildConfig.BASE_URL
import com.example.nework.databinding.ItemInFeedUserBinding
import com.example.nework.dto.Post
import com.example.nework.dto.SelectableUser
import com.example.nework.dto.User
import ru.netology.nmedia.util.loadAvatar

class UserSelectableAdapter() :
    ListAdapter<SelectableUser, SelectableUserViewHolder>(SelectableUserDiffCallBack) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectableUserViewHolder {
        val view = ItemInFeedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SelectableUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectableUserViewHolder, position: Int) {
        val slctUser = getItem(position)
        holder.bind(slctUser)
    }

    override fun onBindViewHolder(
        holder: SelectableUserViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            val slctUser = getItem(position)
            holder.bind(slctUser)
        } else {
            val slctUser = getItem(position)
            payloads.forEach {
                (it as SelectablePayload)?.let { payload ->
                    holder.bind(it, slctUser.isPicked)
                }
            }
        }
    }
}

class SelectableUserViewHolder(
    private val binding: ItemInFeedUserBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(slctUser: SelectableUser) {
        with(binding) {
            checkbox.visibility = View.VISIBLE
            checkbox.isChecked = slctUser.isPicked

            val user = slctUser.user

            val baseUrl = "$BASE_URL/"
            avatar.loadAvatar(baseUrl + user.avatar)

            login.visibility = View.GONE
            name.text = user.name
        }
    }

    fun bind(
        payload: SelectablePayload,
        startState: Boolean
    ) = payload.picked?.let {
        binding.checkbox.setOnClickListener {
            binding.checkbox.isChecked = !startState
        }
    }
}

//for anti-flinking from updating
data class SelectablePayload(
    val picked: Boolean? = null
)

object SelectableUserDiffCallBack :
    DiffUtil.ItemCallback<SelectableUser>() {
    //object without data better that class
    override fun areContentsTheSame(oldItem: SelectableUser, newItem: SelectableUser): Boolean =
        (oldItem.id == newItem.id)

    override fun areItemsTheSame(oldItem: SelectableUser, newItem: SelectableUser): Boolean =
        (oldItem == newItem)

    override fun getChangePayload(
        oldItem: SelectableUser,
        newItem: SelectableUser
    ): SelectablePayload? =
        SelectablePayload(picked = newItem.isPicked.takeIf { it != oldItem.isPicked })
}