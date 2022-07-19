package com.habitrpg.wearos.habitica

import android.app.Application
import android.content.Intent
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.common.habitica.extensions.setupCoil
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.wearos.habitica.data.repositories.TaskRepository
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.ui.activities.BaseActivity
import com.habitrpg.wearos.habitica.ui.activities.FaintActivity
import com.habitrpg.wearos.habitica.ui.activities.MainActivity
import com.habitrpg.wearos.habitica.ui.activities.RYAActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var taskRepository: TaskRepository

    override fun onCreate() {
        super.onCreate()
        MarkdownParser.setup(this)
        setupCoil()
        setupFirebase()

        MainScope().launch {
            userRepository.getUser().onEach {
                if (it.isDead && BaseActivity.currentActivityClassName == MainActivity::class.java.name) {
                    val intent = Intent(this@MainApplication, FaintActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else if (it.needsCron && BaseActivity.currentActivityClassName == MainActivity::class.java.name) {
                    val intent = Intent(this@MainApplication, RYAActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }.collect()
        }

        logLaunch()
    }

    private fun logLaunch() {
        if (!BuildConfig.DEBUG) {
            Firebase.analytics.logEvent("wear_launched", null)
        }
    }

    private fun setupFirebase() {
        if (!BuildConfig.DEBUG) {
            val crashlytics = Firebase.crashlytics
            if (userRepository.hasAuthentication) {
                crashlytics.setUserId(userRepository.userID)
            }
            crashlytics.setCustomKey("is_wear", true)
        }
    }
}