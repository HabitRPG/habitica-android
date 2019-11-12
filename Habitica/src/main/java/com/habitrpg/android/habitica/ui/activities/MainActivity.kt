package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.facebook.drawee.view.SimpleDraweeView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.*
import com.habitrpg.android.habitica.databinding.ActivityMainBinding
import com.habitrpg.android.habitica.events.*
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.DateUtils
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.interactors.CheckClassSelectionUseCase
import com.habitrpg.android.habitica.interactors.DisplayItemDropUseCase
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.notifications.LoginIncentiveData
import com.habitrpg.android.habitica.models.responses.MaintenanceResponse
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.TutorialView
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.ValueBar
import com.habitrpg.android.habitica.ui.views.dialogs.AchievementDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.yesterdailies.YesterdailyDialog
import com.habitrpg.android.habitica.userpicture.BitmapUtils
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider
import com.habitrpg.android.habitica.widget.TodoListWidgetProvider
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject

open class MainActivity : BaseActivity(), TutorialView.OnTutorialReaction {
    @Inject
    internal lateinit var apiClient: ApiClient
    @Inject
    internal lateinit var soundManager: SoundManager
    @Inject
    internal lateinit var maintenanceService: MaintenanceApiService
    @Inject
    internal lateinit var hostConfig: HostConfig
    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager
    @Inject
    internal lateinit var checkClassSelectionUseCase: CheckClassSelectionUseCase
    @Inject
    internal lateinit var displayItemDropUseCase: DisplayItemDropUseCase
    @Inject
    internal lateinit var notifyUserUseCase: NotifyUserUseCase
    @Inject
    internal lateinit var taskRepository: TaskRepository
    @Inject
    internal lateinit var userRepository: UserRepository
    @Inject
    internal lateinit var inventoryRepository: InventoryRepository
    @Inject
    internal lateinit var contentRepository: ContentRepository
    @Inject
    internal lateinit var taskAlarmManager: TaskAlarmManager
    @Inject
    internal lateinit var appConfigManager: AppConfigManager

    lateinit var binding: ActivityMainBinding

    val snackbarContainer: ViewGroup
    get() = binding.snackbarContainer
    var user: User? = null


    private var avatarInHeader: AvatarWithBarsViewModel? = null
    private var notificationsViewModel: NotificationsViewModel? = null
    private var faintDialog: HabiticaAlertDialog? = null
    private var sideAvatarView: AvatarView? = null
    private var activeTutorialView: TutorialView? = null
    private var drawerFragment: NavigationDrawerFragment? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    private var resumeFromActivity = false
    private var userIsOnQuest = false

    private var connectionIssueHandler: Handler? = null

    val userID: String
        get() = user?.id ?: ""

    val isAppBarExpanded: Boolean
        get() = binding.appbar.height - binding.appbar.bottom == 0


    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun getContentView(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    private var launchTrace: com.google.firebase.perf.metrics.Trace? = null

    @SuppressLint("ObsoleteSdkInt")
    public override fun onCreate(savedInstanceState: Bundle?) {
        launchTrace = FirebasePerformance.getInstance().newTrace("MainActivityLaunch")
        launchTrace?.start()
        super.onCreate(savedInstanceState)


        if (!HabiticaBaseApplication.checkUserAuthentication(this, hostConfig)) {
            return
        }

        setupToolbar(binding.toolbar)

        avatarInHeader = AvatarWithBarsViewModel(this, binding.avatarWithBars, userRepository)
        sideAvatarView = AvatarView(this, showBackground = true, showMount = false, showPet = false)

        compositeSubscription.add(userRepository.getUser()
                .subscribe(Consumer { newUser ->
                    this@MainActivity.user = newUser
                    this@MainActivity.setUserData()
                }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(userRepository.getIsUserOnQuest().subscribeWithErrorHandler(Consumer {
            userIsOnQuest = it
        }))

        val viewModel = ViewModelProviders.of(this)
                .get(NotificationsViewModel::class.java)
        notificationsViewModel = viewModel

        val drawerLayout = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)

        drawerFragment = supportFragmentManager.findFragmentById(R.id.navigation_drawer) as? NavigationDrawerFragment

        drawerFragment?.setUp(R.id.navigation_drawer, drawerLayout, viewModel)

        drawerToggle = object : ActionBarDrawerToggle(
                this, /* host Activity */
                findViewById(R.id.drawer_layout), /* DrawerLayout object */
                R.string.navigation_drawer_open, /* "open drawer" description */
                R.string.navigation_drawer_close  /* "close drawer" description */
        ) {

        }

        // Set the drawer toggle as the DrawerListener
        drawerToggle?.let { drawerLayout.addDrawerListener(it) }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationController = findNavController(R.id.nav_host_fragment)
        navigationController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.label.isNullOrEmpty() && user?.isValid == true) {
                binding.toolbarTitle.text = user?.profile?.name
            } else if (user?.isValid == true && user?.profile != null) {
                binding.toolbarTitle.text = destination.label
            }
            drawerFragment?.setSelection(destination.id, null, false)
        }
        MainNavigationController.setup(navigationController)

        setupNotifications()
        setupBottomnavigationLayoutListener()
    }

