package com.example.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework.BuildConfig.BASE_URL
import com.example.nework.databinding.ItemInFeedUserBinding
import com.example.nework.dto.User
import ru.netology.nmedia.util.loadAvatar

class UserAdapter (): ListAdapter<User, UserViewHolder>(UserDiffCallBack){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val view = ItemInFeedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private  val binding: ItemInFeedUserBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind (user: User){
        with(binding) {
            checkbox.visibility = View.GONE

            val baseUrl = "$BASE_URL/"
            avatar.loadAvatar(baseUrl + user.avatar)

            login.text = user.login
            name.text = user.name
        }
    }
}

object UserDiffCallBack :
        DiffUtil.ItemCallback<User>() {
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        (oldItem.id == newItem.id)

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
        (oldItem == newItem)
}