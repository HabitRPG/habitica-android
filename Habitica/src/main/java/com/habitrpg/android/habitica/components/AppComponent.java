package com.habitrpg.android.habitica.components;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;
import com.habitrpg.android.habitica.modules.ApiModule;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver;
import com.habitrpg.android.habitica.ui.activities.AboutActivity;
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity;
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity;
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity;
import com.habitrpg.android.habitica.ui.activities.IntroActivity;
import com.habitrpg.android.habitica.ui.activities.LoginActivity;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.MaintenanceActivity;
import com.habitrpg.android.habitica.ui.activities.PartyInviteActivity;
import com.habitrpg.android.habitica.ui.activities.PrefsActivity;
import com.habitrpg.android.habitica.ui.activities.SetupActivity;
import com.habitrpg.android.habitica.ui.activities.SkillTasksActivity;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;
import com.habitrpg.android.habitica.ui.fragments.NewsFragment;
import com.habitrpg.android.habitica.ui.fragments.faq.FAQDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.faq.FAQOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.customization.AvatarCustomizationFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.customization.AvatarOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.equipment.EquipmentDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.equipment.EquipmentOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemsFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.shops.ShopFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.shops.ShopsFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.MountDetailRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.PetDetailRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragment;
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableRecyclerFragment;
import com.habitrpg.android.habitica.ui.fragments.preferences.PreferencesFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.skills.SkillTasksRecyclerViewFragment;
import com.habitrpg.android.habitica.ui.fragments.skills.SkillsFragment;
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GuildsOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.social.InboxFragment;
import com.habitrpg.android.habitica.ui.fragments.social.InboxMessageListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.PublicGuildsFragment;
import com.habitrpg.android.habitica.ui.fragments.social.TavernFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyMemberListFragment;
import com.habitrpg.android.habitica.ui.fragments.tasks.TaskRecyclerViewFragment;
import com.habitrpg.android.habitica.ui.fragments.tasks.TasksFragment;
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetService;
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, ApiModule.class})
public interface AppComponent {
    void inject(AvatarStatsWidgetService target);

    void inject(ClassSelectionActivity classSelectionActivity);

    void inject(AboutActivity aboutActivity);

    void inject(GroupFormActivity groupFormActivity);

    void inject(IntroActivity introActivity);

    void inject(LoginActivity loginActivity);

    void inject(MainActivity mainActivity);

    void inject(MaintenanceActivity maintenanceActivity);

    void inject(PartyInviteActivity partyInviteActivity);

    void inject(PrefsActivity prefsActivity);

    void inject(SetupActivity setupActivity);

    void inject(SkillTasksActivity skillTasksActivity);

    void inject(TaskFormActivity taskFormActivity);

    void inject(TasksFragment tasksFragment);

    void inject(FAQDetailFragment faqDetailFragment);

    void inject(FAQOverviewFragment faqOverviewFragment);

    void inject(AvatarCustomizationFragment avatarCustomizationFragment);

    void inject(AvatarOverviewFragment avatarOverviewFragment);

    void inject(EquipmentDetailFragment equipmentDetailFragment);

    void inject(EquipmentOverviewFragment equipmentOverviewFragment);

    void inject(ItemRecyclerFragment itemRecyclerFragment);

    void inject(ItemsFragment itemsFragment);

    void inject(MountDetailRecyclerFragment mountDetailRecyclerFragment);

    void inject(PetDetailRecyclerFragment petDetailRecyclerFragment);

    void inject(StableFragment stableFragment);

    void inject(StableRecyclerFragment stableRecyclerFragment);

    void inject(AvatarSetupFragment avatarSetupFragment);

    void inject(IntroFragment introFragment);

    void inject(TaskSetupFragment taskSetupFragment);

    void inject(SkillsFragment skillsFragment);

    void inject(SkillTasksRecyclerViewFragment skillTasksRecyclerViewFragment);

    void inject(PartyFragment partyFragment);

    void inject(PartyInviteFragment partyInviteFragment);

    void inject(PartyMemberListFragment partyMemberListFragment);

    void inject(ChatListFragment chatListFragment);

    void inject(GroupInformationFragment groupInformationFragment);

    void inject(GuildFragment guildFragment);

    void inject(GuildsOverviewFragment guildsOverviewFragment);

    void inject(PublicGuildsFragment publicGuildsFragment);

    void inject(TavernFragment tavernFragment);

    void inject(TaskRecyclerViewFragment taskRecyclerViewFragment);

    void inject(GemsPurchaseFragment gemsPurchaseFragment);

    void inject(NewsFragment newsFragment);

    void inject(HabiticaApplication habiticaApplication);

    void inject(PreferencesFragment preferencesFragment);

    void inject(InboxFragment inboxFragment);

    void inject(InboxMessageListFragment inboxMessageListFragment);

    void inject(ShopsFragment shopsFragment);

    void inject(ShopFragment shopFragment);

    void inject(PushNotificationManager pushNotificationManager);

    void inject(LocalNotificationActionReceiver localNotificationActionReceiver);

    void inject(FullProfileActivity fullProfileActivity);

    void inject(DailiesWidgetProvider dailiesWidgetProvider);
}
