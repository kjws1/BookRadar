package com.example.bookradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bookradar.databinding.FragmentBookInfoBinding
import com.example.bookradar.model.DocumentModel

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
        val item = arguments?.getParcelable<DocumentModel>("item")
        binding.textBookTitle.text = item?.title
        binding.textAuthor.text = item?.authors?.joinToString(", ") ?: getString(R.string.unknown_author)
        binding.textPublisher.text = item?.publisher
        binding.textIsbn.text = item?.isbn

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_info, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(item: DocumentModel) = BookInfoFragment().apply {
            arguments = Bundle().apply {
                putParcelable("item", item)
            }
        }

    }
}