package com.example.memefire.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.memefire.R
import com.example.memefire.databinding.ItemMemeBinding
import com.example.memefire.model.Meme

class MemeListAdapter(
    private val onFavouriteClick: (Meme?) -> Unit,
    private val onShareClick: (Meme?) -> Unit
): RecyclerView.Adapter<MemeListAdapter.MemeVH>() {

    private var memeList = listOf<Meme>()

    fun setData(memeList: List<Meme>) {
        this.memeList = memeList
        notifyDataSetChanged()
    }

    fun setFavouriteMeme(meme: Meme) {
        val pos = memeList.indexOf(meme)
        memeList[pos].isfavoruite = true
        notifyItemChanged(pos)
    }

    inner class MemeVH(private val binding: ItemMemeBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(meme: Meme) {
            with(binding) {
                tvAuthor.text = meme.author
                tvUps.text = meme.ups.toString()

                if(meme.isfavoruite) {
                    ivFavourite.setImageResource(R.drawable.ic_favourite)
                } else {
                    ivFavourite.setImageResource(R.drawable.ic_unfavourite)
                }

                ivMeme.load(meme.url) {
                    placeholder(R.drawable.ic_meme_placeholder)
                }

                ivFavourite.setOnClickListener {
                    onFavouriteClick(meme)
                }

                ivShare.setOnClickListener {
                    onShareClick(meme)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MemeVH, position: Int) {
        holder.bind(memeList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeVH {
       val binding = ItemMemeBinding.inflate(
           LayoutInflater.from(parent.context),
           parent,
           false
       )
        return MemeVH(binding)
    }

    override fun getItemCount(): Int {
        return memeList.size
    }

}