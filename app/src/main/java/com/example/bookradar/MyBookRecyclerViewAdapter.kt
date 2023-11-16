package com.example.bookradar

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.bookradar.placeholder.PlaceholderContent.PlaceholderItem
import com.example.bookradar.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyBookRecyclerViewAdapter(
    private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<MyBookRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
/*
        holder.idView.text = item.id
        holder.contentView.text = item.content
*/

        holder.titleView.text =
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleView = binding.textViewTitle
        val authorView = binding.textViewAuthor
        val publisherView = binding.textViewPublisher
        val availabilityView = binding.textViewAvailability

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}