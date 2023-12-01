package com.example.bookradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.example.bookradar.databinding.FragmentBookInfoBinding
import com.example.bookradar.model.BookModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class BookInfoFragment : Fragment() {
    private lateinit var binding: FragmentBookInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBookInfoBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val item = arguments?.getParcelable<BookModel>("item")
        binding.textBookTitle.text = item?.title
        binding.textAuthor.text =
                item?.authors?.joinToString(", ") ?: getString(R.string.unknown_author)
        binding.textPublisher.text = item?.publisher
        binding.textIsbn.text = item?.isbn
        binding.textContent.text = item?.contents
        // implementation for fetching book info
        binding.floatingActionButton.setOnClickListener {
            val crawler = Crawler()
            val isbns = binding.textIsbn.text.split(' ')
            lifecycleScope.launch {
                crawler.search(isbns[0])

            }
        }
        Glide.with(this)
                .load(item?.thumbnail)
                .apply(bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.imageBookCoverBlur)
        Glide.with(this)
                .load(item?.thumbnail)
                .into(binding.imageBookCover)

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(item: BookModel) = BookInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable("item", item)
            }
        }

    }
}