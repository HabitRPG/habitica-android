package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.PostChallenge;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.List;

import rx.Observable;

public interface ChallengeRepository extends BaseRepository  {
    Observable<Challenge> createChallenge(PostChallenge challenge, List<Task> taskList);
    Observable<Challenge> updateChallenge(PostChallenge challenge, List<Task> taskList);
    Observable<Void> deleteChallenge(String challengeId);
}
