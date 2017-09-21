package com.habitrpg.android.habitica.interactors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import javax.inject.Inject;

import rx.Observable;

import static com.habitrpg.android.habitica.helpers.MathHelper.round;
import static com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType;
import static com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.showSnackbar;

public class NotifyUserUseCase extends UseCase<NotifyUserUseCase.RequestValues, Stats> {

    public static final int MIN_LEVEL_FOR_SKILLS = 11;
    private final UserRepository userRepository;
    private LevelUpUseCase levelUpUseCase;

    @Inject
    public NotifyUserUseCase(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread,
                             LevelUpUseCase levelUpUseCase, UserRepository userRepository) {
        super(threadExecutor, postExecutionThread);
        this.levelUpUseCase = levelUpUseCase;
        this.userRepository = userRepository;
    }

    @Override
    protected Observable<Stats> buildUseCaseObservable(RequestValues requestValues) {
        return Observable.defer(() -> {
            if (requestValues.user == null) {
                return Observable.just(null);
            }
            Stats stats = requestValues.user.getStats();

            if (requestValues.hasLeveledUp) {
                return levelUpUseCase.observable(new LevelUpUseCase.RequestValues(requestValues.user, requestValues.context))
                        .flatMap(aVoid -> userRepository.retrieveUser(true))
                        .map(User::getStats);
            } else {
                Pair<View, SnackbarDisplayType> pair = getNotificationAndAddStatsToUser(requestValues.context, requestValues.xp, requestValues.hp, requestValues.gold, requestValues.mp, requestValues.user);
                showSnackbar(requestValues.context, requestValues.snackbarTargetView, null, null, pair.first, pair.second);
                return Observable.just(stats);
            }
        });
    }

    public static Pair<View, SnackbarDisplayType> getNotificationAndAddStatsToUser(Context context, double xp, double hp, double gold, double mp, User user){

        SnackbarDisplayType displayType = SnackbarDisplayType.NORMAL;

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);

        if (xp > 0) {
            container.addView(createTextView(context, xp, HabiticaIconsHelper.imageOfExperience()));
        }
        if (hp != 0) {
            displayType = SnackbarDisplayType.FAILURE;
            container.addView(createTextView(context, hp, HabiticaIconsHelper.imageOfHeartDarkBg()));
        }
        if (gold != 0) {
            container.addView(createTextView(context, gold, HabiticaIconsHelper.imageOfGold()));
            if (gold < 0) {
                displayType = SnackbarDisplayType.FAILURE;
            }
        }
        if (mp > 0 && user.hasClass()) {
            container.addView(createTextView(context, mp, HabiticaIconsHelper.imageOfMagic()));
        }

        int padding = (int) context.getResources().getDimension(R.dimen.spacing_medium);
        for (int index = 1 ; index < container.getChildCount() ; index++) {
            View view = container.getChildAt(index);
            view.setPadding(padding, 0, 0, 0);
        }

        return new Pair<>(container, displayType);
    }

    private static View createTextView(Context context, double value, Bitmap icon) {
        TextView textView = new TextView(context);
        Drawable iconDrawable = new BitmapDrawable(context.getResources(), icon);
        textView.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
        String text;
        if (value > 0) {
            text = " + "+String.valueOf(Math.abs(round(value, 2)));
        } else {
            text = " - "+String.valueOf(Math.abs(round(value, 2)));
        }
        textView.setText(text);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextColor(ContextCompat.getColor(context, R.color.white));
        return textView;
    }

    public static Pair<SpannableStringBuilder, SnackbarDisplayType> getNotificationAndAddStatsToUserAsText(Context context, double xp, double hp, double gold, double mp) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SnackbarDisplayType displayType = SnackbarDisplayType.NORMAL;

        if (xp > 0) {
            builder.append(" + ").append(String.valueOf(round(xp, 2))).append(" Exp");
        }
        if (hp != 0) {
            displayType = SnackbarDisplayType.FAILURE;
            builder.append(" - ").append(String.valueOf(Math.abs(round(hp, 2)))).append(" Health");
        }
        if (gold != 0) {
            if (gold > 0) {
                builder.append(" + ").append(String.valueOf(round(gold, 2)));
            } else if (gold < 0) {
                displayType = SnackbarDisplayType.FAILURE;
                builder.append(" - ").append(String.valueOf(Math.abs(round(gold, 2))));
            }
            builder.append(" Gold");
        }
        if (mp > 0) {
            builder.append(" + ").append(String.valueOf(round(mp, 2))).append(" Mana");
        }

        return new Pair<>(builder, displayType);
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private AppCompatActivity context;
        private ViewGroup snackbarTargetView;
        private User user;
        private double xp;
        private double hp;
        private double gold;
        private double mp;
        private boolean hasLeveledUp;

        public RequestValues(AppCompatActivity context, ViewGroup snackbarTargetView, User user, double xp, double hp, double gold, double mp, boolean hasLeveledUp) {
            this.context = context;
            this.snackbarTargetView = snackbarTargetView;
            this.user = user;
            this.xp = xp;
            this.hp = hp;
            this.gold = gold;
            this.mp = mp;
            this.hasLeveledUp = hasLeveledUp;
        }
    }
}
