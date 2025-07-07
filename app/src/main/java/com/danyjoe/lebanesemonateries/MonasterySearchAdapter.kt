package com.danyjoe.lebanesemonateries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danyjoe.lebanesemonateries.databinding.ItemMonasterySearchBinding
import com.danyjoe.lebanesemonateries.TranslationManager.getString
import com.google.firebase.storage.FirebaseStorage

class MonasterySearchAdapter(
    private val monasteries: List<Monastery>,
    private val onItemClick: (Monastery) -> Unit
) : RecyclerView.Adapter<MonasterySearchAdapter.MonasteryViewHolder>() {

    inner class MonasteryViewHolder(val binding: ItemMonasterySearchBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonasteryViewHolder {
        val binding = ItemMonasterySearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MonasteryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonasteryViewHolder, position: Int) {
        val monastery = monasteries[position]
        val context = holder.itemView.context
        val translatableMonastery = TranslatableMonastery(monastery, context)

        with(holder.binding) {
            // Set text fields using translated content
            tvMonasteryName.text = translatableMonastery.name
            tvLocation.text = translatableMonastery.address
            tvYearFounded.text = TranslationManager.getFormattedString(
                "founded_year_format",
                context,
                translatableMonastery.yearFounded
            )

            // Set labels
            tvHistoryLabel.text = getString("history", context)

            // Set content
            tvHistory.text = translatableMonastery.history

            // Load image from Firebase Storage
            if (monastery.imageStoragePath.isNotEmpty()) {
                FirebaseStorage.getInstance().reference.child(monastery.imageStoragePath)
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        Glide.with(holder.itemView.context)
                            .load(uri)
                            .placeholder(R.drawable.placeholder_monastery)
                            .error(R.drawable.placeholder_monastery)
                            .into(ivMonastery)
                    }
                    .addOnFailureListener {
                        // If loading fails, set placeholder
                        ivMonastery.setImageResource(R.drawable.placeholder_monastery)
                    }
            } else {
                ivMonastery.setImageResource(R.drawable.placeholder_monastery)
            }

            // Set click listener for the whole item
            root.setOnClickListener {
                onItemClick(monastery)
            }
        }
    }

    override fun getItemCount() = monasteries.size
}