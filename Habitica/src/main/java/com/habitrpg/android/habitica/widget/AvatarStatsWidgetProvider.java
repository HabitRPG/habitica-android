package com.habitrpg.android.habitica.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.NumberAbbreviator;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.AvatarView;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import javax.inject.Inject;
import javax.inject.Named;

public class AvatarStatsWidgetProvider extends BaseWidgetProvider {

    private AppWidgetManager appWidgetManager;

    @Override
    public int layoutResourceId() {
        return R.layout.widget_avatar_stats;
    }

    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;
    @Inject
    UserRepository userRepository;

    private void setUp() {
        if (userRepository == null) {
            HabiticaBaseApplication.getComponent().inject(this);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.setUp();
        this.appWidgetManager = appWidgetManager;
        this.context = context;

        userRepository.getUser(userId).subscribe(this::updateData, RxErrorHandler.handleEmptyError());
    }

    @Override
    public RemoteViews configureRemoteViews(RemoteViews remoteViews, int widgetId, int columns, int rows) {
        if (columns > 3) {
            remoteViews.setViewVisibility(R.id.avatar_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.avatar_view, View.GONE);
        }

        if (rows > 1) {
            remoteViews.setViewVisibility(R.id.mp_wrapper, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.detail_info_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.mp_wrapper, View.GONE);
            remoteViews.setViewVisibility(R.id.detail_info_view, View.GONE);
        }

        return remoteViews;
    }

    private void updateData(User user) {
        if (user == null || user.getStats() == null) {
            return;
        }
        Stats stats = user.getStats();
        ComponentName thisWidget = new ComponentName(context, AvatarStatsWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        String healthValueString = "" + stats.getHp().intValue() + "/" + stats.getMaxHealth();
        String expValueString = "" + stats.getExp().intValue() + "/" + stats.getToNextLevel();
        String mpValueString = "" + stats.getMp().intValue() + "/" + stats.getMaxMP();

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_avatar_stats);

            remoteViews.setTextViewText(R.id.TV_hp_value, healthValueString);
            remoteViews.setTextViewText(R.id.exp_TV_value, expValueString);
            remoteViews.setTextViewText(R.id.mp_TV_value, mpValueString);

            remoteViews.setImageViewBitmap(R.id.ic_hp_header, HabiticaIconsHelper.imageOfHeartDarkBg());
            remoteViews.setImageViewBitmap(R.id.ic_exp_header, HabiticaIconsHelper.imageOfExperience());
            remoteViews.setImageViewBitmap(R.id.ic_mp_header, HabiticaIconsHelper.imageOfMagic());

            remoteViews.setProgressBar(R.id.hp_bar, stats.getMaxHealth(), stats.getHp().intValue(), false);
            remoteViews.setProgressBar(R.id.exp_bar, stats.getToNextLevel(), stats.getExp().intValue(), false);
            remoteViews.setProgressBar(R.id.mp_bar, stats.getMaxMP(), stats.getMp().intValue(), false);
            remoteViews.setViewVisibility(R.id.mp_wrapper, (stats.getHabitClass() == null || stats.getLvl() < 10 || user.getPreferences().getDisableClasses()) ? View.GONE : View.VISIBLE);

            remoteViews.setTextViewText(R.id.gold_tv, NumberAbbreviator.abbreviate(context, stats.getGp()));
            remoteViews.setTextViewText(R.id.gems_tv, String.valueOf((int) (user.getBalance() * 4)));
            int hourGlassCount = user.getHourglassCount();
            if (hourGlassCount == 0) {
                remoteViews.setViewVisibility(R.id.hourglasses_tv, View.GONE);
            } else {
                remoteViews.setTextViewText(R.id.hourglasses_tv, String.valueOf(hourGlassCount));
                remoteViews.setViewVisibility(R.id.hourglasses_tv, View.VISIBLE);
            }
            remoteViews.setImageViewBitmap(R.id.hourglass_cion, HabiticaIconsHelper.imageOfHourglass());
            remoteViews.setImageViewBitmap(R.id.gem_icon, HabiticaIconsHelper.imageOfGem());
            remoteViews.setImageViewBitmap(R.id.gold_icon, HabiticaIconsHelper.imageOfGold());
            remoteViews.setTextViewText(R.id.lvl_tv, context.getString(R.string.user_level, user.getStats().getLvl()));

            AvatarView avatarView = new AvatarView(context, true, true, true);

            avatarView.setAvatar(user);
            RemoteViews finalRemoteViews = remoteViews;
            avatarView.onAvatarImageReady(bitmap -> {
                finalRemoteViews.setImageViewBitmap(R.id.avatar_view, bitmap);
                appWidgetManager.partiallyUpdateAppWidget(allWidgetIds, finalRemoteViews);
            });

            //If user click on life and xp: open the app
            Intent openAppIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent openApp = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_main_view, openApp);


            if (Build.VERSION.SDK_INT >= 16) {
                Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                remoteViews = sizeRemoteViews(context, options, widgetId);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
