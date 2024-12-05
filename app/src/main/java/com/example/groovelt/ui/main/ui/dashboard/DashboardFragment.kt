package com.example.groovelt.ui.main.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.groovelt.data.local.LocalVideoHelper
import com.example.groovelt.data.models.VideoModel
import com.example.groovelt.data.network.FirebaseHelper
import com.example.groovelt.databinding.FragmentDashboardBinding
import com.example.groovelt.ui.adapters.VideoAdapter
import com.example.groovelt.ui.videodetail.VideoDetailActivity
import com.example.groovelt.ui.videodetail.VideoDetailActivity.Companion.EXTRA_VIDEO
import com.example.groovelt.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val firebaseHelper: FirebaseHelper by lazy {
        FirebaseHelper.getInstance()
    }

    private val userId by lazy {
        Constants.getUserId(requireContext())
    }

    private val videoAdapter = VideoAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        observeLibraries()
        setViews()

        return binding.root
    }

    private fun observeLibraries() {
        firebaseHelper.favoritesPathDatabase.child(userId ?: "").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val videoList = mutableListOf<VideoModel>()
                val videoIdList = mutableListOf<Int>()

                for (itemSnapshot in snapshot.children) {
                    val key = itemSnapshot.key ?: continue
                    videoIdList.add(key.toInt())
                }

                videoList.addAll(LocalVideoHelper.getVideosByIds(videoIdList))
                videoAdapter.submitList(videoList)

                binding.tvEmpty.isVisible = videoList.isEmpty()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvEmpty.isVisible = true
            }
        })
    }

    private fun setViews() {
        binding.rvDances.apply {
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