package com.devapp.runningapp.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.devapp.runningapp.R
import com.devapp.runningapp.databinding.ActivityMainBinding
import com.devapp.runningapp.db.RunDao
import com.devapp.runningapp.model.EventBusState
import com.devapp.runningapp.receiver.DeviceBootReceiver
import com.devapp.runningapp.receiver.ReminderReceiver
import com.devapp.runningapp.util.AppHelper
import com.devapp.runningapp.util.AppHelper.showErrorToast
import com.devapp.runningapp.util.Constant
import com.devapp.runningapp.util.Constant.ACTION_TO_TRACKING_INTENT
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.TrackingUtils.hideSoftKeyboard
import com.devapp.runningapp.util.TrackingUtils.toGone
import com.devapp.runningapp.util.TrackingUtils.toVisible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var sharedPref: SharedPreferenceHelper
    @Inject
    lateinit var runDao: RunDao
    private lateinit var binding:ActivityMainBinding
    private val sdf:SimpleDateFormat by lazy { SimpleDateFormat("dd/MM/yyyy") }
    private lateinit var navHostFragment:NavHostFragment
    private val firebaseDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance(Constant.URL_FIREBASE_DB) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateToTrackingFragmentIfNeeded(intent)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
        binding.bottomNavigationView.setOnItemReselectedListener {}
        binding.bottomNavigationView.itemIconTintList = null
        if(!sharedPref.isShowOnBoarding) navHostFragment.navController.navigate(R.id.loginFragment)
        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.settingsFragment,R.id.runFragment,R.id.statisticsFragment,R.id.profileFragment,R.id.premiumFragment->{
                    lifecycleScope.launchWhenResumed {
                        delay(1000)
                        binding.bottomNavigationView.toVisible()
                    }
                }
                else->binding.bottomNavigationView.toGone()
            }
        }
        fetchDataRemote()
    }

    fun fetchDataRemote(){
        if(sharedPref.accessUid.isNullOrEmpty()) return
        firebaseDatabase.getReference("premium").child(sharedPref.accessUid!!).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value
                Log.d(TAG, "onDataChange: $data")
                if(data is HashMap<*, *>){
                    if((data["premiumExpired"] as Long)==0L) {
                        sharedPref.isPremium = true
                        return
                    } else{
                        val premiumExpired = data["premiumExpired"] as Long
                        if(premiumExpired<=System.currentTimeMillis()) {
                            sharedPref.isPremium = false
                            lifecycleScope.launchWhenResumed {
                                firebaseDatabase.getReference("premium").child(sharedPref.accessUid?:"").setValue(hashMapOf("freeClick" to (sharedPref.freeClick),"isPremium" to sharedPref.isPremium, "isUpgrade" to sharedPref.isUpgrade,"lastDate" to sharedPref.lastDate,"upgradePackage" to sharedPref.upgradePackage)).await()
                            }
                        }else{
                            sharedPref.isPremium = true
                            return
                        }
                    }
                    sharedPref.freeClick = data["freeClick"] as Long
                    sharedPref.lastDate = data["lastDate"] as Long
                    sharedPref.isPremium = data["isPremium"] as Boolean
                    if(sharedPref.isUpgrade!=data["isUpgrade"] as Long) {
                        sharedPref.isUpgrade = data["isUpgrade"] as Long
                        if(sharedPref.isUpgrade==2L) AppHelper.showDialogPayment(this@MainActivity,true) else if(sharedPref.isUpgrade==3L) AppHelper.showDialogPayment(this@MainActivity,false)
                    }
                    sharedPref.upgradePackage = data["upgradePackage"] as Long
                    Log.d(TAG, "onDataChange: ${sdf.format(Date(sharedPref.lastDate))} ${sdf.format(Date())}")
                    if(sdf.format(Date(sharedPref.lastDate))!=sdf.format(Date())){
                        sharedPref.lastDate = Date().time
                        sharedPref.freeClick = 3
                        lifecycle.coroutineScope.launchWhenResumed {
                            firebaseDatabase.getReference("premium").child(sharedPref.accessUid?:"").setValue(hashMapOf("freeClick" to (sharedPref.freeClick),"isPremium" to sharedPref.isPremium, "isUpgrade" to sharedPref.isUpgrade,"lastDate" to sharedPref.lastDate,"upgradePackage" to sharedPref.upgradePackage)).await()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showErrorToast("Error server")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        if(!EventBus.getDefault().isRegistered(this)) return
            EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onEventBusEvent(state: EventBusState){
        when (state) {
            EventBusState.RESTART_APP -> restartApp()
            EventBusState.HIDE_KEYBOARD -> hideSoftKeyboard()
            EventBusState.NOTIFICATION_REMINDER -> setUpReminder()
            EventBusState.UPDATE_THEME -> restartApp()
            EventBusState.UPDATE_LOGIN -> {}
            else -> {}
        }
    }

    private fun setUpReminder() {
        val pm = this@MainActivity.packageManager
        val receiver = ComponentName(this@MainActivity, DeviceBootReceiver::class.java)
        val alarmIntent = Intent(this@MainActivity, ReminderReceiver::class.java)
        val pendingFlags = if (Build.VERSION.SDK_INT >= 23) { PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE } else { PendingIntent.FLAG_UPDATE_CURRENT }
        val pendingIntent = PendingIntent.getBroadcast(this@MainActivity, 100, alarmIntent, pendingFlags)
        val manager = this@MainActivity.getSystemService(ALARM_SERVICE) as AlarmManager
        if (sharedPref.isNotifyReminder) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val timeString = sharedPref.timeReminder
            var hour = 0
            var minute = 0
            if (timeString.contains(":")) {
                val list: Array<String> = timeString.split(":".toRegex()).toTypedArray()
                if (list.size == 2) {
                    hour = try {
                        list[0].toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                    minute = try {
                        list[1].toInt()
                    } catch (ex: NumberFormatException) {
                        0
                    }
                }
            }

            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            calendar[Calendar.SECOND] = 1
            // if notification time is before selected time, send notification the next day
            if (Calendar.getInstance().after(calendar)) calendar.add(Calendar.DATE, 1)

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent) }
            //To enable Boot Receiver class
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            StyleableToast.makeText(this, getString(R.string.turn_on_reminders), Toast.LENGTH_SHORT, R.style.toast_reminder_on).show()
        } else {//Disable Daily Notifications
            val pendingFlags = if (Build.VERSION.SDK_INT >= 23) { PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE } else { PendingIntent.FLAG_UPDATE_CURRENT }
            if (PendingIntent.getBroadcast(this@MainActivity, 100, alarmIntent, pendingFlags) != null)
                manager.cancel(pendingIntent)
                pm.setComponentEnabledSetting(
                receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            StyleableToast.makeText(this, getString(R.string.turn_off_reminders), Toast.LENGTH_SHORT, R.style.toast_reminder_off).show()
        }
    }

    fun navigateToPremium(){
        binding.bottomNavigationView.selectedItemId = R.id.premiumFragment
    }

    private fun restartApp(){recreate()}

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