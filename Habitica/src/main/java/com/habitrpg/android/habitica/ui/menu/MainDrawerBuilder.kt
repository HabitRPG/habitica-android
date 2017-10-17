package com.habitrpg.android.habitica.ui.menu

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.widget.Toolbar

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.activities.AboutActivity
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.PrefsActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.NewsFragment
import com.habitrpg.android.habitica.ui.fragments.faq.FAQOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.customization.AvatarOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.equipment.EquipmentOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemsFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.shops.ShopsFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragment
import com.habitrpg.android.habitica.ui.fragments.skills.SkillsFragment
import com.habitrpg.android.habitica.ui.fragments.social.GuildsOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.social.InboxFragment
import com.habitrpg.android.habitica.ui.fragments.social.TavernFragment
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragment
import com.habitrpg.android.habitica.ui.fragments.social.party.PartyFragment
import com.habitrpg.android.habitica.ui.fragments.tasks.TasksFragment
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem

import java.util.Locale

import com.habitrpg.android.habitica.ui.activities.MainActivity.GEM_PURCHASE_REQUEST
import com.habitrpg.android.habitica.ui.fragments.StatsFragment

object MainDrawerBuilder {

    // Change the identificationIDs to the position IDs so that its easier to set the selected entry
    val SIDEBAR_TASKS = 0
    val SIDEBAR_SKILLS = 1
    val SIDEBAR_STATS = 2
    val SIDEBAR_INBOX = 3
    val SIDEBAR_TAVERN = 4
    val SIDEBAR_PARTY = 5
    val SIDEBAR_GUILDS = 6
    val SIDEBAR_CHALLENGES = 7
    val SIDEBAR_SHOPS = 8
    val SIDEBAR_AVATAR = 9
    val SIDEBAR_EQUIPMENT = 10
    val SIDEBAR_ITEMS = 11
    val SIDEBAR_STABLE = 12
    val SIDEBAR_PURCHASE = 13
    val SIDEBAR_NEWS = 14
    val SIDEBAR_SETTINGS = 15
    val SIDEBAR_HELP = 16
    val SIDEBAR_ABOUT = 17

    fun CreateDefaultAccountHeader(activity: Activity): AccountHeaderBuilder {
        return AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.sidebar_background)
                .addProfiles(
                        ProfileDrawerItem()
                )
                .withOnAccountHeaderListener { view, profile, currentProfile -> false }
                .withSelectionListEnabledForSingleProfile(false)
    }


    fun CreateDefaultBuilderSettings(activity: MainActivity, sharedPreferences: SharedPreferences, toolbar: Toolbar?, accountHeader: AccountHeader): DrawerBuilder {
        val builder = DrawerBuilder()
                .withActivity(activity)
                .withFullscreen(true)

        if (toolbar != null) {
            builder.withToolbar(toolbar)
        }

        builder.withHeaderDivider(false)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tasks)).withIdentifier(SIDEBAR_TASKS.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_skills)).withIdentifier(SIDEBAR_SKILLS.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_stats)).withIdentifier(SIDEBAR_STATS.toLong()),

                        SectionIconDrawerItem().withName(activity.getString(R.string.sidebar_section_social).toUpperCase(Locale.getDefault())),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_inbox)).withIdentifier(SIDEBAR_INBOX.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_tavern)).withIdentifier(SIDEBAR_TAVERN.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_party)).withIdentifier(SIDEBAR_PARTY.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_guilds)).withIdentifier(SIDEBAR_GUILDS.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_challenges)).withIdentifier(SIDEBAR_CHALLENGES.toLong()),

                        SectionIconDrawerItem().withName(activity.getString(R.string.sidebar_section_inventory).toUpperCase(Locale.getDefault())),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_shops)).withIdentifier(SIDEBAR_SHOPS.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_avatar)).withIdentifier(SIDEBAR_AVATAR.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_equipment)).withIdentifier(SIDEBAR_EQUIPMENT.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_items)).withIdentifier(SIDEBAR_ITEMS.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_stable)).withIdentifier(SIDEBAR_STABLE.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_purchaseGems)).withIdentifier(SIDEBAR_PURCHASE.toLong()),

                        SectionIconDrawerItem().withName(activity.getString(R.string.sidebar_about).toUpperCase(Locale.getDefault())),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_news)).withIdentifier(SIDEBAR_NEWS.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_settings)).withIdentifier(SIDEBAR_SETTINGS.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_help)).withIdentifier(SIDEBAR_HELP.toLong()),
                        PrimaryDrawerItem().withName(activity.getString(R.string.sidebar_about)).withIdentifier(SIDEBAR_ABOUT.toLong()).withSelectable(false)
                )
                .withStickyFooterDivider(false)
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    var fragment: BaseMainFragment? = null
                    var newActivityClass: Class<*>? = null

                    val identifier = drawerItem.identifier.toInt()
                    when (identifier) {
                        SIDEBAR_TASKS -> {
                            fragment = TasksFragment()
                        }
                        SIDEBAR_SKILLS -> {
                            fragment = SkillsFragment()
                        }
                        SIDEBAR_STATS -> {
                            fragment = StatsFragment()
                        }
                        SIDEBAR_INBOX -> {
                            fragment = InboxFragment()
                        }
                        SIDEBAR_PARTY -> {
                            fragment = PartyFragment()
                        }
                        SIDEBAR_GUILDS -> {
                            fragment = GuildsOverviewFragment()
                        }
                        SIDEBAR_TAVERN -> {
                            fragment = TavernFragment()
                        }
                        SIDEBAR_CHALLENGES -> {
                            fragment = ChallengesOverviewFragment()
                        }
                        SIDEBAR_SHOPS -> {
                            fragment = ShopsFragment()
                        }
                        SIDEBAR_AVATAR -> {
                            fragment = AvatarOverviewFragment()
                        }
                        SIDEBAR_EQUIPMENT -> {
                            fragment = EquipmentOverviewFragment()
                        }
                        SIDEBAR_ITEMS -> {
                            fragment = ItemsFragment()
                        }
                        SIDEBAR_STABLE -> {
                            fragment = StableFragment()
                        }
                        SIDEBAR_PURCHASE -> {
                            newActivityClass = GemPurchaseActivity::class.java
                        }
                        SIDEBAR_NEWS -> {
                            fragment = NewsFragment()
                        }
                        SIDEBAR_SETTINGS -> {
                            newActivityClass = PrefsActivity::class.java
                        }
                        SIDEBAR_HELP -> {
                            fragment = FAQOverviewFragment()
                        }
                        SIDEBAR_ABOUT -> {
                            newActivityClass = AboutActivity::class.java
                        }
                    }

                    sharedPreferences.edit().putInt("lastActivePosition", position).apply()

                    if (fragment != null) {
                        fragment.fragmentSidebarPosition = position
                        activity.displayFragment(fragment)
                        return@withOnDrawerItemClickListener false
                    }
                    if (newActivityClass != null) {
                        val passUserId = Intent(activity, newActivityClass)
                        passUserId.putExtra("userId", activity.userID)
                        if (identifier == SIDEBAR_PURCHASE) {
                            activity.startActivityForResult(passUserId, GEM_PURCHASE_REQUEST)
                        } else {
                            activity.startActivity(passUserId)
                        }
                        return@withOnDrawerItemClickListener false
                    }
                    return@withOnDrawerItemClickListener true
                }

        return builder
    }
}
