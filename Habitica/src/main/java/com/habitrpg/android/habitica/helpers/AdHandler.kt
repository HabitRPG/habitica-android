package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


class AdHandler(var activity: Activity, var rewardAction: (Boolean) -> Unit): OnUserEarnedRewardListener {
    private var rewardedAd: RewardedAd? = null

    companion object {
        private enum class AdStatus {
            UNINITIALIZED,
            INITIALIZING,
            READY,
            DISABLED
        }

        const val TAG = "AdHandler"
        const val adUnitID = "ca-app-pub-3940256099942544/5224354917"

        private var currentAdStatus = AdStatus.UNINITIALIZED

        fun initialize(context: Context, onComplete: () -> Unit) {
            if (currentAdStatus != AdStatus.UNINITIALIZED) return
            currentAdStatus = AdStatus.INITIALIZING
            MobileAds.initialize(context) {
                currentAdStatus = AdStatus.READY
                onComplete()
            }
        }

        fun whenAdsInitialized(context: Context, onComplete: () -> Unit) {
            when (currentAdStatus) {
                AdStatus.READY -> {
                    onComplete()
                }
                AdStatus.DISABLED -> {
                    return
                }
                AdStatus.UNINITIALIZED -> {
                    initialize(context) {
                        onComplete()
                    }
                }
                AdStatus.INITIALIZING -> {
                    return
                }
            }
        }
    }

    fun prepare() {
        whenAdsInitialized(activity) {
            val adRequest = AdRequest.Builder().build()

            RewardedAd.load(activity, adUnitID, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardAction(false)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    this@AdHandler.rewardedAd = rewardedAd
                    configureReward()
                }
            })
        }
    }

    fun show() {
        when (currentAdStatus) {
            AdStatus.READY -> {
                showRewardedAd()
            }
            AdStatus.DISABLED -> {
                rewardAction(false)
                return
            }
            AdStatus.UNINITIALIZED -> {
                initialize(activity) {
                    showRewardedAd()
                }
            }
            AdStatus.INITIALIZING -> {
                return
            }
        }
    }

    private fun configureReward() {
        rewardedAd?.run {
            fullScreenContentCallback = object : FullScreenContentCallback() {
            }
        }
    }

    private fun showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(activity, this)
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        val rewardAmount = rewardItem.amount
        val rewardType = rewardItem.type
        Log.d(TAG, "User earned the reward. ${rewardAmount}, ${rewardType}")
        rewardAction(true)
    }
}