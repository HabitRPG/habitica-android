package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ChallengeRepository;
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.magicmicky.habitrpgwrapper.lib.models.Challenge;
import com.magicmicky.habitrpgwrapper.lib.models.PostChallenge;

import rx.Observable;


public class ChallengeRepositoryImpl extends BaseRepositoryImpl<ChallengeLocalRepository> implements ChallengeRepository {

    public ChallengeRepositoryImpl(ChallengeLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<Challenge> createChallenge(PostChallenge challenge) {
        return apiClient.createChallenge(challenge);
    }

    @Override
    public Observable<Challenge> updateChallenge(PostChallenge challenge) {
        return apiClient.updateChallenge(challenge);
    }

    @Override
    public Observable<Void> deleteChallenge(String challengeId) {
        return apiClient.deleteChallenge(challengeId);
    }
}
