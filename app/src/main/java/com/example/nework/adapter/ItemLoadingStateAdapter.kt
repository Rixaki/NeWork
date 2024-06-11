package com.example.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nework.databinding.ItemLoadingBinding

class ItemLoadingStateAdapter(
    private val retryListener: () -> Unit
) : LoadStateAdapter<ItemLoadingViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ItemLoadingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemLoadingViewHolder(
            ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            ),
            retryListener = retryListener
        )
    }

    override fun onBindViewHolder(holder: ItemLoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}

class ItemLoadingViewHolder(
    private val binding: ItemLoadingBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(loadState: LoadState) {
        binding.apply {
            progress.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error

            retryButton.setOnClickListener {
                retryListener()
            }
        }
    }
}