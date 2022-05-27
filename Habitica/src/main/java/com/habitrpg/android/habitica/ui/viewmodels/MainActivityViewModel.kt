package com.habitrpg.android.habitica.ui.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.responses.MaintenanceResponse
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.TutorialView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.kotlin.isValid
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class MainActivityViewModel : BaseViewModel(), TutorialView.OnTutorialReaction {
    @Inject
    internal lateinit var hostConfig: HostConfig
    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager
    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var contentRepository: ContentRepository
    @Inject
    internal lateinit var taskRepository: TaskRepository
    @Inject
    internal lateinit var inventoryRepository: InventoryRepository
    @Inject
    internal lateinit var taskAlarmManager: TaskAlarmManager
    @Inject
    internal lateinit var analyticsManager: AnalyticsManager
    @Inject
    internal lateinit var maintenanceService: MaintenanceApiService

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    val isAuthenticated: Boolean
        get() = hostConfig.hasAuthentication()
    val launchScreen: String?
        get() = sharedPreferences.getString("launch_screen", "")
    var preferenceLanguage: String?
        get() = sharedPreferences.getString("language", "en")
        set(value) {
            sharedPreferences.edit {
                putString("language", value)
            }
        }

    override fun onCleared() {
        taskRepository.close()
        inventoryRepository.close()
        contentRepository.close()
        super.onCleared()
    }

    fun onCreate() {
        try {
            viewModelScope.launch {
                taskAlarmManager.scheduleAllSavedAlarms(sharedPreferences.getBoolean("preventDailyReminder", false))
            }
        } catch (e: Exception) {
            analyticsManager.logException(e)
        }
    }

    fun onResume() {
        // Track when the app was last opened, so that we can use this to send out special reminders after a week of inactivity
        sharedPreferences.edit {
            putLong("lastAppLaunch", Date().time)
            putBoolean("preventDailyReminder", false)
        }
    }

    fun retrieveUser(forced: Boolean = false) {
        if (hostConfig.hasAuthentication()) {
            disposable.add(
                contentRepository.retrieveWorldState()
                    .flatMap { userRepository.retrieveUser(true, forced) }
                    .doOnNext { user1 ->
                        analyticsManager.setUserProperty("has_party", if (user1.party?.id?.isNotEmpty() == true) "true" else "false")
                        analyticsManager.setUserProperty("is_subscribed", if (user1.isSubscribed) "true" else "false")
                        analyticsManager.setUserProperty("checkin_count", user1.loginIncentives.toString())
                        analyticsManager.setUserProperty("level", user1.stats?.lvl?.toString() ?: "")
                        pushNotificationManager.setUser(user1)
                        pushNotificationManager.addPushDeviceUsingStoredToken()
                    }
                    .flatMap { userRepository.retrieveTeamPlans() }
                    .flatMap { contentRepository.retrieveContent() }
                    .subscribe({ }, RxErrorHandler.handleEmptyError())
            )
        }
    }

    override fun onTutorialCompleted(step: TutorialStep) {
        updateUser("flags.tutorial." + step.tutorialGroup + "." + step.identifier, true)
        logTutorialStatus(step, true)
    }

    override fun onTutorialDeferred(step: TutorialStep) {
        taskRepository.modify(step) { it.displayedOn = Date() }
    }

    fun logTutorialStatus(step: TutorialStep, complete: Boolean) {
        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = complete
        AmplitudeManager.sendEvent(
            "tutorial",
            AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR,
            AmplitudeManager.EVENT_HITTYPE_EVENT,
            additionalData
        )
    }

    fun ifNeedsMaintenance(onResult: ((MaintenanceResponse) -> Unit)) {
        disposable.add(
            this.maintenanceService.maintenanceStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { maintenanceResponse ->
                        if (maintenanceResponse == null) {
                            return@subscribe
                        }
                        onResult(maintenanceResponse)
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    fun getToolbarTitle(
        id: Int,
        label: CharSequence?,
        eggType: String?,
        onSuccess: ((CharSequence?) -> Unit)
    ) {
        if (id == R.id.petDetailRecyclerFragment || id == R.id.mountDetailRecyclerFragment) {
            disposable.add(
                inventoryRepository.getItem("egg", eggType ?: "").firstElement().subscribe(
                    {
                        if (!it.isValid()) return@subscribe
                        onSuccess(
                            if (id == R.id.petDetailRecyclerFragment) {
                                (it as? Egg)?.text
                            } else {
                                (it as? Egg)?.mountText
                            }
                        )
                    },
                    RxErrorHandler.handleEmptyError()
                )
            )
        } else {
            onSuccess(
                if (id == R.id.promoInfoFragment) {
                    ""
                } else if (label.isNullOrEmpty() && user.value?.isValid == true) {
                    user.value?.profile?.name
                } else label ?: ""
            )
        }
    }
}
