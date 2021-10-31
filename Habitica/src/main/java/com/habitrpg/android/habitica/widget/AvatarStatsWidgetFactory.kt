package com.habitrpg.android.habitica.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.helpers.HealthFormatter
import com.habitrpg.android.habitica.helpers.NumberAbbreviator
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class AvatarStatsWidgetFactory(
    private val context: Context,
    private val widgetId: Int
): RemoteViewsService.RemoteViewsFactory {

    private var isInitialized: Boolean = false

    @Inject
    lateinit var userRepository: UserRepository

    private val disposable = CompositeDisposable()

    private var user: User? = null

    private var shouldLoadData: Boolean = false

    private val appWidgetManager = AppWidgetManager.getInstance(context)

    private fun setup() {
        if (!isInitialized) {
            HabiticaBaseApplication.userComponent?.inject(this)
            isInitialized = true
        }
    }

    private fun loadUser() {
        val mainHandler = Handler(context.mainLooper)
        mainHandler.post {
            disposable.add(userRepository.getUser()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { user ->
                        this.user = user
                        this.shouldLoadData = false
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_avatar_list)
                    },
                    RxErrorHandler.handleEmptyError()
                )
            )
        }
    }

    private fun getRemoteViewForUser(user: User, stats: Stats): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_avatar_stats)

        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val cols = BaseWidgetProvider.getCellsForSize(minWidth)
        val rows = BaseWidgetProvider.getCellsForSize(minHeight)

        val showAvatar = cols > 3
        val showManaBar = rows > 1

        val currentHealth = HealthFormatter.format(stats.hp ?: 0.0)
        val currentHealthString = HealthFormatter.formatToString(stats.hp ?: 0.0)
        val healthValueString = currentHealthString + "/" + stats.maxHealth
        val expValueString = "" + stats.exp?.toInt() + "/" + stats.toNextLevel
        val mpValueString = "" + stats.mp?.toInt() + "/" + stats.maxMP

        remoteViews.setTextViewText(R.id.TV_hp_value, healthValueString)
        remoteViews.setTextViewText(R.id.exp_TV_value, expValueString)
        remoteViews.setTextViewText(R.id.mp_TV_value, mpValueString)

        remoteViews.setImageViewBitmap(R.id.ic_hp_header, HabiticaIconsHelper.imageOfHeartLightBg())
        remoteViews.setImageViewBitmap(R.id.ic_exp_header, HabiticaIconsHelper.imageOfExperience())
        remoteViews.setImageViewBitmap(R.id.ic_mp_header, HabiticaIconsHelper.imageOfMagic())

        remoteViews.setProgressBar(R.id.hp_bar, stats.maxHealth ?: 0, currentHealth.toInt(), false)
        remoteViews.setProgressBar(R.id.exp_bar, stats.toNextLevel ?: 0, stats.exp?.toInt() ?: 0, false)
        remoteViews.setProgressBar(R.id.mp_bar, stats.maxMP ?: 0, stats.mp?.toInt() ?: 0, false)
        remoteViews.setViewVisibility(R.id.mp_wrapper, if (showManaBar && (stats.habitClass == null || (stats.lvl ?: 0) < 10 || user.preferences?.disableClasses == true)) View.GONE else View.VISIBLE)

        remoteViews.setTextViewText(R.id.gold_tv, NumberAbbreviator.abbreviate(context, stats.gp ?: 0.0))
        remoteViews.setTextViewText(R.id.gems_tv, (user.balance * 4).toInt().toString())
        val hourGlassCount = user.hourglassCount
        if (hourGlassCount == 0) {
            remoteViews.setViewVisibility(R.id.hourglass_icon, View.GONE)
            remoteViews.setViewVisibility(R.id.hourglasses_tv, View.GONE)
        } else {
            remoteViews.setImageViewBitmap(R.id.hourglass_icon, HabiticaIconsHelper.imageOfHourglass())
            remoteViews.setViewVisibility(R.id.hourglass_icon, View.VISIBLE)
            remoteViews.setTextViewText(R.id.hourglasses_tv, hourGlassCount.toString())
            remoteViews.setViewVisibility(R.id.hourglasses_tv, View.VISIBLE)
        }
        remoteViews.setImageViewBitmap(R.id.gem_icon, HabiticaIconsHelper.imageOfGem())
        remoteViews.setImageViewBitmap(R.id.gold_icon, HabiticaIconsHelper.imageOfGold())
        remoteViews.setTextViewText(R.id.lvl_tv, context.getString(R.string.user_level, user.stats?.lvl ?: 0))

        if (showAvatar) {
            val avatarView =
                AvatarView(context, showBackground = true, showMount = true, showPet = true)
            val layoutParams = ViewGroup.LayoutParams(140.dpToPx(context), 147.dpToPx(context))
            avatarView.layoutParams = layoutParams
            avatarView.setAvatar(user)
            avatarView.createAvatarImage()?.let { bitmap ->
                remoteViews.setImageViewBitmap(R.id.avatar_view, bitmap)
            }

        }

        if (showAvatar) {
            remoteViews.setViewVisibility(R.id.avatar_view, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.avatar_view, View.GONE)
        }

        if (showManaBar) {
            remoteViews.setViewVisibility(R.id.detail_info_view, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.detail_info_view, View.GONE)
        }

        return remoteViews
    }

    override fun onCreate() {
        setup()
        loadUser()
    }

    override fun onDestroy() {
        disposable.clear()
    }

    override fun onDataSetChanged() {
        if (shouldLoadData) {
            loadUser()
        }
        shouldLoadData = true
    }

    override fun getCount(): Int {
        return 1
    }

    override fun getViewAt(p0: Int): RemoteViews {
        val user = this.user
        val stats = user?.stats
        return if (user != null && stats != null) {
            getRemoteViewForUser(user, stats)
        } else {
            RemoteViews(context.packageName, R.layout.widget_avatar_stats)
        }
    }


    override fun getLoadingView() = RemoteViews(context.packageName, R.layout.widget_avatar_stats)

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true
}