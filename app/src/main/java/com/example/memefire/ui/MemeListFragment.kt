package com.example.memefire.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.memefire.R
import com.example.memefire.databinding.FragmentMemeListBinding
import com.example.memefire.model.ApiResult
import com.example.memefire.model.MemeEvent
import com.example.memefire.ui.adapter.MemeListAdapter
import com.example.memefire.utils.showToast
import com.example.memefire.viewmodel.MemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MemeListFragment : Fragment(R.layout.fragment_meme_list) {

    private lateinit var binding: FragmentMemeListBinding
    private val viewModel by activityViewModels<MemeViewModel>()
    private lateinit var memeAdapter: MemeListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMemeListBinding.bind(view)

        memeAdapter = MemeListAdapter(
            onFavouriteClick = { viewModel.onAddFavMeme(it) },
            onShareClick = { meme ->
                meme?.let {
                    viewModel.onGetBitmap(requireContext(), it).observe(viewLifecycleOwner, { bmpUri ->
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, bmpUri)
                            type = "image/*"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Meme")
                        startActivity(shareIntent)
                    })
                }
            }
        )

        binding.rvMeme.apply {
            adapter = memeAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            addOnScrollListener(object: OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }

       loadMemeData()

        binding.toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(R.id.profileFragment)
                }
                else -> Unit
            }
            false
        }

        lifecycleScope.launch {
            viewModel.memeEvent.collect { event ->
                when(event) {
                    is MemeEvent.AddFavMeme -> {
                        event.msg?.showToast(requireContext())
                        event.meme?.let { meme ->
                            val pos = viewModel.memeList.indexOf(meme)

                            viewModel.memeList[pos].isfavoruite = true

                            memeAdapter.setFavouriteMeme(meme)
                        }
                    }
                    is MemeEvent.NavigateToLoginFragment -> {
                        findNavController().navigate(R.id.loginFragment)
                        event.msg.showToast(requireContext())
                    }
                }
            }
        }
    }

    private fun loadMemeData() {
        viewModel.memeAPI.observe(viewLifecycleOwner, { result ->
            binding.progressBar.isVisible = result is ApiResult.Loading
            binding.tvEmptyResult.isVisible = result is ApiResult.Error

            when(result) {
                is ApiResult.Success -> {
                    viewModel.memeList = result.data
                    memeAdapter.setData(viewModel.memeList)
                }
                is ApiResult.Error -> result.message?.showToast(requireContext())
                ApiResult.Loading -> Unit
            }
        })
    }

}