package com.habitrpg.android.habitica.api

import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.IAPGift
import com.habitrpg.shared.habitica.models.PurchaseValidationRequest
import com.habitrpg.shared.habitica.models.Transaction
import com.habitrpg.shared.habitica.models.auth.UserAuth
import com.habitrpg.shared.habitica.models.auth.UserAuthSocial
import com.habitrpg.shared.habitica.models.auth.UserAuthSocialTokens
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.realm.RealmList
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Date

/**
 * Serializes the request bodies the app sends and compares the JSON with a snapshot.
 * Key order and whitespace are ignored. Run with UPDATE_SNAPSHOTS=1 to rewrite the snapshots.
 */
class RequestBodySnapshotTest :
    WordSpec({
        val snapshotDir = File("src/test/resources/fixtures/requests")
        val serverTasks =
            ServerFixtures
                .fixture<TaskList>("tasks")
                .data!!
                .tasks.values
                .sortedBy { it.id }
        val newTask =
            Task().apply {
                id = "new-task"
                text = "New daily"
                notes = "Some notes"
                type = TaskType.DAILY
                frequency = Frequency.WEEKLY
                everyX = 1
                startDate = Date(1700000000000)
                tags = RealmList(Tag().apply { id = "tag-1" })
                checklist = RealmList(ChecklistItem("item-1", "Check", false))
                reminders =
                    RealmList(
                        RemindersItem().apply {
                            id = "reminder-1"
                            time = "2026-11-02T09:00:00"
                        },
                    )
            }
        val cases: Map<String, () -> String> =
            mapOf(
                "server_tasks" to { ServerFixtures.serialize<List<Task>>(serverTasks) },
                "new_task" to { ServerFixtures.serialize(newTask) },
                "tag" to {
                    ServerFixtures.serialize(
                        Tag().apply {
                            id = "tag-1"
                            name = "Work"
                        },
                    )
                },
                "user_tags" to { ServerFixtures.serialize<List<Tag>>(ServerFixtures.fixture<User>("user").data!!.tags!!) },
                "party" to { ServerFixtures.serialize(ServerFixtures.fixture<Group>("groups_party").data!!) },
                "challenge" to { ServerFixtures.serialize(ServerFixtures.fixture<List<Challenge>>("challenges").data!!.first()) },
                "purchase_validation" to {
                    ServerFixtures.serialize(
                        PurchaseValidationRequest().apply {
                            sku = "com.habitrpg.android.habitica.iap.21gems"
                            transaction =
                                Transaction().apply {
                                    receipt = "{\"orderId\":\"1\"}"
                                    signature = "sig"
                                }
                            gift = IAPGift("user-2")
                        },
                    )
                },
                "user_auth" to { ServerFixtures.serialize(UserAuth("name", "pw", "pw", "a@example.com")) },
                "user_auth_social" to {
                    ServerFixtures.serialize(
                        UserAuthSocial().apply {
                            network = "google"
                            authResponse =
                                UserAuthSocialTokens().apply {
                                    client_id = "client"
                                    access_token = "token"
                                }
                        },
                    )
                },
                "leave_challenge" to { ServerFixtures.serialize(LeaveChallengeBody("keep-all")) },
                "update_user" to {
                    ServerFixtures.serialize<Map<String, Any?>>(
                        mapOf(
                            "preferences.sleep" to true,
                            "preferences.size" to "slim",
                            "stats.hp" to 1,
                            "stats.gp" to 12.5,
                            "party.seeking" to Date(1700000000000),
                            "profile.blurb" to null,
                            "tags" to listOf("a", "b"),
                        ),
                    )
                },
                "invite" to {
                    ServerFixtures.serialize<Map<String, Any>>(
                        mapOf(
                            "emails" to listOf(mapOf("name" to "Friend", "email" to "f@example.com")),
                            "uuids" to listOf("user-2"),
                            "usernames" to emptyList<String>(),
                        ),
                    )
                },
                "allocate_stats" to { ServerFixtures.serialize<Map<String, Map<String, Int>>>(mapOf("stats" to mapOf("str" to 2))) },
                "bulk_score" to { ServerFixtures.serialize<List<Map<String, String>>>(listOf(mapOf("id" to "t1", "direction" to "up"))) },
            )

        "request bodies" should {
            cases.forEach { (name, serialize) ->
                "serialize $name" {
                    val json = serialize()
                    val snapshot = File(snapshotDir, "$name.json")
                    if (System.getenv("UPDATE_SNAPSHOTS") != null || !snapshot.exists()) {
                        snapshotDir.mkdirs()
                        snapshot.writeText(json)
                    }
                    Json.parseToJsonElement(json) shouldBe Json.parseToJsonElement(snapshot.readText())
                }
            }
        }
    })
