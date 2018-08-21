package com.habitrpg.android.habitica.widget

import android.annotation.TargetApi
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.util.Pair
import android.text.SpannableStringBuilder
import android.widget.RemoteViews
import android.widget.Toast

import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.interactors.NotifyUserUseCase
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import java.util.HashMap

import java.util.Objects

import javax.inject.Inject


abstract class BaseWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var userRepository: UserRepository

    protected var context: Context? = null

    /**
     * Returns number of cells needed for given size of the widget.<br></br>
     * see http://stackoverflow.com/questions/14270138/dynamically-adjusting-widgets-content-and-layout-to-the-size-the-user-defined-t
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private fun getCellsForSize(size: Int): Int {
        var n = 2
        while (70 * n - 30 < size) {
            ++n
        }
        return n - 1
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        this.context = context
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

        appWidgetManager.partiallyUpdateAppWidget(appWidgetId,
                sizeRemoteViews(context, options, appWidgetId))

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions)

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun sizeRemoteViews(context: Context, options: Bundle, widgetId: Int): RemoteViews {
        this.context = context
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        // First find out rows and columns based on width provided.
        val rows = getCellsForSize(minHeight)
        val columns = getCellsForSize(minWidth)
        val remoteViews = RemoteViews(context.packageName,
                layoutResourceId())

        return configureRemoteViews(remoteViews, widgetId, columns, rows)
    }

    protected fun showToastForTaskDirection(context: Context, data: TaskScoringResult?, userID: String) {
        if (userRepository == null) {
            HabiticaBaseApplication.component?.inject(this)
        }
        if (data != null) {
            val pair = NotifyUserUseCase.getNotificationAndAddStatsToUserAsText(context, data.experienceDelta!!, data.healthDelta!!, data.goldDelta!!, data.manaDelta!!)
            val toast = Toast.makeText(context, pair.first, Toast.LENGTH_LONG)
            toast.show()
        }
    }

    abstract fun layoutResourceId(): Int

    abstract fun configureRemoteViews(remoteViews: RemoteViews, widgetId: Int, columns: Int, rows: Int): RemoteViews

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val additionalData = HashMap<String, Any>()
        additionalData["identifier"] = this.javaClass.simpleName
        AmplitudeManager.sendEvent("widgets", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_CREATE_WIDGET, additionalData)
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val additionalData = HashMap<String, Any>()
        additionalData["identifier"] = this.javaClass.simpleName
        AmplitudeManager.sendEvent("widgets", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_REMOVE_WIDGET, additionalData)
        super.onDeleted(context, appWidgetIds)
    }
}
