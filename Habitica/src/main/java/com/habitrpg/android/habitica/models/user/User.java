package com.habitrpg.android.habitica.models.user;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.Avatar;
import com.habitrpg.android.habitica.models.PushDevice;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.invitations.Invitations;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.habitrpg.android.habitica.ui.AvatarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject implements Avatar {

    @Ignore
    public TaskList tasks;

    @PrimaryKey
    @SerializedName("_id")
    private String id;
    private double balance;
    private Stats stats;
    private Inbox inbox;
    private Preferences preferences;
    private Profile profile;
    private UserParty party;
    private Items items;
    @SerializedName("auth")
    private Authentication authentication;
    private Flags flags;
    private ContributorInfo contributor;
    private Invitations invitations;

    RealmList<Tag> tags;

    @Ignore
    private List<PushDevice> pushDevices;

    private Purchases purchased;

    @Ignore
    private TasksOrder tasksOrder;

    private RealmList<Challenge> challenges;

    private Date lastCron;
    private Boolean needsCron;
    private int loginIncentives;

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
        if (preferences != null && id != null && !preferences.isManaged()) {
            preferences.setUserId(id);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (stats != null && !stats.isManaged()) {
            stats.setUserId(id);
        }
        if (inbox != null && !inbox.isManaged()) {
            inbox.setUserId(id);
        }
        if (preferences != null && !preferences.isManaged()) {
            preferences.setUserId(id);
        }
        if (profile != null && !profile.isManaged()) {
            profile.setUserId(id);
        }
        if (items != null && !items.isManaged()) {
            items.setUserId(id);
        }
        if (authentication != null && !authentication.isManaged()) {
            authentication.setUserId(id);
        }
        if (flags != null && !flags.isManaged()) {
            flags.setUserId(id);
        }
        if (contributor != null && !contributor.isManaged()) {
            contributor.setUserId(id);
        }
        if (invitations != null && !invitations.isManaged()) {
            invitations.setUserId(id);
        }
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
        if (stats != null && id != null && !stats.isManaged()) {
            stats.setUserId(id);
        }
    }

    public Inbox getInbox() {
        return inbox;
    }

    public void setInbox(Inbox inbox) {
        this.inbox = inbox;
        if (inbox != null && id != null && !inbox.isManaged()) {
            inbox.setUserId(id);
        }
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        if (profile != null && id != null && !profile.isManaged()) {
            profile.setUserId(id);
        }
    }

    public ContributorInfo getContributor() {
        return contributor;
    }

    public void setContributor(ContributorInfo contributor) {
        this.contributor = contributor;
        if (contributor != null && id != null && !contributor.isManaged()) {
            contributor.setUserId(id);
        }
    }

    public Invitations getInvitations() {
        return invitations;
    }

    public void setInvitations(Invitations invitations) {
        this.invitations = invitations;
        if (invitations != null && id != null && !invitations.isManaged()) {
            invitations.setUserId(id);
        }
    }

    public UserParty getParty() {
        return party;
    }

    public void setParty(UserParty party) {
        this.party = party;
        if (party != null && id != null && !party.isManaged()) {
            party.setUserId(id);
        }
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
        if (items != null && id != null && !items.isManaged()) {
            items.setUserId(id);
        }
    }

    public double getBalance() {
        return this.balance;
    }

    public Integer getGemCount(){
        return (int)(this.balance * 4);
    }

    @Override
    public Integer getHourglassCount() {
        if (getPurchased() != null) {
            return getPurchased().getPlan().consecutive.getTrinkets();
        }
        return 0;
    }

    @Override
    public Outfit getCostume() {
        if (getItems() != null && getItems().getGear() != null) {
            return getItems().getGear().getCostume();
        }
        return null;
    }

    @Override
    public Outfit getEquipped() {
        if (getItems() != null && getItems().getGear() != null) {
            return getItems().getGear().getEquipped();
        }
        return null;
    }

    @Override
    public boolean hasClass() {
        return getPreferences() != null && getFlags() != null && (!getPreferences().getDisableClasses() && getFlags().getClassSelected() && getStats().habitClass.length() != 0);
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
        if (authentication != null && id != null) {
            authentication.setUserId(id);
        }
    }

    public Purchases getPurchased() {
        return purchased;
    }

    public void setPurchased(Purchases purchased) {
        this.purchased = purchased;
        if (purchased != null && id != null) {
            purchased.setUserId(id);
        }
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
        if (flags != null && id != null) {
            flags.setUserId(id);
        }
    }

    public TasksOrder getTasksOrder() {
        return tasksOrder;
    }

    public void setTasksOrder(TasksOrder tasksOrder) {
        this.tasksOrder = tasksOrder;
    }

    public List<PushDevice> getPushDevices() {
        return this.pushDevices;
    }

    public void setPushDevices(List<PushDevice> pushDevices) {
        this.pushDevices = pushDevices;
    }

    @Override
    public String getCurrentMount() {
        if (getItems() != null) {
            return getItems().getCurrentMount();
        }
        return "";
    }

    @Override
    public String getCurrentPet() {

        if (getItems() != null) {
            return getItems().getCurrentPet();
        }
        return "";
    }

    @Override
    public String getBackground() {
        if (getPreferences() != null) {
            return getPreferences().getBackground();
        }
        return "";
    }

    @Override
    public boolean getSleep() {
        return getPreferences() != null && getPreferences().getSleep();
    }

    public int getPetsFoundCount() {
        return items == null || items.getPets() == null ? 0 : items.getPets().size();
    }

    public int getMountsTamedCount() {
        return items == null || items.getMounts() == null ? 0 : items.getMounts().size();
    }

    public RealmList<Tag> getTags() {
        return tags;
    }

    public void setTags(RealmList<Tag> tags) {
        this.tags = tags;
    }

    public RealmList<Challenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(RealmList<Challenge> challenges) {
        this.challenges = challenges;
    }

    public boolean hasParty() {
        return party != null && party.id != null && party.id.length() > 0;
    }

    public Boolean getNeedsCron() {
        if (needsCron == null) {
            return false;
        }
        return needsCron;
    }

    public Date getLastCron() {
        return lastCron;
    }

    public void setLastCron(Date lastCron) {
        this.lastCron = lastCron;
    }

    public void setNeedsCron(boolean needsCron) {
        this.needsCron = needsCron;
    }

    public int getLoginIncentives() {
        return loginIncentives;
    }

    public void setLoginIncentives(int loginIncentives) {
        this.loginIncentives = loginIncentives;
    }
}
