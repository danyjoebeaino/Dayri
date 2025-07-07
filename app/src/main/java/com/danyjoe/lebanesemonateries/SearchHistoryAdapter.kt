package com.danyjoe.lebanesemonateries

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danyjoe.lebanesemonateries.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(
    private val searchHistory: MutableList<String>,
    private val onItemClick: (String) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSearchHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val query = searchHistory[position]

        with(holder.binding) {
            tvSearchQuery.text = query

            // Set click listener for the whole item
            root.setOnClickListener {
                onItemClick(query)
            }

            // Set click listener for remove button
            ivRemove.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onRemoveClick(searchHistory[pos])
                    searchHistory.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        }
    }

    override fun getItemCount() = searchHistory.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newSearchHistory: List<String>) {
        searchHistory.clear()
        searchHistory.addAll(newSearchHistory)
        notifyDataSetChanged()
    }
}