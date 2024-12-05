package com.example.groovelt.ui.videodetail

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.groovelt.R
import com.example.groovelt.data.models.VideoModel
import com.example.groovelt.data.network.FirebaseHelper
import com.example.groovelt.databinding.ActivityVideoDetailBinding
import com.example.groovelt.utils.Constants


class VideoDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoDetailBinding
    private lateinit var progressDialog: AlertDialog

    private val firebaseHelper: FirebaseHelper by lazy {
        FirebaseHelper.getInstance()
    }

    private val userId by lazy {
        Constants.getUserId(this)
    }

    private var isLiked = 0

    private val videoExtra: VideoModel? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_VIDEO, VideoModel::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_VIDEO)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLike()
        setViews()
    }

    private fun initLike() {
        firebaseHelper.favoritesPathDatabase.child(userId ?: "").child(videoExtra?.id.toString())
            .get()
            .addOnSuccessListener { dataSnapshot ->
                val qty = dataSnapshot.getValue(Int::class.java)
                isLiked = qty ?: 0

                binding.btnToggle.isVisible = true

                if (isLiked > 0) {
                    binding.btnToggle.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@VideoDetailActivity,
                            R.drawable.ic_remove
                        )
                    )
                } else {
                    binding.btnToggle.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@VideoDetailActivity,
                            R.drawable.ic_add
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error Fetching Favorited Dances: ${exception.localizedMessage}")
                finish()
            }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setViews() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            videoExtra?.let {
                toolbar.setTitle(it.title)

                val videoId = it.url.split("v=")[1].split("&")[0]
                val video = """
    <iframe width="100%" height="100%" 
            src="https://www.youtube.com/embed/$videoId" 
            title="YouTube video player" 
            frameborder="0" 
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
            allowfullscreen></iframe>
""".trimIndent()

                webView.loadData(video, "text/html", "utf-8")
                webView.settings.javaScriptEnabled = true
                webView.webChromeClient = WebChromeClient()

                tvDesc.text = it.desc

                binding.btnToggle.setOnClickListener {
                    showLoadingDialog()
                    if (isLiked > 0) {
                        firebaseHelper.favoritesPathDatabase.child(userId ?: "")
                            .child(videoExtra?.id.toString()).removeValue()
                            .addOnSuccessListener {
                                hideLoadingDialog()
                                binding.btnToggle.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@VideoDetailActivity,
                                        R.drawable.ic_add
                                    )
                                )
                                showToast("Successfully Deleted from Library!")
                            }
                            .addOnFailureListener { exception ->
                                hideLoadingDialog()
                                showToast("Error: ${exception.localizedMessage}")
                            }
                    } else {
                        firebaseHelper.favoritesPathDatabase.child(userId ?: "")
                            .child(videoExtra?.id.toString()).setValue(1)
                            .addOnSuccessListener {
                                hideLoadingDialog()
                                isLiked = 1
                                binding.btnToggle.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@VideoDetailActivity,
                                        R.drawable.ic_remove
                                    )
                                )
                                showToast("Successfully Added to Library!")
                            }
                            .addOnFailureListener { exception ->
                                hideLoadingDialog()
                                showToast("Error: ${exception.localizedMessage}")
                            }
                    }
                }
            }
        }
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress_dialog)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog.show()
    }

    private fun hideLoadingDialog() {
        progressDialog.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_VIDEO = "extra_video"
    }
}