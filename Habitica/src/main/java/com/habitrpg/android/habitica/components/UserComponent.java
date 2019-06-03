package com.habitrpg.android.habitica.components;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.helpers.RemindersManager;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.UserScope;
import com.habitrpg.android.habitica.helpers.notifications.HabiticaFirebaseMessagingService;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;
import com.habitrpg.android.habitica.modules.UserRepositoryModule;
import com.habitrpg.android.habitica.modules.UserModule;
import com.habitrpg.android.habitica.receivers.LocalNotificationActionReceiver;
import com.habitrpg.android.habitica.receivers.NotificationPublisher;
import com.habitrpg.android.habitica.receivers.TaskAlarmBootReceiver;
import com.habitrpg.android.habitica.receivers.TaskReceiver;
import com.habitrpg.android.habitica.ui.activities.AboutActivity;
import com.habitrpg.android.habitica.ui.activities.ChallengeFormActivity;
import com.habitrpg.android.habitica.ui.activities.ClassSelectionActivity;
import com.habitrpg.android.habitica.ui.activities.FixCharacterValuesActivity;
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity;
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity;
import com.habitrpg.android.habitica.ui.activities.GiftIAPActivity;
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity;
import com.habitrpg.android.habitica.ui.activities.GroupInviteActivity;
import com.habitrpg.android.habitica.ui.activities.HabitButtonWidgetActivity;
import com.habitrpg.android.habitica.ui.activities.IntroActivity;
import com.habitrpg.android.habitica.ui.activities.LoginActivity;
import com.habitrpg.android.habitica.ui.activities.MainActivity;
import com.habitrpg.android.habitica.ui.activities.MaintenanceActivity;
import com.habitrpg.android.habitica.ui.activities.NotificationsActivity;
import com.habitrpg.android.habitica.ui.activities.PrefsActivity;
import com.habitrpg.android.habitica.ui.activities.ReportMessageActivity;
import com.habitrpg.android.habitica.ui.activities.SetupActivity;
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity;
import com.habitrpg.android.habitica.ui.activities.SkillTasksActivity;
import com.habitrpg.android.habitica.ui.activities.TaskFormActivity;
import com.habitrpg.android.habitica.ui.activities.VerifyUsernameActivity;
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.DailiesRecyclerViewHolder;
import com.habitrpg.android.habitica.ui.adapter.tasks.HabitsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.RewardsRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.TodosRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.fragments.AboutFragment;
import com.habitrpg.android.habitica.ui.fragments.AchievementsFragment;
import com.habitrpg.android.habitica.ui.fragments.GemsPurchaseFragment;
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment;
import com.habitrpg.android.habitica.ui.fragments.NewsFragment;
import com.habitrpg.android.habitica.ui.fragments.StatsFragment;
import com.habitrpg.android.habitica.ui.fragments.SubscriptionFragment;
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
import com.habitrpg.android.habitica.ui.fragments.preferences.APIPreferenceFragment;
import com.habitrpg.android.habitica.ui.fragments.preferences.AuthenticationPreferenceFragment;
import com.habitrpg.android.habitica.ui.fragments.preferences.PreferencesFragment;
import com.habitrpg.android.habitica.ui.fragments.preferences.ProfilePreferencesFragment;
import com.habitrpg.android.habitica.ui.fragments.preferences.PushNotificationsPreferencesFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment;
import com.habitrpg.android.habitica.ui.fragments.setup.WelcomeFragment;
import com.habitrpg.android.habitica.ui.fragments.skills.SkillTasksRecyclerViewFragment;
import com.habitrpg.android.habitica.ui.fragments.skills.SkillsFragment;
import com.habitrpg.android.habitica.ui.fragments.social.ChatFragment;
import com.habitrpg.android.habitica.ui.fragments.social.ChatListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GroupInformationFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GuildDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GuildFragment;
import com.habitrpg.android.habitica.ui.fragments.social.GuildsOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.social.InboxFragment;
import com.habitrpg.android.habitica.ui.fragments.social.InboxMessageListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.PublicGuildsFragment;
import com.habitrpg.android.habitica.ui.fragments.social.QuestDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.social.TavernDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.social.TavernFragment;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeListFragment;
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyDetailFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyInviteFragment;
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyMemberListFragment;
import com.habitrpg.android.habitica.ui.fragments.tasks.TaskRecyclerViewFragment;
import com.habitrpg.android.habitica.ui.fragments.tasks.TasksFragment;
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel;
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel;
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog;
import com.habitrpg.android.habitica.ui.views.social.ChatBarView;
import com.habitrpg.android.habitica.ui.views.stats.BulkAllocateStatsDialog;
import com.habitrpg.android.habitica.ui.views.tasks.TaskFilterDialog;
import com.habitrpg.android.habitica.widget.AvatarStatsWidgetProvider;
import com.habitrpg.android.habitica.widget.BaseWidgetProvider;
import com.habitrpg.android.habitica.widget.DailiesWidgetProvider;
import com.habitrpg.android.habitica.widget.HabitButtonWidgetProvider;
import com.habitrpg.android.habitica.widget.HabitButtonWidgetService;
import com.habitrpg.android.habitica.widget.TaskListFactory;
import com.habitrpg.android.habitica.widget.TaskListWidgetProvider;

