package com.devapp.runningapp.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.devapp.runningapp.BuildConfig
import com.devapp.runningapp.util.AppHelper
import com.devapp.runningapp.util.BooleanCallback
import com.devapp.runningapp.util.SharedPreferenceHelper
import com.devapp.runningapp.util.VoidCallback
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.internal.managers.ViewComponentManager
import org.greenrobot.eventbus.EventBus
import java.util.*

class AdsInterval constructor(private val context: Context) : AdListener() {

    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var idInter: String
    private val preferenceHelper: SharedPreferenceHelper by lazy { SharedPreferenceHelper(context) }
    private var onCloseListener: VoidCallback? = null
    private var onCloseAdsEarExplainQuestion: VoidCallback? = null

    fun setOnCloseListener(listener:VoidCallback){
        onCloseListener = listener
    }

    init {
        initDataInterval()
        createFullAds()
    }

    private fun initDataInterval() {
        idInter = "ca-app-pub-3940256099942544/8691691433"
    }


    fun createFullAds() {
        if (mInterstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, idInter, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                mInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    }
                    override fun onAdShowedFullScreenContent() {
                        mInterstitialAd = null
                        createFullAds()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        onCloseAdsEarExplainQuestion?.execute()
                        onCloseAdsEarExplainQuestion = null
                        onCloseListener?.execute()
                        createFullAds()
                    }

                    override fun onAdImpression() {}
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                mInterstitialAd = null
            }
        })
    }

    fun showIntervalAds(voidCallback: VoidCallback) {
        if (mInterstitialAd == null) {
            return
        }
        Log.d("MainActivity", "showIntervalAds: ${adShowAble()}")
        val contextWrapper = if(context is ViewComponentManager.FragmentContextWrapper) context.baseContext else context
        if (adShowAble()) {
            if (!preferenceHelper.isPremium) {
                AppHelper.showDialogSkipAds(contextWrapper as Activity, object : BooleanCallback {
                    override fun execute(boolean: Boolean) {
                        if (!boolean) {
                            mInterstitialAd!!.show(contextWrapper)
                        } else {
                            voidCallback.execute()
                        }
                    }
                })
            } else {
                mInterstitialAd!!.show(contextWrapper as Activity)
            }
        } else {
            onCloseListener?.execute()
        }
    }

    private fun adShowAble(): Boolean {
        val isPremium: Boolean = preferenceHelper.isPremium
        if (isPremium) {
            return false
        }
        return true
    }

    override fun onAdClosed() {
        super.onAdClosed()
        onCloseListener?.execute()
    }
}