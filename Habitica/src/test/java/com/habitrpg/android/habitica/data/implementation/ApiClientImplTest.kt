package com.habitrpg.android.habitica.data.implementation

import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.models.PurchaseValidationRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import retrofit2.HttpException
import java.lang.ref.WeakReference
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class ApiClientImplTest :
    WordSpec({
        lateinit var server: MockWebServer
        lateinit var apiClient: ApiClientImpl
        val hostConfig = mockk<HostConfig>(relaxed = true)
        val notificationsManager = mockk<NotificationsManager>(relaxed = true)
        val activity = mockk<BaseActivity>(relaxed = true)
        val context = mockk<HabiticaBaseApplication>(relaxed = true)
        val cacheDir = Files.createTempDirectory("api-client-test").toFile()

        // The client waits up to 40 minutes for a response, so fail fast if a test forgets to enqueue one
        timeout = 10_000L

        fun enqueue(
            data: String = "null",
            code: Int = 200,
            notifications: String? = null,
        ) {
            val notificationsField = notifications?.let { ""","notifications":$it""" } ?: ""
            server.enqueue(MockResponse.Builder().code(code).body("""{"success":true,"data":$data$notificationsField}""").build())
        }

        fun enqueueError(
            code: Int,
            body: String,
        ) {
            server.enqueue(MockResponse.Builder().code(code).body(body).build())
        }

        fun takeRequest(): RecordedRequest = server.takeRequest(1, TimeUnit.SECONDS)!!

        fun RecordedRequest.route() = "$method ${url.encodedPath.removePrefix("/api/v4")}"

        fun RecordedRequest.bodyText() = body?.utf8() ?: ""

        beforeSpec {
            mockkObject(Analytics)
            every { Analytics.logError(any()) } just runs
            every { Analytics.logException(any()) } just runs
            mockkStatic(FirebasePerformance::class)
            every { FirebasePerformance.getInstance() } returns mockk(relaxed = true)
        }
        afterSpec {
            unmockkObject(Analytics)
            unmockkStatic(FirebasePerformance::class)
            cacheDir.deleteRecursively()
        }

        beforeEach {
            server = MockWebServer()
            server.start()
            every { hostConfig.address } returns server.url("/").toString()
            every { hostConfig.hasAuthentication() } returns true
            every { hostConfig.apiKey } returns "api-key"
            every { hostConfig.userID } returns "user-1"
            every { context.cacheDir } returns cacheDir
            every { context.getString(any()) } answers { "string-${firstArg<Int>()}" }
            every { context.currentActivity } returns WeakReference(activity)
            apiClient = ApiClientImpl(ApiClientImpl.createGsonFactory(), hostConfig, notificationsManager, context)
        }
        afterEach {
            server.close()
            clearMocks(hostConfig, activity, notificationsManager, Analytics, answers = false)
        }

        "requests" should {
            "send the authentication and client headers" {
                enqueue("""{"status":"up"}""")
                apiClient.getStatus()?.status shouldBe "up"
                val request = takeRequest()
                request.route() shouldBe "GET /status"
                request.headers["x-api-key"] shouldBe "api-key"
                request.headers["x-api-user"] shouldBe "user-1"
                request.headers["x-client"] shouldBe "habitica-android"
                request.headers["x-user-timezoneOffset"]?.toIntOrNull() shouldNotBe null
            }

            "not send authentication headers without credentials" {
                every { hostConfig.hasAuthentication() } returns false
                enqueue()
                apiClient.getStatus()
                takeRequest().headers["x-api-key"] shouldBe null
            }

            "pass notifications of the response to the notifications manager" {
                enqueue(notifications = """[{"id":"n1","type":"NEW_STUFF"}]""")
                apiClient.getStatus()
                verify { notificationsManager.setNotifications(match { it.single().id == "n1" }) }
            }

            "go to the new server after the url changed" {
                val newServer = MockWebServer()
                newServer.start()
                every { hostConfig.address } returns newServer.url("/").toString()
                apiClient.updateServerUrl(newServer.url("/").toString())
                newServer.enqueue(MockResponse.Builder().body("""{"data":{"status":"up"}}""").build())
                apiClient.getStatus()?.status shouldBe "up"
                newServer.requestCount shouldBe 1
                server.requestCount shouldBe 0
                newServer.close()
            }

            "ignore blank server urls" {
                apiClient.updateServerUrl("")
                verify(exactly = 0) { hostConfig.address = any() }
            }
        }

        "endpoints" should {
            val endpoints: List<Pair<String, suspend ApiClient.() -> Any?>> =
                listOf(
                    "POST /user/stat-sync" to { syncUserStats() },
                    "GET /content" to { getContent("de") },
                    "PUT /user/" to { updateUser(mapOf("preferences.sleep" to true)) },
                    "GET /user/in-app-rewards" to { retrieveInAppRewards() },
                    "POST /user/equip/equipped/weapon_1" to { equipItem("equipped", "weapon_1") },
                    "POST /user/buy/armoire" to { buyItem("armoire", 1) },
                    "POST /user/purchase/gems/gem" to { purchaseItem("gems", "gem", 1) },
                    "POST /user/purchase-hourglass/pets/Wolf-Veteran" to { purchaseHourglassItem("pets", "Wolf-Veteran") },
                    "POST /user/buy-mystery-set/201501" to { purchaseMysterySet("201501") },
                    "POST /user/buy-quest/dilatory" to { purchaseQuest("dilatory") },
                    "POST /user/buy-special-spell/snowball" to { purchaseSpecialSpell("snowball") },
                    "POST /user/sell/eggs/Wolf" to { sellItem("eggs", "Wolf") },
                    "POST /user/hatch/Wolf/Base" to { hatchPet("Wolf", "Base") },
                    "GET /tasks/user" to { getTasks("todos") },
                    "GET /tasks/task-1" to { getTask("task-1") },
                    "POST /tasks/task-1/score/up" to { postTaskDirection("task-1", "up") },
                    "POST /tasks/task-1/move/to/2" to { postTaskNewPosition("task-1", 2) },
                    "POST /tasks/task-1/checklist/item-1/score" to { scoreChecklistItem("task-1", "item-1") },
                    "DELETE /tasks/task-1" to { deleteTask("task-1") },
                    "DELETE /tags/tag-1" to { deleteTag("tag-1") },
                    "POST /user/sleep" to { sleep() },
                    "POST /user/revive" to { revive() },
                    "POST /user/class/cast/fireball" to { useSkill("fireball", "task", "task-1") },
                    "POST /user/change-class" to { changeClass("wizard") },
                    "POST /user/disable-classes" to { disableClasses() },
                    "GET /groups" to { listGroups("party") },
                    "GET /groups/group-1" to { getGroup("group-1") },
                    "GET /groups/group-1/chat" to { listGroupChat("group-1") },
                    "POST /groups/group-1/join" to { joinGroup("group-1") },
                    "POST /groups/group-1/leave" to { leaveGroup("group-1", "remain-in-challenges") },
                    "DELETE /groups/group-1/chat/message-1" to { deleteMessage("group-1", "message-1") },
                    "DELETE /inbox/messages/message-1" to { deleteInboxMessage("message-1") },
                    "POST /groups/group-1/chat/message-1/like" to { likeMessage("group-1", "message-1") },
                    "POST /groups/group-1/chat/seen" to { seenMessages("group-1") },
                    "POST /groups/group-1/reject-invite" to { rejectGroupInvite("group-1") },
                    "POST /groups/group-1/quests/accept" to { acceptQuest("group-1") },
                    "POST /groups/group-1/quests/reject" to { rejectQuest("group-1") },
                    "POST /groups/group-1/quests/cancel" to { cancelQuest("group-1") },
                    "POST /groups/group-1/quests/abort" to { abortQuest("group-1") },
                    "POST /groups/group-1/quests/leave" to { leaveQuest("group-1") },
                    "POST /groups/group-1/quests/invite/dilatory" to { inviteToQuest("group-1", "dilatory") },
                    "GET /members/member-1" to { getMember("member-1") },
                    "GET /members/username/tester" to { getMemberWithUsername("tester") },
                    "GET /members/member-1/achievements" to { getMemberAchievements("member-1") },
                    "GET /shops/market" to { retrieveShopIventory("market") },
                    "DELETE /user/push-devices/device-1" to { deletePushDevice("device-1") },
                    "GET /challenges/user" to { getUserChallenges(0, true) },
                    "GET /challenges/challenge-1" to { getChallenge("challenge-1") },
                    "POST /challenges/challenge-1/join" to { joinChallenge("challenge-1") },
                    "DELETE /challenges/challenge-1" to { deleteChallenge("challenge-1") },
                    "GET /news" to { getNews() },
                    "POST /notifications/notification-1/read" to { readNotification("notification-1") },
                    "POST /notifications/see" to { seeNotifications(mapOf("notificationIds" to listOf("n1"))) },
                    "POST /user/open-mystery-item" to { openMysteryItem() },
                    "POST /cron" to { runCron() },
                    "POST /user/reroll" to { reroll() },
                    "POST /user/rebirth" to { rebirth() },
                    "POST /user/allocate" to { allocatePoint("str") },
                    "GET /group-plans" to { getTeamPlans() },
                    "GET /world-state" to { getWorldState() },
                    "POST /user/block/member-1" to { blockMember("member-1") },
                    "GET /user/toggle-pinned-item/marketGear/gear.flat.weapon_1" to { togglePinnedItem("marketGear", "gear.flat.weapon_1") },
                    "GET /hall/heroes/member-1" to { getHallMember("member-1") },
                    "GET /looking-for-party" to { retrievePartySeekingUsers(0) },
                    "POST /tasks/task-1/assign" to { assignToTask("task-1", listOf("member-1")) },
                    "POST /tasks/task-1/unassign/member-1" to { unassignFromTask("task-1", "member-1") },
                    "POST /tasks/task-1/needs-work/member-1" to { markTaskNeedsWork("task-1", "member-1") },
                )
            endpoints.forEach { (route, call) ->
                "call $route" {
                    enqueue()
                    apiClient.call()
                    takeRequest().route() shouldBe route
                }
            }
        }

        "retrieveUser" should {
            "load the user and attach the tasks" {
                enqueue("""{"_id":"user-1"}""")
                enqueue("""[{"_id":"task-1","type":"todo"}]""")
                val user = apiClient.retrieveUser(true)
                user?.id shouldBe "user-1"
                user?.tasks?.tasks?.keys shouldBe setOf("task-1")
                takeRequest().route() shouldBe "GET /user/"
                takeRequest().route() shouldBe "GET /tasks/user"
            }
        }

        "authentication" should {
            "send the credentials to register" {
                enqueue("""{"id":"user-1","apiToken":"token","newUser":true}""")
                val response = apiClient.registerUser("tester", "a@b.c", "secret", "secret")
                response?.id shouldBe "user-1"
                response?.newUser shouldBe true
                val request = takeRequest()
                request.route() shouldBe "POST /user/auth/local/register"
                request.bodyText() shouldContain "\"username\":\"tester\""
                request.bodyText() shouldContain "\"email\":\"a@b.c\""
                request.bodyText() shouldContain "\"confirmPassword\":\"secret\""
            }

            "send the credentials to log in" {
                enqueue("""{"id":"user-1","apiToken":"token"}""")
                apiClient.connectUser("tester", "secret")?.apiToken shouldBe "token"
                val request = takeRequest()
                request.route() shouldBe "POST /user/auth/local/login"
                request.bodyText() shouldContain "\"password\":\"secret\""
            }

            "return the social login response" {
                enqueue("""{"id":"user-1","apiToken":"token"}""")
                val response = apiClient.connectSocial("google", "client", "access", true)
                response?.id shouldBe "user-1"
                response?.userExists shouldBe true
                takeRequest().bodyText() shouldContain "\"access_token\":\"access\""
            }

            "report a missing user for social logins that are not found" {
                enqueueError(404, "{}")
                apiClient.connectSocial("google", "client", "access", false)?.userExists shouldBe false
            }

            "report a missing user for social logins without id" {
                enqueue("""{"id":"","apiToken":""}""")
                apiClient.connectSocial("google", "client", "access", false)?.userExists shouldBe false
            }

            "report a missing user if the social login failed" {
                enqueueError(200, "not json")
                apiClient.connectSocial("google", "client", "access", false)?.userExists shouldBe false
            }

            "report whether disconnecting a social account worked" {
                enqueue()
                apiClient.disconnectSocial("google") shouldBe true
                takeRequest().route() shouldBe "DELETE /user/auth/social/google"
                enqueueError(400, "{}")
                apiClient.disconnectSocial("google") shouldBe false
            }
        }

        "request bodies" should {
            "include the password only if one was given when updating the email" {
                enqueue()
                apiClient.updateEmail("new@b.c", "")
                takeRequest().bodyText() shouldBe """{"newEmail":"new@b.c"}"""
                enqueue()
                apiClient.updateEmail("new@b.c", "secret")
                takeRequest().bodyText() shouldContain "\"password\":\"secret\""
            }

            "send all passwords when updating the password" {
                enqueue()
                apiClient.updatePassword("old", "new", "new")
                val request = takeRequest()
                request.route() shouldBe "PUT /user/auth/update-password"
                request.bodyText() shouldContain "\"newPassword\":\"new\""
                request.bodyText() shouldContain "\"confirmPassword\":\"new\""
            }

            "send all stats when allocating in bulk" {
                enqueue()
                apiClient.bulkAllocatePoints(1, 2, 3, 4)
                val body = takeRequest().bodyText()
                body shouldContain "\"str\":1"
                body shouldContain "\"int\":2"
                body shouldContain "\"con\":3"
                body shouldContain "\"per\":4"
            }

            "send the recipient and amount when gifting gems" {
                enqueue()
                apiClient.transferGems("member-1", 20)
                val request = takeRequest()
                request.route() shouldBe "POST /members/transfer-gems"
                request.bodyText() shouldContain "\"toUserId\":\"member-1\""
                request.bodyText() shouldContain "\"gemAmount\":20"
            }
        }

        "resetAccount" should {
            "return whether the reset worked" {
                enqueue()
                apiClient.resetAccount("secret") shouldBe true
                enqueueError(401, """{"message":"Wrong password"}""")
                apiClient.resetAccount("wrong") shouldBe false
            }
        }

        "purchase validation" should {
            "validate every purchase" {
                enqueue()
                enqueue()
                apiClient.validatePurchase(PurchaseValidationRequest())
                apiClient.validatePurchase(PurchaseValidationRequest())
                server.requestCount shouldBe 2
            }

            "validate every subscription" {
                enqueue()
                enqueue()
                apiClient.validateSubscription(PurchaseValidationRequest())
                apiClient.validateSubscription(PurchaseValidationRequest())
                server.requestCount shouldBe 2
            }

            "pass server errors to the caller instead of showing them" {
                enqueueError(401, """{"message":"Invalid receipt"}""")
                shouldThrow<HttpException> { apiClient.validatePurchase(PurchaseValidationRequest()) }
                verify(exactly = 0) { activity.showConnectionProblem(any(), any(), any(), any()) }
            }
        }

        "feedPet" should {
            "copy the response message into the feed response" {
                server.enqueue(MockResponse.Builder().body("""{"data":5,"message":"Yum"}""").build())
                val response = apiClient.feedPet("Wolf-Base", "Meat")
                response?.value shouldBe 5
                response?.message shouldBe "Yum"
            }
        }

        "error handling" should {
            "show the error message of client errors" {
                enqueueError(400, """{"message":"Not enough gems"}""")
                apiClient.getStatus() shouldBe null
                verify { activity.showConnectionProblem(1, "", "Not enough gems", false) }
            }

            "prefer the message of the first error" {
                enqueueError(400, """{"message":"Invalid","errors":[{"message":"Name is too long"}]}""")
                apiClient.getStatus()
                verify { activity.showConnectionProblem(1, "", "Name is too long", false) }
            }

            "show an authentication error for unauthorized requests without message" {
                enqueueError(401, "{}")
                apiClient.getStatus()
                verify {
                    activity.showConnectionProblem(
                        1,
                        "string-${R.string.authentication_error_title}",
                        "string-${R.string.authentication_error_body}",
                        false,
                    )
                }
            }

            "show a generic error for server errors" {
                enqueueError(500, "{}")
                apiClient.getStatus()
                verify { activity.showConnectionProblem(1, null, "string-${R.string.internal_error_api}", false) }
            }

            "count consecutive errors and reset the count after a success" {
                enqueueError(500, "{}")
                enqueueError(500, "{}")
                enqueue()
                apiClient.getStatus()
                apiClient.getStatus()
                verify { activity.showConnectionProblem(2, null, any(), false) }
                apiClient.getStatus()
                verify(exactly = 1) { activity.hideConnectionProblem() }
            }

            "mark invites as user input" {
                enqueueError(400, """{"message":"Already invited"}""")
                apiClient.inviteToGroup("group-1", mapOf("uuids" to listOf("member-1")))
                verify { activity.showConnectionProblem(1, "", "Already invited", true) }
            }

            "not show already used receipts or missing authentication headers" {
                enqueueError(400, """{"message":"RECEIPT_ALREADY_USED"}""")
                apiClient.getStatus()
                enqueueError(401, """{"message":"Missing authentication headers."}""")
                apiClient.getStatus()
                verify(exactly = 0) { activity.showConnectionProblem(any(), any(), any(), any()) }
            }

            "not show errors for push devices" {
                enqueueError(400, """{"message":"Push device already exists"}""")
                apiClient.addPushDevice(mapOf("regId" to "device-1"))
                verify(exactly = 0) { activity.showConnectionProblem(any(), any(), any(), any()) }
            }

            "log invalid json" {
                enqueueError(200, "not json")
                apiClient.getStatus() shouldBe null
                verify { Analytics.logError(match { it.startsWith("Json Error") }) }
            }
        }
    })
