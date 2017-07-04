package com.habitrpg.android.habitica.models.members;

import android.text.TextUtils;

import com.habitrpg.android.habitica.models.Avatar;
import com.habitrpg.android.habitica.models.social.UserParty;
import com.habitrpg.android.habitica.models.user.Buffs;
import com.habitrpg.android.habitica.models.user.ContributorInfo;
import com.habitrpg.android.habitica.models.user.Flags;
import com.habitrpg.android.habitica.models.user.Hair;
import com.habitrpg.android.habitica.models.user.Inbox;
import com.habitrpg.android.habitica.models.user.Outfit;
import com.habitrpg.android.habitica.models.user.Preferences;
import com.habitrpg.android.habitica.models.user.Profile;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.ui.AvatarView;

import java.util.EnumMap;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Member extends RealmObject implements Avatar {


    @PrimaryKey
    private String id;
    private Stats stats;
    private Inbox inbox;
    private Preferences preferences;
    private Profile profile;
    private UserParty party;
    private Flags flags;
    private ContributorInfo contributor;

    private Outfit costume;
    private Outfit equipped;

    private String currentMount;
    private String currentPet;

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
        if (flags != null && !flags.isManaged()) {
            flags.setUserId(id);
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

    public Flags getFlags() {
        return flags;
    }

    @Override
    public Integer getGemCount() {
        return 0;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
        if (flags != null && id != null) {
            flags.setUserId(id);
        }
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

    public void setEquipped(Outfit equipped) {
        this.equipped = equipped;
        if (equipped != null && id != null) {
            equipped.setUserId(id+"equipped");
        }
    }

    public EnumMap<AvatarView.LayerType, String> getAvatarLayerMap() {
        EnumMap<AvatarView.LayerType, String> layerMap = new EnumMap<>(AvatarView.LayerType.class);

        Preferences prefs = getPreferences();
        if (prefs == null) {
            return layerMap;
        }
        Outfit outfit;
        if (prefs.getCostume()) {
            outfit = getCostume();
        } else {
            outfit = getEquipped();
        }

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
}
