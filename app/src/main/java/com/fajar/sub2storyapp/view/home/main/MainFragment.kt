package com.fajar.sub2storyapp.view.home.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.fajar.sub2storyapp.R
import com.fajar.sub2storyapp.data.local.UserPreference
import com.fajar.sub2storyapp.databinding.FragmentMainBinding
import com.fajar.sub2storyapp.model.UserViewModel
import com.fajar.sub2storyapp.model.ViewModelFactory
import com.fajar.sub2storyapp.view.adapter.LoadingAdapter
import com.fajar.sub2storyapp.view.adapter.MainViewAdapter
import com.fajar.sub2storyapp.view.home.welcome.WelcomeActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private val getBinding get() = binding

    private lateinit var preference: UserPreference
    private val fragmentViewModel: UserViewModel by viewModels { ViewModelFactory.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)

        getBinding?.fabAddStory?.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createStoryActivity)
        }

        getBinding?.fabLocationStory?.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_mapsActivity2)
        }
        return getBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preference = UserPreference.getInstance(requireContext().dataStore)
        val storyAdapter = MainViewAdapter()

        fragmentViewModel.loadUser(preference).observe(viewLifecycleOwner) { pref ->
            fragmentViewModel.loadStory(pref.token).observe(viewLifecycleOwner) { pagingData ->
                storyAdapter.submitData(lifecycle, pagingData)
            }
        }

        storyAdapter.addLoadStateListener { loadState ->
            val isError =
                loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.Error
            val isLoading =
                loadState.refresh is LoadState.Loading && loadState.source.refresh is LoadState.Loading

            binding?.ivEmptyImage?.isVisible = isError
            binding?.tvEmpty?.isVisible = isError
            binding?.loadingProgress?.isVisible = isLoading
        }

        getBinding?.rvStory?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingAdapter { storyAdapter.retry() }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }

            R.id.logout -> {
                fragmentViewModel.logout(preference)
                val intent = Intent(requireContext(), WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                requireActivity().startActivity(intent)
                requireActivity().finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}