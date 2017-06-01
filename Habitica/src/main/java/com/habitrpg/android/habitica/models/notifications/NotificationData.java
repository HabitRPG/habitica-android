package com.habitrpg.android.habitica.models.notifications;

import java.util.List;

/**
 * Created by krh12 on 11/30/2016.
 */

public class NotificationData {

    public String groupId;
    public String message;
    public Integer nextRewardAt;
    public String rewardText;
    public List<String> rewardKey;
    public List<Reward> reward;

}
