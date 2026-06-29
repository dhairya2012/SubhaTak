package com.example.subhatak.ui.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.example.subhatak.R
import com.example.subhatak.data.model.Articles
import com.facebook.shimmer.ShimmerFrameLayout
import java.text.SimpleDateFormat
import java.util.Locale

class NewsPageAdapter : RecyclerView.Adapter<NewsPageAdapter.NewsPageHolder>() {


    inner class NewsPageHolder(
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_layout,
            parent,
            false,
        ),
    ) {
        private val ivNewsImage: ImageView = itemView.findViewById(R.id.ivNewsImage)
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmerLayout)
        private val newsTitle: TextView = itemView.findViewById(R.id.newsHeading)
        private val newsDescription: TextView = itemView.findViewById(R.id.newsBody)
        private val authorText: TextView = itemView.findViewById(R.id.author)
        private val dateText: TextView = itemView.findViewById(R.id.date)
        private val imageView: FrameLayout = itemView.findViewById(R.id.newsImage)

        fun bind(article: Articles) {
            newsTitle.text = article.title
            newsDescription.text = article.description
            authorText.text = article.author ?: "Unknown Author"
            dateText.text = article.publishedAt?.formatToDisplayDate() ?: "No date"

            imageView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                itemView.context.startActivity(intent)
            }


            shimmerLayout.startShimmer()
            shimmerLayout.visibility = View.VISIBLE



            ivNewsImage.load(article.urlToImage) {
                placeholder(R.drawable.shimmer_placeholder)
                error(R.drawable.shimmer_placeholder)
                crossfade(true)
                crossfade(1000)
                scale(Scale.FILL)
                transformations(RoundedCornersTransformation(8f))




                listener(
                    onStart = {
                        shimmerLayout.startShimmer()
                        Log.d("NewsPageAdapter", "Image loading started")
                    },
                    onSuccess = { _, _ ->
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                        Log.d("NewsPageAdapter", "Image loaded successfully")


                    },
                    onError = { _, error ->
                        Log.e("NewsPageAdapter", "Image loading failed", error.throwable)
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE
                    }
                )
            }
        }
    }

    private fun String.formatToDisplayDate(): String = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(this)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        this
    }

    private var articles = listOf<Articles>()
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = NewsPageHolder(parent)


    override fun onBindViewHolder(
        holder: NewsPageHolder, position: Int
    ) {
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size
    fun setArticles(articles: List<Articles>) {
        this.articles = articles
        notifyDataSetChanged()
    }


}