    private fun setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val channel = NotificationChannel(
                    channelId,
                    "Habitica Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun setupBottomnavigationLayoutListener() {
        binding.bottomNavigation.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.bottomNavigation.visibility == View.VISIBLE) {
                snackbarContainer.setPadding(0, 0, 0, binding.bottomNavigation.barHeight + 12.dpToPx(this))
            } else {
                snackbarContainer.setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle?.syncState()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e("RESTORED:", savedInstanceState.toString())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle?.onOptionsItemSelected(item) == true) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onResume() {
        super.onResume()

        if(!resumeFromActivity){
            retrieveUser()
            this.checkMaintenance()
        }
        resumeFromActivity = false


        if (this.sharedPreferences.getLong("lastReminderSchedule", 0) < Date().time - DateUtils.hoursInMs(2)) {
            try {
                taskAlarmManager.scheduleAllSavedAlarms(sharedPreferences.getBoolean("preventDailyReminder", false))
            } catch (e: Exception) {
                crashlyticsProxy.logException(e)
            }
        }

        //Track when the app was last opened, so that we can use this to send out special reminders after a week of inactivity
        sharedPreferences.edit {
            putLong("lastAppLaunch", Date().time)
            putBoolean("preventDailyReminder", false)
        }

        if (intent.hasExtra("notificationIdentifier")) {
            val identifier = intent.getStringExtra("notificationIdentifier") ?: ""
            val additionalData = HashMap<String, Any>()
            additionalData["identifier"] = identifier
            AmplitudeManager.sendEvent("open notification", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
            NotificationOpenHandler.handleOpenedByNotification(identifier, intent, user)
        }

        launchTrace?.stop()
        launchTrace = null
    }

    override fun onPause() {
        updateWidgets()
        super.onPause()
    }

    override fun startActivity(intent: Intent?) {
        resumeFromActivity = true
        super.startActivity(intent)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        resumeFromActivity = true
        super.startActivityForResult(intent, requestCode)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        resumeFromActivity = true
        super.startActivity(intent, options)
    }

    private fun updateWidgets() {
        updateWidget(AvatarStatsWidgetProvider::class.java)
        updateWidget(TodoListWidgetProvider::class.java)
        updateWidget(DailiesWidgetProvider::class.java)
        updateWidget(HabitButtonWidgetProvider::class.java)
    }

    private fun updateWidget(widgetClass: Class<*>) {
        val intent = Intent(this, widgetClass)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, widgetClass))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    fun navigate(transitionId: Int) {
        findNavController(R.id.nav_host_fragment).navigate(transitionId)
    }

