package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.models.Article

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.ivArticleImage)
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val source: TextView = itemView.findViewById(R.id.tvSource)
        val share: Button = itemView.findViewById(R.id.btnShare)
        val favourite: ImageView = itemView.findViewById(R.id.btnFavorite)
    }

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {

        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_article_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.apply {
            Glide.with(itemView)
                .load(article.urlToImage)
                .error(R.drawable.default_news_img)
                .into(image)
            source.text = article.source?.name
            title.text = article.title
            description.text = article.description
            itemView.setOnClickListener{
                onItemClickListener?.let { it(article) }
            }
        }
        holder.share.setOnClickListener {
            onShareButtonClickListener?.let { it(article) }
        }
        holder.favourite.setOnClickListener {
            onFavouriteButtonClickListener?.let { it(article) }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemClickListener: ((Article) -> Unit)? = null
    private var onShareButtonClickListener: ((Article) -> Unit)? = null
    private var onFavouriteButtonClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnFavouriteButtonClickListener(listener: (Article) -> Unit) {
        onFavouriteButtonClickListener = listener
    }

    fun setOnShareButtonClickListener(listener: (Article) -> Unit) {
        onShareButtonClickListener = listener
    }
}