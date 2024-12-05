package com.example.groovelt.ui.main.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.groovelt.data.local.LocalVideoHelper
import com.example.groovelt.databinding.FragmentHomeBinding
import com.example.groovelt.ui.adapters.VideoAdapter
import com.example.groovelt.ui.videodetail.VideoDetailActivity
import com.example.groovelt.ui.videodetail.VideoDetailActivity.Companion.EXTRA_VIDEO

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val videoAdapter = VideoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setViews()

        return root
    }

    private fun setViews() {
        binding.rvDances.apply {
            videoAdapter.submitList(LocalVideoHelper.getAllVideo())
            videoAdapter.onVideoClick = {
                val iDetail = Intent(requireContext(), VideoDetailActivity::class.java)
                iDetail.putExtra(EXTRA_VIDEO, it)
                startActivity(iDetail)
            }

            adapter = videoAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }
}