    private fun setUserData() {
        if (user != null) {
            val preferences = user?.preferences

            preferences?.language?.let { apiClient.setLanguageCode(it) }
            preferences?.sound?.let { soundManager.soundTheme = it }

            displayDeathDialogIfNeeded()
            YesterdailyDialog.showDialogIfNeeded(this, user?.id, userRepository, taskRepository)

            if (user?.flags?.isVerifiedUsername == false && isActivityVisible) {
                val intent = Intent(this, VerifyUsernameActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (this.activeTutorialView != null) {
            this.removeActiveTutorialView()
        }
        if (drawerFragment?.isDrawerOpen == true) {
            drawerFragment?.closeDrawer()
        } else {
            try {
                super.onBackPressed()
            } catch (ignored: Exception) {
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == SELECT_CLASS_RESULT) {
            retrieveUser()
        } else if (requestCode == GEM_PURCHASE_REQUEST) {
            retrieveUser()
        }
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == NOTIFICATION_CLICK && data?.hasExtra("notificationId") == true) {
            notificationsViewModel?.click(
                    data.getStringExtra("notificationId"),
                    MainNavigationController
            )
        }

        if (resultCode == NOTIFICATION_ACCEPT && data?.hasExtra("notificationId") == true) {
            notificationsViewModel?.accept(
                    data.getStringExtra("notificationId")
            )
        }

        if (resultCode == NOTIFICATION_REJECT && data?.hasExtra("notificationId") == true) {
            notificationsViewModel?.reject(
                    data.getStringExtra("notificationId")
            )
        }
        PurchaseHandler.findForActivity(this)?.onResult(requestCode, resultCode, data)
    }

    // region Events

    public override fun onDestroy() {
        userRepository.close()
        inventoryRepository.close()
        super.onDestroy()
    }

    @Subscribe
    fun onEvent(event: FeedCommand) {
        if (event.usingFood == null || event.usingPet == null) {
            return
        }
        val pet = event.usingPet
        compositeSubscription.add(this.inventoryRepository.feedPet(event.usingPet, event.usingFood)
                .subscribe(Consumer { feedResponse ->
                    HabiticaSnackbar.showSnackbar(snackbarContainer, getString(R.string.notification_pet_fed, pet.text), SnackbarDisplayType.NORMAL)
                    if (feedResponse.value == -1) {
                        val mountWrapper = View.inflate(this, R.layout.pet_imageview, null) as? FrameLayout
                        val mountImageView = mountWrapper?.findViewById(R.id.pet_imageview) as? SimpleDraweeView

                        DataBindingUtils.loadImage(mountImageView, "Mount_Icon_" + event.usingPet.key)
                        val dialog = HabiticaAlertDialog(this@MainActivity)
                        dialog.setTitle(getString(R.string.evolved_pet_title, pet.text))
                        dialog.setAdditionalContentView(mountWrapper)
                        dialog.addButton(R.string.onwards, true)
                        dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                                    val event1 = ShareEvent()
                                    event1.sharedMessage = getString(R.string.share_raised, pet.text) + " https://habitica.com/social/raise-pet"
                                    val mountImageSideLength = 99
                                    val sharedImage = Bitmap.createBitmap(mountImageSideLength, mountImageSideLength, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(sharedImage)
                                    canvas.drawColor(ContextCompat.getColor(this, R.color.brand_300))
                                    mountImageView?.drawable?.setBounds(0, 0, mountImageSideLength, mountImageSideLength)
                                    mountImageView?.drawable?.draw(canvas)
                                    event1.shareImage = sharedImage
                                    EventBus.getDefault().post(event1)
                                    hatchingDialog.dismiss()
                                }
                        dialog.enqueue()
                    }
                }, RxErrorHandler.handleEmptyError()))
    }

    // endregion

    internal fun displayTaskScoringResponse(data: TaskScoringResult?) {
        if (user != null && data != null) {
            compositeSubscription.add(notifyUserUseCase.observable(NotifyUserUseCase.RequestValues(this, snackbarContainer,
                    user, data.experienceDelta, data.healthDelta, data.goldDelta, data.manaDelta, if (userIsOnQuest) data.questDamage else 0.0, data.hasLeveledUp))
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }

        compositeSubscription.add(displayItemDropUseCase.observable(DisplayItemDropUseCase.RequestValues(data, this, snackbarContainer))
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }


    private fun displayDeathDialogIfNeeded() {
        if (user?.stats?.hp ?: 1.0 > 0) {
            return
        }

        if (this.faintDialog == null && !this.isFinishing) {

            val customView = View.inflate(this, R.layout.dialog_faint, null)
            if (customView != null) {
                val hpBarView = customView.findViewById<View>(R.id.hpBar) as? ValueBar

                hpBarView?.setLightBackground(true)
                hpBarView?.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())

                val dialogAvatarView = customView.findViewById<View>(R.id.avatarView) as? AvatarView
                user?.let { dialogAvatarView?.setAvatar(it) }
            }

            this.faintDialog = HabiticaAlertDialog(this)
            faintDialog?.setTitle(R.string.faint_header)
            faintDialog?.setAdditionalContentView(customView)
            faintDialog?.addButton(R.string.faint_button, true) { _, _ ->
                        faintDialog = null
                        user?.let {
                            userRepository.revive(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                        }
                    }
            soundManager.loadAndPlayAudio(SoundManager.SoundDeath)
            this.faintDialog?.enqueue()
        }
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            drawerFragment?.openDrawer()
            return true
        }

        return super.onKeyUp(keyCode, event)
    }

    protected fun retrieveUser() {
        if (hostConfig.hasAuthentication()) {
            compositeSubscription.add(this.userRepository.retrieveUser(true)
                    .doOnNext { user1 ->
                        FirebaseAnalytics.getInstance(this).setUserProperty("has_party", if (user1.party?.id?.isNotEmpty() == true) "true" else "false")
                        FirebaseAnalytics.getInstance(this).setUserProperty("is_subscribed", if (user1.isSubscribed) "true" else "false")
                        pushNotificationManager.setUser(user1)
                        pushNotificationManager.addPushDeviceUsingStoredToken()
                    }
                    .flatMap { contentRepository.retrieveContent(this,false) }
                    .flatMap { contentRepository.retrieveWorldState() }
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun displayTutorialStep(step: TutorialStep, text: String, canBeDeferred: Boolean) {
        removeActiveTutorialView()
        val view = TutorialView(this, step, this)
        this.activeTutorialView = view
        view.setTutorialText(text)
        view.onReaction = this
        view.setCanBeDeferred(canBeDeferred)
        binding.overlayFrameLayout.addView(view)

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = false
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    fun displayTutorialStep(step: TutorialStep, texts: List<String>, canBeDeferred: Boolean) {
        removeActiveTutorialView()
        val view = TutorialView(this, step, this)
        this.activeTutorialView = view
        view.setTutorialTexts(texts)
        view.onReaction = this
        view.setCanBeDeferred(canBeDeferred)
        binding.overlayFrameLayout.addView(view)

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = false
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    override fun onTutorialCompleted(step: TutorialStep) {
        val path = "flags.tutorial." + step.tutorialGroup + "." + step.identifier
        val updateData = HashMap<String, Any>()
        updateData[path] = true
        compositeSubscription.add(userRepository.updateUser(user, updateData)
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        binding.overlayFrameLayout.removeView(this.activeTutorialView)
        this.removeActiveTutorialView()

        val additionalData = HashMap<String, Any>()
        additionalData["eventLabel"] = step.identifier + "-android"
        additionalData["eventValue"] = step.identifier ?: ""
        additionalData["complete"] = true
        AmplitudeManager.sendEvent("tutorial", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    override fun onTutorialDeferred(step: TutorialStep) {
        taskRepository.executeTransaction(Realm.Transaction { step.displayedOn = Date() })
        this.removeActiveTutorialView()
    }

    private fun removeActiveTutorialView() {
        if (this.activeTutorialView != null) {
            binding.overlayFrameLayout.removeView(this.activeTutorialView)
            this.activeTutorialView = null
        }
    }

    @Subscribe
    fun shareEvent(event: ShareEvent) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "*/*"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, event.sharedMessage)
        val f = BitmapUtils.saveToShareableFile("$filesDir/shared_images", "share.png", event.shareImage)
        val fileUri = f?.let { FileProvider.getUriForFile(this, getString(R.string.content_provider), it) }
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        val resInfoList = this.packageManager.queryIntentActivities(sharingIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            this.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
    }

    private fun checkMaintenance() {
        compositeSubscription.add(this.maintenanceService.maintenanceStatus
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { maintenanceResponse ->
                    if (maintenanceResponse == null) {
                        return@Consumer
                    }
                    if (maintenanceResponse.activeMaintenance) {
                        val intent = createMaintenanceIntent(maintenanceResponse, false)
                        startActivity(intent)
                    } else {
                        if (maintenanceResponse.minBuild != null) {
                            try {
                                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                                @Suppress("DEPRECATION")
                                if (packageInfo.versionCode < maintenanceResponse.minBuild) {
                                    val intent = createMaintenanceIntent(maintenanceResponse, true)
                                    startActivity(intent)
                                }
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }, RxErrorHandler.handleEmptyError()))
    }

    private fun createMaintenanceIntent(maintenanceResponse: MaintenanceResponse, isDeprecationNotice: Boolean): Intent {
        val intent = Intent(this, MaintenanceActivity::class.java)
        val data = Bundle()
        data.putString("title", maintenanceResponse.title)
        data.putString("imageUrl", maintenanceResponse.imageUrl)
        data.putString("description", maintenanceResponse.description)
        data.putBoolean("deprecationNotice", isDeprecationNotice)
        intent.putExtras(data)
        return intent
    }

    @Subscribe
    fun showSnackBarEvent(event: ShowSnackbarEvent) {
        HabiticaSnackbar.showSnackbar(snackbarContainer, event.leftImage, event.title, event.text, event.specialView, event.rightIcon, event.rightTextColor, event.rightText, event.type)
    }

    @Subscribe
    fun showCheckinDialog(event: ShowCheckinDialog) {
        val notificationData = event.notification.data as? LoginIncentiveData
        val title = notificationData?.message

        val factory = LayoutInflater.from(this)
        val view = factory.inflate(R.layout.dialog_login_incentive, null)

        val imageView = view.findViewById(R.id.imageView) as? SimpleDraweeView
        var imageKey = notificationData?.rewardKey?.get(0)
        if (imageKey?.contains("armor") == true) {
            imageKey = "slim_$imageKey"
        }
        DataBindingUtils.loadImage(imageView, imageKey)

        val youEarnedMessage = this.getString(R.string.checkInRewardEarned, notificationData?.rewardText)
        val youEarnedTexView = view.findViewById(R.id.you_earned_message) as? TextView
        youEarnedTexView?.text = youEarnedMessage

        val nextUnlockTextView = view.findViewById(R.id.next_unlock_message) as? TextView
        if (event.nextUnlockCount > 0) {
            nextUnlockTextView?.text = event.nextUnlockText
        } else {
            nextUnlockTextView?.visibility = View.GONE
        }

        compositeSubscription.add(Completable.complete()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action {
                    val alert = HabiticaAlertDialog(this)
                    alert.setAdditionalContentView(view)
                    alert.setTitle(title)
                    alert.addButton(R.string.see_you_tomorrow, true) { _, _ ->
                        apiClient.readNotification(event.notification.id)
                                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                    }
                    alert.show()
                }, RxErrorHandler.handleEmptyError()))
    }

    @Subscribe
    fun showAchievementDialog(event: ShowAchievementDialog) {
        compositeSubscription.add(Completable.complete()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Action {
                    val dialog = AchievementDialog(this)
                    dialog.setType(event.type)
                    dialog.enqueue()
                    apiClient.readNotification(event.id)
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }, RxErrorHandler.handleEmptyError()))
    }

    override fun onEvent(event: ShowConnectionProblemEvent) {
        if (event.title != null) {
            super.onEvent(event)
        } else {
            connectionIssueHandler?.removeCallbacksAndMessages(null)
            binding.connectionIssueTextview.visibility = View.VISIBLE
            binding.connectionIssueTextview.text = event.message
            connectionIssueHandler = Handler()
            connectionIssueHandler?.postDelayed({
                binding.connectionIssueTextview.visibility = View.GONE
            }, 5000)
        }
    }

    fun hatchPet(potion: HatchingPotion, egg: Egg) {
        compositeSubscription.add(this.inventoryRepository.hatchPet(egg, potion) {
            val petWrapper = View.inflate(this, R.layout.pet_imageview, null) as? FrameLayout
            val petImageView = petWrapper?.findViewById(R.id.pet_imageview) as? SimpleDraweeView

            DataBindingUtils.loadImage(petImageView, "Pet-" + egg.key + "-" + potion.key)
            val potionName = potion.text
            val eggName = egg.text
            val dialog = HabiticaAlertDialog(this)
            dialog.setTitle(getString(R.string.hatched_pet_title, potionName, eggName))
            dialog.setAdditionalContentView(petWrapper)
            dialog.addButton(R.string.onwards, true) { hatchingDialog, _ -> hatchingDialog.dismiss() }
            dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                val event1 = ShareEvent()
                event1.sharedMessage = getString(R.string.share_hatched, potionName, eggName) + " https://habitica.com/social/hatch-pet"
                val petImageSideLength = 140
                val sharedImage = Bitmap.createBitmap(petImageSideLength, petImageSideLength, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(sharedImage)
                canvas.drawColor(ContextCompat.getColor(this, R.color.brand_300))
                petImageView?.drawable?.setBounds(0, 0, petImageSideLength, petImageSideLength)
                petImageView?.drawable?.draw(canvas)
                event1.shareImage = sharedImage
                EventBus.getDefault().post(event1)
                hatchingDialog.dismiss()
            }
            dialog.enqueue()
        }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    @Subscribe
    fun onConsumablePurchased(event: ConsumablePurchasedEvent) {
        userRepository.retrieveUser(false, true).subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
    }

    companion object {
        const val SELECT_CLASS_RESULT = 11
        const val GEM_PURCHASE_REQUEST = 111
        const val NOTIFICATION_CLICK = 222
        const val NOTIFICATION_ACCEPT = 223
        const val NOTIFICATION_REJECT = 224
    }
}
