package com.example.bookradar

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookradar.databinding.FragmentHomeBinding
import com.example.bookradar.model.DocumentModel
import com.example.bookradar.retrofit.RetrofitHelper
import kotlinx.coroutines.CoroutineExceptionHandler
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
    private lateinit var bookList: MutableList<DocumentModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val searchBar = binding.searchBook
        recyclerView = binding.listBook
        bookList = mutableListOf<DocumentModel>()
        adapter = MyBookRecyclerViewAdapter(bookList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        searchBar.clearFocus()
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    Toast.makeText(context, R.string.error_empty_search_field, Toast.LENGTH_LONG)
                        .show()
                    return true
                }
                val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
                    throwable.printStackTrace()
                }
                lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                    val apikey: String =
                        "KakaoAK " + requireContext().packageManager.getApplicationInfo(
                            requireContext().packageName,
                            PackageManager.GET_META_DATA
                        ).metaData.getString("kakao_api")!!
                    Log.d("apikey", apikey)

                    try {
                        val response = RetrofitHelper.getInstance().getBooks(apikey, query)
                        bookList.clear()
                        bookList.addAll(response.documents)
                        withContext(Dispatchers.Main) {
                            adapter.notifyItemRangeChanged(0, bookList.size)
                        }
                    } catch (e: Exception) {
                        Log.e("Network Error", "Error fetching data", e)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // false if no custom behavior is defined.
                return false
            }

        })

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