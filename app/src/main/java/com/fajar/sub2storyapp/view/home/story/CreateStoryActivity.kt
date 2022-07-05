package com.fajar.sub2storyapp.view.home.story

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.databinding.ActivityCreateStoryBinding
import com.fajar.sub2storyapp.model.UserViewModel
import com.fajar.sub2storyapp.model.ViewModelFactory
import com.fajar.sub2storyapp.utils.helper.showToast
import com.fajar.sub2storyapp.utils.reduceFileImage
import com.fajar.sub2storyapp.utils.rotateBitmap
import com.fajar.sub2storyapp.utils.uriToFile
import com.fajar.sub2storyapp.view.home.main.MainActivity
import com.fajar.sub2storyapp.view.home.story.camera.CameraActivity
import com.fajar.sub2storyapp.view.home.welcome.WelcomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.io.File
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class CreateStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateStoryBinding
    private lateinit var preference: UserPreference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var token: String

    private var getFile: File? = null
    private val newStoryViewModel: UserViewModel by viewModels { ViewModelFactory.getInstance() }

    private fun isFileNotNull() = getFile != null
    private fun isDescNotEmpty() = binding.edtDesc.text.toString().trim().isNotEmpty()

    private fun isRequestBodyReady(): Boolean {
        return isFileNotNull() && isDescNotEmpty()
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun randomCoordinate(): Double {
        return Random.nextDouble(15.0, 100.0)
    }

    private fun sendRequest() {
        val address = binding.edtLocation.text.toString()
        val location = addressToCoordinate(address)
        val file = reduceFileImage(getFile as File)
        val description = binding.edtDesc.text.toString()

        newStoryViewModel.uploadStory(token, file, description, location)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.logout -> {
                newStoryViewModel.logout(preference)
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        preference = UserPreference.getInstance(dataStore)
        newStoryViewModel.loadUser(preference).observe(this) { pref ->
            token = pref.token
        }

        newStoryViewModel.uploadResponse.observe(this) { data ->
            binding.loadingProgress.isVisible = data.message.isEmpty()
            if (!data.error && data.message.isNotEmpty()) {
                startActivity(Intent(this@CreateStoryActivity, MainActivity::class.java))
                finish()
            }
            if (data.error) {
                showToast(this, data.message)
                return@observe
            }
        }

        binding.uploadStory.setOnClickListener {
            if (!isFileNotNull()) {
                showToast(this@CreateStoryActivity, getString(R.string.input_error))
                return@setOnClickListener
            }

            if (!isDescNotEmpty()) {
                binding.edtDesc.error = getString(R.string.input_error)
                return@setOnClickListener
            }

            if (isRequestBodyReady()) {
                sendRequest()
            }
        }

        binding.openCamera.setOnClickListener {
            if (!allPermissionsGranted()) {
                requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
                return@setOnClickListener
            }

            startCamerax()
        }

        binding.openFolder.setOnClickListener { startGallery() }
        binding.openMaps.setOnClickListener { getLocationNow() }
    }

    private fun startCamerax() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CAMERA_X_RESULT) {
                val myFile: File = it.data?.getSerializableExtra(EXTRA_PHOTO) as File
                val backCamera = it.data?.getBooleanExtra(BACK_CAMERA, true) as Boolean
                getFile = myFile

                val result = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    BitmapFactory.decodeFile(myFile.path)
                } else {
                    rotateBitmap(BitmapFactory.decodeFile(myFile.path), backCamera)
                }

                binding.ivStoryImage.setImageBitmap(result)
            }
        }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_item))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedItem: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedItem, this@CreateStoryActivity)

            getFile = myFile
            binding.ivStoryImage.setImageURI(selectedItem)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> getLocationNow()
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> getLocationNow()
            else -> { //Do Nothing
            }
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

    private fun getLocationNow() {
        if (
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val address = getAddress(LatLng(location.latitude, location.longitude))
                    binding.edtLocation.setText(address)
                } else {
                    showToast(this, getString(R.string.empty_address))
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun addressToCoordinate(locationName: String): LatLng {
        return try {
            val randLatitude = randomCoordinate()
            val randLongitude = randomCoordinate()

            val geocoder = Geocoder(this)
            val allLocation = geocoder.getFromLocationName(locationName, 1)
            if (allLocation.isEmpty()) {
                LatLng(randLatitude, randLongitude)
            } else {
                LatLng(allLocation[0].latitude, allLocation[0].longitude)
            }
        } catch (e: Exception) {
            LatLng(0.0, 0.0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                showToast(this, getString(R.string.permission_no_granted))
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val EXTRA_PHOTO = "extra_photo"
        const val BACK_CAMERA = "extra_BackCamera"

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}