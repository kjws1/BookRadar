package com.example.bookradar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookradar.databinding.BottomSheetBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

@Suppress("DEPRECATION")
class MapsFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val school = LatLng(37.500062,126.868063)
        googleMap.setOnMarkerClickListener(this)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(school, 20f))
        arguments?.getParcelableArray("books")?.forEach {
            val book = it as BookInfo
            val marker = googleMap.addMarker(
                MarkerOptions().position(book.library!!.location).title(book.library!!.getName(requireContext())).snippet(book.loc)
            )
            marker?.tag = book
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        Log.d("MapsFragment", arguments?.getParcelableArray("books")?.size.toString())
        mapFragment?.getMapAsync(callback)

    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val book = p0.tag as BookInfo
        ModalBottomSheet().apply {
            this.book = book
        }.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)
        return true
    }
}
class ModalBottomSheet : BottomSheetDialogFragment() {
    lateinit var book: BookInfo
    private lateinit var binding: BottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetBinding.inflate(inflater, container, false)
        binding.textLibraryName.text = book.library!!.getName(requireContext())
        binding.textLibraryLocation.text = book.loc
        binding.buttonBorrow.setOnClickListener {
            MapsFragmentDirections.actionNavMapsToNavBorrow(book).let {
                (requireActivity() as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)!!
                    .findNavController().navigate(it)
            }
        }
        return binding.root
    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
