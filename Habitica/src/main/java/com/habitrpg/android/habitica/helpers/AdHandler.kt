package com.habitrpg.android.habitica.helpers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.BuildConfig
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

enum class AdType {
    ARMOIRE,
    SPELL,
    FAINT;

    val adUnitID: String
        get() {
            return when (this) {
                ARMOIRE -> "ca-app-pub-5911973472413421/9392092486"
                SPELL -> "ca-app-pub-5911973472413421/1738504765"
                FAINT -> "ca-app-pub-5911973472413421/1738504765"
            }
        }

    val cooldownTime: Date?
        get() {
            return when (this) {
                SPELL -> Date(Date().time + 1.toDuration(DurationUnit.HOURS).inWholeMilliseconds)
                else -> null
            }
        }
}

fun String.md5(): String? {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val array = md.digest(this.toByteArray())
        val sb = StringBuffer()
        for (i in array.indices) {
            sb.append(Integer.toHexString(array[i].toInt() and 0xFF or 0x100).substring(1, 3))
        }
        sb.toString()
    } catch (e: java.security.NoSuchAlgorithmException) {
        null
    } catch (ex: UnsupportedEncodingException) {
        null
    }
}

class AdHandler(val activity: Activity, val type: AdType, val rewardAction: (Boolean) -> Unit) : OnUserEarnedRewardListener {
    private var rewardedAd: RewardedAd? = null

    companion object {
        private enum class AdStatus {
            UNINITIALIZED,
            INITIALIZING,
            READY,
            DISABLED
        }

        private lateinit var sharedPreferences: SharedPreferences
        const val TAG = "AdHandler"

        private var currentAdStatus = AdStatus.UNINITIALIZED

        private var nextAdAllowed: MutableMap<AdType, Date> = mutableMapOf()

        fun nextAdAllowedDate(type: AdType): Date? {
            return nextAdAllowed[type]
        }

        fun isAllowed(type: AdType): Boolean {
            return nextAdAllowedDate(type)?.after(Date()) == true
        }

        fun setNextAllowedDate(type: AdType) {
            val date = type.cooldownTime
            if (date != null) {
                nextAdAllowed[type] = date
                sharedPreferences.edit {
                    putLong("nextAd${type.name}", date.time)
                }
            } else {
                nextAdAllowed.remove(type)
                sharedPreferences.edit {
                    remove("nextAd${type.name}")
                }
            }
        }

        fun initialize(context: Context, onComplete: () -> Unit) {
            if (currentAdStatus != AdStatus.UNINITIALIZED) return

            if (BuildConfig.DEBUG || BuildConfig.TESTING_LEVEL == "staff" || BuildConfig.TESTING_LEVEL == "alpha") {
                val android_id: String =
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                val deviceId: String = android_id.md5()?.uppercase() ?: ""
                val configuration = RequestConfiguration.Builder().setTestDeviceIds(listOf(deviceId)).build()
                MobileAds.setRequestConfiguration(configuration)
            }

            currentAdStatus = AdStatus.INITIALIZING
            MobileAds.initialize(context) {
                currentAdStatus = AdStatus.READY
                onComplete()
                FirebaseCrashlytics.getInstance().recordException(Throwable("Ads Initialized"))
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

        fun setup(sharedPrefs: SharedPreferences) {
            this.sharedPreferences = sharedPrefs

            for (type in AdType.values()) {
                val time = sharedPrefs.getLong("nextAd${type.name}", 0)
                if (time > 0) {
                    nextAdAllowed[type] = Date(time)
                }
            }
        }
    }

    fun prepare(onComplete: ((Boolean) -> Unit)? = null) {
        whenAdsInitialized(activity) {
            val adRequest = AdRequest.Builder()
                .build()

            if (BuildConfig.DEBUG || BuildConfig.TESTING_LEVEL == "staff" || BuildConfig.TESTING_LEVEL == "alpha") {
                if (!adRequest.isTestDevice(activity)) {
                    // users in this group need to be configured as Test device. better to fail if they aren't
                    // currentAdStatus = AdStatus.DISABLED
                    FirebaseCrashlytics.getInstance().recordException(Throwable("Device not test device"))
                }
            }

            RewardedAd.load(
                activity,
                type.adUnitID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
                        rewardAction(false)
                        onComplete?.invoke(false)
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        this@AdHandler.rewardedAd = rewardedAd
                        configureReward()
                        onComplete?.invoke(true)
                    }
                }
            )
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
        rewardedAd?.run { }
    }

    private fun showRewardedAd() {
        if (nextAdAllowedDate(type)?.after(Date()) == true) {
            return
        }
        if (rewardedAd != null) {
            rewardedAd?.show(activity, this)
            setNextAllowedDate(type)
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }
    }

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        Analytics.sendEvent(
            "adRewardEarned",
            EventCategory.BEHAVIOUR,
            HitType.EVENT,
            mapOf(
                "type" to type.name
            )
        )
        FirebaseAnalytics.getInstance(activity).logEvent(
            "adRewardEarned",
            bundleOf(
                Pair("type", type.name)
            )
        )
        rewardAction(true)
    }
}
