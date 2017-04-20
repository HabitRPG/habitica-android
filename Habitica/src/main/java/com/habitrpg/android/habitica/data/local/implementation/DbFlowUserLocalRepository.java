package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.UserLocalRepository;
import com.habitrpg.android.habitica.models.Skill;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.user.SpecialItems;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class DbFlowUserLocalRepository implements UserLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<HabitRPGUser> getUser(String userID) {
        return Observable.defer(() -> Observable.just(new Select()
                .from(HabitRPGUser.class).where(Condition.column("id").eq(userID)).querySingle()));
    }

    @Override
    public void saveUser(HabitRPGUser user) {
        user.async().save();
    }

    @Override
    public Observable<List<TutorialStep>> getTutorialSteps() {
        return Observable.defer(() -> Observable.just(new Select()
                .from(TutorialStep.class).queryList()));
    }

    @Override
    public Observable<List<Skill>> getSkills(HabitRPGUser user) {
        return Observable.defer(() -> Observable.just(new Select()
                .from(Skill.class)
                .where(Condition.column("habitClass").eq(user.getStats().get_class()))
                .and(Condition.column("lvl").lessThanOrEq(user.getStats().getLvl()))
                .queryList()));
    }

    @Override
    public Observable<List<Skill>> getSpecialItems(HabitRPGUser user) {
        SpecialItems specialItems = user.getItems().getSpecial();
        if (specialItems != null) {

            Condition.In specialsWhere = Condition.column("key").in("");

            if (specialItems.getSnowball() > 0) {
                specialsWhere.and("snowball");
            }

            if (specialItems.getShinySeed() > 0) {
                specialsWhere.and("shinySeed");
            }

            if (specialItems.getSeafoam() > 0) {
                specialsWhere.and("seafoam");
            }

            if (specialItems.getSpookySparkles() > 0) {
                specialsWhere.and("spookySparkles");
            }
            return Observable.defer(() -> Observable.from(new Select()
                    .from(Skill.class)
                    .where(specialsWhere)
                    .queryList()))
                    .map(skill -> {
                        skill.isSpecialItem = true;
                        skill.target = "party";
                        return skill;
                    })
                    .toList();
        } else {
            return Observable.just(new ArrayList<>());
        }
    }
}
