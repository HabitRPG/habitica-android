package com.habitrpg.android.habitica.widget;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.callbacks.HabitRPGUserCallback;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import javax.inject.Inject;

/**
 * The service that should update the simple widget
 *
 * @see com.habitrpg.android.habitica.widget.SimpleWidget
 * Created by Mickael on 01/11/13.
 */
public class UpdateWidgetService extends Service implements HabitRPGUserCallback.OnUserReceived {
    private static final String LOG = ".simplewidget.service";
    private AppWidgetManager appWidgetManager;

    @Inject
    public APIHelper apiHelper;

    public UpdateWidgetService() {
        super();
        ((HabiticaApplication)getApplication()).getComponent().inject(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        this.appWidgetManager = AppWidgetManager.getInstance(this);
        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
        ComponentName thisWidget = new ComponentName(this,
                SimpleWidget.class);
        int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);

        if (apiHelper != null) {
            apiHelper.retrieveUser(true).subscribe(new HabitRPGUserCallback(this));
            for (int widgetId : allWidgetIds) {
                RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.simple_widget);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        } else {
            for (int widgetId : allWidgetIds) {
                RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.simple_widget);
                RemoteViews textConnect = new RemoteViews(this.getPackageName(), R.layout.simple_textview);
                textConnect.setTextViewText(R.id.TV_simple_textview, getString(R.string.please_connect));
                remoteViews.removeAllViews(R.id.LL_header);
                remoteViews.addView(R.id.LL_header, textConnect);


                Intent clickIntent = new Intent(this.getApplicationContext(), SimpleWidget.class);
                clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
                PendingIntent updateIntent = PendingIntent.getBroadcast(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.BT_refresh, updateIntent);

                Intent openAppIntent = new Intent(this.getApplicationContext(), MainActivity.class);
                PendingIntent openApp = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget_main_view, openApp);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);

            }
        }
        stopSelf();

        return START_STICKY;
    }

    private void updateData(HabitRPGUser user, AppWidgetManager appWidgetManager) {
        ComponentName thisWidget = new ComponentName(this, SimpleWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.simple_widget);
            remoteViews.setTextViewText(R.id.TV_HP, "" + user.getStats().getHp().intValue() + "/" + (int) user.getStats().getMaxHealth() + " " + this.getString(R.string.HP_default));
            remoteViews.setTextViewText(R.id.TV_XP, "" + user.getStats().getExp().intValue() + "/" + (int) user.getStats().getToNextLevel() + " " + this.getString(R.string.XP_default));
            //remoteViews.setImageViewBitmap(R.id.IMG_ProfilePicture, dealWithUserPicture(user,this));
            remoteViews.setProgressBar(R.id.V_HPBar, (int) user.getStats().getMaxHealth(), user.getStats().getHp().intValue(), false);
            remoteViews.setProgressBar(R.id.V_XPBar, (int) user.getStats().getToNextLevel(), user.getStats().getExp().intValue(), false);

            // If user click on refresh: refresh
            Intent clickIntent = new Intent(this.getApplicationContext(), SimpleWidget.class);
            clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
            PendingIntent updateIntent = PendingIntent.getBroadcast(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.BT_refresh, updateIntent);

            //If user click on life and xp: open the app
            Intent openAppIntent = new Intent(this.getApplicationContext(), MainActivity.class);
            PendingIntent openApp = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.LL_header, openApp);
            remoteViews.setOnClickPendingIntent(R.id.IMG_ProfilePicture, openApp);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onUserReceived(HabitRPGUser user) {
        this.updateData(user, appWidgetManager);

    }
}
