package com.devapp.runningapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ActivityMainBinding
import com.devapp.runningapp.db.RunDao
import com.devapp.runningapp.util.Constant
import com.devapp.runningapp.util.Constant.ACTION_TO_TRACKING_INTENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var runDao: RunDao
    private lateinit var binding:ActivityMainBinding
    private lateinit var navHostFragment:NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        navigateToTrackingFragmentIfNeeded(intent)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
        binding.bottomNavigationView.setOnItemReselectedListener {}
        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.settingsFragment,R.id.runFragment,R.id.statisticsFragment->binding.bottomNavigationView.visibility = View.VISIBLE
                else->binding.bottomNavigationView.visibility = View.GONE
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        navigateToTrackingFragmentIfNeeded(intent)
        super.onNewIntent(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if(intent!=null && intent.action == ACTION_TO_TRACKING_INTENT){
            try {
                navHostFragment.findNavController().navigate(R.id.action_notification_to_trackingFragment)
            }catch (e:Exception){
                navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
                navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
                    when(destination.id){
                        R.id.settingsFragment,R.id.runFragment,R.id.statisticsFragment->binding.bottomNavigationView.visibility = View.VISIBLE
                        else->binding.bottomNavigationView.visibility = View.GONE
                    }
                }
                navHostFragment.findNavController().navigate(R.id.action_notification_to_trackingFragment)
            }
        }
    }
}