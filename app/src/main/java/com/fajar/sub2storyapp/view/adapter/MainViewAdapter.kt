package com.fajar.sub2storyapp.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.data.remote.response.ListStoryItem
import com.fajar.sub2storyapp.databinding.ItemRowStoryBinding
import com.fajar.sub2storyapp.utils.helper.Helper.withDateFormat
import com.fajar.sub2storyapp.utils.loadImage
import com.fajar.sub2storyapp.view.home.detail.DetailActivity

class MainViewAdapter :
    PagingDataAdapter<ListStoryItem, MainViewAdapter.MainViewHolder>(mDiffCallback) {

    class MainViewHolder(val binding: ItemRowStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStoryItem) {
            val (photo_url, createdAt, name, description, _, _, _) = data
            binding.apply {
                imgItemPhoto.loadImage(imgItemPhoto.context, photo_url)
                tvTime.text =
                    itemView.resources.getString(R.string.created_at, createdAt.withDateFormat())
                tvUsername.text = name
                tvDescription.text = description

                cardStory.setOnClickListener {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(imgItemPhoto, "photo"),
                            Pair(tvTime, "date"),
                            Pair(tvUsername, "name"),
                            Pair(tvDescription, "description")
                        )
                    val extras = ActivityNavigatorExtras(optionsCompat)
                    val bundle = bundleOf(DetailActivity.EXTRA_STORY to data)

                    it.findNavController().navigate(
                        R.id.action_mainFragment_to_detailActivity,
                        args = bundle,
                        navOptions = null,
                        navigatorExtras = extras
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        return MainViewHolder(
            ItemRowStoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    companion object {
        val mDiffCallback = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}