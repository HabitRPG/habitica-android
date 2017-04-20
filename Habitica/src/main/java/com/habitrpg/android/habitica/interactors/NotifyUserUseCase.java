package com.habitrpg.android.habitica.interactors;

import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.models.user.User;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.ui.helpers.UiUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;

import static com.habitrpg.android.habitica.helpers.MathHelper.round;
import static com.habitrpg.android.habitica.ui.helpers.UiUtils.showSnackbar;

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
            Stats stats = requestValues.user.getStats();

            if (requestValues.lvl > stats.getLvl()) {
                return levelUpUseCase.observable(new LevelUpUseCase.RequestValues(requestValues.user, requestValues.lvl, requestValues.context))
                        .flatMap(aVoid -> userRepository.retrieveUser(false))
                        .map(User::getStats);
            } else {
                Pair<String, UiUtils.SnackbarDisplayType> pair = getNotificationAndAddStatsToUser(requestValues.user, requestValues.xp, requestValues.hp, requestValues.gold, requestValues.mp);
                showSnackbar(requestValues.context, requestValues.snackbarTargetView, pair.first, pair.second);
                return Observable.just(stats);
            }
        });
    }

    public static Pair<String, UiUtils.SnackbarDisplayType> getNotificationAndAddStatsToUser(User user, double xp, double hp, double gold, double mp){

        StringBuilder message = new StringBuilder();
        Stats stats = user.getStats();
        UiUtils.SnackbarDisplayType displayType = UiUtils.SnackbarDisplayType.NORMAL;

        if (xp > stats.getExp()) {
            message.append(" + ").append(round(xp - stats.getExp(), 2)).append(" XP");
            stats.setExp(xp);
        }
        if (hp != stats.getHp()) {
            displayType = UiUtils.SnackbarDisplayType.FAILURE;
            message.append(" - ").append(round(stats.getHp() - hp, 2)).append(" HP");
            stats.setHp(hp);
        }
        if (gold > stats.getGp()) {
            message.append(" + ").append(round(gold - stats.getGp(), 2)).append(" GP");
            stats.setGp(gold);
        } else if (gold < stats.getGp()) {
            displayType = UiUtils.SnackbarDisplayType.FAILURE;
            message.append(" - ").append(round(stats.getGp() - gold, 2)).append(" GP");
            stats.setGp(gold);
        }
        if (mp > stats.getMp() && stats.getLvl() >= MIN_LEVEL_FOR_SKILLS) {
            message.append(" + ").append(round(mp - stats.getMp(), 2)).append(" MP");
            stats.setMp(mp);
        }

        return new Pair<>(message.toString(), displayType);
    }

     public static final class RequestValues implements UseCase.RequestValues {

        private AppCompatActivity context;
        private View snackbarTargetView;
        private Action0 retrieveUser;
        private User user;
        private double xp;
        private double hp;
        private double gold;
        private double mp;
        private int lvl;

        public RequestValues(AppCompatActivity context, View snackbarTargetView, Action0 retrieveUser,
                             User user, double xp, double hp, double gold, double mp, int lvl) {
            this.context = context;
            this.snackbarTargetView = snackbarTargetView;
            this.retrieveUser = retrieveUser;
            this.user = user;
            this.xp = xp;
            this.hp = hp;
            this.gold = gold;
            this.mp = mp;
            this.lvl = lvl;
        }
    }
}
