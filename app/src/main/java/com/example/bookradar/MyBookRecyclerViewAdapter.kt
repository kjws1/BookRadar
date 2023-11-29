package com.example.bookradar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookradar.databinding.FragmentItemBinding
import com.example.bookradar.model.DocumentModel

class MyBookRecyclerViewAdapter(
    private var values: MutableList<DocumentModel>?,
    private val listener: (DocumentModel) -> Unit
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
        val item = values?.get(position) ?: return
        /*
                holder.idView.text = item.id
                holder.contentView.text = item.content
        */
        holder.titleView.text = item.title
        holder.authorView.text = item.authors.joinToString(", ")
        holder.publisherView.text = item.publisher
        holder.isbnView.text = item.isbn
        holder.layoutBookItem.setOnClickListener{
            listener(item)
        }

        Glide.with(holder.itemView.context)
            .load(item.thumbnail)
            .into(holder.coverImage)
    }

    override fun getItemCount(): Int = values?.size ?: 0

    interface OnItemClickListener {
        fun onItemClick(item: DocumentModel)
    }

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleView = binding.textTitle
        val authorView = binding.textAuthor
        val publisherView = binding.textPublisher
        val isbnView = binding.textISBN
        val coverImage = binding.imageBookCover
        val layoutBookItem = binding.linearLayoutBookItem

    }

}