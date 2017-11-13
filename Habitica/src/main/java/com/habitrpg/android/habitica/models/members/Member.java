package com.habitrpg.android.habitica.models.members;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.Avatar;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.user.ContributorInfo;
import com.habitrpg.android.habitica.models.user.Inbox;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Profile;
import com.habitrpg.android.habitica.models.user.Stats;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Member extends RealmObject implements Avatar {


    @PrimaryKey
    @SerializedName("_id")
    private String id;
    private Stats stats;
    private Inbox inbox;
    private MemberPreferences preferences;
    private Profile profile;
    private UserParty party;
    private ContributorInfo contributor;

    private Outfit costume;
    private Outfit equipped;

    private String currentMount;
    private String currentPet;

    private Boolean participatesInQuest;

    public MemberPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(MemberPreferences preferences) {
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
        if (contributor != null && !contributor.isManaged()) {
            contributor.setUserId(id);
        }
        if (costume != null && !costume.isManaged()) {
            costume.setUserId(id+"costume");
        }
        if (equipped != null && !equipped.isManaged()) {
            equipped.setUserId(id+"equipped");
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

    public UserParty getParty() {
        return party;
    }

    public void setParty(UserParty party) {
        this.party = party;
        if (party != null && id != null && !party.isManaged()) {
            party.setUserId(id);
        }
    }

    @Override
    public Integer getGemCount() {
        return 0;
    }

    @Override
    public Integer getHourglassCount() {
        return 0;
    }

    public Outfit getCostume() {
        return costume;
    }

    public void setCostume(Outfit costume) {
        this.costume = costume;
        if (costume != null && id != null) {
            costume.setUserId(id+"costume");
        }
    }

    public Outfit getEquipped() {
        return equipped;
    }

    @Override
    public boolean hasClass() {
        return getPreferences() != null && (!getPreferences().getDisableClasses() && getStats().habitClass.length() != 0);
    }

    public void setEquipped(Outfit equipped) {
        this.equipped = equipped;
        if (equipped != null && id != null) {
            equipped.setUserId(id+"equipped");
        }
    }

    @Override
    public String getCurrentMount() {
        return currentMount;
    }

    public void setCurrentMount(String currentMount) {
        this.currentMount = currentMount;
    }

    @Override
    public String getCurrentPet() {
        return currentPet;
    }

    public void setCurrentPet(String currentPet) {
        this.currentPet = currentPet;
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

    public Boolean getParticipatesInQuest() {
        return participatesInQuest;
    }

    public void setParticipatesInQuest(Boolean participatesInQuest) {
        this.participatesInQuest = participatesInQuest;
    }

    public String getDisplayName() {
        if (profile == null) {
            return "";
        }
        return profile.getName();
    }
}
