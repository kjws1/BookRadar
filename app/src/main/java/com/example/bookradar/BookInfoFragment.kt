package com.example.bookradar

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.example.bookradar.databinding.FragmentBookInfoBinding
import com.example.bookradar.model.BookModel
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    ): View {
        // Update text views with received book info
        val item = arguments?.getParcelable<BookModel>("item")
        binding.textBookTitle.text = item?.title
        binding.textAuthor.text =
            item?.authors?.joinToString(", ") ?: getString(R.string.unknown_author)
        binding.textPublisher.text = item?.publisher
        binding.textIsbn.text = item?.isbn
        binding.textContent.text = item?.contents
        binding.floatingActionButton.setOnClickListener {
            // Store the job in a variable so we can cancel it when the user clicks the cancel button
            var jobSearchBook: Job? = null
            val loadingDialog = AlertDialog.Builder(activity).run {
                setView(R.layout.loading_dialog)
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    // Cancel the job
                    if (jobSearchBook?.isActive == true)
                        jobSearchBook?.cancel()
                    dialog.dismiss()
                    Toast.makeText(context, getString(R.string.search_canceled), Toast.LENGTH_SHORT)
                        .show()
                }
                setCancelable(false)
                create()
            }
            loadingDialog.show()

            // Start scraping websites
            val crawler = Crawler()
            val isbns = binding.textIsbn.text.split(' ')
            var bInfos: ArrayList<BookInfo>?
            jobSearchBook = lifecycleScope.launch {
                // Suspend functions must be called from a coroutine or another suspend function
                withContext(Dispatchers.IO) {
                    bInfos = crawler.getBookInfos(isbns)?.let { it1 -> ArrayList(it1) }
                    bInfos?.forEach {
                        it.book = item
                    }
                }
                // Navigate to the map fragment if there is any book found
                if (bInfos != null) {
                    val action =
                        BookInfoFragmentDirections.actionNavBookInfoToNavMaps(bInfos!!.toTypedArray())
                    findNavController().navigate(action)
                }
                // Show a toast if there is no book found
                else {
                    Toast.makeText(context, getString(R.string.no_book_found), Toast.LENGTH_SHORT)
                        .show()
                    Log.d("BookInfoFragment", "No book found")
                }
                loadingDialog.dismiss()
            }
        }
        // Blurred book cover
        Glide.with(this)
            .load(item?.thumbnail)
            .apply(bitmapTransform(BlurTransformation(25, 3)))
            .into(binding.imageBookCoverBlur)
        // Regular book cover
        Glide.with(this)
            .load(item?.thumbnail)
            .into(binding.imageBookCover)

        // Inflate the layout for this fragment
        return binding.root
    }

}
