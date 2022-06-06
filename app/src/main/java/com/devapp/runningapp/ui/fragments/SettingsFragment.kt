package com.devapp.runningapp.ui.fragments

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devapp.runningapp.R
import com.devapp.runningapp.adapters.SettingAdapter
import com.devapp.runningapp.databinding.FragmentSettingsBinding
import com.devapp.runningapp.databinding.FragmentSetupBinding
import com.devapp.runningapp.model.EventBusState
import com.devapp.runningapp.model.SettingTypes
import com.devapp.runningapp.ui.dialog.ReminderFragmentDialog
import com.devapp.runningapp.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.common.eventbus.EventBus
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import org.greenrobot.eventbus.EventBus.getDefault
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    @Inject
    lateinit var preferences: SharedPreferenceHelper
    private var _binding: FragmentSettingsBinding?=null
    private val binding get() = _binding!!
    private var settingAdapter: SettingAdapter? = null
    private var hasInitializedRootView = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            if(_binding==null) _binding = FragmentSettingsBinding.inflate(inflater,container,false)
            else (binding.root.parent as ViewGroup).removeView(binding.root)
        }catch (e:Exception){

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasInitializedRootView) return
        hasInitializedRootView = true
        initView()
    }

    private fun initView(){
        if (context == null) return
        val sectionList = getItemSections()
        if (settingAdapter == null) {
            settingAdapter = SettingAdapter(preferences,settingListener).also { it.setNewSections(sectionList) }
            binding.rvMore.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = settingAdapter
            }
        } else settingAdapter!!.setNewSections(sectionList)
    }

    private fun getItemSections(): List<SettingTypes> {
        val sections: MutableList<SettingTypes> = ArrayList()
        sections.add(SettingTypes.VERSION_APP)

        if (preferences.statusSignIn > 0) {
            sections.add(SettingTypes.TITLE_ACCOUNT)
            sections.add(SettingTypes.LOG_OUT)
        }

        sections.add(SettingTypes.TITLE_OVERVIEW)
        sections.add(SettingTypes.LANGUAGE_DEVICE)
        sections.add(SettingTypes.REMINDER)
        sections.add(SettingTypes.POLICY)
        sections.add(SettingTypes.TITLE_SUPPORT)
        sections.add(SettingTypes.FEEDBACK)
        sections.add(SettingTypes.TELL_FRIEND)
        sections.add(SettingTypes.RATE)
        return sections
    }

    private fun doShare() {
        if (activity == null) return

        val intentShare = Intent(Intent.ACTION_SEND)
        intentShare.type = "text/plain"
        intentShare.putExtra(Intent.EXTRA_SUBJECT, "HeyKorea")

        val appPackageName = requireActivity().packageName // getPackageName() from Context or Activity object
        val url = "https://play.google.com/store/apps/details?id=$appPackageName"
        intentShare.putExtra(Intent.EXTRA_TEXT, url)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActivity(Intent.createChooser(intentShare, getString(R.string.share_with)))
        } else {
            startActivity(Intent.createChooser(intentShare, getString(R.string.share_with)))
        }
    }

    private var isShowDialog = false
    private val settingListener = object : SettingListener {
        override fun itemSwitchExecute(type: SettingTypes, isChecked: Boolean) {
            when (type) {
                SettingTypes.THEME_APP -> {
                    if (preferences.isNightMode != isChecked) {
                        preferences.isNightMode = isChecked
                        getDefault().post(EventBusState.UPDATE_THEME)
                    }
                }
                else -> {
                }
            }
        }

        override fun itemClickListener(type: SettingTypes) {
            when (type) {
                SettingTypes.LANGUAGE_DEVICE -> {
                    if (activity == null) return
//                    if (!isShowDialog) {
//                        isShowDialog = true
//                        LanguageDialog(requireActivity(), preferences.languageApp,
//                            object : StringCallback {
//                                override fun execute(string: String) {
//                                    isShowDialog = false
//                                    string.let { code ->
//                                        if (preferences.languageApp != code) {
//                                            if (!isAdded) return
//                                            preferences.languageApp = code
//                                            Lingver.getInstance().setLocale(requireContext(), code)
//                                            EventBus.getDefault().post(EventBusState.RESTART_APP)
//                                        }
//                                    }
//                                }
//                            }).show()
//                    }
                }

                SettingTypes.REMINDER -> {
                    if (!isShowDialog) {
                        isShowDialog = true
                        val reminderDialog = ReminderFragmentDialog.newInstance(object :
                            VoidCallback {
                            override fun execute() {
                                isShowDialog = false
                            }
                        })
                        reminderDialog.show(childFragmentManager, reminderDialog.tag)
                    }
                }
                SettingTypes.POLICY -> {
                    if (activity != null)
                        AppHelper.goUrl(requireActivity(), "https://eupgroup.net/apps/heykorea/terms.html")
                }
                SettingTypes.FEEDBACK -> {
                    //FEED BACK
                }
                SettingTypes.TELL_FRIEND -> {
                    doShare()
                }
                SettingTypes.RATE -> {
                    if (activity == null) return
                    AppHelper.goStore(requireActivity())
                }
                else -> {
                }
            }
        }

        override fun goUrl(url: String) {
            if (activity == null) return
            try {
                requireActivity().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$url")))
                return
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }

        override fun logOutListener() {
            if (preferences.statusSignIn <=0) return
            //LOGOUT

        }
    }


    override fun onDestroy() {
        _binding=null
        super.onDestroy()
    }

}