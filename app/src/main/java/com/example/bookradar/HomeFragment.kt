package com.example.bookradar

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookradar.databinding.FragmentHomeBinding
import com.example.bookradar.retrofit.RetrofitHelper
import com.example.bookradar.retrofit.model.DocumentModel
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


        recyclerView = binding.list
        bookList = mutableListOf<DocumentModel>()
        adapter = MyBookRecyclerViewAdapter(bookList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        binding.searchImage.setOnClickListener {
            val keyword = binding.editSearch.text.toString()
            if (keyword.isEmpty()) {
                Toast.makeText(context, "Please type in the keyword.", Toast.LENGTH_LONG).show()
            }
            val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
                throwable.printStackTrace()
            }
            lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                val apikey:String = "KakaoAK " + requireContext().packageManager.getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA).metaData.getString("kakao_api")!!
                Log.d("apikey", apikey)

                try{
                    val response = RetrofitHelper.getInstance().getBooks(apikey, keyword)
                    bookList.clear()
                    bookList.addAll(response.documents)
                    withContext(Dispatchers.Main){
                        adapter.notifyDataSetChanged()
                    }
                } catch (e: Exception){
                    Log.e("Network Error", "Error fetching data", e)
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