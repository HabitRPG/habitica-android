package com.habitrpg.android.habitica.data.local;

import com.magicmicky.habitrpgwrapper.lib.models.Group;
import java.util.List;

import rx.Observable;

public interface ChallengeLocalRepository extends BaseLocalRepository {

    void setUsersGroups(List<Group> group);

    Observable<List<Group>> getGroups();
}
