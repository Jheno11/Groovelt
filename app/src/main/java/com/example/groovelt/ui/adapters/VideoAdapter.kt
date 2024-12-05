package com.example.groovelt.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.groovelt.R
import com.example.groovelt.data.models.VideoModel
import com.example.groovelt.databinding.ItemVideoGridBinding

class VideoAdapter : ListAdapter<VideoModel, VideoAdapter.ViewHolder>(DIFF_CALLBACK) {
    var onVideoClick: ((VideoModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemVideoGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let { foodList ->
            foodList?.let { item ->
                with(holder.binding) {
                    Glide.with(root)
                        .load(item.thumbnailUrl)
                        .placeholder(
                            ContextCompat.getDrawable(
                                root.context,
                                R.drawable.placeholder
                            )
                        )
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivDance)


                    tvTitle.text = item.title

                    layoutDance.setOnClickListener { onVideoClick?.invoke(item) }
                }
            }
        }
    }

    inner class ViewHolder(var binding: ItemVideoGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VideoModel>() {
            override fun areItemsTheSame(
                oldStories: VideoModel,
                newStories: VideoModel
            ): Boolean {
                return oldStories == newStories
            }

            override fun areContentsTheSame(
                oldStories: VideoModel,
                newStories: VideoModel
            ): Boolean {
                return oldStories.id == newStories.id
            }
        }
    }
}