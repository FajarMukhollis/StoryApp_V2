package com.fajar.sub2storyapp.view.home.maps

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.data.remote.response.ListStoryItem
import com.fajar.sub2storyapp.databinding.ActivityMapsBinding
import com.fajar.sub2storyapp.model.UserViewModel
import com.fajar.sub2storyapp.model.ViewModelFactory
import com.fajar.sub2storyapp.utils.helper.showToast
import com.fajar.sub2storyapp.view.home.main.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var storyWithLocation: List<ListStoryItem>

    private val boundsBuilder = LatLngBounds.Builder()
    private val mapsViewModel: UserViewModel by viewModels { ViewModelFactory.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapsFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapsViewModel.loadUser(UserPreference.getInstance(dataStore)).observe(this) { pref ->
            mapsViewModel.loadStoryLocation(pref.token).observe(this) { response ->
                val isError = !response.error && response.message.isNotEmpty()
                if (isError) {
                    storyWithLocation = response.listStory
                    mapsFragment.getMapAsync(this)
                }
            }
        }

        binding.backToList.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        setupView()
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        setMapUI()
        setMapStyle()
        setupLocationStory()
        mMap.setOnMarkerClickListener(this)
    }

    private fun setMapUI() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    private fun setMapStyle() {
        try {
            val mapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            val success = mMap.setMapStyle(mapStyle)
            if (!success) {
                showToast(this, getString(R.string.empty_map))
            }
        } catch (exception: Resources.NotFoundException) {
            showToast(this, getString(R.string.empty_map))
        }
    }

    private fun getAddress(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(this)
            val completedAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (completedAddress.isEmpty()) getString(R.string.empty_address) else completedAddress[0].getAddressLine(
                0
            )
        } catch (e: Exception) {
            getString(R.string.empty_address)
        }
    }

    private fun setupLocationStory() {
        if (storyWithLocation.isNullOrEmpty()) return

        storyWithLocation.forEach { story ->
            val location = LatLng(story.lat, story.lon)
            val address = getAddress(location)
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(address)
            )

            mMap.animateCamera(CameraUpdateFactory.newLatLng(location))
            boundsBuilder.include(location)
            marker?.showInfoWindow()
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker.isInfoWindowShown) {
            marker.hideInfoWindow()
        } else {
            marker.showInfoWindow()
        }
        return true
    }

    @Suppress("DEPRECATION")
    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}