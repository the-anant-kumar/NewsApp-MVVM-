package com.example.newsapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentBreakingNewsBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.ui.NewsViewModel
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class BreakingNewsFragment : Fragment(){

    lateinit var binding: FragmentBreakingNewsBinding
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBreakingNewsBinding.inflate(layoutInflater, container, false)

        viewModel = (activity as NewsActivity).viewModel
        setupRecyclerView()
        callBreakingNews()

        binding.srlBreakingNews.setOnRefreshListener {
            setupRecyclerView()
            MainScope().launch {
                viewModel.getBreakingNews("in")
            }
            binding.srlBreakingNews.isRefreshing = false

        }

        newsAdapter.setOnItemClickListener {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity as NewsActivity, Uri.parse(it.url))
        }

        newsAdapter.setOnShareButtonClickListener {
            val url = it.url
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/*"
            intent.putExtra(Intent.EXTRA_TEXT, url)

            val chooser = Intent.createChooser(intent, "Share News")
            startActivity(chooser)

        }

        newsAdapter.setOnFavouriteButtonClickListener {
            viewModel.savedArticle(it)
            Snackbar.make(requireView(), "Article Saved Successfully", Snackbar.LENGTH_SHORT).show()
        }
        return binding.root

    }

    private fun callBreakingNews() {
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer {   response ->
            when(response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if(isLastPage) {
                            binding.rvBreakingNews.setPadding(0,0,0,10)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }
    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            //means currently we are scrolling
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling


            if(shouldPaginate) {
                viewModel.getBreakingNews("in")
                isScrolling = false
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }
}

