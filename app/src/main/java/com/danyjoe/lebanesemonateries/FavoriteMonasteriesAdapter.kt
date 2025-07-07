// FavoriteMonasteriesAdapter.kt
package com.danyjoe.lebanesemonateries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danyjoe.lebanesemonateries.R

class FavoriteMonasteriesAdapter(
    private val monasteryIds: List<String>,
    private val monasteryNames: Map<String, String>,
    private val onItemClick: (String) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<FavoriteMonasteriesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMonasteryName: TextView = view.findViewById(R.id.tvMonasteryName)
        val ivRemove: ImageView = view.findViewById(R.id.ivRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_monastery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val monasteryId = monasteryIds[position]
        val monasteryName = monasteryNames[monasteryId] ?: "Unknown Monastery"

        holder.tvMonasteryName.text = monasteryName

        holder.itemView.setOnClickListener {
            onItemClick(monasteryId)
        }

        holder.ivRemove.setOnClickListener {
            onRemoveClick(monasteryId)
        }
    }

    override fun getItemCount() = monasteryIds.size
}