package com.example.bookradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookradar.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var adapter: MyBookRecyclerViewAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookList: MutableList<BookInfo>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        recyclerView = binding.list
        bookList = mutableListOf<BookInfo>()
        adapter = MyBookRecyclerViewAdapter(bookList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        binding.searchImage.setOnClickListener {
            val keyword = binding.editSearch.text.toString()
            if (keyword.isEmpty()) {
                Toast.makeText(context, "Please type in the keyword.", Toast.LENGTH_LONG).show()
            }
            val crawler = Crawler()
            lifecycleScope.launch(Dispatchers.IO) {
                bookList.clear()
                bookList.addAll(crawler.search(keyword))

                withContext(Dispatchers.Main){
                    adapter.notifyDataSetChanged()

                }

            }
        }

        /* val textView: TextView = binding.textHome
         homeViewModel.text.observe(viewLifecycleOwner) {
             textView.text = it
         }*/
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}