package com.habitrpg.android.habitica.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.HostConfig;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import javax.inject.Inject;

public class AvatarStatsWidgetService extends Service {
    private static final String LOG = ".avatarwidget.service";
    @Inject
    public HostConfig hostConfig;
    private AppWidgetManager appWidgetManager;


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        HabiticaApplication application = (HabiticaApplication) getApplication();
        application.getComponent().inject(this);
        this.appWidgetManager = AppWidgetManager.getInstance(this);

        new Select().from(HabitRPGUser.class).where(Condition.column("id").eq(hostConfig.getUser())).async().querySingle(userTransactionListener);

        stopSelf();

        return START_STICKY;
    }

    private TransactionListener<HabitRPGUser> userTransactionListener = new TransactionListener<HabitRPGUser>() {
        @Override
        public void onResultReceived(HabitRPGUser habitRPGUser) {
            updateData(habitRPGUser);
        }

        @Override
        public boolean onReady(BaseTransaction<HabitRPGUser> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<HabitRPGUser> baseTransaction, HabitRPGUser habitRPGUser) {
            return true;
        }
    };

    private void updateData(HabitRPGUser user) {
        if (user == null || user.getStats() == null) {
            return;
        }
        Stats stats = user.getStats();
        ComponentName thisWidget = new ComponentName(this, AvatarStatsWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        String healthValueString = "" + stats.getHp().intValue() + "/" + stats.getMaxHealth();
        String expValueString = "" + stats.getExp().intValue() + "/" + stats.getToNextLevel();
        String mpValueString = "" + stats.getMp().intValue() + "/" + stats.getMaxMP();

        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_avatar_stats);

        remoteViews.setTextViewText(R.id.TV_hp_value, healthValueString);
        remoteViews.setTextViewText(R.id.exp_TV_value, expValueString);
        remoteViews.setTextViewText(R.id.mp_TV_value, mpValueString);

        remoteViews.setProgressBar(R.id.hp_bar, stats.getMaxHealth(), stats.getHp().intValue(), false);
        remoteViews.setProgressBar(R.id.exp_bar, stats.getToNextLevel(), stats.getExp().intValue(), false);
        remoteViews.setProgressBar(R.id.mp_bar, stats.getMaxMP(), stats.getMp().intValue(), false);
        remoteViews.setViewVisibility(R.id.mp_wrapper, (stats.get_class() == null || stats.getLvl() < 10 || user.getPreferences().getDisableClasses()) ? View.GONE : View.VISIBLE);

        int gp = (stats.getGp().intValue());
        int sp = (int) ((stats.getGp() - gp) * 100);
        remoteViews.setTextViewText(R.id.gold_tv, String.valueOf(gp));
        remoteViews.setTextViewText(R.id.silver_tv, String.valueOf(sp));
        remoteViews.setTextViewText(R.id.gems_tv, String.valueOf((int)(user.getBalance() * 4)));
        remoteViews.setTextViewText(R.id.lvl_tv, getString(R.string.user_level, user.getStats().getLvl()));

        AvatarView avatarView = new AvatarView(this, true, true, true);;
        avatarView.setUser(user);
        avatarView.onAvatarImageReady(bitmap -> {
             remoteViews.setImageViewBitmap(R.id.avatar_view, bitmap);
            appWidgetManager.partiallyUpdateAppWidget(allWidgetIds, remoteViews);
        });

        //If user click on life and xp: open the app
        Intent openAppIntent = new Intent(this.getApplicationContext(), MainActivity.class);
        PendingIntent openApp = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_main_view, openApp);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            appWidgetManager.partiallyUpdateAppWidget(allWidgetIds, remoteViews);
        } else {
            appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
