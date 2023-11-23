package com.example.bookradar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookradar.databinding.FragmentItemBinding

class MyBookRecyclerViewAdapter(
    private var values: MutableList<BookInfo>
) : RecyclerView.Adapter<MyBookRecyclerViewAdapter.ViewHolder>() {

    fun updateData(newData: List<BookInfo>){
        values = newData.toMutableList()
        notifyDataSetChanged()
    }

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
        holder.titleView.text = item.book!!.title
        holder.authorView.text = item.book!!.author
        holder.publisherView.text = item.book!!.publisher
        holder.availabilityView.text = if (item.availability) "대출가능" else "대출불가능"
        holder.coverImage.setImageBitmap(item.image)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleView = binding.textTitle
        val authorView = binding.textAuthor
        val publisherView = binding.textPublisher
        val availabilityView = binding.textAvailability
        val isbnView = binding.textISBN
        val coverImage = binding.imageBookCover
    }

    // [Copied]: https://stackoverflow.com/questions/18953632/how-to-set-image-from-url-for-imageview

}