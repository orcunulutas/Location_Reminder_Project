package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    private fun onLocationSelected(poi: PointOfInterest) {
        _viewModel.selectedPOI.value = poi
        _viewModel.longitude.value = poi.latLng.longitude
        _viewModel.latitude.value = poi.latLng.latitude
        _viewModel.reminderSelectedLocationStr.value = poi.name
        findNavController().navigate(R.id.action_selectLocationFragment_to_saveReminderFragment)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val zoom = 18f

        if (isPermissionGranted()) {
            mMap.isMyLocationEnabled = true
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val latitude = location.latitude
            val longitude = location.longitude
            val latLng = LatLng(latitude, longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        setPoiClickListener(mMap)
    }

    private fun setPoiClickListener(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            createConfirmationDialog(poiMarker, poi)
        }
    }

    private fun createConfirmationDialog(marker: Marker, poi: PointOfInterest) {
        val dialog = AlertDialog.Builder(requireActivity())
            .setPositiveButton(
                getString(R.string.confirm)
            ) { dialog, _ ->
                onLocationSelected(poi)
                dialog?.dismiss()
            }
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialog, _ ->
                marker.remove()
                dialog?.dismiss()
            }
            .setTitle(getString(R.string.add_location))
            .setCancelable(false)
            .setMessage(getString(R.string.would_you_like_to_add_location))
            .create()

        dialog.show()
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