import org.jetbrains.annotations.NotNull;

import dagger.Subcomponent;

@UserScope
@Subcomponent(modules = {UserModule.class, UserRepositoryModule.class})
public interface UserComponent {
    void inject(ClassSelectionActivity classSelectionActivity);

    void inject(AboutActivity aboutActivity);

    void inject(GroupFormActivity groupFormActivity);

    void inject(IntroActivity introActivity);

    void inject(LoginActivity loginActivity);

    void inject(MainActivity mainActivity);

    void inject(MaintenanceActivity maintenanceActivity);

    void inject(GroupInviteActivity groupInviteActivity);

    void inject(PrefsActivity prefsActivity);

    void inject(NotificationsActivity notificationsActivity);

    void inject(SetupActivity setupActivity);

    void inject(SkillTasksActivity skillTasksActivity);

    void inject(SkillMemberActivity skillMembersActivity);

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

    void inject(HabiticaBaseApplication habiticaApplication);

    void inject(PreferencesFragment preferencesFragment);

    void inject(InboxFragment inboxFragment);

    void inject(InboxMessageListFragment inboxMessageListFragment);

    void inject(ShopsFragment shopsFragment);

    void inject(ShopFragment shopFragment);

    void inject(PushNotificationManager pushNotificationManager);

    void inject(LocalNotificationActionReceiver localNotificationActionReceiver);

    void inject(FullProfileActivity fullProfileActivity);

    void inject(DailiesWidgetProvider dailiesWidgetProvider);

    void inject(HabitButtonWidgetService habitButtonWidgetService);

    void inject(HabitButtonWidgetActivity habitButtonWidgetActivity);

    void inject(HabitButtonWidgetProvider habitButtonWidgetProvider);

    void inject(AvatarStatsWidgetProvider avatarStatsWidgetProvider);

    void inject(SoundManager soundManager);

    void inject(ChallengesOverviewFragment challengesOverviewFragment);

    void inject(ChallengeListFragment challengeListFragment);

    void inject(ApiClient apiClient);

    void inject(TaskListWidgetProvider taskListWidgetProvider);

    void inject(RemindersManager remindersManager);

    void inject(DailiesRecyclerViewHolder dailiesRecyclerViewHolder);

    void inject(HabitsRecyclerViewAdapter habitsRecyclerViewAdapter);

    void inject(RewardsRecyclerViewAdapter rewardsRecyclerViewAdapter);

    void inject(TodosRecyclerViewAdapter todosRecyclerViewAdapter);

    void inject(SubscriptionFragment subscriptionFragment);

    void inject(ChallengeTasksRecyclerViewAdapter challengeTasksRecyclerViewAdapter);

    void inject(TaskListFactory taskListFactory);

    void inject(GemPurchaseActivity gemPurchaseActivity);

    void inject(TaskFilterDialog taskFilterDialog);

    void inject(TaskReceiver taskReceiver);

    void inject(TaskAlarmBootReceiver taskAlarmBootReceiver);

    void inject(HabiticaFirebaseMessagingService habiticaFirebaseMessagingService);

    void inject(BaseWidgetProvider baseWidgetProvider);

    void inject(NotificationPublisher notificationPublisher);

    void inject(ChallengeFormActivity challengeFormActivity);

    void inject(TavernDetailFragment tavernDetailFragment);

    void inject(PartyDetailFragment partyDetailFragment);

    void inject(QuestDetailFragment questDetailFragment);

    void inject(PurchaseDialog purchaseDialog);

    void inject(@NotNull FixCharacterValuesActivity fixCharacterValuesActivity);

    void inject(@NotNull AuthenticationPreferenceFragment authenticationPreferenceFragment);

    void inject(@NotNull ProfilePreferencesFragment profilePreferencesFragment);

    void inject(@NotNull APIPreferenceFragment apiPreferenceFragment);

    void inject(@NotNull StatsFragment statsFragment);

    void inject(@NotNull BulkAllocateStatsDialog bulkAllocateStatsDialog);

    void inject(@NotNull PushNotificationsPreferencesFragment pushNotificationsPreferencesFragment);

    void inject(WelcomeFragment welcomeFragment);

    void inject(@NotNull NavigationDrawerFragment navigationDrawerFragment);

    void inject(@NotNull ChallengeDetailFragment challengeDetailFragment);

    void inject(@NotNull VerifyUsernameActivity verifyUsernameActivity);

    void inject(@NotNull GroupViewModel viewModel);

    void inject(@NotNull NotificationsViewModel viewModel);

    void inject(@NotNull ChatFragment chatFragment);

    void inject(@NotNull GiftIAPActivity giftIAPActivity);

    void inject(@NotNull AboutFragment aboutFragment);

    void inject(@NotNull ChatBarView chatBarView);

    void inject(@NotNull TaskFormActivity taskFormActivity);

    void inject(@NotNull ReportMessageActivity reportMessageActivity);

    void inject(@NotNull GuildDetailFragment guildDetailFragment);

    void inject(@NotNull AchievementsFragment achievementsFragment);
}
