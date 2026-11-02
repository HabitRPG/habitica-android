package com.habitrpg.android.habitica.api

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Parses every captured server response and compares the whole model graph with a snapshot.
 * Run with UPDATE_SNAPSHOTS=1 to rewrite the snapshots after an intended change.
 */
class ServerResponseSnapshotTest :
    WordSpec({
        val snapshotDir = File("src/test/resources/fixtures/snapshots")
        val cases: Map<String, () -> Any?> =
            mapOf(
                "user" to { ServerFixtures.fixture<User>("user") },
                "tasks" to { ServerFixtures.fixture<TaskList>("tasks") },
                "world_state" to { ServerFixtures.fixture<WorldState>("world_state") },
                "content" to { ServerFixtures.fixture<ContentResult>("content") },
                "groups_party" to { ServerFixtures.fixture<Group>("groups_party") },
                "groups_guilds" to { ServerFixtures.fixture<List<Group>>("groups_guilds") },
                "party_chat" to { ServerFixtures.fixture<List<ChatMessage>>("party_chat") },
                "party_members" to { ServerFixtures.fixture<List<Member>>("party_members") },
                "inbox_messages" to { ServerFixtures.fixture<List<ChatMessage>>("inbox_messages") },
                "challenges" to { ServerFixtures.fixture<List<Challenge>>("challenges") },
                "in_app_rewards" to { ServerFixtures.fixture<List<ShopItem>>("in_app_rewards") },
                "shop_market" to { ServerFixtures.fixture<Shop>("shop_market") },
                "shop_quests" to { ServerFixtures.fixture<Shop>("shop_quests") },
                "market_gear" to { ServerFixtures.fixture<Shop>("market_gear") },
                "member" to { ServerFixtures.fixture<Member>("member") },
                "member_achievements" to { ServerFixtures.fixture<List<Achievement>>("member_achievements") },
                "group_plans" to { ServerFixtures.fixture<List<TeamPlan>>("group_plans") },
                "looking_for_party" to { ServerFixtures.fixture<List<Member>>("looking_for_party") },
            )

        "server responses" should {
            cases.forEach { (name, parse) ->
                "parse $name" {
                    val dump = ModelDump.dump(parse())
                    val snapshot = File(snapshotDir, "$name.txt")
                    if (System.getenv("UPDATE_SNAPSHOTS") != null || !snapshot.exists()) {
                        snapshotDir.mkdirs()
                        snapshot.writeText(dump)
                    }
                    val expected = snapshot.readText().lines()
                    val actual = dump.lines()
                    val diff = (expected - actual.toSet()).take(20).map { "- $it" } + (actual - expected.toSet()).take(20).map { "+ $it" }
                    diff.joinToString("\n") shouldBe ""
                }
            }
        }
    })
