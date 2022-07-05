package com.fajar.sub2storyapp.view.home.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.navArgs
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.databinding.ActivityDetailBinding
import com.fajar.sub2storyapp.model.UserViewModel
import com.fajar.sub2storyapp.model.ViewModelFactory
import com.fajar.sub2storyapp.utils.helper.Helper.withDateFormat
import com.fajar.sub2storyapp.utils.loadImage
import com.fajar.sub2storyapp.view.home.welcome.WelcomeActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var preferences: UserPreference

    private val userViewModel: UserViewModel by viewModels { ViewModelFactory.getInstance() }
    private val args: DetailActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val receiveIntent = args.extraStory
        val (photoUrl, createdAt, name, description, _, _, _) = receiveIntent

        binding.apply {
            ivStory.loadImage(this@DetailActivity, photoUrl)
            tvTime.text = getString(R.string.created_at, createdAt.withDateFormat())
            tvUsername.text = name
            tvDescription.text = description
        }
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
                userViewModel.logout(preferences)
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}