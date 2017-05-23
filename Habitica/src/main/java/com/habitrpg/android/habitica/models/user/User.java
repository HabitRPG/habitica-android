package com.habitrpg.android.habitica.models.user;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.models.PushDevice;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.invitations.Invitations;
import com.habitrpg.android.habitica.models.social.Challenge;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.habitrpg.android.habitica.ui.AvatarView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

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

    public int getGemCount(){
        return (int)(this.balance * 4);
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

    public List<String> getAvatarLayerNames() {
        List<String> layerNames = new ArrayList<String>();

        Preferences prefs = this.getPreferences();

        if (prefs.getChair() != null) {
            layerNames.add(prefs.getChair());
        }

        Outfit outfit = null;
        if (this.getItems() != null) {
            if (prefs.getCostume()) {
                outfit = this.getItems().getGear().getCostume();
            } else {
                outfit = this.getItems().getGear().getEquipped();
            }
        }

        if (outfit != null) {
            if (outfit.getBack() != null) {
                layerNames.add(outfit.getBack());
            }
        }

        if (prefs.getSleep()) {
            layerNames.add("skin_" + prefs.getSkin() + "_sleep");
        } else {
            layerNames.add("skin_" + prefs.getSkin());
        }
        layerNames.add(prefs.getSize() + "_shirt_" + prefs.getShirt());
        layerNames.add("head_0");

        if (outfit != null) {
            String armor = outfit.getArmor();

            if (armor != null && !armor.equals("armor_base_0")) {
                layerNames.add(prefs.getSize() + "_" + armor);
            }
            if (outfit.getBody() != null && !outfit.getBody().equals("body_base_0")) {
                layerNames.add(outfit.getBody());
            }
        }

        Hair hair = prefs.getHair();
        if (hair != null) {
            String hairColor = hair.getColor();

            if (hair.getBase() > 0) {
                layerNames.add("hair_base_" + hair.getBase() + "_" + hairColor);
            }
            if (hair.getBangs() > 0) {
                layerNames.add("hair_bangs_" + hair.getBangs() + "_" + hairColor);
            }
            if (hair.getMustache() > 0) {
                layerNames.add("hair_mustache_" + hair.getMustache() + "_" + hairColor);
            }
            if (hair.getBeard() > 0) {
                layerNames.add("hair_beard_" + hair.getBeard() + "_" + hairColor);
            }
        }

        if (outfit != null) {
            if (outfit.getEyeWear() != null && !outfit.getEyeWear().equals("eyewear_base_0")) {
                layerNames.add(outfit.getEyeWear());
            }
            if (outfit.getHead() != null && !outfit.getHead().equals("head_base_0")) {
                layerNames.add(outfit.getHead());
            }
            if (outfit.getHeadAccessory() != null && !outfit.getHeadAccessory().equals("headAccessory_base_0")) {
                layerNames.add(outfit.getHeadAccessory());
            }
            if (outfit.getShield() != null && !outfit.getShield().equals("shield_base_0")) {
                layerNames.add(outfit.getShield());
            }
            if (outfit.getWeapon() != null && !outfit.getWeapon().equals("weapon_base_0")) {
                layerNames.add(outfit.getWeapon());
            }
        }

        if (prefs.getSleep()) {
            layerNames.add("zzz");
        }

        return layerNames;
    }

    public EnumMap<AvatarView.LayerType, String> getAvatarLayerMap() {
        EnumMap<AvatarView.LayerType, String> layerMap = new EnumMap<>(AvatarView.LayerType.class);

        Preferences prefs = getPreferences();
        Outfit outfit = (prefs.getCostume()) ? getItems().getGear().getCostume() : getItems().getGear().getEquipped();

        boolean hasVisualBuffs = false;

        if (stats != null && stats.getBuffs() != null) {
            Buffs buffs = stats.getBuffs();

            if (buffs.getSnowball()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "snowman");
                hasVisualBuffs = true;
            }

            if (buffs.getSeafoam()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "seafoam_star");
                hasVisualBuffs = true;
            }

            if (buffs.getShinySeed()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "avatar_floral_" + stats.getHabitClass());
                hasVisualBuffs = true;
            }

            if (buffs.getSpookySparkles()) {
                layerMap.put(AvatarView.LayerType.VISUAL_BUFF, "ghost");
                hasVisualBuffs = true;
            }
        }

        if (!hasVisualBuffs) {
            if (!TextUtils.isEmpty(prefs.getChair())) {
                layerMap.put(AvatarView.LayerType.CHAIR, prefs.getChair());
            }

            if (outfit != null) {
                if (!TextUtils.isEmpty(outfit.getBack())) {
                    layerMap.put(AvatarView.LayerType.BACK, outfit.getBack());
                }
                if (outfit.isAvailable(outfit.getArmor())) {
                    layerMap.put(AvatarView.LayerType.ARMOR, prefs.getSize() + "_" + outfit.getArmor());
                }
                if (outfit.isAvailable(outfit.getBody())) {
                    layerMap.put(AvatarView.LayerType.BODY, outfit.getBody());
                }
                if (outfit.isAvailable(outfit.getEyeWear())) {
                    layerMap.put(AvatarView.LayerType.EYEWEAR, outfit.getEyeWear());
                }
                if (outfit.isAvailable(outfit.getHead())) {
                    layerMap.put(AvatarView.LayerType.HEAD, outfit.getHead());
                }
                if (outfit.isAvailable(outfit.getHeadAccessory())) {
                    layerMap.put(AvatarView.LayerType.HEAD_ACCESSORY, outfit.getHeadAccessory());
                }
                if (outfit.isAvailable(outfit.getShield())) {
                    layerMap.put(AvatarView.LayerType.SHIELD, outfit.getShield());
                }
                if (outfit.isAvailable(outfit.getWeapon())) {
                    layerMap.put(AvatarView.LayerType.WEAPON, outfit.getWeapon());
                }
            }

            layerMap.put(AvatarView.LayerType.SKIN, "skin_" + prefs.getSkin() + ((prefs.getSleep()) ? "_sleep" : ""));
            layerMap.put(AvatarView.LayerType.SHIRT, prefs.getSize() + "_shirt_" + prefs.getShirt());
            layerMap.put(AvatarView.LayerType.HEAD_0, "head_0");

            Hair hair = prefs.getHair();
            if (hair != null) {
                String hairColor = hair.getColor();

                if (hair.isAvailable(hair.getBase())) {
                    layerMap.put(AvatarView.LayerType.HAIR_BASE, "hair_base_" + hair.getBase() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getBangs())) {
                    layerMap.put(AvatarView.LayerType.HAIR_BANGS, "hair_bangs_" + hair.getBangs() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getMustache())) {
                    layerMap.put(AvatarView.LayerType.HAIR_MUSTACHE, "hair_mustache_" + hair.getMustache() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getBeard())) {
                    layerMap.put(AvatarView.LayerType.HAIR_BEARD, "hair_beard_" + hair.getBeard() + "_" + hairColor);
                }
                if (hair.isAvailable(hair.getFlower())) {
                    layerMap.put(AvatarView.LayerType.HAIR_FLOWER, "hair_flower_" + hair.getFlower());
                }
            }
        } else {
            Hair hair = prefs.getHair();

            // Show flower all the time!
            if (hair != null && hair.isAvailable(hair.getFlower())) {
                layerMap.put(AvatarView.LayerType.HAIR_FLOWER, "hair_flower_" + hair.getFlower());
            }
        }

        return layerMap;
    }

    public int getPetsFoundCount() {
        return getNullableMapSize(items.getPets());
    }

    public int getMountsTamedCount() {
        return getNullableMapSize(items.getMounts());
    }

    private int getNullableMapSize(RealmList map) {
        int mapSize = 0;

        if (map != null) {
            mapSize = map.size();
        }

        return mapSize;
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
}
